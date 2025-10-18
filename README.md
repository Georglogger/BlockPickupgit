# BlockPickup Plugin

Ein Minecraft Plugin für Version 1.21.6+, das Spielern erlaubt, Kisten, Öfen und andere Container samt Inhalt aufzuheben sowie Entities zu transportieren.

## Features

- **Container aufheben**: Hebe Kisten, Öfen, Fässer, Trichter, Spender und mehr mit ihrem kompletten Inventar auf
- **Entities aufheben**: Hebe Tiere, Villager, Rüstungsständer und weitere Entities auf und trage sie mit dir
- **Inhalt bleibt erhalten**: Der gesamte Inventarinhalt bleibt beim Aufheben und Platzieren erhalten
- **Ofen-Status**: Auch Brennzeit und Kochfortschritt werden gespeichert
- **Entity-Daten**: Gesundheit, Alter, Farbe, Beruf und weitere Eigenschaften bleiben erhalten
- **Konfigurierbar**: Passe an, welche Blöcke und Entities aufgehoben werden können
- **Permissions**: Detaillierte Rechte-Verwaltung pro Block-Typ und Entity-Typ

## Installation

1. Java 17 oder höher installieren
2. Maven installieren
3. Plugin kompilieren:
   ```bash
   cd BlockPickup
   mvn clean package
   ```
4. Die generierte JAR-Datei aus `target/BlockPickup-1.0.0.jar` in den `plugins` Ordner deines Servers kopieren
5. Server neu starten

## Verwendung

### Blöcke aufheben
1. Halte **Shift** (Schleichen)
2. Breche einen Container (z.B. Kiste, Ofen) ab
3. Der Block wird mit seinem kompletten Inhalt in dein Inventar gelegt
4. Platziere den Block wieder - der Inhalt ist noch vorhanden!

### Entities aufheben
1. Halte **Shift** (Schleichen)
2. Rechtsklicke auf ein Entity (z.B. Kuh, Schaf)
3. Das Entity wird in ein Spawn-Ei verwandelt und in dein Inventar gelegt
4. Rechtsklicke mit dem Ei auf einen Block um das Entity wieder zu spawnen

## Befehle

- `/blockpickup help` - Zeigt die Hilfe
- `/blockpickup info` - Zeigt Plugin-Informationen
- `/blockpickup reload` - Lädt die Konfiguration neu (benötigt `blockpickup.admin`)

Aliase: `/bp`, `/pickup`

## Permissions

### Hauptrechte
- `blockpickup.admin` - Zugriff auf Admin-Befehle (Standard: op)
- `blockpickup.pickup.block` - Blöcke aufheben (Standard: true)
- `blockpickup.pickup.entity` - Entities aufheben (Standard: true)

### Block-spezifische Rechte
- `blockpickup.pickup.chest` - Kisten aufheben
- `blockpickup.pickup.furnace` - Öfen aufheben
- `blockpickup.pickup.barrel` - Fässer aufheben
- `blockpickup.pickup.hopper` - Trichter aufheben
- `blockpickup.pickup.dropper` - Werfer aufheben
- `blockpickup.pickup.dispenser` - Spender aufheben

## Konfiguration

Die `config.yml` ermöglicht folgende Anpassungen:

```yaml
# Muss der Spieler sneaken um Blöcke aufzuheben?
require-sneak: true

# Muss die Hand leer sein?
require-empty-hand: false

# Welche Blöcke können aufgehoben werden?
blocks:
  enabled: true
  allowed-types:
    - CHEST
    - FURNACE
    - BARREL
    # ... weitere

# Welche Entities können aufgehoben werden?
entities:
  enabled: true
  allowed-types:
    - COW
    - PIG
    - VILLAGER
    # ... weitere

# Anpassbare Nachrichten
messages:
  pickup-success: "&aErfolgreich aufgehoben!"
  pickup-failed: "&cDu kannst das nicht aufheben!"
  # ... weitere
```

## Unterstützte Container

- Kisten (normale & gefangene)
- Öfen (normale, Blast Furnace, Smoker)
- Fässer
- Trichter
- Spender & Werfer
- Braustand
- Shulker-Boxen (alle Farben)

## Unterstützte Entities

### Tiere
- Kuh, Schwein, Schaf, Huhn
- Pferd, Esel, Maultier, Lama
- Katze, Wolf

### Andere
- Dorfbewohner
- Rüstungsständer
- Bilderrahmen
- Loren (alle Typen)
- Boote (alle Holzarten)

## Technische Details

- **Minecraft Version**: 1.21.4+ (kompatibel mit 1.21.6+)
- **API**: Paper/Spigot API
- **Java Version**: 17 (minimum)
- **Speicherung**: NBT-Daten über PersistentDataContainer

## Entwicklung

### Projektstruktur
```
BlockPickup/
├── src/main/
│   ├── java/de/blockpickup/
│   │   ├── BlockPickupPlugin.java      # Haupt-Plugin-Klasse
│   │   ├── ConfigManager.java          # Konfigurationsverwaltung
│   │   ├── commands/
│   │   │   └── BlockPickupCommand.java # Befehls-Handler
│   │   ├── listeners/
│   │   │   ├── BlockPickupListener.java    # Block-Events
│   │   │   └── EntityPickupListener.java   # Entity-Events
│   │   └── utils/
│   │       └── NBTUtils.java           # NBT-Datenverarbeitung
│   └── resources/
│       ├── plugin.yml                  # Plugin-Metadaten
│       └── config.yml                  # Standard-Konfiguration
└── pom.xml                             # Maven-Konfiguration
```

### Building

```bash
mvn clean package
```

Die kompilierte JAR-Datei befindet sich in `target/BlockPickup-1.0.0.jar`.

## Lizenz

Dieses Plugin wurde für den privaten Gebrauch erstellt.

## Support

Bei Problemen oder Fragen erstelle ein Issue im Repository.
