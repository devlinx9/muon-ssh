# Muon SSH Terminal/SFTP client ( Formerly Snowflake ) [![Github All Releases](https://img.shields.io/github/downloads/subhra74/snowflake/total.svg)]()

- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_es.md">Español</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README.md">English</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_pt.md">Portuguese</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_ru.md">Pусский</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_fr.md">Français</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_de.md">Deutsch</a>

Une façon amusante et facile de travailler avec des serveurs distants via SSH. 

Ce projet a été renommé Muon en raison de la confusion de son ancien nom "Snowflake" car il existe un produit populaire portant le même nom. Muon est un client SSH graphique. 

Il dispose d'un navigateur de fichiers SFTP amélioré, d'un émulateur de terminal SSH, d'un gestionnaire de processus de ressources à distance, d'un analyseur d'espace disque de serveur, d'un éditeur de texte à distance, d'un énorme visualiseur de journaux à distance et de nombreux autres outils utiles, ce qui facilite le travail avec des serveurs distants. 

Muon fournit des fonctionnalités similaires aux panneaux de contrôle Web, mais fonctionne via SSH à partir de l'ordinateur local, donc aucune installation de serveur n'est requise. Il fonctionne sous Linux et Windows. Muon a été testé avec divers serveurs Linux et UNIX, tels que Ubuntu Server, CentOS, RHEL, OpenSUSE, FreeBSD, OpenBSD, NetBSD et HP-UX.

<div>
  <img src="https://raw.githubusercontent.com/devlinx9/muonssh-screenshots/master/file-browser/2.png">
</div>

## Installation

### Snap (Recommended)
[![Install Muon SSH Terminal using Snap](https://snapcraft.io/muon-ssh/badge.svg)](https://snapcraft.io/muon-ssh)

Muon SSH est disponible sous forme de paquet Debian (.deb) pour les utilisateurs préférant une installation manuelle.

Vous pouvez l’installer avec la commande suivante :
```sh
sudo snap install muon-ssh
```
or last build package (may brake something)

```sh
sudo snap install muon-ssh --edge
```

### Deb package
Muon SSH est disponible sous forme de paquet Debian (.deb) pour les utilisateurs préférant une installation manuelle.

Vous pouvez l’installer avec la commande suivante :
```sh
sudo dpkg -i muon_package.deb
```

<h3>Public objectif</h3>
<p>L'application est principalement destinée aux développeurs webbackend qui déploient et déboguent souvent leur code sur des serveurs distants et n'aiment pas trop les commandes complexes basées sur les terminaux. Cela peut également être utile pour les administrateurs système qui gèrent manuellement de nombreux serveurs distants.
</p>

<h3>Comment ça marche</h3>
<div>
  <img src="https://github.com/subhra74/snowflake-screenshots/raw/master/arch-overview2.png">
</div>

<h2>Télécharger*:</h2>

<table>
  <tr>
    <th>Versions</th>
    <th>Windows</th>
    <th>Ubuntu/Mint/Debian</th>
    <th>MacOS</th>
    <th>Autres OSes</th>
    <th>Portable</th>
  </tr>
<tr>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.deb">v2.4.0</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.exe">Exe file</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.deb">DEB installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.dmg">DMG installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.jar">Generic installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.jar">Portable JAR (Java 11)</a>
    </td>
  </tr>
  
</table>


<p>
<a href="https://github.com/devlinx9/muon-ssh/releases">Autres versions</a>
</p>

<p>
<b>* Vous devez avoir Java 11 installé au minimum</b>
</p>


<h2>Construire à la source:</h2>
<pre> Il s'agit d'un projet Maven standard. Si vous avez configuré Java et Maven, utilisez: 
 <b>mvn clean install</b> pour compiler le projet.
 Le jar sera créé dans le répertoire de destination.
 </pre>

<h2>Caractéristiques:</h2>

<ul>
  <li>Interface graphique simple pour les opérations courantes sur les fichiers</li>
  <li>Éditeur de texte intégré avec coloration syntaxique et prise en charge sudo</li>
  <li>Affichez et recherchez simplement d'énormes fichiers texte de journal en un tournemain</li>
  <li>Recherche rapide et puissante de fichiers et de contenu, alimentée par la commande de recherche</li>
  <li>Terminal intégré et extrait de commande</li>
  <li>Gestionnaire de tâches complet</li>
  <li>Analyseur d'espace disque graphique intégré</li>
  <li>Outils spécifiques à Linux</li>
  <li>Gérez facilement les clés SSH</li>
  <li>Outils réseau</li>
  <li>Prise en charge multilingue</li>
</ul>



<h2>Documentation:</h2>

<p>
  <a href="https://github.com/devlinx9/muon-ssh/wiki">
    https://github.com/devlinx9/muon-ssh/wiki
  </a>
</p>
