package org.ponte;

import com.fazecast.jSerialComm.SerialPort;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final String API_URL = "http://localhost:8080/portas/status/1";
    private static String COM_PORT = "COM3"; // Será detectado automaticamente
    private static final int POLLING_INTERVAL_MS = 2000;

    private static SerialPort comPort;
    private static OutputStream outToArduino;
    private static String ultimoStatusAPI = "";
    private static String ultimoStatusArduino = "DESCONHECIDO";

    public static void main(String[] args) {
        System.out.println("🔍 Detectando portas seriais disponíveis...\n");

        // Lista todas as portas disponíveis
        SerialPort[] portas = SerialPort.getCommPorts();

        if (portas.length == 0) {
            System.err.println("❌ ERRO: Nenhuma porta serial detectada!");
            System.err.println("💡 Verifique se:");
            System.err.println("   1. O Arduino está conectado via USB");
            System.err.println("   2. O driver CH340/FTDI está instalado");
            System.err.println("   3. O cabo USB está funcionando");
            return;
        }

        System.out.println("📋 Portas disponíveis:");
        for (int i = 0; i < portas.length; i++) {
            System.out.println("   [" + i + "] " + portas[i].getSystemPortName() +
                    " - " + portas[i].getDescriptivePortName());
        }

        // Tenta encontrar Arduino automaticamente
        SerialPort portaArduino = null;
        for (SerialPort porta : portas) {
            String desc = porta.getDescriptivePortName().toLowerCase();
            if (desc.contains("arduino") ||
                    desc.contains("ch340") ||
                    desc.contains("usb serial") ||
                    desc.contains("usb-serial")) {
                portaArduino = porta;
                COM_PORT = porta.getSystemPortName();
                System.out.println("\n✅ Arduino detectado automaticamente: " + COM_PORT);
                break;
            }
        }

        // Se não encontrou automaticamente, usa a primeira porta
        if (portaArduino == null) {
            portaArduino = portas[0];
            COM_PORT = portaArduino.getSystemPortName();
            System.out.println("\n⚠️  Arduino não detectado automaticamente.");
            System.out.println("   Tentando usar primeira porta disponível: " + COM_PORT);
        }

        // Configura a porta serial
        comPort = portaArduino;
        comPort.setBaudRate(9600);
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);

        if (!comPort.openPort()) {
            System.err.println("\n❌ ERRO: Não foi possível abrir a porta " + COM_PORT);
            System.err.println("💡 Possíveis causas:");
            System.err.println("   1. Monitor Serial do Arduino IDE está aberto");
            System.err.println("   2. Outra aplicação está usando a porta");
            System.err.println("   3. Permissões insuficientes (Windows: rode como Admin)");
            System.err.println("\n🔧 Solução:");
            System.err.println("   1. Feche TODOS os programas que acessam serial");
            System.err.println("   2. Desconecte e reconecte o cabo USB");
            System.err.println("   3. Tente novamente");
            return;
        }

        System.out.println("✅ Ponte Serial iniciada na porta " + COM_PORT);
        System.out.println("⏳ Aguardando 2 segundos para o Arduino resetar...");

        try {
            Thread.sleep(2000); // Aguarda reset do Arduino
            outToArduino = comPort.getOutputStream();

            // Thread 1: Lê mensagens do Arduino (status, erros, logs)
            Thread leitorArduino = new Thread(() -> lerDoArduino());
            leitorArduino.setDaemon(true);
            leitorArduino.start();

            // Thread 2: Faz polling na API e sincroniza com Arduino
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(
                    () -> sincronizarComAPI(),
                    0,
                    POLLING_INTERVAL_MS,
                    TimeUnit.MILLISECONDS
            );

            System.out.println("🔄 Sistema de ponte ativo. Pressione Ctrl+C para sair.\n");

            // Mantém o programa rodando
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("❌ Erro fatal: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (comPort != null) {
                comPort.closePort();
                System.out.println("\n🔌 Porta serial fechada.");
            }
        }
    }

    private static void lerDoArduino() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(comPort.getInputStream()))) {

            String linha;
            while ((linha = reader.readLine()) != null) {
                linha = linha.trim();

                if (linha.isEmpty()) continue;

                System.out.println("📥 Arduino: " + linha);

                // Atualiza o status conhecido do Arduino
                if (linha.startsWith("STATUS:")) {
                    ultimoStatusArduino = linha.replace("STATUS:", "");
                    System.out.println("   ℹ️  Status atualizado: " + ultimoStatusArduino);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao ler do Arduino: " + e.getMessage());
        }
    }

    private static void sincronizarComAPI() {
        try {
            // 1. Consulta a API
            String statusAPI = consultarStatusAPI();

            // 2. Se o status da API mudou, registra
            if (!statusAPI.equals(ultimoStatusAPI)) {
                System.out.println("\n🌐 API mudou de '" + ultimoStatusAPI + "' para '" + statusAPI + "'");
                ultimoStatusAPI = statusAPI;
            }

            // 3. Verifica se Arduino precisa ser atualizado
            if (!statusAPI.equals(ultimoStatusArduino)) {
                System.out.println("🔄 Sincronizando: API quer '" + statusAPI +
                        "', Arduino está '" + ultimoStatusArduino + "'");
                enviarComandoParaArduino(statusAPI);

                // Aguarda um pouco para o Arduino processar
                Thread.sleep(500);

                // Solicita confirmação do status
                solicitarStatusArduino();
            }

        } catch (Exception e) {
            System.err.println("⚠️  Erro no ciclo de sincronização: " + e.getMessage());
        }
    }

    private static String consultarStatusAPI() throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(3000);
        con.setReadTimeout(3000);

        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("API retornou código " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        // Parse do JSON: {"status":"ABERTO"} ou {"status":"FECHADO"}
        String jsonResponse = content.toString();
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        String status = jsonObject.get("status").getAsString().toUpperCase();

        // Valida o status
        if (!status.equals("ABERTO") && !status.equals("FECHADO")) {
            throw new Exception("Status inválido recebido da API: " + status);
        }

        return status;
    }

    private static void enviarComandoParaArduino(String comando) throws Exception {
        String msg = comando + "\n";
        outToArduino.write(msg.getBytes());
        outToArduino.flush();
        System.out.println("📤 Enviado para Arduino: " + comando);
    }

    private static void solicitarStatusArduino() throws Exception {
        String msg = "GET_STATUS\n";
        outToArduino.write(msg.getBytes());
        outToArduino.flush();
    }
}