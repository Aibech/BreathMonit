## 🚀 Instrukcja Uruchomienia
1. Pobierz plik BreatheCheck.zip na telefon
2. Rozpakuj plik
3. Zainstaluj aplikację poprzez kliknięcie programu "BreathCheck.apk"
4. Uruchom aplikacje i zaakceptuj zgody na udostepnienie funkcji aplikacji
5. Przy podłączonym urządzeniu wciśnij przycisk "SCAN", aby połączyć się z urzadzeniem

## 📝 Ważne Uwagi
- W celu wyczyszczenia przebiegów czasowych należy wcisnąć przycisk "Wyczyść"
- aby sprawić by wykres nie przesuwał się i przedstawiał dane punktowe od początku do obecnej chwili, należy przeciągnąć wykres w lewo(przesunąć palcem z lewa na prawo) oraz oddalić wykres.
- Zasięg działania łączności bezprzewodowej wynosi około 20 kroków/1 pokój  
- Ustalenie punktu początkowego przy kalibracji wykonuje się przy podłączeniu urządzenia do zasilania. W celu ponownego ustalenia punktu zerowego należy odłączyć i podłączyć urzadzenie

# System Monitorowania Oddechu (Breath Monitoring System)

## Opis Projektu
Projekt ten zawiera kod oprogramowania dla urządzenia badawczego służącego do monitorowania oddechu (np. podczas snu). System opiera się na elastycznym pasie pomiarowym z wbudowanymi tensometrami, które rejestrują zmiany nacisku wywołane ruchami klatki piersiowej lub powłok brzusznych podczas cyklu oddechowego. 

Zgromadzone dane są odczytywane przez mikrokontroler, a następnie przesyłane w czasie rzeczywistym –  bezprzewodowo za pomocą technologii Bluetooth Low Energy (BLE) do smartfona.


## 💻 Wymagania Programowe
1.Projekt został napisany w środowisku Arduino IDE. Do poprawnej kompilacji i działania kodu niezbędne jest zainstalowanie następujących bibliotek z poziomu Menedżera Bibliotek (Library Manager):
* `HX711` 
* `ArduinoBLE`
2.Do tego został napisany kod aplikacji na telefon z systemem Android. 



