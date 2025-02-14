# Muon SSH Terminal/SFTP client ( Formerly Snowflake ) [![Github All Releases](https://img.shields.io/github/downloads/subhra74/snowflake/total.svg)]()

- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_es.md">Español</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README.md">English</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_pt.md">Portuguese</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_ru.md">Pусский</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_fr.md">Français</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_de.md">Deutsch</a>

Easy and fun way to work with remote servers over SSH.

This project is being renamed as previous name "Snowflake" is confusing since there is already a popular product with the same name.

Muon is a graphical SSH client. It has a enhanced SFTP file browser, SSH terminal emulator, remote resource/process manager, server disk space analyzer, remote text editor, huge remote log viewer and lots of other helpful tools, which makes it easy to work with remote servers. Muon provides functionality similar to web based control panels but, it works over SSH from local computer, hence no installation required on server. It runs on Linux and Windows.
Muon has been tested with several Linux and UNIX servers, like Ubuntu server, CentOS, RHEL, OpenSUSE, FreeBSD, OpenBSD, NetBSD and HP-UX.

<div>
  <img src="https://raw.githubusercontent.com/devlinx9/muonssh-screenshots/master/file-browser/2.png">
</div>

## Installation

### Snap (Recommended)

[![Install Muon SSH Terminal using Snap](https://snapcraft.io/muon-ssh/badge.svg)](https://snapcraft.io/muon-ssh)

Muon SSH is available as a Snap package for easy installation and updates across multiple Linux distributions.

You can install it using:
```sh
sudo snap install muon-ssh
```
or last build package (may brake something)

```sh
sudo snap install muon-ssh --edge
```


### Deb package
Muon SSH is available as a Snap package for easy installation and updates across multiple Linux distributions.

You can install it using:
```sh
sudo dpkg -i muon_package.deb
```

<h3>Intended audience</h3>
<p>The application is targeted mainly towards web/backend developers who often deploy/debug their code on remote servers and not overly fond of complex terminal based commands. It could also be useful for sysadmins as well who manages lots of remote servers manually.
</p>

<h3>How it works</h3>
<div>
  <img src="https://github.com/subhra74/snowflake-screenshots/raw/master/arch-overview2.png">
</div>

<h2>Download*:</h2>

<table>
  <tr>
    <th>Versions</th>
    <th>Windows</th>
    <th>Ubuntu/Mint/Debian</th>
    <th>MacOS</th>
    <th>Other OSes</th>
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
<a href="https://github.com/devlinx9/muon-ssh/releases">Other Releases</a>
</p>

<p>
<b>* You must have Java 11 installed minimum</b>
</p>

<h2>Building from source:</h2>
<pre> This is a standard maven project. If you have configured Java and Maven use: 
 <b>mvn clean install</b> to build the project.
 The jar will be created in target directory
 </pre>

<h2>Features:</h2>

<ul>
  <li>Simple graphical interface for common file operations</li>
  <li>Built in text editor with syntax highlighting and support for sudo</li>
  <li>Simply view and search huge log/text files in a jiffy</li>
  <li>Fast powerful file and content search, powered by find command</li>
  <li>Built in terminal and command snippet</li>
  <li>Fully equiped task manager</li>
  <li>Built in graphical disk space analyzer</li>
  <li>Linux specific tools</li>
  <li>Manage SSH keys easily</li>
  <li>Network tools</li>
  <li>MultiLanguage Support</li>
</ul>

<h2>Documentation:</h2>

<p>
  <a href="https://github.com/devlinx9/muon-ssh/wiki">
    https://github.com/devlinx9/muon-ssh/wiki
  </a>
</p>


<h2>Devs:</h2>

<p>
<b>Main branch: it's for stable version</b> <br>
<b>Develop branch: it's for current development</b>
</p>