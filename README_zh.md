# Muon SSH 终端/SFTP 客户端 (原 Snowflake)

[![Github All Releases](https://img.shields.io/github/downloads/subhra74/snowflake/total.svg)]()

<div> <img src="https://raw.githubusercontent.com/devlinx9/muonssh-screenshots/master/file-browser/2.png"> </div> 

**Muon SSH 终端/SFTP 客户端** 是一款功能强大的图形化 SSH 客户端，旨在简化与远程服务器的交互。该项目原名 "Snowflake"，为避免与另一款同名流行产品混淆，现已更名为 Muon SSH。

该项目是 **subhra74** 的持续开发成果，最初以 Snowflake 的名义发布。

---

## 为什么需要多语言支持？

并非所有人都习惯或喜欢使用基于英语的软件。通过提供多语言支持，我们希望使工具更加用户友好，并让全球用户能够以自己最熟悉的语言使用它。

- [Español](README_es.md)
- [English](README.md)
- [Portuguese](README_pt.md)
- [Pусский](README_ru.md)
- [Français](README_fr.md)
- [Deutsch](README_de.md)
- [中文](README_zh.md)

---

## 目录
- [关于 Muon SSH](#关于-muon-ssh)
- [功能](#功能)
- [安装](#安装)
  - [Snap 包 (推荐)](#snap-包-推荐)
  - [Deb 包](#deb-包)
- [目标用户](#目标用户))
- [下载](#下载)
- [从源代码构建](#从源代码构建)
- [更新日志](#更新日志)
- [许可证与第三方库](#许可证与第三方库)
- [路线图](#路线图)
- [文档](#文档)
- [开发](#开发)

---

## 关于 Muon SSH
Muon SSH 是一款功能丰富的图形化 SSH 客户端，提供直观的界面来管理远程服务器。它包括以下工具：
- 增强的 SFTP 文件浏览器
- SSH 终端模拟器
- 远程资源/进程管理器
- 服务器磁盘空间分析器
- 支持语法高亮的远程文本编辑器
- 大日志文件查看器
- 以及更多功能

Muon SSH 无需在服务器端安装任何软件，因为它完全通过 SSH 从本地机器运行。它兼容 Linux, Mac 和 Windows，并已在多种 Linux/UNIX 服务器上测试，包括 Ubuntu、CentOS、RHEL、OpenSUSE、FreeBSD、OpenBSD、NetBSD 和 HP-UX。

---

## 功能
- **图形化文件操作界面**：通过直观的界面简化常见的文件操作。
- **内置文本编辑器**：远程编辑文件，支持语法高亮和 sudo 权限。
- **日志/文本文件查看器**：快速查看和搜索大型日志或文本文件。
- **文件与内容搜索**：利用 `find` 命令的强大功能进行快速搜索。
- **终端模拟器**：轻松执行命令。
- **任务管理器**：监控和管理远程进程。
- **磁盘空间分析器**：通过图形化工具可视化磁盘使用情况。
- **SSH 密钥管理**：轻松管理 SSH 密钥。
- **网络工具**：访问一系列网络实用工具。
- **多语言支持**：使用您偏好的语言操作 Muon SSH。

---

## 安装

### Snap 包 (推荐)
[![通过 Snap 安装 Muon SSH 终端](https://snapcraft.io/muon-ssh/badge.svg)](https://snapcraft.io/muon-ssh)

Muon SSH 提供 Snap 包，确保在多种 Linux 发行版上轻松安装和更新。

安装命令：
```sh  
sudo snap install muon-ssh  
```

安装最新版本（可能不稳定）：
```sh  
sudo snap install muon-ssh --edge    
```

### Deb 包
Muon SSH 也提供 Debian 包。使用以下命令安装：
```sh  
sudo dpkg -i muon_package.deb   
```

---

## 目标用户
Muon SSH 专为以下用户设计：

**Web/后端开发人员**：简化远程服务器的部署和调试。

**系统管理员**：高效管理多个远程服务器，无需依赖复杂的终端命令。

---

## 下载
**注意**：需要 Java 11 或更高版本。

<table>
  <tr>
    <th>版本</th>
    <th>Windows</th>
    <th>Ubuntu/Mint/Debian</th>
    <th>Mac OSes</th>
    <th>便携版</th>
  </tr>
  <tr>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.deb">v2.4.0</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.exe">Exe 文件</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.deb">DEB 安装包</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.dmg">DMG 安装包</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.jar">便携版 JAR (Java 11)</a>
    </td>
  </tr>
</table>

其他版本请访问 <a href="https://github.com/devlinx9/muon-ssh/releases">发布页面</a>。

---

## 从源代码构建
Muon SSH 是一个标准的 Maven 项目。要从源代码构建，请确保已安装 Java 和 Maven，然后运行：
```sh  
mvn clean install  
```

编译后的 JAR 文件将位于 `target` 目录中。

---

## 路线图
查看 [ROADMAP.md](ROADMAP.md)

---

## 文档
详细文档请访问 <a href="https://github.com/devlinx9/muon-ssh/wiki">Muon SSH Wiki</a>。

---

## 开发
**主分支**: <a href="https://github.com/devlinx9/muon-ssh">稳定版</a>

**开发分支**: <a href="https://github.com/devlinx9/muon-ssh/tree/develop">活跃开发版</a>

---

## 许可证与第三方库
**Muon SSH**: 参见 [LICENSE](LICENSE)

**JSch**: 参见 [LICENSE-Jsch](LICENSE-Jsch)

---

## 更新日志
详细的变更、错误修复和新功能列表，请参阅 [CHANGELOG.md](CHANGELOG.md)