# Muon SSH Terminal/Client SFTP (anciennement Snowflake)

[![Github All Releases](https://img.shields.io/github/downloads/subhra74/snowflake/total.svg)]()

<div> <img src="https://raw.githubusercontent.com/devlinx9/muonssh-screenshots/master/file-browser/2.png"> </div> 

**Muon SSH Terminal/Client SFTP** est un puissant client SSH graphique conçu pour simplifier le travail avec les serveurs distants. Anciennement connu sous le nom de "Snowflake", le projet a été renommé pour éviter toute confusion avec un autre produit populaire du même nom.

Ce projet est la continuation du travail de **subhra74**, qui l'a initialement développé sous le nom de Snowflake.

---

## Pourquoi le support multilingue ?

Tout le monde ne préfère ou ne se sent pas à l'aise de travailler avec des logiciels en anglais. En offrant un support multilingue, nous visons à rendre notre outil plus convivial et accessible à un public mondial, en veillant à ce que les utilisateurs puissent travailler dans la langue qui leur est la plus confortable.

- [Español](README_es.md)
- [English](README.md)
- [Portuguese](README_pt.md)
- [Pусский](README_ru.md)
- [Français](README_fr.md)
- [Deutsch](README_de.md)
- [中文](README_zh.md)

---

## Table des matières
- [À propos de Muon SSH](#à-propos-de-muon-ssh)
- [Fonctionnalités](#fonctionnalités)
- [Installation](#installation)
    - [Paquet Snap (Recommandé)](#paquet-snap-recommandé)
    - [Paquet Deb](#paquet-deb)
- [Public cible](#public-cible)
- [Téléchargement](#téléchargement)
- [Compilation à partir du code source](#compilation-à-partir-du-code-source)
- [Journal des modifications](#journal-des-modifications)
- [Licence et bibliothèques tierces](#licence-et-bibliothèques-tierces)
- [Feuille de route](#feuille-de-route)
- [Documentation](#documentation)
- [Développement](#développement)

---

## À propos de Muon SSH
Muon SSH est un client SSH graphique riche en fonctionnalités qui fournit une interface intuitive pour gérer les serveurs distants. Il comprend des outils tels que :
- Navigateur de fichiers SFTP amélioré
- Émulateur de terminal SSH
- Gestionnaire de ressources/processus distants
- Analyseur d'espace disque du serveur
- Éditeur de texte distant avec coloration syntaxique
- Visionneuse de fichiers journaux volumineux
- Et bien plus encore

Muon SSH élimine le besoin d'installations côté serveur, car il fonctionne entièrement via SSH depuis votre machine locale. Il est compatible avec Linux, Mac et Windows et a été testé sur divers serveurs Linux/UNIX, notamment Ubuntu, CentOS, RHEL, OpenSUSE, FreeBSD, OpenBSD, NetBSD et HP-UX.

---

## Fonctionnalités
- **Interface graphique pour les opérations sur les fichiers**: Simplifie les tâches courantes sur les fichiers avec une interface intuitive.
- **Éditeur de texte intégré**: Modifiez les fichiers à distance avec coloration syntaxique et support sudo.
- **Visionneuse de fichiers journaux/texte**: Visualisez et recherchez rapidement dans les fichiers journaux ou texte volumineux.
- **Recherche de fichiers et de contenu**: Exploitez la puissance de la commande `find` pour des recherches rapides.
- **Émulateur de terminal**: Exécutez des commandes facilement avec le terminal intégré.
- **Gestionnaire de tâches**: Surveillez et gérez les processus distants.
- **Analyseur d'espace disque**: Visualisez l'utilisation du disque avec un outil graphique.
- **Gestion des clés SSH**: Gérez facilement les clés SSH.
- **Outils réseau**: Accédez à une suite d'utilitaires réseau.
- **Support multilingue**: Utilisez Muon SSH dans votre langue préférée.

---

## Installation

### Paquet Snap (Recommandé)
[![Installez Muon SSH Terminal avec Snap](https://snapcraft.io/muon-ssh/badge.svg)](https://snapcraft.io/muon-ssh)

Muon SSH est disponible en tant que paquet Snap, garantissant une installation et une mise à jour faciles sur plusieurs distributions Linux.

Pour installer, exécutez :
```sh  
sudo snap install muon-ssh  
```

Pour la dernière version (peut être instable) :
```sh  
sudo snap install muon-ssh --edge    
```

### Paquet Deb
Muon SSH est également disponible en tant que paquet Debian. Installez-le en utilisant :
```sh  
sudo dpkg -i muon_package.deb   
```

---

## Public cible
Muon SSH est conçu pour :

**Développeurs Web/Backend:** Simplifiez le déploiement et le débogage sur les serveurs distants.

**Administrateurs système:** Gérez plusieurs serveurs distants efficacement sans recourir à des commandes terminal complexes.

---

## Téléchargement
**Remarque**: Java 11 ou supérieur est requis.

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
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v3.0.0/muonssh_3.0.0.deb">v3.0.0</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v3.0.0/muonssh_3.0.0.exe">Fichier Exe</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v3.0.0/muonssh_3.0.0.deb">Installeur DEB</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v3.0.0/muonssh_3.0.0.dmg">Installeur DMG</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v3.0.0/muonssh_3.0.0.jar">JAR portable (Java 11)</a>
    </td>
  </tr>
</table>

Pour d'autres versions, visitez la <a href="https://github.com/devlinx9/muon-ssh/releases">page des versions</a>.

---

## Compilation à partir du code source
Muon SSH est un projet Maven standard. Pour le compiler à partir du code source, assurez-vous d'avoir Java et Maven installés, puis exécutez :
```sh  
mvn clean install  
```

Le fichier JAR compilé se trouvera dans le répertoire `target`.

---

## Feuille de route
Voir [ROADMAP.md](ROADMAP.md)

---

## Documentation
Pour une documentation détaillée, consultez le <a href="https://github.com/devlinx9/muon-ssh/wiki">Wiki de Muon SSH</a>.

---

## Développement
**Branche principale**: <a href="https://github.com/devlinx9/muon-ssh">Version stable</a>

**Branche de développement**: <a href="https://github.com/devlinx9/muon-ssh/tree/develop">Développement actif</a>

---

## Licence et bibliothèques tierces
**Muon SSH**: Voir [LICENSE](LICENSE)

**JSch**: Voir [LICENSE-Jsch](LICENSE-Jsch)

---

## Journal des modifications
Pour une liste détaillée des modifications, corrections de bugs et nouvelles fonctionnalités, consultez le [CHANGELOG.md](CHANGELOG.md)