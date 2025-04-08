# Muon SSH Terminal/SFTP Client (Formerly Snowflake)

[![Github All Releases](https://img.shields.io/github/downloads/subhra74/snowflake/total.svg)]()

<div> <img src="https://raw.githubusercontent.com/devlinx9/muonssh-screenshots/master/file-browser/2.png"></div> 

**Muon SSH Terminal/SFTP Client** is a powerful graphical SSH client designed to simplify working with remote servers. Formerly known as "Snowflake," the project has been renamed to avoid confusion with another popular product of the same name.

This project is the continued work of **subhra74**, who originally developed it as Snowflake.

---

## Why Language Support?

Not everyone prefers or is comfortable working with English-based software. By offering multi-language support, we aim to make our tool more user-friendly and accessible to a global audience, ensuring that users can work in the language they are most comfortable with.

- [Español](README_es.md)
- [English](README.md)
- [Portuguese](README_pt.md)
- [Pусский](README_ru.md)
- [Français](README_fr.md)
- [Deutsch](README_de.md)
- [中文](README_zh.md)


---

## Table of Contents
- [About Muon SSH](#about-muon-ssh)
- [Features](#features)
- [Installation](#installation)
    - [Snap Package (Recommended)](#snap-package-recommended)
    - [Deb Package](#deb-package)
- [Intended Audience](#intended-audience)
- [Download](#download)
- [Building from Source](#building-from-source)
- [Changelog](#changelog)
- [License and Third-Party Libraries](#license-and-third-party-libraries)
- [Roadmap](#roadmap)
- [Documentation](#documentation)
- [Development](#development)

---

## About Muon SSH
Muon SSH is a feature-rich graphical SSH client that provides an intuitive interface for managing remote servers. It includes tools such as:
- Enhanced SFTP file browser
- SSH terminal emulator
- Remote resource/process manager
- Server disk space analyzer
- Remote text editor with syntax highlighting
- Large log file viewer
- And much more

Muon SSH eliminates the need for server-side installations, as it operates entirely over SSH from your local machine. It is compatible with Linux, Mac and Windows and has been tested on various Linux/UNIX servers, including Ubuntu, CentOS, RHEL, OpenSUSE, FreeBSD, OpenBSD, NetBSD, and HP-UX.

---

## Features
- **Graphical Interface for File Operations**: Simplify common file tasks with an intuitive interface.
- **Built-in Text Editor**: Edit files remotely with syntax highlighting and sudo support.
- **Log/Text File Viewer**: Quickly view and search large log or text files.
- **File and Content Search**: Leverage the power of the `find` command for fast searches.
- **Terminal Emulator**: Execute commands with ease using the built-in terminal.
- **Task Manager**: Monitor and manage remote processes.
- **Disk Space Analyzer**: Visualize disk usage with a graphical tool.
- **SSH Key Management**: Easily manage SSH keys.
- **Network Tools**: Access a suite of network utilities.
- **Multi-language Support**: Use Muon SSH in your preferred language.

---

## Installation

### Snap Package (Recommended)
[![Install Muon SSH Terminal using Snap](https://snapcraft.io/muon-ssh/badge.svg)](https://snapcraft.io/muon-ssh)

Muon SSH is available as a Snap package, ensuring easy installation and updates across multiple Linux distributions.

To install, run:
```sh  
sudo snap install muon-ssh  
```

For the latest build (may be unstable):

```sh  
sudo snap install muon-ssh --edge    
```
### Deb Package
Muon SSH is also available as a Debian package. Install it using:

```sh  
sudo dpkg -i muon_package.deb   
```
## Intended Audience
Muon SSH is designed for:

**Web/Backend Developers:** Simplify deployment and debugging on remote servers.

**System Administrators:** Manage multiple remote servers efficiently without relying on complex terminal commands.

## Download
**Note**: Java 11 or higher is required.

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
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v3.0.0/muonssh_3.0.0.exe">Exe file</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v3.0.0/muonssh_3.0.0.deb">DEB installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v3.0.0/muonssh_3.0.0.dmg">DMG installer</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v3.0.0/muonssh_3.0.0.jar">Portable JAR (Java 11)</a>
    </td>
  </tr>

</table>
For other releases, visit <a href="https://github.com/devlinx9/muon-ssh/releases">release page</a> 

## Building from Source
Muon SSH is a standard Maven project. To build from source, ensure you have Java and Maven installed, then run:

```sh  
mvn clean install  
```

The compiled JAR file will be located in the target directory.

---

## Roadmap
See [ROADMAP.md](ROADMAP.md)

## Documentation
For detailed documentation, visit the <a href="https://github.com/devlinx9/muon-ssh/wiki">Muon SSH Wiki</a>

## Development
**Main Branch**: <a href="https://github.com/devlinx9/muon-ssh">Stable release</a> 

**Develop Branch**: <a href="https://github.com/devlinx9/muon-ssh/tree/develop">Active development</a>  

---

## License and Third-Party Libraries
**Muon SSH** refer to [LICENSE](LICENSE)

**JSch**: refer to [LICENSE-Jsch](LICENSE-Jsch)

---

## Changelog
For a detailed list of changes, bug fixes, and new features, refer to the [CHANGELOG.md](CHANGELOG.md)