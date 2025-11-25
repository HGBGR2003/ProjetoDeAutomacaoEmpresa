#include <Stepper.h>
#define SENSOR_FIM_ABERTO 2
#define SENSOR_FIM_FECHADO 3

const int stepsPerRevolution = 2048;
Stepper motor(stepsPerRevolution, 8, 10, 9, 11);

enum EstadoPortao {
  ABERTO,
  FECHADO,
  MOVENDO,
  DESCONHECIDO
};

EstadoPortao estadoAtual = DESCONHECIDO;

unsigned long ultimoTempo = 0;
const long intervalo = 2000; 

void setup() {

  Serial.begin(9600); 
  
  motor.setSpeed(10); 

  pinMode(SENSOR_FIM_ABERTO, INPUT_PULLUP);
  pinMode(SENSOR_FIM_FECHADO, INPUT_PULLUP);

  Serial.println("--- Iniciando Sistema via USB ---");

  Serial.println("Calibrando: Fechando portão até o limite...");

  while (digitalRead(SENSOR_FIM_FECHADO) == HIGH) {
    motor.step(-5); 
    delay(10);      
  }

  estadoAtual = FECHADO;
  Serial.println("Calibrado! Posição atual: FECHADO.");
}

void loop() {

  if (millis() - ultimoTempo > intervalo) {
    ultimoTempo = millis();

    if (estadoAtual != MOVENDO) {
      
      String comandoAPI = pedirStatusParaPC();
      
      comandoAPI.trim(); 

      if (comandoAPI == "ABERTO" && estadoAtual == FECHADO) {
        Serial.println("Java mandou abrir. Executando...");
        abrirPortao();
      } 
      else if (comandoAPI == "FECHADO" && estadoAtual == ABERTO) {
        Serial.println("Java mandou fechar. Executando...");
        fecharPortao();
      } 
      else if (comandoAPI != "") {
      }
    }
  }
}


String pedirStatusParaPC() {
  
  while (Serial.available() > 0) {
    Serial.read();
  }

  Serial.println("REQUISICAO_GET");

  unsigned long tempoInicio = millis();
  String resposta = "";
  
  while (millis() - tempoInicio < 1000) { 