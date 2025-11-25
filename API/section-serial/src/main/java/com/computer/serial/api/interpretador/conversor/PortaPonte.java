package com.computer.serial.api.interpretador.conversor;

import com.fazecast.jSerialComm.SerialPort;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class PortaPonte {
    private static final String API_URL = "http://localhost:8081/portas/1";
    private static final String COM_PORT = "COM3";
    private static final int POLLING_INTERVAL_MS = 2000; // Consulta a API a cada 2 segundos

    private static SerialPort comPort;
    private static OutputStream outToArduino;
    private static String ultimoStatusAPI = "";
    private static String ultimoStatusArduino = "DESCONHECIDO";

    public static void main(String[] args) {
        // Configura a porta serial
        comPort = SerialPort.getCommPort(COM_PORT);
        comPort.setBaudRate(9600);
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);

        if (!comPort.openPort()) {
            System.err.println("❌ ERRO: Não foi possível abrir a porta " + COM_PORT);
            System.err.println("💡 Dica: Feche o Monitor Serial do Arduino IDE.");
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

    /**
     * Thread que lê continuamente as mensagens enviadas pelo Arduino
     */
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

    /**
     * Consulta a API periodicamente e envia comandos para o Arduino se necessário
     */
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

    /**
     * Faz GET request na API e extrai o status do JSON
     */
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

    /**
     * Envia comando para o Arduino (ABRIR ou FECHAR)
     */
    private static void enviarComandoParaArduino(String comando) throws Exception {
        String msg = comando + "\n";
        outToArduino.write(msg.getBytes());
        outToArduino.flush();
        System.out.println("📤 Enviado para Arduino: " + comando);
    }

    /**
     * Solicita o status atual do Arduino
     */
    private static void solicitarStatusArduino() throws Exception {
        String msg = "GET_STATUS\n";
        outToArduino.write(msg.getBytes());
        outToArduino.flush();
    }
}
