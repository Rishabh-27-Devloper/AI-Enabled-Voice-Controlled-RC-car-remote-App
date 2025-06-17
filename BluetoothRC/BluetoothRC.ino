#include "BluetoothSerial.h"
#include <Arduino.h>
BluetoothSerial BT;


byte MotorPin = 5;
byte IN1 = 18;
byte IN2 = 19;
byte IN3 = 32;
byte IN4 = 33;

void setup() {
    pinMode(MotorPin,OUTPUT);
    pinMode(IN1,OUTPUT);
    pinMode(IN2,OUTPUT);
    pinMode(IN3,OUTPUT);
    pinMode(IN4,OUTPUT);

    ledcSetup(0,1000,8);
    ledcAttachPin(MotorPin,0);


    Serial.begin(115200);
    BT.begin("ESP32 RC");

    Serial.println("Bluetooth Started! Ready to receive.");
}
void OffAll(){
  digitalWrite(IN1,LOW);
  digitalWrite(IN2,LOW);
  digitalWrite(IN3,LOW);
  digitalWrite(IN4,LOW);
  ledcWrite(0,0);
}
void forwardMotor(int speed){
  digitalWrite(IN1,HIGH);
  digitalWrite(IN2,LOW);
  ledcWrite(0, speed);
}
void reverseMotor(int speed){
  digitalWrite(IN1,LOW);
  digitalWrite(IN2,HIGH);
  ledcWrite(0, speed);
}

void Left(){
  digitalWrite(IN3,HIGH);
  digitalWrite(IN4,LOW);
}
void Right(){
  digitalWrite(IN3,LOW);
  digitalWrite(IN4,HIGH);
}
void Straight(){
  digitalWrite(IN3,LOW);
  digitalWrite(IN4,LOW);
}
void stopMotors() {
    digitalWrite(IN1, LOW);
    digitalWrite(IN2, LOW);
    digitalWrite(IN3, LOW);
    digitalWrite(IN4, LOW);
    ledcWrite(0, 0);
}

void loop() {
    if (BT.available()) {  // If data is received
      String recv;
      while (BT.available()){
        char c = BT.read();
        if (c == 'x') break;
        recv += c;
      }
      char Gear = recv[0];
      if (Gear == 'F' || Gear == 'B'){
        String snum;
        char s1,s2,s3;
        s1 = recv[1];
        s2 = recv[2];
        s3 = recv[3];
        snum = String(s1) + String(s2) + String(s3);
        int Force = snum.toInt();
        Serial.println(Force);
        if (Force > 255){
          Force = 255;
        }else if(Force < 0){
          Force = 0;
        }
        if (Gear == 'F'){
          forwardMotor(Force);
        }else{
          reverseMotor(Force);
        }
      }else if (Gear == 'L'){
        Left();
      }else if (Gear == 'R'){
        Right();
      }else if (Gear == 'N'){
        Straight();
      }else if (Gear == 'S'){
        stopMotors();
      }
    }
}
