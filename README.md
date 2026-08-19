# PhoneGap — Phone Assistant for HC-05 (Version 1)

Version 1: single Arduino Nano handling three HC‑05 modules and peripherals.

Overview (version 1)
- Hardware:
  - Arduino Nano x1 ("Nano1")
  - HC-05 #1 -> Joystick data -> connected to Nano1 (SoftwareSerial port A)
  - HC-05 #2 -> I2C display commands -> connected to Nano1 (SoftwareSerial port B)
  - HC-05 #3 -> Numpad input -> connected to Nano1 (SoftwareSerial port C)
  - I2C LCD (e.g., 16x2 with PCF8574 backpack, typical address 0x27)
- Android app (Kotlin + Gradle) connects to each HC‑05 independently and sends role-specific commands.

Design notes and limitations
- The Nano uses SoftwareSerial to attach multiple HC‑05 modules at once. SoftwareSerial supports multiple objects but only one can listen at a time; the sketch uses `listen()` and polls the three links in turn. This is acceptable for version 1 but may miss bytes if traffic is simultaneous. For production or high-throughput, use a board with multiple UARTs (e.g., Mega) or add a Bluetooth master gateway.
- All HC‑05 modules must be set to the same UART baud (9600 by default in the sketches below) and be configured as Slave devices, paired with the phone.

Wiring (pins used in sketch)
- Nano1 power: 5V, GND
- I2C LCD:
  - SDA -> A4
  - SCL -> A5
  - VCC -> 5V
  - GND -> GND
- HC‑05 #1 (Joystick)
  - HC05_1 TX -> Nano D10 (SoftwareSerial RX for ssJoy)
  - HC05_1 RX <- Nano D11 (SoftwareSerial TX for ssJoy) (use voltage divider on TX)
  - VCC -> 5V, GND -> GND
- HC‑05 #2 (I2C display commands)
  - HC05_2 TX -> Nano D8 (ssDisp RX)
  - HC05_2 RX <- Nano D9 (ssDisp TX) (use voltage divider)
  - VCC -> 5V, GND -> GND
- HC‑05 #3 (Numpad)
  - HC05_3 TX -> Nano D6 (ssNum RX)
  - HC05_3 RX <- Nano D7 (ssNum TX)
  - VCC -> 5V, GND -> GND

Important: When uploading sketches to the Nano, disconnect HC‑05 TX/RX lines from the hardware Serial (D0/D1) if they are wired there. In this layout we use only SoftwareSerial pins, so you can keep HC‑05s connected during upload — but if you changed wiring to D0/D1, disconnect them before upload.

Serial protocol used (from Android app to Nano via each HC‑05)
- Joystick (to HC‑05 #1)
  - JOY:x,y      x and y integers in -100..100 (e.g. JOY:10,-50)
  - The Nano acknowledges with: ACK:JOY
- I2C display (to HC‑05 #2)
  - DISP:Line1|Line2  (use a `|` to separate lines for 16x2 display). Example: DISP:Hello|World
  - The Nano writes to the I2C LCD and replies ACK:DISP
- Numpad (to HC‑05 #3)
  - NUM:n   where n is a single digit or token (e.g., NUM:5)
  - The Nano replies ACK:NUM:5

How to use (quick)
1. Configure each HC‑05 name by entering AT mode and renaming them to unique names (e.g., HC05_JOY, HC05_DISP, HC05_NUM). Optionally set UART to 9600.
2. Pair each HC‑05 with the phone.
3. Compile & upload `arduino/nano_v1/nano_v1.ino` to the Nano (use Arduino IDE or arduino-cli). Keep HC‑05s disconnected from pins if you use hardware Serial for upload (not necessary for this wiring).
4. Wire the HC‑05 modules and I2C LCD as above; power the Nano + modules.
5. In the Android app, refresh paired devices, connect to each HC‑05 and assign the corresponding role (Joystick -> HC05_JOY, I2C Display -> HC05_DISP, Numpad -> HC05_NUM).
6. Use the app UI to send JOY, DISP, NUM commands. Watch the serial monitor for ACKs if you connect via USB.

Files changed in this version
- Added sketch: arduino/nano_v1/nano_v1.ino
- Updated README to describe version 1 wiring and mapping

If you want, next I will:
- Update the Android app UI to add a one-tap "Auto-assign" that finds paired devices named with HC05_JOY / HC05_DISP / HC05_NUM and assigns them automatically.
- Provide an arduino-cli script to compile & upload the sketch to a given port.

