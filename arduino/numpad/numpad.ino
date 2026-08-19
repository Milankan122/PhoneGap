// Numpad example: listens for NUM:n and prints back

void setup() {
  Serial.begin(9600);
}

void loop() {
  if (Serial.available()) {
    String line = Serial.readStringUntil('\n');
    line.trim();
    if (line.startsWith("NUM:")) {
      String v = line.substring(4);
      Serial.print("ACK:NUM:");
      Serial.println(v);
    }
  }
}
