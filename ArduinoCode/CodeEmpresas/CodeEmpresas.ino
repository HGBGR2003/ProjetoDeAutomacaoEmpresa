#include <Stepper.h>

#define SENSOR_FIM_ABERTO 2
#define SENSOR_FIM_FECHADO 13

const int stepsPerRevolution = 2048;
Stepper motor(stepsPerRevolution, 8, 10, 9, 11);

enum EstadoPortao {
  ABERTO,
  FECHADO,
  MOVENDO,
  DESCONHECIDO
};

EstadoPortao estadoAtual = DESCONHECIDO;

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
  Serial.println("Sistema pronto. Aguardando comandos...");
}

void loop() {
  // Verifica se há comandos chegando pela serial
  if (Serial.available() > 0) {
    String comando = Serial.readStringUntil('\n');
    comando.trim();

    // Responde a requisições de status
    if (comando == "GET_STATUS") {
      enviarStatus();
    }
    // Executa comandos de movimento
    else if (comando == "ABERTO" && estadoAtual == FECHADO) {
      Serial.println("Comando recebido: ABERTO");
      abrirPortao();
      enviarStatus(); // Envia status atualizado após a ação
    } 
    else if (comando == "FECHADO" && estadoAtual == ABERTO) {
      Serial.println("Comando recebido: FECHADO");
      fecharPortao();
      enviarStatus(); // Envia status atualizado após a ação
    }
    else if (comando == "ABERTO" && estadoAtual == ABERTO) {
      Serial.println("ERRO: Portão já está ABERTO");
    }
    else if (comando == "FECHADO" && estadoAtual == FECHADO) {
      Serial.println("ERRO: Portão já está FECHADO");
    }
    else if (estadoAtual == MOVENDO) {
      Serial.println("ERRO: Portão em MOVIMENTO");
    }
    else if (comando != "") {
      Serial.println("ERRO: Comando desconhecido");
    }
  }
}

void abrirPortao() {
  estadoAtual = MOVENDO;
  Serial.println("Status: MOVENDO (abrindo)");

  while (digitalRead(SENSOR_FIM_ABERTO) == HIGH) {
    motor.step(10);
  }

  motor.step(0);
  estadoAtual = ABERTO;
  Serial.println("Status: Portão Totalmente ABERTO.");
}

void fecharPortao() {
  estadoAtual = MOVENDO;
  Serial.println("Status: MOVENDO (fechando)");

  while (digitalRead(SENSOR_FIM_FECHADO) == HIGH) {
    motor.step(-10);
  }

  motor.step(0);
  estadoAtual = FECHADO;
  Serial.println("Status: Portão Totalmente FECHADO.");
}

void enviarStatus() {
  switch (estadoAtual) {
    case ABERTO:
      Serial.println("STATUS:ABERTO");
      break;
    case FECHADO:
      Serial.println("STATUS:FECHADO");
      break;
    case MOVENDO:
      Serial.println("STATUS:MOVENDO");
      break;
    case DESCONHECIDO:
      Serial.println("STATUS:DESCONHECIDO");
      break;
  }
}