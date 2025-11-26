package org.henrique.matheus;

import com.fazecast.jSerialComm.SerialPort;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String API_URL = "http://localhost:8080/portas/status/1";
    private static String COM_PORT = "COM3";
    private static final int POLLING_INTERVAL_MS = 2000;

    private static SerialPort comPort;
    private static OutputStream outToArduino;
    private static String ultimoStatusAPI = "";
    private static String ultimoStatusArduino = "DESCONHECIDO";
    private static volatile boolean executando = true;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private static void lerDoArduino() {
        StringBuilder buffer = new StringBuilder();
        byte[] readBuffer = new byte[1024];

        System.out.println("🔍 Thread de leitura iniciada...");

        while (executando) {
            try {
                int bytesDisponiveis = comPort.bytesAvailable();

                if (bytesDisponiveis > 0) {
                    int numRead = comPort.readBytes(readBuffer, bytesDisponiveis);

                    if (numRead > 0) {
                        String dados = new String(readBuffer, 0, numRead);
                        buffer.append(dados);

                        // Processa linhas completas
                        int indiceNovaLinha;
                        while ((indiceNovaLinha = buffer.indexOf("\n")) != -1) {
                            String linha = buffer.substring(0, indiceNovaLinha).trim();
                            buffer.delete(0, indiceNovaLinha + 1);

                            if (!linha.isEmpty()) {
                                System.out.println("📥 Arduino: " + linha);

                                if (linha.startsWith("STATUS:")) {
                                    ultimoStatusArduino = linha.replace("STATUS:", "").trim();
                                    System.out.println("   ℹ️ Status atualizado: " + ultimoStatusArduino);
                                }
                            }
                        }
                    }
                }

                Thread.sleep(50); // Pequeno delay para não sobrecarregar CPU

            } catch (Exception e) {
                if (executando) {
                    System.err.println("❌ Erro ao ler do Arduino: " + e.getMessage());
                }
            }
        }

        System.out.println("🛑 Thread de leitura finalizada");
    }

    public static void sincronizarComAPI() {
        try {
            String statusAPI = consultarStatusAPI();
            if (!statusAPI.equals(ultimoStatusAPI)) {
                System.out.println("\n🌐 API mudou de '" + ultimoStatusAPI + "' para '" + statusAPI + "'");
                ultimoStatusAPI = statusAPI;
            }

            if (!statusAPI.equals(ultimoStatusArduino)) {
                System.out.printf("🔄 Sincronizando: API quer '%s', Arduino está '%s'%n",
                        statusAPI, ultimoStatusArduino);

                enviarComandoParaArduino(statusAPI);

                Thread.sleep(500);

                solicitarStatusArduino();
            }

        } catch (Exception e) {
            System.err.println("⚠️ Erro no ciclo de sincronização: " + e.getMessage());
        }
    }

    private static String consultarStatusAPI() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

        HttpResponse<String> res =
                httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() != 200)
            throw new Exception("API retornou HTTP " + res.statusCode());

        JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
        String status = json.get("status").getAsString().toUpperCase();

        if (!status.equals("ABERTO") && !status.equals("FECHADO"))
            throw new Exception("Status inválido da API: " + status);

        return status;
    }

    private static void enviarComandoParaArduino(String comando) throws Exception {
        String mensagem = comando + "\n";
        outToArduino.write(mensagem.getBytes());
        outToArduino.flush();
        System.out.println("📤 Enviado: " + comando);
    }

    private static void solicitarStatusArduino() throws Exception {
        outToArduino.write("GET_STATUS\n".getBytes());
        outToArduino.flush();
        System.out.println("📤 Solicitado status do Arduino");
    }

    public static void main(String[] args) {
        System.out.println("🚀 Iniciando sistema de sincronização...\n");

        SerialPort[] portas = SerialPort.getCommPorts();

        if (portas.length == 0) {
            System.err.println("❌ Nenhuma porta serial detectada!");
            return;
        }

        System.out.println("📡 Portas disponíveis:");
        for (int i = 0; i < portas.length; i++) {
            System.out.println("   " + i + ": " + portas[i].getSystemPortName() +
                    " - " + portas[i].getDescriptivePortName());
        }

        SerialPort portaArduino = portas[0];
        COM_PORT = portaArduino.getSystemPortName();
        System.out.println("\n✅ Usando porta: " + COM_PORT);

        comPort = portaArduino;
        comPort.setBaudRate(9600);
        comPort.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);

        if (!comPort.openPort()) {
            System.err.println("❌ Erro ao abrir porta " + COM_PORT);
            return;
        }

        System.out.println("⏳ Aguardando Arduino inicializar (3 segundos)...");

        try {
            Thread.sleep(3000); // Aumentei para 3 segundos
            outToArduino = comPort.getOutputStream();

            // Limpa buffers
            while (comPort.bytesAvailable() > 0) {
                comPort.readBytes(new byte[comPort.bytesAvailable()], comPort.bytesAvailable());
            }

            System.out.println("✅ Porta aberta e pronta!\n");

            // Inicia thread de leitura
            Thread threadLeitura = new Thread(Main::lerDoArduino, "Arduino-Reader");
            threadLeitura.setDaemon(false);
            threadLeitura.start();

            // Aguarda um pouco antes de começar sincronização
            Thread.sleep(1000);

            // Solicita status inicial
            solicitarStatusArduino();

            // Inicia scheduler de sincronização
            ScheduledExecutorService scheduler =
                    Executors.newSingleThreadScheduledExecutor();

            scheduler.scheduleAtFixedRate(
                    Main::sincronizarComAPI,
                    2,  // Delay inicial de 2 segundos
                    POLLING_INTERVAL_MS,
                    TimeUnit.MILLISECONDS
            );

            // Adiciona shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n🛑 Encerrando sistema...");
                executando = false;
                scheduler.shutdown();
                if (comPort != null && comPort.isOpen()) {
                    comPort.closePort();
                }
                System.out.println("✅ Sistema encerrado");
            }));

            // Mantém programa rodando
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("💥 Erro fatal: " + e.getMessage());
            e.printStackTrace();
        } finally {
            executando = false;
            if (comPort != null && comPort.isOpen()) {
                comPort.closePort();
            }
        }
    }
}