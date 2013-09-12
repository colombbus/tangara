int val = 0;
int led = 8;
 
void setup()
{
 Serial.begin(9600);
 pinMode(led, OUTPUT);
 }
void loop()
{
 delay(100);
}
 
void serialEvent() // To check if there is any data on the Serial line
 
{
while (Serial.available())
 {
val = Serial.parseInt();
if(val == 1)   //Switch on the LED, if the received value is 1.
{
digitalWrite(led, HIGH);
}
else if(val == 0) //Switch off the LED, if the received value is 1.
{
digitalWrite(led, LOW);
}
}
 
Serial.println("Succesfully received.");
 }
