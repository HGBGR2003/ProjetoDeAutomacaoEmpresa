#include <SoftwareSerial.h>
#include <Stepper.h>

#define RX 4
#define TX 5
SoftwareSerial esp(RX, TX);

#define SENSOR_FIM_ABERTO 2
#define SENSOR_FIM_FECHADO 3

const int stepsPerRevolution = 2048;
Stepper motor(stepsPerRevolution, 8, 10, 9, 11);

String ssid = "SEU_WIFI";
String password = "SUA_SENHA_WIFI";
String host = "http://10.56.49.8:8080/portas/status/1";

enum EstadoPortao {
  ABERTO,
  FECHADO,
  MOVENDO,
  DESCONHECIDO
};

EstadoPortao estadoAtual = DESCONHECIDO;

void setup() {
  Serial.begin(9600);
  esp.begin(9600);
  motor.setSpeed(10);

  pinMode(SENSOR_FIM_ABERTO, INPUT_PULLUP);
  pinMode(SENSOR_FIM_FECHADO, INPUT_PULLUP);

  Serial.println("--- Iniciando Sistema ---");
  Serial.println("Calibrando: Fechando portão até o limite...");
  
  while (digitalRead(SENSOR_FIM_FECHADO) == HIGH) {
    motor.step(-5); 
    delay(10);      
  }

  estadoAtual = FECHADO;
  Serial.println("Calibrado! Posição atual: FECHADO.");

  Serial.println("Iniciando conexão WiFi...");
  conectarWiFi();
}

void loop() {
  if (estadoAtual != MOVENDO) {
    
    String comandoAPI = fazerRequisicao();

    if (comandoAPI == "ABERTO" && estadoAtual == FECHADO) {
      Serial.println("Comando: ABRIR.");
      abrirPortao();
    } else if (comandoAPI == "FECHADO" && estadoAtual == ABERTO) {
      Serial.println("Comando: FECHAR.");
      fecharPortao();
    } else if (comandoAPI != "") {
    }
  }

  delay(1000);
}

void abrirPortao() {
  estadoAtual = MOVENDO;

  while (digitalRead(SENSOR_FIM_ABERTO) == HIGH) {
    motor.step(10);
  }
  
  motor.step(0);
  estadoAtual = ABERTO;
  Serial.println("Status: Portão Totalmente ABERTO.");
}

void fecharPortao() {
  estadoAtual = MOVENDO;
  
  while (digitalRead(SENSOR_FIM_FECHADO) == HIGH) {
    motor.step(-10);
  }
  
  motor.step(0);
  estadoAtual = FECHADO;
  Serial.println("Status: Portão Totalmente FECHADO.");
}

void conectarWiFi() {
  enviarComando("AT+RST", 2000);
  enviarComando("AT+CWMODE=1", 1000);
  String cmdConexao = "AT+CWJAP=\"" + ssid + "\",\"" + password + "\"";
  enviarComando(cmdConexao, 8000);
}

String fazerRequisicao() {
  String cmdStart = "AT+CIPSTART=\"TCP\",\"" + host + "\",80";
  enviarComando(cmdStart, 2000);

  String requisicao = "GET /status HTTP/1.1\r\n";
  requisicao += "Host: " + host + "\r\n";
  requisicao += "Connection: close\r\n\r\n";
  
  String cmdSend = "AT+CIPSEND=" + String(requisicao.length());
  enviarComando(cmdSend, 1000);
  
  esp.print(requisicao);
  
  String resposta = lerResposta(4000);
  
  enviarComando("AT+CIPCLOSE", 1000);

  if (resposta.indexOf("ABERTO") > 0) return "ABERTO";
  if (resposta.indexOf("FECHADO") > 0) return "FECHADO";
  
  return "";
}

void enviarComando(String comando, int tempo) {
  while (esp.available()) esp.read();
  
  esp.println(comando);
  delay(tempo);
  
 
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

  return conteudo;
}