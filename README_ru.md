# Muon SSH Terminal/SFTP client ( Formerly Snowflake ) [![Github All Releases](https://img.shields.io/github/downloads/subhra74/snowflake/total.svg)]()

- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_es.md">Español</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README.md">English</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_pt.md">Portuguese</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_ru.md">Pусский</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_fr.md">Français</a>
- <a href="https://github.com/devlinx9/muon-ssh/blob/master/README_de.md">Deutsch</a>

Простой и увлекательный способ работы с удаленными серверами через SSH. Этот проект был переименован в Muon, потому что его предыдущее название «Снежинка» сбивает с толку, и есть популярный продукт с таким же названием. 

Muon - это графический SSH-клиент. Он имеет улучшенный браузер файлов SFTP, эмулятор терминала SSH, диспетчер процессов удаленных ресурсов, анализатор дискового пространства сервера, удаленный текстовый редактор, огромный удаленный просмотрщик журналов и многие другие полезные инструменты, которые упрощают работу с ним. удаленные серверы. 

Muon предоставляет аналогичные функциональные возможности веб-панелям управления, но работает через SSH с локального компьютера, поэтому установка сервера не требуется. Работает в Linux и Windows. 

Muon был протестирован с различными серверами Linux и UNIX, такими как сервер Ubuntu, CentOS, RHEL, OpenSUSE, FreeBSD, OpenBSD, NetBSD и HP-UX.

<div>
  <img src="https://raw.githubusercontent.com/devlinx9/muonssh-screenshots/master/file-browser/2.png">
</div>

## Installation

### Snap (Recommended)

[![Install Muon SSH Terminal using Snap](https://snapcraft.io/muon-ssh/badge.svg)](https://snapcraft.io/muon-ssh)

Muon SSH доступен в виде пакета Debian (.deb) для пользователей, предпочитающих ручную установку.

Вы можете установить его с помощью команды:

```sh
sudo snap install muon-ssh
```
or last build package (may brake something)

```sh
sudo snap install muon-ssh --edge
```

### Deb package
Muon SSH доступен в виде пакета Debian (.deb) для пользователей, предпочитающих ручную установку.

Вы можете установить его с помощью команды:

```sh
sudo dpkg -i muon_package.deb
```

<h3>Целевые аудитории</h3>
<p>Приложение в первую очередь нацелено на веб-разработчиков, которые часто развертывают отладку своего кода на удаленных серверах и не слишком любят сложные терминальные команды. Это также может быть полезно для системных администраторов, которые вручную управляют множеством удаленных серверов.
</p>

<h3>Как это работает</h3>
<div>
  <img src="https://github.com/subhra74/snowflake-screenshots/raw/master/arch-overview2.png">
</div>

<h2>Скачать*:</h2>

<table>
  <tr>
    <th>Версии</th>
    <th>Windows</th>
    <th>Ubuntu/Mint/Debian</th>
    <th>MacOS</th>
    <th>Другие OSes</th>
    <th>Портативный</th>
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
<a href="https://github.com/devlinx9/muon-ssh/releases">Другие версии</a>
</p>

<p>
<b>* У вас должна быть установлена ​​как минимум Java 11.</b>
</p>



<h2>Строительство из первоисточника:</h2>
<pre> Это стандартный проект maven. Если вы настроили Java и Maven, используйте: 
 <b>mvn clean install</b> скомпилировать проект.
 Баночка будет создана в целевом каталоге..
 </pre>

<h2>Характеристики:</h2>

<ul>
  <li>Простой графический интерфейс для стандартных файловых операций</li>
  <li>Встроенный текстовый редактор с подсветкой синтаксиса и поддержкой sudo</li>
  <li>Просто просматривайте и ищите огромные текстовые файлы журналов в мгновение ока</li>
  <li>Быстрый и эффективный поиск файлов и содержимого с помощью команды поиска</li>
  <li>Интегрированный терминал и фрагмент команды</li>
  <li>Полностью оборудованный диспетчер задач</li>
  <li>Встроенный графический анализатор дискового пространства</li>
  <li>Специальные инструменты для Linux</li>
  <li>Легко управлять ключами SSH</li>
  <li>Сетевые инструменты</li>
  <li>Многоязычная поддержка</li>
</ul>



<h2>Документация:</h2>

<p>
  <a href="https://github.com/devlinx9/muon-ssh/wiki">
    https://github.com/devlinx9/muon-ssh/wiki
  </a>
</p>
