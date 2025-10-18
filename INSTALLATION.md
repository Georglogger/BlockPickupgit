# Installation Anleitung

## Schnellstart mit GitHub (Empfohlen)

### 1. Repository auf GitHub hochladen

1. Erstelle ein neues Repository auf GitHub (z.B. `BlockPickup`)
2. Öffne ein Terminal/CMD im BlockPickup Ordner
3. Führe folgende Befehle aus:

```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/DEIN-USERNAME/BlockPickup.git
git push -u origin main
```

### 2. Automatischer Build

Sobald du den Code hochgeladen hast:
1. Gehe zu deinem Repository auf GitHub
2. Klicke auf "Actions" (oben im Menü)
3. Der Build startet automatisch
4. Nach ca. 1-2 Minuten ist der Build fertig
5. Klicke auf den neuesten Workflow-Run
6. Scrolle runter zu "Artifacts"
7. Lade "BlockPickup" herunter (ZIP-Datei)
8. Entpacke die ZIP - darin ist die `BlockPickup-1.0.0.jar`

### 3. JAR auf Server installieren

1. Kopiere die `BlockPickup-1.0.0.jar` in den `plugins` Ordner deines Minecraft-Servers
2. Starte den Server neu
3. Fertig!

---

## Alternative: Release erstellen (für stabile Versionen)

Wenn du eine stabile Version veröffentlichen möchtest:

```bash
git tag v1.0.0
git push origin v1.0.0
```

GitHub erstellt dann automatisch ein Release mit der fertigen JAR-Datei unter "Releases"!

---

## Alternative: Lokaler Build (Ohne GitHub)

Falls du das Plugin lokal bauen möchtest:

### Windows

1. **Java 17 installieren**:
   - Gehe zu https://adoptium.net/
   - Lade "Eclipse Temurin 17 (LTS)" herunter
   - Installiere es

2. **Maven installieren**:
   - Gehe zu https://maven.apache.org/download.cgi
   - Lade "Binary zip archive" herunter
   - Entpacke es nach `C:\Program Files\Maven`
   - Füge `C:\Program Files\Maven\bin` zu deinem PATH hinzu:
     - Windows-Taste → "Umgebungsvariablen" suchen
     - "Systemumgebungsvariablen bearbeiten"
     - "Umgebungsvariablen" → Path → Bearbeiten
     - "Neu" → `C:\Program Files\Maven\bin` einfügen
     - OK, OK, OK

3. **Plugin bauen**:
   - Öffne CMD/PowerShell im BlockPickup Ordner
   - Führe aus: `mvn clean package`
   - Die JAR-Datei ist dann in `target\BlockPickup-1.0.0.jar`

---

## Konfiguration

Nach dem ersten Start wird der Ordner `plugins/BlockPickup` erstellt mit:
- `config.yml` - Hier kannst du alles anpassen

Bearbeite die Datei nach deinen Wünschen und nutze `/bp reload` um die Änderungen zu laden.

---

## Problembehandlung

### Plugin lädt nicht
- Prüfe die `logs/latest.log` auf Fehler
- Stelle sicher dass du Minecraft 1.21+ nutzt
- Prüfe ob Paper oder Spigot installiert ist (nicht Vanilla!)

### Blöcke können nicht aufgehoben werden
- Prüfe ob du die Permission hast: `/lp user DEINNAME permission set blockpickup.pickup.block`
- Stelle sicher dass du **Shift/Schleichen** hältst
- Prüfe die `config.yml` ob der Block-Typ aktiviert ist

### Entities können nicht aufgehoben werden
- Prüfe die Permission: `/lp user DEINNAME permission set blockpickup.pickup.entity`
- Stelle sicher dass du **Shift/Schleichen** hältst beim Rechtsklick
- Prüfe die `config.yml` ob der Entity-Typ aktiviert ist

---

## Support

Bei Fragen erstelle ein Issue auf GitHub: https://github.com/DEIN-USERNAME/BlockPickup/issues
