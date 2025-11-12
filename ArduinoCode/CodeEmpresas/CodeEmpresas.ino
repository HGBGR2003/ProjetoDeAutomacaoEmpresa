#include <SoftwareSerial.h>
#include <Stepper.h>

#define RX 2
#define TX 3
SoftwareSerial esp(RX, TX);

const int stepsPerRevolution = 2048; 
Stepper motor(stepsPerRevolution, 8, 10, 9, 11);

String apiURL = "http://seu-endereco-da-api.com/status"; 
String ssid = "SEU_WIFI";
String password = "SUA_SENHA_WIFI";

void setup() {
  Serial.begin(9600);
  esp.begin(9600);
  motor.setSpeed(10); 
  
  Serial.println("Iniciando conexão WiFi...");
  conectarWiFi();
}

void loop() {
  String statusPortao = fazerRequisicao();

  if (statusPortao == "ABERTO") {
    Serial.println("Abrindo portão...");
    motor.step(stepsPerRevolution);  
  } 
  else if (statusPortao == "FECHADO") {
    Serial.println("Fechando portão...");
    motor.step(-stepsPerRevolution); 
  }

  delay(500); // intervalo de 0,5 segundos
}

void conectarWiFi() {
  enviarComando("AT+RST", 2000);
  enviarComando("AT+CWMODE=1", 1000);
  enviarComando("AT+CWJAP=\"" + ssid + "\",\"" + password + "\"", 6000);
}

String fazerRequisicao() {
  enviarComando("AT+CIPSTART=\"TCP\",\"seu-endereco-da-api.com\",80", 2000);
  String requisicao = "GET /status HTTP/1.1\r\nHost: seu-endereco-da-api.com\r\nConnection: close\r\n\r\n";
  
  String cmd = "AT+CIPSEND=" + String(requisicao.length());
  enviarComando(cmd, 2000);
  esp.print(requisicao);
  
  String resposta = lerResposta(4000);
  enviarComando("AT+CIPCLOSE", 1000);
  
  if (resposta.indexOf("ABERTO") > 0) return "ABERTO";
  if (resposta.indexOf("FECHADO") > 0) return "FECHADO";
  return "";
}

void enviarComando(String comando, int tempo) {
  esp.println(comando);
  delay(tempo);
  while (esp.available()) {
    Serial.write(esp.read());
  }
}

String lerResposta(int tempo) {
  String conteudo = "";
  long t = millis();
  while ((millis() - t) < tempo) {
    while (esp.available()) {
      char c = esp.read();
      conteudo += c;
    }
  }
  Serial.println("Resposta: " + conteudo);
  return conteudo;
}
