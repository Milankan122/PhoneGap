# PhoneGap — Phone Assistant for HC-05

This repository contains an Android app (Kotlin, Gradle, minSdk 23) that connects to HC-05 Bluetooth modules and controls peripherals (Joystick, Visualizer, Matrix, Numpad, DeviceInfo). It also contains simple Arduino sketches implementing a matching serial protocol.

Files included:
- Android project in `app/` (Kotlin sources, layouts, manifest)
- Arduino sketches in `arduino/` for joystick, visualizer, matrix, numpad, deviceinfo
- README (this file)
- LICENSE (MIT)

Protocol (simple line-based commands):
- From phone to Arduino:
  - JOY:x,y       e.g. JOY:10,-50
  - MATRIX:Hello  prints text on a matrix display
  - LED:rainbow   visualizer patterns
  - NUM:5         numeric input
  - REQUEST:I2C    request sensors/I2C data
- From Arduino to phone:
  - I2C:temp=24.5;hum=55
  - ACK:LED
  - ERR:msg

Wiring summary:
- HC-05 VCC -> 5V
- HC-05 GND -> GND
- HC-05 TX -> Arduino RX (direct)
- HC-05 RX -> Arduino TX (through voltage divider if Arduino 5V)

Notes:
- Pair HC-05 modules with the phone using system Bluetooth settings. Name them clearly (HC-05_1, HC-05_2, ...).
- The app uses Bluetooth Classic SPP (UUID 00001101-0000-1000-8000-00805F9B34FB).
- You can consolidate peripherals on fewer Arduinos using I2C and multiplexing.

License: MIT
