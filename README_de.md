# Muon SSH Terminal/SFTP client ( Formerly Snowflake ) ![Java CI](https://github.com/subhra74/snowflake/workflows/Java%20CI/badge.svg?branch=master) [![Github All Releases](https://img.shields.io/github/downloads/subhra74/snowflake/total.svg)]()

- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_es.md">Español</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README.md">English</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_pt.md">Portuguese</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_ru.md">Pусский</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_fr.md">Français</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_de.md">Deutsch</a>

Eine unterhaltsame und einfache Möglichkeit, über SSH mit Remote-Servern zu arbeiten. 
Dieses Projekt wurde in Muon umbenannt, da der frühere Name "Snowflake" verwirrend war, da es ein beliebtes Produkt mit demselben Namen gibt. 

Muon ist ein grafischer SSH-Client. Es verfügt über einen verbesserten SFTP-Dateibrowser, einen SSH-Terminalemulator, einen Remote-Ressourcen-Prozessmanager, einen Server-Speicherplatzanalysator, einen Remote-Texteditor, einen riesigen Remote-Log-Viewer und viele andere nützliche Tools, die die Arbeit mit Remote-Servern erleichtern . 

Muon bietet ähnliche Funktionen wie webbasierte Control Panels, funktioniert jedoch über SSH vom lokalen Computer aus, sodass keine Serverinstallation erforderlich ist. Es funktioniert unter Linux und Windows. Muon wurde mit verschiedenen Linux- und UNIX-Servern wie Ubuntu Server, CentOS, RHEL, OpenSUSE, FreeBSD, OpenBSD, NetBSD und HP-UX getestet.

[![IMAGE ALT TEXT](https://raw.githubusercontent.com/subhra74/snowflake-screenshots/master/Capture32.PNG)](https://youtu.be/G2qHZ2NodeM "View on YouTube")

## Installation

### Snap (Recommended)

[![Install Muon SSH Terminal using Snap](https://snapcraft.io/muon-ssh/badge.svg)](https://snapcraft.io/muon-ssh)

Muon SSH ist als Snap-Paket verfügbar, um eine einfache Installation und Updates auf verschiedenen Linux-Distributionen zu ermöglichen.

Sie können es mit folgendem Befehl installieren:
```sh
sudo snap install muon-ssh
```

### Deb package
Muon SSH ist als Snap-Paket verfügbar, um eine einfache Installation und Updates auf verschiedenen Linux-Distributionen zu ermöglichen.

Sie können es mit folgendem Befehl installieren:
```sh
sudo dpgk -i muon_package.deb
```

<h3>Zielpublikum</h3>
<p>Die App richtet sich in erster Linie an Webbackend-Entwickler, die ihren Code häufig auf Remote-Servern bereitstellen und debuggen und komplexe Terminal-basierte Befehle nicht besonders mögen. Es könnte auch für Systemadministratoren nützlich sein, die viele entfernte Server manuell verwalten.
</p>

<h3>Wie funktioniert es</h3>
<div>
  <img src="https://github.com/subhra74/snowflake-screenshots/raw/master/arch-overview2.png">
</div>

<h2>Abladen*:</h2>

<table>
  <tr>
    <th>Wendung</th>
    <th>Windows</th>
    <th>Ubuntu/Mint/Debian</th>
    <th>MacOS</th>
    <th>Andere OSes</th>
    <th>Portabel</th>
  </tr>
<tr>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.3.0/muonssh_2.3.0.deb">v2.3.0</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.3.0/muonssh_2.3.0.exe">Exe file</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.3.0/muonssh_2.3.0.deb">DEB installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.3.0/muonssh_2.3.0.dmg">DMG installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.3.0/muonssh_2.3.0.jar">Generic installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.3.0/muonssh_2.3.0.jar">Portable JAR (Java 11)</a>
    </td>
  </tr>
  <tr>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.1.0/muonssh_2.1.0.deb">v2.1.0</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.1.0/muonssh_2.1.0.exe">Exe file</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.1.0/muonssh_2.1.0.deb">DEB installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.1.0/muonssh_2.1.0.dmg">DMG installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.1.0/muonssh_2.1.0.jar">Generic installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.1.0/muonssh_2.1.0.jar">Portable JAR (Java 11)</a>
    </td>
  </tr>
  <tr>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.0.0/muonssh_2.0.0.deb">v2.0.0</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.0.0/muonssh_2.0.0.exe">Exe file</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.0.0/muonssh_2.0.0.deb">DEB installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.0.0/muonssh_2.0.0.dmg">DMG installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.0.0/muonssh_2.0.0.jar">Generic installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.0.0/muonssh_2.0.0.jar">Portable JAR (Java 11)</a>
    </td>
  </tr>
  <tr>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v1.2.1/muon_1.2.1.deb">v1.2.1</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v1.2.1/muon_1.2.1.exe">Exe file</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v1.2.1/muon_1.2.1.deb">DEB installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v1.2.1/muon_1.2.1.dmg">DMG installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v1.2.1/muon_1.2.1.jar">Generic installer</a>
    </td>   
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v1.2.1/muon_1.2.1.jar">Portable JAR (Java 11)</a>
    </td>
  </tr>
</table>


<p>
<a href="https://github.com/devlinx9/muon-ssh/releases">Andere Versionen</a>
</p>

<p>
<b>* Sie müssen mindestens Java 11 installiert haben</b>
</p>


<h2>Bauen von der Quelle:</h2>
<pre> Dies ist ein Standard-Maven-Projekt. Wenn Sie Java und Maven konfiguriert haben, verwenden Sie: 
 <b>mvn clean install</b> um das Projekt zu kompilieren.
 Das JAR wird im Zielverzeichnis erstellt.
 </pre>

<h2>Eigenschaften:</h2>

<ul>
  <li>Einfache grafische Oberfläche für allgemeine Dateioperationen</li>
  <li>Integrierter Texteditor mit Syntaxhervorhebung und Sudo-Unterstützung</li>
  <li>Sehen und durchsuchen Sie einfach riesige Protokolltextdateien im Handumdrehen</li>
  <li>Schnelle und leistungsstarke Suche nach Dateien und Inhalten, unterstützt durch den Suchbefehl</li>
  <li>Integriertes Terminal- und Befehls-Snippet</li>
  <li>Voll ausgestatteter Task-Manager</li>
  <li>Integrierter grafischer Speicherplatzanalysator</li>
  <li>Linux-spezifische Tools</li>
  <li>Verwalten Sie SSH-Schlüssel einfach</li>
  <li>Netzwerk-Tools</li>
  <li>Mehrsprachige Unterstützung</li>
</ul>



<h2>Beurkundung:</h2>

<p>
  <a href="https://github.com/devlinx9/muon-ssh/wiki">
    https://github.com/devlinx9/muon-ssh/wiki
  </a>
</p>
