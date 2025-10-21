# Server Installation - BlockPickup Plugin

## 📥 Download

### Option 1: Von GitHub Actions (Empfohlen)
1. Gehe zu: https://github.com/[DEIN-USERNAME]/BlockPickupgit/actions
2. Klicke auf den neuesten erfolgreichen Build (grüner Haken ✓)
3. Scrolle nach unten zu **"Artifacts"**
4. Klicke auf **"BlockPickup"** zum Herunterladen
5. Entpacke die ZIP-Datei

### Option 2: Vom neuesten Release
1. Gehe zu: https://github.com/[DEIN-USERNAME]/BlockPickupgit/releases
2. Lade die neueste `BlockPickup-*.jar` Datei herunter

---

## 🚀 Installation auf dem Server

### Voraussetzungen
- **Minecraft Server Version:** 1.21.4 - 1.21.6
- **Server Software:** PaperMC, Spigot oder Bukkit
- **Java Version:** 21 oder höher

### Installations-Schritte

1. **Server stoppen**
   ```bash
   # Führe im Server-Terminal aus:
   stop
   ```

2. **Plugin hochladen**
   - Verbinde dich per FTP/SFTP zu deinem Server
   - Navigiere zum `plugins/` Ordner deines Servers
   - Lade die `BlockPickup-1.0.0.jar` Datei in den `plugins/` Ordner hoch

   **Ordner-Struktur:**
   ```
   minecraft-server/
   ├── plugins/
   │   └── BlockPickup-1.0.0.jar  ← Hier hin
   ├── server.jar
   └── ...
   ```

3. **Server starten**
   ```bash
   # Starte den Server neu
   java -Xmx2G -Xms2G -jar server.jar nogui
   ```

4. **Überprüfung**
   ```bash
   # Im Server-Terminal:
   plugins
   ```
   Du solltest "BlockPickup" in der Liste sehen (grün = geladen)

---

## ⚙️ Konfiguration (Optional)

Nach dem ersten Start wird eine Konfigurationsdatei erstellt:

**Datei:** `plugins/BlockPickup/config.yml`

```yaml
# Hier kannst du einstellen, welche Blöcke/Entities aufgehoben werden können
```

Nach Änderungen der Config:
```bash
# Im Server-Terminal:
blockpickup reload
```

---

## 🎮 Verwendung

### Blöcke mit Inhalt aufheben
- Halte **Shift** (Schleichen)
- Breche Container-Blöcke ab (Kisten, Öfen, etc.)
- Der Block wird mit seinem kompletten Inhalt als Item gedroppt

### Entities aufheben
- Halte **Shift** (Schleichen)
- Rechtsklick auf ein Entity (z.B. Rüstungsständer, Minecart, Boot)
- Das Entity wird als Spawn Egg oder Item gedroppt

### Blöcke/Entities wieder platzieren
- Platziere das aufgehobene Item wie gewohnt
- Der komplette Inhalt wird wiederhergestellt

---

## 🐛 Troubleshooting

### Plugin lädt nicht
**Problem:** Plugin erscheint rot in `/plugins`

**Lösung:**
1. Überprüfe die Server-Version: Muss 1.21.4 - 1.21.6 sein
2. Überprüfe Java-Version:
   ```bash
   java -version
   # Muss Java 21 sein
   ```
3. Schaue in die `logs/latest.log` für Fehler

### Java Version zu alt
**Fehler:** `Unsupported class file major version 65`

**Lösung:** Installiere Java 21:
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jre

# Windows: Download von adoptium.net
```

---

## 📝 Befehle

| Befehl | Beschreibung | Permission |
|--------|-------------|------------|
| `/blockpickup` | Zeigt Plugin-Info | `blockpickup.use` |
| `/blockpickup reload` | Lädt Config neu | `blockpickup.admin` |
| `/blockpickup help` | Zeigt Hilfe | `blockpickup.use` |

---

## 🔐 Permissions

Standardmäßig haben alle Spieler Zugriff. Für Permissions-Plugins (z.B. LuckPerms):

```yaml
# Normale Spieler
blockpickup.use: true

# Admins
blockpickup.admin: true
```

---

## 📞 Support

Bei Problemen:
1. Schaue in `logs/latest.log`
2. Erstelle ein Issue auf GitHub
3. Füge Log-Auszüge bei
