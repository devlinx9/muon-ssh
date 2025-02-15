# Muon SSH Terminal/Cliente SFTP (Anteriormente Snowflake)

[![Github All Releases](https://img.shields.io/github/downloads/subhra74/snowflake/total.svg)]()

<div> <img src="https://raw.githubusercontent.com/devlinx9/muonssh-screenshots/master/file-browser/2.png"> </div> 

**Muon SSH Terminal/Cliente SFTP** es un potente cliente SSH gráfico diseñado para simplificar el trabajo con servidores remotos. Anteriormente conocido como "Snowflake", el proyecto ha sido renombrado para evitar confusiones con otro producto popular del mismo nombre.

Este proyecto es el trabajo continuo de **subhra74**, quien originalmente lo desarrolló como Snowflake.

---

## ¿Por qué soporte de idiomas?

No todo el mundo prefiere o se siente cómodo trabajando con software en inglés. Al ofrecer soporte multilingüe, nuestro objetivo es hacer que nuestra herramienta sea más amigable y accesible para una audiencia global, asegurando que los usuarios puedan trabajar en el idioma con el que se sientan más cómodos.

- [Español](README_es.md)
- [English](README.md)
- [Portuguese](README_pt.md)
- [Pусский](README_ru.md)
- [Français](README_fr.md)
- [Deutsch](README_de.md)
- [中文](README_zh.md)

---

## Tabla de contenidos
- [Acerca de Muon SSH](#acerca-de-muon-ssh)
- [Características](#características)
- [Instalación](#instalación)
    - [Paquete Snap (Recomendado)](#paquete-snap-recomendado)
    - [Paquete Deb](#paquete-deb)
- [Audiencia objetivo](#audiencia-objetivo)
- [Descarga](#descarga)
- [Compilación desde el código fuente](#compilación-desde-el-código-fuente)
- [Registro de cambios](#registro-de-cambios)
- [Licencia y bibliotecas de terceros](#licencia-y-bibliotecas-de-terceros)
- [Hoja de ruta](#hoja-de-ruta)
- [Documentación](#documentación)
- [Desarrollo](#desarrollo)

---

## Acerca de Muon SSH
Muon SSH es un cliente SSH gráfico rico en funciones que proporciona una interfaz intuitiva para gestionar servidores remotos. Incluye herramientas como:
- Navegador de archivos SFTP mejorado
- Emulador de terminal SSH
- Gestor de recursos/procesos remotos
- Analizador de espacio en disco del servidor
- Editor de texto remoto con resaltado de sintaxis
- Visor de archivos de registro grandes
- Y mucho más

Muon SSH elimina la necesidad de instalaciones en el servidor, ya que opera completamente a través de SSH desde tu máquina local. Es compatible con Linux y Windows y ha sido probado en varios servidores Linux/UNIX, incluyendo Ubuntu, CentOS, RHEL, OpenSUSE, FreeBSD, OpenBSD, NetBSD y HP-UX.

---

## Características
- **Interfaz gráfica para operaciones de archivos**: Simplifica tareas comunes de archivos con una interfaz intuitiva.
- **Editor de texto integrado**: Edita archivos de forma remota con resaltado de sintaxis y soporte para sudo.
- **Visor de archivos de registro/texto**: Visualiza y busca rápidamente en archivos de registro o texto grandes.
- **Búsqueda de archivos y contenido**: Aprovecha el poder del comando `find` para búsquedas rápidas.
- **Emulador de terminal**: Ejecuta comandos fácilmente usando la terminal integrada.
- **Gestor de tareas**: Monitorea y gestiona procesos remotos.
- **Analizador de espacio en disco**: Visualiza el uso del disco con una herramienta gráfica.
- **Gestión de claves SSH**: Gestiona fácilmente las claves SSH.
- **Herramientas de red**: Accede a un conjunto de utilidades de red.
- **Soporte multilingüe**: Usa Muon SSH en tu idioma preferido.

---

## Instalación

### Paquete Snap (Recomendado)
[![Instala Muon SSH Terminal usando Snap](https://snapcraft.io/muon-ssh/badge.svg)](https://snapcraft.io/muon-ssh)

Muon SSH está disponible como un paquete Snap, lo que garantiza una instalación y actualización sencilla en múltiples distribuciones de Linux.

Para instalar, ejecuta:

```sh  
sudo snap install muon-ssh  
```

Para la última compilación (puede ser inestable):
```sh  
sudo snap install muon-ssh --edge    
```

### Paquete Deb
Muon SSH también está disponible como un paquete Debian. Instálalo usando:

```sh  
sudo dpkg -i muon_package.deb   
```

---

## Audiencia objetivo
Muon SSH está diseñado para:

**Desarrolladores Web/Backend:** Simplifica la implementación y depuración en servidores remotos.

**Administradores de sistemas:** Gestiona múltiples servidores remotos de manera eficiente sin depender de comandos complejos de terminal.

---

## Descarga
**Nota**: Se requiere Java 11 o superior.

<table>
  <tr>
    <th>Versión</th>
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
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.exe">Archivo Exe</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.deb">Instalador DEB</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.dmg">Instalador DMG</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.jar">JAR portable (Java 11)</a>
    </td>
  </tr>
</table>

Para otras versiones, visita la <a href="https://github.com/devlinx9/muon-ssh/releases">página de lanzamientos</a>.

---

## Compilación desde el código fuente
Muon SSH es un proyecto estándar de Maven. Para compilar desde el código fuente, asegúrate de tener Java y Maven instalados, luego ejecuta:
```sh  
mvn clean install  
```


El archivo JAR compilado se ubicará en el directorio `target`.

---

## Hoja de ruta
Consulta [ROADMAP.md](ROADMAP.md)

---

## Documentación
Para documentación detallada, visita la <a href="https://github.com/devlinx9/muon-ssh/wiki">Wiki de Muon SSH</a>.

---

## Desarrollo
**Rama principal**: <a href="https://github.com/devlinx9/muon-ssh">Versión estable</a>

**Rama de desarrollo**: <a href="https://github.com/devlinx9/muon-ssh/tree/develop">Desarrollo activo</a>

---

## Licencia y bibliotecas de terceros
**Muon SSH**: consulta [LICENSE](LICENSE)

**JSch**: consulta [LICENSE-Jsch](LICENSE-Jsch)

---

## Registro de cambios
Para una lista detallada de cambios, correcciones de errores y nuevas características, consulta el [CHANGELOG.md](CHANGELOG.md)