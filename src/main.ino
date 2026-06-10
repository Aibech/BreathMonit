#include "HX711.h"
#include <ArduinoBLE.h> // Dodana biblioteka do komunikacji ze smartfonem



// Definicje pinów (dostosowane np. do Seeed Studio XIAO)
#define HX711_DT  D1
#define HX711_SCK D3

HX711 scale;

// Konfiguracja Bluetooth (Unikalne identyfikatory UUID)
BLEService breathService("19B10000-E8F2-537E-4F6C-D104768A1214"); 
BLEStringCharacteristic breathChar("19B10001-E8F2-537E-4F6C-D104768A1214", BLERead | BLENotify, 20);

// Zmienne do nieblokującego odmierzania czasu (zamiast delay)
unsigned long previousMillis = 0;
const long interval = 100; // Próbkowanie co 100ms (10 Hz - wystarczające dla oddechu)

void setup() {
  Serial.begin(115200);
  
  // Inicjalizacja HX711
  scale.begin(HX711_DT, HX711_SCK);
  if (scale.is_ready()) {
    scale.tare(20);   // Tarowanie - ważne, by pas nie był naciągnięty przy starcie
    Serial.println("HX711: Tarowanie zakonczone sukcesem.");
  } else {
    Serial.println("HX711: Blad polaczenia z czujnikiem!");
  }

  // Inicjalizacja modułu Bluetooth
  if (!BLE.begin()) {
    Serial.println("BLE: Blad uruchamiania modulu!");
    while (1);
  }

  // Ustawienie nazwy widocznej w smartfonie i dodanie serwisu
  BLE.setLocalName("OnBreath_Monitor");
  BLE.setAdvertisedService(breathService);
  breathService.addCharacteristic(breathChar);
  BLE.addService(breathService);
  BLE.advertise();

  Serial.println("BLE: Urzadzenie gotowe do parowania.");
}

void loop() {
  // Nasłuchiwanie połączenia ze smartfonem
  BLEDevice central = BLE.central();
  
  unsigned long currentMillis = millis();

  // Niezależnie od tego, czy BLE jest połączone, czytamy dane w stałych odstępach (nieblokująco)
  if (currentMillis - previousMillis >= interval) {
    previousMillis = currentMillis;

    if (scale.is_ready()) {
      long raw = scale.read_average(3); // Mniejsza średnia = szybsza reakcja w czasie rzeczywistym
      long value = scale.get_value(3); 

      // Format przygotowany pod Serial Plotter w Arduino IDE
      Serial.print("RAW:");
      Serial.print(raw);
      Serial.print("\tNACISK:");
      Serial.println(value);

      // Jeśli smartfon jest połączony, wyślij dane
      if (central && central.connected()) {
        String dataPacket = String(value);
        breathChar.writeValue(dataPacket);
      }
    } else {
      Serial.println("RAW:0\tNACISK:0\tSTATUS:Blad_czujnika");
    }
  }
}
