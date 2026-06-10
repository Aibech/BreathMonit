# System Monitorowania Oddechu (Breath Monitoring System)

## Opis Projektu
Projekt ten zawiera kod oprogramowania dla urządzenia badawczego służącego do monitorowania oddechu (np. podczas snu). System opiera się na elastycznym pasie pomiarowym z wbudowanymi tensometrami, które rejestrują zmiany nacisku wywołane ruchami klatki piersiowej lub powłok brzusznych podczas cyklu oddechowego. 

Zgromadzone dane są odczytywane przez mikrokontroler, a następnie przesyłane w czasie rzeczywistym – przewodowo przez port szeregowy lub bezprzewodowo za pomocą technologii Bluetooth Low Energy (BLE) do smartfona.

## 🛠️ Architektura Sprzętowa
Aby zbudować urządzenie, wymagane są następujące komponenty:
* **Mikrokontroler:** Seeed Studio XIAO (wyposażony w moduł BLE)
* **Czujniki:** Tensometry zintegrowane z pasem pomiarowym
* **Wzmacniacz sygnału i przetwornik ADC:** Moduł HX711

### Schemat podłączeń (Pinout)
| Moduł HX711 | Seeed Studio XIAO |
| :--- | :--- |
| VCC | 3.3V (lub 5V, zgodnie z logiką) |
| GND | GND |
| DT (Data) | D1 |
| SCK (Clock) | D3 |

## 💻 Wymagania Programowe
Projekt został napisany w środowisku Arduino IDE. Do poprawnej kompilacji i działania kodu niezbędne jest zainstalowanie następujących bibliotek z poziomu Menedżera Bibliotek (Library Manager):
* `HX711` autorstwa Bogdan Necula
* `ArduinoBLE` autorstwa Arduino

## 🚀 Instrukcja Uruchomienia
1. Sklonuj repozytorium na swój komputer lub pobierz pliki źródłowe w formie archiwum ZIP.
2. Otwórz plik `.ino` w programie Arduino IDE.
3. Podłącz urządzenie Seeed Studio XIAO do komputera.
4. Z menu `Narzędzia -> Płytka` wybierz odpowiednią konfigurację dla swojego modelu XIAO.
5. Z menu `Narzędzia -> Port` wybierz port, pod którym wykryto mikrokontroler.
6. Skompiluj i wgraj kod na urządzenie.

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
