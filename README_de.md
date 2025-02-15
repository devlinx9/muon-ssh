# Muon SSH Terminal/SFTP-Client (ehemals Snowflake)

[![Github All Releases](https://img.shields.io/github/downloads/subhra74/snowflake/total.svg)]()

<div> <img src="https://raw.githubusercontent.com/devlinx9/muonssh-screenshots/master/file-browser/2.png"> </div> 

**Muon SSH Terminal/SFTP-Client** ist ein leistungsstarker grafischer SSH-Client, der entwickelt wurde, um die Arbeit mit Remote-Servern zu vereinfachen. Ehemals bekannt als "Snowflake", wurde das Projekt umbenannt, um Verwechslungen mit einem anderen beliebten Produkt gleichen Namens zu vermeiden.

Dieses Projekt ist die Weiterentwicklung von **subhra74**, der es ursprünglich als Snowflake entwickelt hat.

---

## Warum Sprachunterstützung?

Nicht jeder bevorzugt oder fühlt sich wohl bei der Arbeit mit englischsprachiger Software. Durch die Bereitstellung von Mehrsprachenunterstützung möchten wir unser Tool benutzerfreundlicher und für ein globales Publikum zugänglicher machen, sodass Benutzer in der Sprache arbeiten können, in der sie sich am wohlsten fühlen.

- [Español](README_es.md)
- [English](README.md)
- [Portuguese](README_pt.md)
- [Pусский](README_ru.md)
- [Français](README_fr.md)
- [Deutsch](README_de.md)
- [中文](README_zh.md)

---

## Inhaltsverzeichnis
- [Über Muon SSH](#über-muon-ssh)
- [Funktionen](#funktionen)
- [Installation](#installation)
    - [Snap-Paket (Empfohlen)](#snap-paket-empfohlen)
    - [Deb-Paket](#deb-paket)
- [Zielgruppe](#zielgruppe)
- [Download](#download)
- [Aus dem Quellcode erstellen](#aus-dem-quellcode-erstellen)
- [Änderungsprotokoll](#änderungsprotokoll)
- [Lizenz und Drittanbieter-Bibliotheken](#lizenz-und-drittanbieter-bibliotheken)
- [Roadmap](#roadmap)
- [Dokumentation](#dokumentation)
- [Entwicklung](#entwicklung)

---

## Über Muon SSH
Muon SSH ist ein funktionsreicher grafischer SSH-Client, der eine intuitive Oberfläche zur Verwaltung von Remote-Servern bietet. Es umfasst Tools wie:
- Verbesserter SFTP-Dateibrowser
- SSH-Terminalemulator
- Remote-Ressourcen-/Prozessmanager
- Server-Datenträger-Analyzer
- Remote-Texteditor mit Syntaxhervorhebung
- Großer Logdatei-Viewer
- Und vieles mehr

Muon SSH eliminiert die Notwendigkeit von Installationen auf der Serverseite, da es vollständig über SSH von Ihrem lokalen Rechner aus arbeitet. Es ist kompatibel mit Linux und Windows und wurde auf verschiedenen Linux/UNIX-Servern getestet, darunter Ubuntu, CentOS, RHEL, OpenSUSE, FreeBSD, OpenBSD, NetBSD und HP-UX.

---

## Funktionen
- **Grafische Oberfläche für Dateioperationen**: Vereinfacht gängige Dateiaufgaben mit einer intuitiven Oberfläche.
- **Integrierter Texteditor**: Bearbeiten Sie Dateien remote mit Syntaxhervorhebung und sudo-Unterstützung.
- **Log-/Textdatei-Viewer**: Schnelles Anzeigen und Durchsuchen großer Log- oder Textdateien.
- **Datei- und Inhaltsuche**: Nutzen Sie die Leistung des `find`-Befehls für schnelle Suchen.
- **Terminalemulator**: Führen Sie Befehle einfach mit dem integrierten Terminal aus.
- **Task-Manager**: Überwachen und verwalten Sie Remote-Prozesse.
- **Datenträger-Analyzer**: Visualisieren Sie die Datenträgernutzung mit einem grafischen Tool.
- **SSH-Schlüsselverwaltung**: Verwalten Sie SSH-Schlüssel einfach.
- **Netzwerk-Tools**: Zugriff auf eine Suite von Netzwerkdienstprogrammen.
- **Mehrsprachenunterstützung**: Verwenden Sie Muon SSH in Ihrer bevorzugten Sprache.

---

## Installation

### Snap-Paket (Empfohlen)
[![Installieren Sie Muon SSH Terminal mit Snap](https://snapcraft.io/muon-ssh/badge.svg)](https://snapcraft.io/muon-ssh)

Muon SSH ist als Snap-Paket verfügbar, das eine einfache Installation und Aktualisierung über mehrere Linux-Distributionen hinweg gewährleistet.

Zur Installation führen Sie aus:
```sh  
sudo snap install muon-ssh  
```

Für die neueste Version (kann instabil sein):
```sh  
sudo snap install muon-ssh --edge    
```

### Deb-Paket
Muon SSH ist auch als Debian-Paket verfügbar. Installieren Sie es mit:
```sh  
sudo dpkg -i muon_package.deb   
```

---

## Zielgruppe
Muon SSH ist konzipiert für:

**Web-/Backend-Entwickler:** Vereinfachen Sie die Bereitstellung und Fehlerbehebung auf Remote-Servern.

**Systemadministratoren:** Verwalten Sie mehrere Remote-Server effizient, ohne sich auf komplexe Terminalbefehle verlassen zu müssen.

---

## Download
**Hinweis**: Java 11 oder höher ist erforderlich.

<table>
  <tr>
    <th>Version</th>
    <th>Windows</th>
    <th>Ubuntu/Mint/Debian</th>
    <th>Mac OSes</th>
    <th>Portable</th>
  </tr>
  <tr>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.deb">v2.4.0</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.exe">Exe-Datei</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.deb">DEB-Installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.dmg">DMG-Installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.jar">Portable JAR (Java 11)</a>
    </td>
  </tr>
</table>

Für andere Versionen besuchen Sie die <a href="https://github.com/devlinx9/muon-ssh/releases">Release-Seite</a>.

---

## Aus dem Quellcode erstellen
Muon SSH ist ein Standard-Maven-Projekt. Um es aus dem Quellcode zu erstellen, stellen Sie sicher, dass Java und Maven installiert sind, und führen Sie dann aus:
```sh  
mvn clean install  
```

Die kompilierte JAR-Datei befindet sich im Verzeichnis `target`.

---

## Roadmap
Siehe [ROADMAP.md](ROADMAP.md)

---

## Dokumentation
Detaillierte Dokumentation finden Sie im <a href="https://github.com/devlinx9/muon-ssh/wiki">Muon SSH Wiki</a>.

---

## Entwicklung
**Hauptbranch**: <a href="https://github.com/devlinx9/muon-ssh">Stabile Version</a>

**Entwicklungsbranch**: <a href="https://github.com/devlinx9/muon-ssh/tree/develop">Aktive Entwicklung</a>

---

## Lizenz und Drittanbieter-Bibliotheken
**Muon SSH**: Siehe [LICENSE](LICENSE)

**JSch**: Siehe [LICENSE-Jsch](LICENSE-Jsch)

---

## Änderungsprotokoll
Eine detaillierte Liste der Änderungen, Fehlerbehebungen und neuen Funktionen finden Sie im [CHANGELOG.md](CHANGELOG.md)