# System Monitorowania Oddechu (Breath Monitoring System)

## Opis Projektu
Projekt ten zawiera kod oprogramowania dla urządzenia badawczego służącego do monitorowania oddechu (np. podczas snu). System opiera się na elastycznym pasie pomiarowym z wbudowanymi tensometrami, które rejestrują zmiany nacisku wywołane ruchami klatki piersiowej lub powłok brzusznych podczas cyklu oddechowego. 

Zgromadzone dane są odczytywane przez mikrokontroler, a następnie przesyłane w czasie rzeczywistym –  bezprzewodowo za pomocą technologii Bluetooth Low Energy (BLE) do smartfona.


## 💻 Wymagania Programowe
Projekt został napisany w środowisku Arduino IDE. Do poprawnej kompilacji i działania kodu niezbędne jest zainstalowanie następujących bibliotek z poziomu Menedżera Bibliotek (Library Manager):
* `HX711` 
* `ArduinoBLE`
  Do tego został napisany kod aplikacji na telefon z system Android.

## 🚀 Instrukcja Uruchomienia
1. Pobierz plik BreatheMonit.zip na telefon
2. Rozpakuj plik 

## 📡 Odczyt Danych
System oferuje dwa niezależne kanały transmisji danych pomiarowych:

**1. Połączenie Przewodowe (Monitor Portu Szeregowego)**
Otwórz "Serial Plotter" lub "Monitor Portu Szeregowego" w Arduino IDE. Ustaw prędkość transmisji (Baud Rate) na **115200**. Zobaczysz dane spływające w formacie:
`RAW:<wartosc_surowa>    NACISK:<wartosc_po_tarowaniu>`

**2. Połączenie Bezprzewodowe (Bluetooth Low Energy)**
Uruchom na smartfonie aplikację wspierającą BLE (np. nRF Connect, LightBlue lub dedykowaną aplikację mobilną). 
* Znajdź i połącz się z urządzeniem o nazwie: **`OnBreath_Monitor`**
* Subskrybuj charakterystykę (Notify), aby otrzymywać bieżące wartości nacisku pasa w czasie rzeczywistym.

## 📝 Ważne Uwagi
* **Tarowanie:** Podczas włączania zasilania i uruchamiania mikrokontrolera pas pomiarowy nie powinien być naciągnięty. System wykonuje automatyczne tarowanie (zerowanie) przy starcie.
* **Próbkowanie:** Kod został napisany asynchronicznie (bez funkcji `delay()`), co zapewnia płynne działanie modułu Bluetooth oraz częstotliwość próbkowania na poziomie 10 Hz (co 100 ms).
