# 💱 Currency Exchange Rate App

Nowoczesna aplikacja na system Android do śledzenia aktualnych i historycznych kursów walut. Projekt został napisany w języku **Kotlin** z wykorzystaniem w pełni deklaratywnego interfejsu **Jetpack Compose** oraz nowoczesnej architektury aplikacji mobilnych.

### 🌟 Główne funkcje

* **Śledzenie walut na żywo:** Przeglądaj aktualne kursy dla wybranej waluty bazowej.
* **Wykresy historyczne (Vico):** Detaliczny widok każdej waluty z interaktywnym wykresem liniowym prezentującym dane z 1, 7 lub 30 dni.
* **Tryb Offline:** Aplikacja automatycznie buforuje ostatnio pobrane dane. W przypadku braku połączenia z internetem wyświetla ostrzeżenie i ładuje dane z pamięci podręcznej.
* **Ulubione waluty:** Możliwość personalizacji ekranu głównego poprzez przypinanie i odpinanie wybranych walut (wsparcie dla flag państw).
* **Praca w tle (WorkManager):** Opcjonalne, automatyczne odświeżanie kursów w tle z konfigurowalnym interwałem czasowym (np. co 15 minut) lub wymuszone odświeżanie ręczne.
* **Zaawansowane ustawienia:** Zmiana waluty bazowej, dostosowanie formatu wyświetlania (liczba miejsc po przecinku) oraz zarządzanie częstotliwością odświeżania.

---

### 🛠 Technologie i Biblioteki

Aplikacja wykorzystuje nowoczesny stos technologiczny Androida:
* **Język:** Kotlin
* **UI:** Jetpack Compose, Material Design 3
* **Architektura:** MVVM (Model-View-ViewModel) z użyciem Kotlin Flow / StateFlow
* **Sieć:** Retrofit2 + Gson do komunikacji z API
* **Pamięć lokalna:** * `DataStore Preferences` (ustawienia aplikacji i ulubione)
  * `EncryptedSharedPreferences` (bezpieczne przechowywanie klucza API i wrażliwych danych)
* **Zadania w tle:** WorkManager (CoroutineWorker)
* **Wykresy:** Vico Compose (interaktywne i płynne wykresy)
* **Multimedia:** Coil (asynchroniczne ładowanie flag państw z sieci)

---

### 🔑 Konfiguracja API (Wymagane do uruchomienia)

Aplikacja pobiera dane o kursach walut z serwisu **ExchangeRate-API**. Aby projekt działał poprawnie na Twoim urządzeniu, musisz wygenerować własny darmowy klucz API i dodać go do kodu.

**Krok 1: Zdobądź klucz API**
1. Wejdź na stronę: 👉 [https://app.exchangerate-api.com/keys](https://app.exchangerate-api.com/keys)
2. Załóż darmowe konto lub zaloguj się.
3. Skopiuj swój wygenerowany klucz API (ciąg znaków).

**Krok 2: Dodaj klucz do projektu**
Otwórz plik `MainActivity.kt` znajdujący się w ścieżce:
`app/src/main/java/com/example/currencyexchangerateapp/MainActivity.kt`

Znajdź poniższy fragment kodu w metodzie `onCreate` i podmień `api-key-here` na swój skopiowany klucz (pamiętaj o dodaniu cudzysłowów `"Twój-Klucz"`, jeśli wymaga tego składnia):

```kotlin
val currentKey = settingsManager.getApiKey()

if (currentKey.isEmpty()) {
    // PODMIEŃ PONIŻSZY TEKST NA SWÓJ KLUCZ API
    settingsManager.saveApiKey("TUTAJ_WKLEJ_SWOJ_KLUCZ")
}
```
*Uwaga: Zastosowany `SettingsManager` korzysta z `EncryptedSharedPreferences`, dzięki czemu klucz jest bezpiecznie szyfrowany w pamięci urządzenia.*

---

### 📱 Struktura Ekranów

Aplikacja składa się z 4 głównych widoków zorganizowanych w architekturze nawigacji dolnej (Bottom Navigation):

1. **Home (MainScreen):** Siatka obserwowanych (ulubionych) walut. Pokazuje aktualny kurs w stosunku do waluty bazowej oraz procentową zmianę w czasie.
2. **DetailsScreen:** Szczegółowy ekran wybranej waluty z interaktywnym wykresem historii, aktualnym kursem i informacjami o źródle danych.
3. **FavouriteScreen:** Lista wszystkich dostępnych walut z przełącznikami (Switch) pozwalającymi łatwo zarządzać ekranem głównym.
4. **SettingsScreen:** Zarządzanie kluczem, odświeżaniem w tle, formatem liczb, statusem sieci oraz walutą bazową. Wymuszenie aktualizacji danych.

---

### 🚀 Jak uruchomić projekt lokalnie?

1. Sklonuj to repozytorium na swój komputer.
2. Otwórz projekt w **Android Studio**.
3. Zsynchronizuj pliki Gradle (IDE powinno to zrobić automatycznie).
4. Postępuj zgodnie z instrukcją w sekcji **Konfiguracja API**, aby podać swój klucz.
5. Wybierz emulator lub fizyczne urządzenie i kliknij **Run** (Shift + F10).
