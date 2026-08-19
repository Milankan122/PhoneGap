// DeviceInfo example: responds to REQUEST:I2C with fake sensor data

void setup() {
  Serial.begin(9600);
}

void loop() {
  if (Serial.available()) {
    String line = Serial.readStringUntil('\n');
    line.trim();
    if (line == "REQUEST:I2C") {
      // fake sensor values
      Serial.println("I2C:temp=24.5;hum=55");
    }
  }
}
