# Library Management System

Ein umfassendes Bibliotheksverwaltungssystem entwickelt mit JavaFX für den Kurs IN257 Relational Databases.

## Überblick

Das Library Management System ist eine Desktop-Anwendung zur Verwaltung von Bibliotheksbeständen, Mitgliedern, Ausleihen und Gebühren. Die Anwendung bietet eine benutzerfreundliche grafische Oberfläche für Bibliothekspersonal zur effizienten Verwaltung aller bibliothekarischen Prozesse.

## Features

### 📊 Dashboard
- Übersicht über wichtige Statistiken (Gesamtmitglieder, Medien, aktive Ausleihen, überfällige Ausleihen)
- Schnellzugriff auf häufige Aktionen
- Diagramme zur Visualisierung von Daten

### 👥 Mitgliederverwaltung
- Neue Mitglieder hinzufügen, bearbeiten und verwalten
- Mitgliederstatus und Details anzeigen
- Kontaktaufnahme mit Mitgliedern

### 📚 Medienverwaltung
- Bücher, DVDs und CDs verwalten
- Autorendaten und Kategorien verwalten
- Verfügbarkeit und Standorte nachverfolgen

### 📋 Ausleihverwaltung
- Neue Ausleihen erstellen
- Rückgaben verarbeiten
- Ausleihhistorie verwalten

### ⚠️ Überfällige Ausleihen
- Überfällige Medien überwachen
- Automatische Benachrichtigungen

### 💰 Gebührenverwaltung
- Automatische Generierung von Mahngebühren
- Gebührenzahlungen verwalten
- Ausstehende Beträge nachverfolgen

### 📈 Statistiken
- Detaillierte Berichte und Analysen
- Nutzungsstatistiken
- Trends und Übersichten

## Technische Details

### Technologie-Stack
- **Frontend**: JavaFX 21
- **Datenbank**: H2 Database (in-memory/file-based)
- **Build-Tool**: Maven
- **Java-Version**: Java 17
- **IDE**: Beliebige Java-IDE (IntelliJ IDEA, Eclipse, etc.)

### Architektur
```
src/main/java/com/library/
├── LibraryManagementSystem.java    # Hauptklasse
├── model/                          # Datenmodelle
│   ├── Member.java
│   ├── Media.java
│   ├── Loan.java
│   ├── Fine.java
│   ├── Author.java
│   ├── Category.java
│   └── Staff.java
├── ui/                            # UI-Komponenten
│   ├── MemberManagementView.java
│   ├── MediaManagementView.java
│   ├── LoanManagementView.java
│   ├── FineManagementView.java
│   └── verschiedene Dialog-Klassen
└── service/
    └── DatabaseManager.java      # Datenbankoperationen
```

## Installation und Setup

### Voraussetzungen
- Java 17 oder höher
- Maven 3.6 oder höher
- Mindestens 2GB RAM
- Betriebssystem: Windows, macOS oder Linux

### Installation

1. **Projekt kompilieren**
   ```bash
   mvn clean compile
   ```

3. **Abhängigkeiten installieren**
   ```bash
   mvn dependency:resolve
   ```

## Verwendung

### Anwendung starten mit Maven

```bash
mvn javafx:run
```

### Erste Schritte

1. **Anwendung starten**
   - Die Anwendung startet mit dem Dashboard
   - Die Datenbank wird automatisch initialisiert

2. **Testdaten laden** (optional)
   - Die Anwendung erstellt automatisch die notwendigen Tabellen
   - Testdaten können über die SQL-Datei `library_data.sql` geladen werden

3. **Navigation verwenden**
   - Verwenden Sie das Seitenmenü zur Navigation zwischen den Modulen
   - Dashboard bietet einen schnellen Überblick über alle wichtigen Kennzahlen

### Grundlegende Workflows

#### Neues Mitglied hinzufügen
1. Zu "Members" navigieren
2. "Add Member" klicken
3. Formulär ausfüllen und speichern

#### Neue Ausleihe erstellen
1. Zu "Loans" navigieren
2. "Create Loan" klicken
3. Mitglied und Medium auswählen
4. Ausleihdetails bestätigen

#### Rückgabe verarbeiten
1. Zu "Loans" navigieren
2. Aktive Ausleihe auswählen
3. "Return" klicken
4. Rückgabe bestätigen

## Datenbankstruktur

Die Anwendung verwendet eine H2-Datenbank mit folgenden Haupttabellen:
- `members` - Bibliotheksmitglieder
- `media` - Bücher, DVDs, CDs
- `authors` - Autoreinformationen
- `categories` - Medienkategorien
- `loans` - Ausleihvorgänge
- `fines` - Gebühren und Mahnungen
- `staff` - Bibliothekspersonal

Details zur Datenbankstruktur finden Sie in der Datei `library_data.sql`.

## Konfiguration

### Datenbankeinstellungen
Die Datenbankverbindung kann in `src/main/resources/database.properties` konfiguriert werden.

### Styling
Das Erscheinungsbild kann über die CSS-Datei `src/main/resources/library-style.css` angepasst werden.

---

*Dieses Projekt wurde als Teil des Kurses IN257 Relational Databases und IN255 Programming 3 (Java) entwickelt und demonstriert die praktische Anwendung von Datenbankkonzepten in einer Desktop-Anwendung.*