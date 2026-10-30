#include <ArduinoBLE.h>

#define ADS1_DOUT D0
#define ADS1_SCLK D1
#define ADS1_PDWN D2

#define ADS2_DOUT D3
#define ADS2_SCLK D4
#define ADS2_PDWN D5

#define STATUS_LED LED_BUILTIN

const unsigned long READ_TIMEOUT_MS = 1500;


BLEService tensometerService("19B10000-E8F2-537E-4F6C-D104768A1214");
BLEStringCharacteristic dataCharacteristic("19B10001-E8F2-537E-4F6C-D104768A1214", BLERead | BLENotify, 32);

void setupAds1232(int pdwnPin, int doutPin, int sclkPin) {
  pinMode(pdwnPin, OUTPUT);
  pinMode(sclkPin, OUTPUT);
  pinMode(doutPin, INPUT);

  digitalWrite(sclkPin, LOW);
  digitalWrite(pdwnPin, LOW);
  delay(100);
  digitalWrite(pdwnPin, HIGH);
  delay(1000);
}

bool waitForReady(int doutPin) {
  unsigned long start = millis();
  while (digitalRead(doutPin) == HIGH) {
    if (millis() - start > READ_TIMEOUT_MS) {
      return false;
    }
  }
  return true;
}

long readAds1232Raw(int doutPin, int sclkPin, bool &ok) {
  ok = false;
  if (!waitForReady(doutPin)) return 0;

  long value = 0;
  noInterrupts();
  for (int i = 0; i < 24; i++) {
    digitalWrite(sclkPin, HIGH);
    delayMicroseconds(2);
    value <<= 1;
    if (digitalRead(doutPin)) value++;
    digitalWrite(sclkPin, LOW);
    delayMicroseconds(2);
  }
  digitalWrite(sclkPin, HIGH);
  delayMicroseconds(2);
  digitalWrite(sclkPin, LOW);
  delayMicroseconds(2);
  interrupts();

  if (value & 0x800000) value |= 0xFF000000;
  ok = true;
  return value;
}

void setup() {
  pinMode(STATUS_LED, OUTPUT);
  digitalWrite(STATUS_LED, LOW);

  Serial.begin(115200);

  // Inicjalizacja Bluetooth
  if (!BLE.begin()) {
    Serial.println("Inicjalizacja BLE nie powiodła się!");
    while (1);
  }

  //Bezpośredni zapis do rejestru układu nRF52840 moc sygnału TX od 0dBm(domyślnie) do +8dBm
  NRF_RADIO->TXPOWER = 6;
  // Konfiguracja nazwy urządzenia i serwisu BLE
  BLE.setLocalName("Xiao_Tensometr");
  BLE.setAdvertisedService(tensometerService);
  tensometerService.addCharacteristic(dataCharacteristic);
  BLE.addService(tensometerService);
  
  // Wartość początkowa
  dataCharacteristic.writeValue("0,0");
  
  // Rozpoczęcie rozgłaszania 
  BLE.advertise();
  Serial.println("BLE aktywne, oczekiwanie na połączenie...");

  for (int i = 0; i < 3; i++) {
    digitalWrite(STATUS_LED, HIGH);
    delay(150);
    digitalWrite(STATUS_LED, LOW);
    delay(150);
  }

  setupAds1232(ADS1_PDWN, ADS1_DOUT, ADS1_SCLK);
  setupAds1232(ADS2_PDWN, ADS2_DOUT, ADS2_SCLK);
}

void loop() {
  // Oczekiwanie na połączenie z telefonem
  BLEDevice central = BLE.central();

  //Flaga "Czy pomiar się udał"
  bool ads1_ok = false;
  bool ads2_ok = false;

  digitalWrite(STATUS_LED, HIGH);
  long ads1Raw = readAds1232Raw(ADS1_DOUT, ADS1_SCLK, ads1_ok);
  long ads2Raw = readAds1232Raw(ADS2_DOUT, ADS2_SCLK, ads2_ok);
  digitalWrite(STATUS_LED, LOW);

//Sprawdza czy pomiar się udał, jeżeli tak to wyślij ads1Raw, jak nie to wyślij '0' )
  String payload = String(ads1_ok ? ads1Raw : 0) + "," + String(ads2_ok ? ads2Raw : 0);
  
  Serial.println(payload); // Podgląd w Serial Monitorze

  if (central && central.connected()) {
    dataCharacteristic.writeValue(payload);
  }

  delay(200);
}