// nano_v1.ino
// Version 1: single Nano handles three HC-05 modules via SoftwareSerial and an I2C LCD
// Roles:
//  - ssJoy  (HC-05 #1) : receives JOY:x,y
//  - ssDisp (HC-05 #2) : receives DISP:line1|line2 and writes to I2C LCD
//  - ssNum  (HC-05 #3) : receives NUM:n

#include <SoftwareSerial.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>

// SoftwareSerial pins (RX, TX) - RX is Arduino pin that listens to module TX
SoftwareSerial ssJoy(10, 11);   // HC-05 #1 (Joystick)
SoftwareSerial ssDisp(8, 9);    // HC-05 #2 (I2C display commands)
SoftwareSerial ssNum(6, 7);     // HC-05 #3 (Numpad)

LiquidCrystal_I2C lcd(0x27, 16, 2); // change address if your module differs

const unsigned long READ_TIMEOUT = 200; // ms to wait for a line

void setup() {
  Serial.begin(115200); // USB debug
  ssJoy.begin(9600);
  ssDisp.begin(9600);
  ssNum.begin(9600);

  Wire.begin();
  lcd.init();
  lcd.backlight();
  lcd.clear();
  lcd.setCursor(0,0);
  lcd.print("NanoV1 ready");
  lcd.setCursor(0,1);
  lcd.print("Waiting...");

  Serial.println("NanoV1 started");
}

String readLineFrom(SoftwareSerial &s) {
  String line = "";
  unsigned long started = millis();
  while (millis() - started < READ_TIMEOUT) {
    if (s.available()) {
      char c = s.read();
      if (c == '\r') continue; // ignore
      if (c == '\n') {
        break;
      }
      line += c;
    }
  }
  line.trim();
  return line;
}

void handleLine(const String &srcName, const String &line) {
  if (line.length() == 0) return;
  Serial.print("[From "); Serial.print(srcName); Serial.print("] "); Serial.println(line);

  if (line.startsWith("JOY:")) {
    // JOY:x,y
    String v = line.substring(4);
    int comma = v.indexOf(',');
    if (comma > 0) {
      int x = v.substring(0, comma).toInt();
      int y = v.substring(comma+1).toInt();
      // handle joystick values (for demo, print and blink)
      Serial.print("JOY X="); Serial.print(x); Serial.print(" Y="); Serial.println(y);
      // simple visual feedback: blink builtin led proportional to intensity
      int intensity = min(255, (abs(x) + abs(y)));
      digitalWrite(LED_BUILTIN, HIGH);
      delay(20);
      digitalWrite(LED_BUILTIN, LOW);
      // ACK back to sender on relevant port
      if (srcName == "JOY") ssJoy.println("ACK:JOY");
    }
  } else if (line.startsWith("DISP:")) {
    // DISP:Line1|Line2
    String v = line.substring(5);
    int pipe = v.indexOf('|');
    String l1 = v;
    String l2 = "";
    if (pipe >= 0) {
      l1 = v.substring(0, pipe);
      l2 = v.substring(pipe + 1);
    }
    l1.trim(); l2.trim();
    // write to LCD
    lcd.clear();
    lcd.setCursor(0,0);
    lcd.print(l1);
    lcd.setCursor(0,1);
    lcd.print(l2);
    Serial.println("Wrote to LCD");
    // ACK
    ssDisp.println("ACK:DISP");
  } else if (line.startsWith("NUM:")) {
    String v = line.substring(4);
    v.trim();
    Serial.print("NUM input: "); Serial.println(v);
    ssNum.println("ACK:NUM:" + v);
  } else if (line == "REQUEST:I2C") {
    // If someone requests it, send a fake sensor read back
    String resp = "I2C:temp=24.5;hum=55";
    // Send response on the requesting channel - choose best guess
    ssNum.println(resp);
    ssDisp.println(resp);
    ssJoy.println(resp);
  } else {
    Serial.print("Unknown cmd: "); Serial.println(line);
  }
}

void loop() {
  // Poll each SoftwareSerial in turn. Use listen() so the correct port can receive.

  // 1) Check Joy
  ssJoy.listen();
  if (ssJoy.available()) {
    String line = readLineFrom(ssJoy);
    handleLine("JOY", line);
  }

  // 2) Check Display
  ssDisp.listen();
  if (ssDisp.available()) {
    String line = readLineFrom(ssDisp);
    handleLine("DISP", line);
  }

  // 3) Check Numpad
  ssNum.listen();
  if (ssNum.available()) {
    String line = readLineFrom(ssNum);
    handleLine("NUM", line);
  }

  // small delay to yield
  delay(10);
}
