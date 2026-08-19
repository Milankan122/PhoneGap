// Simple joystick example for Arduino Nano/Uno
// Listens on Serial, accepts lines like: JOY:x,y

void setup() {
  Serial.begin(9600);
  pinMode(LED_BUILTIN, OUTPUT);
}

void loop() {
  if (Serial.available()) {
    String line = Serial.readStringUntil('\n');
    line.trim();
    if (line.startsWith("JOY:")) {
      // parse x,y
      String v = line.substring(4);
      int comma = v.indexOf(',');
      if (comma > 0) {
        int x = v.substring(0, comma).toInt();
        int y = v.substring(comma+1).toInt();
        // map to something; here, blink LED faster depending on x
        int delayMs = map(abs(x), 0, 100, 500, 50);
        digitalWrite(LED_BUILTIN, HIGH);
        delay(50);
        digitalWrite(LED_BUILTIN, LOW);
        delay(delayMs);
        Serial.println("ACK:JOY");
      }
    }
  }
}
