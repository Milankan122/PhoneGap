// Simple visualizer example
// Supports commands: LED:rainbow, LED:off

void setup() {
  Serial.begin(9600);
  pinMode(LED_BUILTIN, OUTPUT);
}

void loop() {
  if (Serial.available()) {
    String line = Serial.readStringUntil('\n');
    line.trim();
    if (line.startsWith("LED:")) {
      String cmd = line.substring(4);
      if (cmd == "rainbow") {
        // placeholder action
        for (int i=0;i<3;i++){
          digitalWrite(LED_BUILTIN, HIGH); delay(100);
          digitalWrite(LED_BUILTIN, LOW); delay(100);
        }
        Serial.println("ACK:LED:rainbow");
      } else if (cmd == "off") {
        digitalWrite(LED_BUILTIN, LOW);
        Serial.println("ACK:LED:off");
      }
    }
  }
}
