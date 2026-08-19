// Matrix display example (prints text to Serial or display)
// Accepts: MATRIX:Hello

void setup() {
  Serial.begin(9600);
}

void loop() {
  if (Serial.available()) {
    String line = Serial.readStringUntil('\n');
    line.trim();
    if (line.startsWith("MATRIX:")) {
      String text = line.substring(7);
      // For demo, just echo back
      Serial.print("MATRIX_DISPLAY:");
      Serial.println(text);
    }
  }
}
