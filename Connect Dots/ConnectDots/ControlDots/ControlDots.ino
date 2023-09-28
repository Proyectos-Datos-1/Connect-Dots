/**
 * @author Fabricio Mena, Joseph Murillo, Nathalia Ocampo
 * Código para el manejo de botones y detección de cambios de estado.
 * Este programa detecta cambios de estado en varios botones y envía
 * caracteres a través de la comunicación serial cuando se presionan
 * los botones.
 */

// Pines de los botones
const int botonAPin = 7;  // Pin del botón A
const int botonBPin = 6;  // Pin del botón B
const int botonGPin = 9;  // Pin del botón G
const int botonFPin = 8;  // Pin del botón F
const int botonJPin = 5;  // Pin del botón del Joystick

// Variables para el debounce
int estadoAnteriorBotonA = HIGH;
int estadoAnteriorBotonB = HIGH;
int estadoAnteriorBotonG = HIGH;
int estadoAnteriorBotonF = HIGH;
int estadoAnteriorBotonJ = HIGH;

/**
 * Configuración inicial del programa.
 * Se configuran los pines de los botones como entradas y se inicia
 * la comunicación serial a una velocidad de 9600 baudios.
 */
void setup() {
  // Configurar los pines de los botones como entradas
  pinMode(botonAPin, INPUT);
  pinMode(botonBPin, INPUT);
  pinMode(botonGPin, INPUT);
  pinMode(botonFPin, INPUT);
  pinMode(botonJPin, INPUT);
  
  // Iniciar la comunicación serial
  Serial.begin(9600);
}

/**
 * Función principal del programa.
 * Se detectan cambios de estado en los botones y se envían caracteres
 * a través de la comunicación serial cuando se presionan los botones.
 */
void loop() {
  int estadoBotonA = digitalRead(botonAPin);
  int estadoBotonB = digitalRead(botonBPin);
  int estadoBotonG = digitalRead(botonGPin);
  int estadoBotonF = digitalRead(botonFPin);
  int estadoBotonJ = digitalRead(botonJPin);

  // Eliminar el rebote verificando cambios de estado
  if (estadoBotonA != estadoAnteriorBotonA) {
    // Cambio de estado detectado para el botón A
    if (estadoBotonA == HIGH) {
      Serial.write('A'); // Enviar 'A' para mover hacia la izquierda
    }
  }
  
  if (estadoBotonB != estadoAnteriorBotonB) {
    // Cambio de estado detectado para el botón B
    if (estadoBotonB == HIGH) {
      Serial.write('B'); // Enviar 'B' para mover hacia arriba
    }
  }
  
  if (estadoBotonG != estadoAnteriorBotonG) {
    // Cambio de estado detectado para el botón G
    if (estadoBotonG == HIGH) {
      Serial.write('G'); // Enviar 'G' para mover hacia abajo
    }
  }
  
  if (estadoBotonF != estadoAnteriorBotonF) {
    // Cambio de estado detectado para el botón F
    if (estadoBotonF == HIGH) {
      Serial.write('F'); // Enviar 'F' para mover hacia la derecha
    }
  }

  if (estadoBotonJ != estadoAnteriorBotonJ) {
    // Cambio de estado detectado para el botón J
    if (estadoBotonJ == HIGH) {
      Serial.write('J'); // Enviar 'F' para mover hacia la derecha
    }
  }
  
  // Actualizar el estado anterior de los botones
  estadoAnteriorBotonA = estadoBotonA;
  estadoAnteriorBotonB = estadoBotonB;
  estadoAnteriorBotonG = estadoBotonG;
  estadoAnteriorBotonF = estadoBotonF;
  estadoAnteriorBotonJ = estadoBotonJ;
  
  // Pequeño retardo para evitar lecturas múltiples en una sola pulsación
  delay(100);
}
