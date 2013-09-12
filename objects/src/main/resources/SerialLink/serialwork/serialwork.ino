#include <LiquidCrystal.h>

LiquidCrystal lcd(8, 9, 4, 5, 6, 7);

String chain = "";
char recup;
int i = 0;

void setup() {
  Serial.begin(9600);
  lcd.begin(16,2);
  lcd.print("Welcome!");
  delay(3000);
}

void loop() {
  lcd.clear();
  Serial.println(recup);
  delay(1500);
  while(Serial.available()) {
    recup = Serial.read();
    chain.concat(recup);
  }
  lcd.print(chain);
  delay(1500);
  chain = "";
}
