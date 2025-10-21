# BlockPickup Plugin - Chat Protokoll

**Datum:** 19. Oktober 2025
**Projekt:** BlockPickup Minecraft Plugin

---

## Session 1 - Plugin Erstellung

### Anforderungen
- Minecraft Plugin für Version 1.21.6+
- Spieler sollen Kisten, Öfen und Container mit Inhalt aufheben können
- Entities sollen aufgehoben werden können
- Inhalte sollen in der Hand gehalten werden (nicht verloren gehen)

### Durchgeführte Schritte

#### 1. Projekt-Setup
- ✅ Maven-Projektstruktur erstellt
- ✅ pom.xml konfiguriert mit Paper API 1.21.4
- ✅ Java Version: 17 (minimum)
- ✅ plugin.yml mit Metadaten und Permissions erstellt
- ✅ config.yml mit vollständiger Konfiguration erstellt

#### 2. Implementierte Features

**Container-System:**
- BlockPickupListener.java - Behandelt das Aufheben und Platzieren von Containern
- Unterstützte Container:
  - Kisten (normal & trapped)
  - Öfen (Furnace, Blast Furnace, Smoker)
  - Fässer (Barrel)
  - Trichter (Hopper)
  - Spender & Werfer (Dispenser & Dropper)
  - Braustand (Brewing Stand)
  - Shulker-Boxen (alle Farben)

**Entity-System:**
- EntityPickupListener.java - Behandelt das Aufheben und Spawnen von Entities
- Unterstützte Entities:
  - Tiere: Kuh, Schwein, Schaf, Huhn, Pferd, Esel, Maultier, Lama, Katze, Wolf
  - NPCs: Villager
  - Objekte: Rüstungsständer, Bilderrahmen, Minecarts, Boote

**Datenspeicherung:**
- NBTUtils.java - Speichert und lädt alle relevanten Daten
- Container-Inventare bleiben erhalten
- Ofen-Brennzeit und Kochfortschritt werden gespeichert
- Entity-Eigenschaften: Gesundheit, Alter, Farbe, Beruf, Zähmung, etc.
- Verwendet PersistentDataContainer für NBT-Daten

**Konfiguration & Verwaltung:**
- ConfigManager.java - Verwaltet alle Einstellungen
- BlockPickupCommand.java - Commands: /bp help, /bp info, /bp reload
- Vollständige Permission-Kontrolle pro Block/Entity-Typ

#### 3. Build-System
- ✅ GitHub Actions Workflows erstellt
- ✅ Automatischer Build bei Push
- ✅ Release-System mit Tags (v1.0.0)
- ✅ Artifact-Upload für einfachen Download

#### 4. Dokumentation
- ✅ README.md - Vollständige Projekt-Dokumentation
- ✅ INSTALLATION.md - Detaillierte Installationsanleitung
- ✅ .gitignore erstellt

#### 5. Java Version Anpassung
**Änderung: Java 21 → Java 17**

User wollte das Plugin mit Java 17 kompatibel machen:
- ✅ pom.xml: java.version von 21 auf 17 geändert
- ✅ build.yml: JDK 21 → JDK 17
- ✅ release.yml: JDK 21 → JDK 17
- ✅ README.md aktualisiert
- ✅ INSTALLATION.md aktualisiert

### Verwendung

**Blöcke aufheben:**
1. Shift halten
2. Container abbauen
3. Item mit Inhalt wird in Inventar gelegt
4. Beim Platzieren wird Inhalt wiederhergestellt

**Entities aufheben:**
1. Shift halten
2. Entity rechtsklicken
3. Spawn-Ei mit gespeicherten Daten erhalten
4. Mit Ei rechtsklicken um Entity wieder zu spawnen

### Konfiguration
```yaml
require-sneak: true          # Shift erforderlich?
require-empty-hand: false    # Leere Hand erforderlich?

blocks:
  enabled: true
  allowed-types: [...]       # Liste der erlaubten Blöcke

entities:
  enabled: true
  allowed-types: [...]       # Liste der erlaubten Entities
```

### Build & Deployment

**Option 1: GitHub Actions (Empfohlen)**
1. Repository auf GitHub erstellen
2. Code hochladen
3. Actions → Workflow läuft automatisch
4. Artifacts → BlockPickup.jar herunterladen

**Option 2: Lokaler Build**
```bash
cd BlockPickup
mvn clean package
# JAR: target/BlockPickup-1.0.0.jar
```

### Aktueller Status

**Maven Installation:**
- User installiert gerade Maven mit Chocolatey
- Fehler beim Chocolatey-Installationsbefehl (mehrzeilig kopiert)
- Alternative Lösungen angeboten:
  - PowerShell Befehl in einer Zeile
  - Manuelle Maven Installation

**Nächste Schritte:**
1. Maven Installation abschließen
2. Plugin bauen und testen
3. JAR auf Server installieren
4. Testen im Spiel

---

## Technische Details

**Projektstruktur:**
```
BlockPickup/
├── .github/workflows/
│   ├── build.yml         # Auto-Build
│   └── release.yml       # Release-Erstellung
├── src/main/
│   ├── java/de/blockpickup/
│   │   ├── BlockPickupPlugin.java
│   │   ├── ConfigManager.java
│   │   ├── commands/
│   │   │   └── BlockPickupCommand.java
│   │   ├── listeners/
│   │   │   ├── BlockPickupListener.java
│   │   │   └── EntityPickupListener.java
│   │   └── utils/
│   │       └── NBTUtils.java
│   └── resources/
│       ├── plugin.yml
│       └── config.yml
├── pom.xml
├── README.md
├── INSTALLATION.md
└── .gitignore
```

**Abhängigkeiten:**
- Paper API 1.21.4-R0.1-SNAPSHOT
- Java 17 (minimum)
- Maven 3.x

**Permissions:**
- `blockpickup.admin` - Admin-Befehle
- `blockpickup.pickup.block` - Blöcke aufheben
- `blockpickup.pickup.entity` - Entities aufheben
- `blockpickup.pickup.<blocktype>` - Spezifische Blöcke

---

## Offene Punkte

- [ ] Maven Installation abschließen
- [ ] Erstes Build durchführen
- [ ] Plugin auf Test-Server installieren
- [ ] In-Game Tests durchführen

---

**Letzte Aktualisierung:** 19.10.2025 - Session 1
