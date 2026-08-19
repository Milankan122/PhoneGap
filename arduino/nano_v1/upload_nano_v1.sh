# Arduino upload helper (example) - update PORT to your device

# Linux / macOS example usage (requires arduino-cli installed and configured):
# 1) Set PORT to the serial device for your Nano (e.g., /dev/ttyUSB0 or /dev/ttyACM0)
# 2) Run: sh upload_nano_v1.sh

FQBN="arduino:avr:nano"
PORT="/dev/ttyUSB0"

arduino-cli compile --fqbn $FQBN arduino/nano_v1
arduino-cli upload -p $PORT --fqbn $FQBN arduino/nano_v1
