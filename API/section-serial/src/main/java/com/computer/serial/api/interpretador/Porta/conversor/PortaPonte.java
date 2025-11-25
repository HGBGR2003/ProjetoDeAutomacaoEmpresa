package com.computer.serial.api.interpretador.Porta.conversor;

import com.fazecast.jSerialComm.SerialPort;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


public class PortaPonte {
    private static final String API_URL = "http://localhost:8081/portas/1";
    private static final String COM_PORT = "COM3"; 
    
    public static void main(String[] args) {
        SerialPort comPort = SerialPort.getCommPort(COM_PORT);
        comPort.setBaudRate(9600);
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        if (!comPort.openPort()) {
            System.out.println("Erro: Não foi possível abrir a porta " + COM_PORT);
            System.out.println("Dica: Fecha o Monitor Serial do Arduino IDE.");
            return;
        }
        System.out.println("Ponte iniciada na " + COM_PORT + ". À espera do Arduino...");

        try (Scanner scanner = new Scanner(comPort.getInputStream());
             OutputStream outToArduino = comPort.getOutputStream()) {

            while (scanner.hasNextLine()) {
                String comando = scanner.nextLine().trim();
                System.out.println("Recebido do Arduino: " + comando);

                if (comando.equals("REQUISICAO_GET")) {
                    try {
                        String resposta = fazerGetRequest();
                        System.out.println("Resposta da API: " + resposta);

        
                        String msg = resposta + "\n";
                        outToArduino.write(msg.getBytes());
                        outToArduino.flush();
                    } catch (Exception e) {
                        System.err.println("Erro ao conectar na API: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            comPort.closePort();
        }
    }

    private static String fazerGetRequest() throws Exception {
        @SuppressWarnings("deprecation")
        URL url = new URL(API_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }
}
