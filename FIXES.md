# Bug Fixes - BlockPickup Plugin

## Datum: 2025-10-26

### Behobene Probleme:

#### 1. Memory Leak bei Display Updates
**Problem:** Der Task für das visuelle Display-Update wurde nie gestoppt, was zu einem Memory Leak führte.

**Lösung:**
- Hinzugefügt: `displayUpdateTasks` Map zur Speicherung der Task-IDs
- Neue Methode: `stopDisplayUpdate(Player)` um Tasks korrekt zu beenden
- Tasks werden nun gestoppt wenn:
  - Display entfernt wird
  - Spieler nichts mehr trägt
  - Plugin disabled wird

**Betroffene Datei:** `CarryingManager.java` (Zeilen 31, 182-219, 224-229, 258-261)

---

#### 2. Datenverlust bei Entity-Serialisierung
**Problem:** Wenn `serializeAsBytes()` fehlschlug, wurde ein Fallback verwendet, der alle NBT-Daten (Verzauberungen, Custom Names, etc.) verlor.

**Lösung:**
- Verbesserte `serializeItem()` Methode mit mehreren Fallback-Stufen:
  1. **B64 Format** (bevorzugt): Vollständige Byte-Serialisierung
  2. **YAML Format** (Fallback): Bewahrt grundlegende Daten
  3. **SIMPLE Format** (letzter Ausweg): Nur Material + Menge
- Alle Formate haben einen eindeutigen Prefix zur Identifikation
- Besseres Logging bei Fehlern

**Betroffene Datei:** `NBTUtils.java` (Zeilen 643-685)

---

#### 3. Fehlerhafte Item-Deserialisierung
**Problem:** Die alte `deserializeItem()` Methode konnte nur das alte Format lesen.

**Lösung:**
- Erweiterte `deserializeItem()` Methode unterstützt nun:
  - Neues B64 Format
  - YAML Fallback Format
  - SIMPLE Format
  - Legacy Format (für Abwärtskompatibilität)
- Automatische Format-Erkennung anhand von Prefixes

**Betroffene Datei:** `NBTUtils.java` (Zeilen 690-742)

---

#### 4. Block-Pickup mit falscher Event-Aktion
**Problem:** `LEFT_CLICK_BLOCK` wurde behandelt, was eigentlich für das Abbauen gedacht ist, nicht für das Aufheben.

**Lösung:**
- Entfernt: `LEFT_CLICK_BLOCK` aus der Event-Behandlung
- Nur noch `RIGHT_CLICK_BLOCK` wird verwendet
- Klarere Dokumentation: "Sneaken + Rechtsklick"

**Betroffene Datei:** `BlockPickupListener.java` (Zeilen 31-39)

---

## Verbesserungen:

### Besseres Error Handling
- Alle Serialisierungs-Fehler werden nun geloggt
- Bessere Fehlermeldungen zeigen an, welches Item/Entity das Problem verursacht

### Performance
- Memory Leaks behoben
- Tasks werden korrekt gestoppt
- Keine unnötigen Task-Timer mehr

### Datenintegrität
- Entity-Daten gehen nicht mehr verloren
- Items mit Verzauberungen, Custom Names, etc. bleiben erhalten
- Container-Inventare bleiben vollständig erhalten

---

## Testen:

### Entity-Daten Test:
1. Zähme ein Tier (z.B. Wolf mit Namen und Halsband)
2. Hebe es auf (Shift + Rechtsklick)
3. Platziere es wieder (Rechtsklick auf Block)
4. **Erwartet:** Alle Daten (Name, Halsband-Farbe, Zähmung) bleiben erhalten

### Container-Inhalt Test:
1. Fülle eine Kiste mit verschiedenen Items (auch verzauberte Items)
2. Hebe die Kiste auf (Shift + Rechtsklick)
3. Platziere sie wieder
4. **Erwartet:** Alle Items mit allen Enchantments bleiben erhalten

### Ofen-Status Test:
1. Starte einen Schmelzvorgang in einem Ofen
2. Hebe den Ofen auf während er schmilzt
3. Platziere ihn wieder
4. **Erwartet:** Brennzeit und Schmelzfortschritt bleiben erhalten

### Memory Leak Test:
1. Hebe mehrere Objekte nacheinander auf und platziere sie
2. Nutze `/timings` oder einen Profiler
3. **Erwartet:** Keine zunehmende Task-Anzahl, kein Memory-Anstieg

---

## Wichtige Hinweise:

- **Abwärtskompatibilität:** Alte gespeicherte Entities/Items sollten weiterhin funktionieren (Legacy-Format-Support)
- **Backup:** Es wird empfohlen, vor dem Update ein Backup zu machen
- **Logs prüfen:** Bei Problemen die Logs auf Warnungen überprüfen

---

## Nächste Schritte:

Optional können folgende Features hinzugefügt werden:
- [ ] Unterstützung für mehr Entity-Typen (Creeper, Zombies, etc.)
- [ ] Konfigurierbare Geschwindigkeitsreduktion beim Tragen
- [ ] Partikel-Effekte beim Aufheben/Platzieren
- [ ] Sound-Effekte
- [ ] Cooldown-System
