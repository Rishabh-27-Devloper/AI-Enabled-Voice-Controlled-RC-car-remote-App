# 🔧 BluetoothRC – Android Controlled Bluetooth Robot

![Arduino](https://img.shields.io/badge/Platform-Arduino-blue?logo=arduino)
![Android](https://img.shields.io/badge/Android-Java-green?logo=android)
![Bluetooth](https://img.shields.io/badge/Communication-Bluetooth-blueviolet)
![AI Optional](https://img.shields.io/badge/Optional-Gemini%20API-lightgrey)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

BluetoothRC is a dual-component project featuring an Android app and an Arduino-powered robot. The app sends commands to the robot over Bluetooth, and the robot executes these commands to control movement. This can be a foundation for many IoT, robotics, and embedded projects.

---

## 🚀 Features

- Android app with a clean UI to send directional commands.
- Bluetooth serial communication.
- Real-time command processing on Arduino.
- Modular and extendable code (GeminiAI support for expansion).
- MIT Licensed – free to use, modify, and distribute.

---

## 🧠 Project Components

### 1. 🤖 Arduino Sketch: `BluetoothRC.ino`

- Receives commands via Bluetooth (HC-05/HC-06 module).
- Controls motors or actuators accordingly (forward, back, left, right).
- Easy to modify for different robots (2WD/4WD).

### 2. 📱 Android App

- `MainActivity.java`: Main control screen with buttons for directional input.
- `GeminiApiService.java`: Optional class to integrate AI/voice/chat commands using Gemini API.

---

## 📂 File Overview

| File | Description |
|------|-------------|
| `BluetoothRC.ino` | Arduino sketch to control the robot using received Bluetooth commands |
| `MainActivity.java` | Android activity with UI elements for control |
| `GeminiApiService.java` | (Optional) Gemini API integration for AI-based features |

---

## 🔌 Requirements

### 🧰 Hardware

- Arduino Uno/Nano
- HC-05/HC-06 Bluetooth module
- Motor driver (L298N or similar)
- 2WD/4WD robot chassis
- Battery pack

### 📱 Software

- Android Studio (to build the app)
- Arduino IDE
- Bluetooth-enabled Android device

---

## 🛠️ How to Use

### ⚙️ Arduino Setup

1. Upload the `BluetoothRC.ino` sketch to your Arduino.
2. Connect HC-05/HC-06 RX/TX pins to Arduino TX/RX (with logic level conversion if needed).
3. Wire motor driver to appropriate pins (as defined in code).
4. Power the circuit.

### 📲 Android App

1. Open the project in Android Studio.
2. Make sure Bluetooth permissions are added in `AndroidManifest.xml`.
3. Build and run the app on your Android phone.
4. Pair with the HC-05 module (usually password is `1234` or `0000`).
5. Control the robot using on-screen buttons.

---

## 📡 Communication Protocol

Commands sent from the Android app to the Arduino are single-character values:

| Command | Meaning |
|---------|---------|
| `F` | Move Forward |
| `B` | Move Backward |
| `L` | Turn Left |
| `R` | Turn Right |
| `S` | Stop |

You can easily extend this by modifying `BluetoothRC.ino` and `MainActivity.java`.

---

## 🧠 AI Extension (Optional)

Using `GeminiApiService.java`, you can expand the project to support:

- Voice commands
- Natural language processing
- Chat-based control

Requires Gemini API key and internet connectivity.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contribution

Feel free to fork the repo, open issues, or submit pull requests. Contributions are welcome!

---

## 📷 Screenshots

<!-- You can add screenshots of the Android UI or hardware setup here -->

---

## 👨‍💻 Developed by

**Prakhar Shukla**  
B. Tech ECE, Babasaheb Bhimrao Ambedkar University  
Freelancer & Embedded Systems Enthusiast

---
