package org.henrique.matheus;

import com.fazecast.jSerialComm.SerialPort;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private static void lerDoArduino() {
        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(comPort.getInputStream()))) {

            String linha;
            while ((linha = reader.readLine()) != null) {
                linha = linha.trim();

                if (linha.isEmpty()) continue;

                System.out.println("📥 Arduino: " + linha);

                if (linha.startsWith("STATUS:")) {
                    ultimoStatusArduino = linha.replace("STATUS:", "");
                    System.out.println("   ℹ️ Status atualizado: " + ultimoStatusArduino);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao ler do Arduino: " + e.getMessage());
        }
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
        outToArduino.write((comando + "\n").getBytes());
        outToArduino.flush();
        System.out.println("📤 Enviado: " + comando);
    }

    private static void solicitarStatusArduino() throws Exception {
        outToArduino.write("GET_STATUS\n".getBytes());
        outToArduino.flush();
    }

    public static void main(String[] args) {

        SerialPort[] portas = SerialPort.getCommPorts();

        if (portas.length == 0) {
            System.err.println("Nenhuma porta serial detectada!");
            return;
        }

        SerialPort portaArduino = portas[0];
        COM_PORT = portaArduino.getSystemPortName();

        comPort = portaArduino;
        comPort.setBaudRate(9600);
        comPort.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);

        if (!comPort.openPort()) {
            System.err.println("Erro ao abrir porta " + COM_PORT);
            return;
        }

        try {
            Thread.sleep(2000);
            outToArduino = comPort.getOutputStream();

            Thread.ofVirtual().start(Main::lerDoArduino);

            ScheduledExecutorService scheduler =
                    Executors.newSingleThreadScheduledExecutor(
                            Thread.ofVirtual().factory());

            scheduler.scheduleAtFixedRate(
                    Main::sincronizarComAPI,
                    0,
                    POLLING_INTERVAL_MS,
                    TimeUnit.MILLISECONDS
            );

            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Erro fatal: " + e.getMessage());
        } finally {
            comPort.closePort();
        }
    }
}
