# ItemBan Plugin

Ein Minecraft Plugin für Bukkit/Paper/Folia Server, das es ermöglicht, bestimmte Items in definierten Regionen und während des Kampfes zu verbieten.

## Features

- ✅ **Regionen-System**: Erstelle Regionen wo bestimmte Items nicht verwendet werden können
- ✅ **Kampf-System**: Verbiete Items für eine bestimmte Zeit nach einem Angriff
- ✅ **Folia Support**: Vollständig kompatibel mit Folia Servern
- ✅ **Konfigurierbare Nachrichten**: Alle Nachrichten können angepasst werden
- ✅ **MiniMessage Support**: Moderne Textformatierung mit MiniMessage

## Installation

1. Lade die `.jar` Datei herunter
2. Platziere sie im `plugins/` Ordner deines Servers
3. Starte den Server neu
4. Konfiguriere das Plugin nach Bedarf

## Commands

| Command | Beschreibung | Permission |
|---------|--------------|------------|
| `/itemban wand` | Gibt dir eine Regions-Axt | `itemban.command` |
| `/itemban create <name> [items]` | Erstellt eine neue Region | `itemban.command` |
| `/itemban delete <name>` | Löscht eine Region | `itemban.command` |
| `/itemban list` | Zeigt alle Regionen an | `itemban.command` |
| `/itemban info <name>` | Zeigt Informationen zu einer Region | `itemban.command` |
| `/itemban additem <region> <item>` | Fügt ein verbotenes Item zu einer Region hinzu | `itemban.command` |
| `/itemban removeitem <region> <item>` | Entfernt ein verbotenes Item aus einer Region | `itemban.command` |
| `/itemban reload` | Lädt die Konfiguration neu | `itemban.command` |

## Permissions

| Permission | Beschreibung | Standard |
|------------|--------------|----------|
| `itemban.admin` | Administrative Rechte | `op` |
| `itemban.command` | Verwendung der Befehle | `op` |
| `itemban.bypass` | Umgeht alle Item-Verbote | `false` |

## Verwendung

### Region erstellen

1. Verwende `/itemban wand` um die Regions-Axt zu erhalten
2. **Linksklick** auf einen Block um Position 1 zu setzen
3. **Rechtsklick** auf einen Block um Position 2 zu setzen
4. Verwende `/itemban create <n> [items]` um die Region zu erstellen

**Beispiel:**
```
/itemban create spawn ENDER_PEARL,CHORUS_FRUIT,TNT
```

### Items zu bestehender Region hinzufügen

```
/itemban additem spawn GOLDEN_APPLE
/itemban removeitem spawn TNT
```

### Kampf-System

Das Kampf-System wird automatisch aktiviert wenn:
- Ein Spieler einen anderen Spieler angreift
- Ein Spieler von einem anderen Spieler angegriffen wird
- Ein Spieler von einem Projektil getroffen wird, das von einem anderen Spieler abgefeuert wurde

Während des Kampfes können bestimmte Items (konfigurierbar) nicht verwendet werden.

## Konfiguration

Die Hauptkonfiguration befindet sich in `plugins/ItemBan/config.yml`:

```yaml
# Kampf-System
combat:
  enabled: true
  duration: 15  # Sekunden
  banned-items:
    - "ENDER_PEARL"
    - "CHORUS_FRUIT"
    - "GOLDEN_APPLE"

# Nachrichten
messages:
  enabled: true
  use-minimessage: true
  prefix: "<red>[ItemBan]</red>"
  item-banned-region: "&c&lDieses Item ist in dieser Region nicht erlaubt!"
  # ... weitere Nachrichten

# Regions-Axt
wand:
  material: "DIAMOND_AXE"
  name: "&6&lRegions-Wand"
  lore:
    - "&7Linksklick: Position 1 setzen"
    - "&7Rechtsklick: Position 2 setzen"
```

## Textformatierung

Das Plugin unterstützt drei verschiedene Textformate:

### Legacy Color Codes
```yaml
item-banned-region: "&c&lDieses Item ist nicht erlaubt!"
```

### Hex Colors
```yaml
item-banned-region: "&#FF0000&#lDieses Item ist nicht erlaubt!"
```

### MiniMessage (empfohlen)
```yaml
item-banned-region: "<red><bold>Dieses Item ist nicht erlaubt!"
```

## Folia Kompatibilität

Das Plugin ist vollständig kompatibel mit Folia und verwendet die entsprechenden Scheduler-APIs für optimale Performance.

## Build Instructions

```bash
git clone <repository>
cd ItemBan
mvn clean package
```

Die fertige `.jar` Datei findest du im `target/` Ordner.

## Systemanforderungen

- **Minecraft Version**: 1.21+
- **Server Software**: Paper, Bukkit, Spigot, Folia
- **Java Version**: 21+

## Support

Bei Problemen oder Fragen:
1. Überprüfe die Konsole auf Fehlermeldungen
2. Stelle sicher, dass alle Permissions korrekt gesetzt sind
3. Teste mit `/itemban reload` ob die Konfiguration korrekt ist

## Lizenz

Dieses Plugin steht unter der MIT Lizenz.
