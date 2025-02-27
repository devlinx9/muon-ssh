# Muon SSH Terminal/Cliente SFTP (Anteriormente Snowflake)

[![Github All Releases](https://img.shields.io/github/downloads/subhra74/snowflake/total.svg)]()

<div> <img src="https://raw.githubusercontent.com/devlinx9/muonssh-screenshots/master/file-browser/2.png"> </div> 

**Muon SSH Terminal/Cliente SFTP** é um poderoso cliente SSH gráfico projetado para simplificar o trabalho com servidores remotos. Anteriormente conhecido como "Snowflake", o projeto foi renomeado para evitar confusão com outro produto popular de mesmo nome.

Este projeto é o trabalho contínuo de **subhra74**, que originalmente o desenvolveu como Snowflake.

---

## Por que suporte a idiomas?

Nem todos preferem ou se sentem confortáveis trabalhando com software em inglês. Ao oferecer suporte multilíngue, nosso objetivo é tornar nossa ferramenta mais amigável e acessível para um público global, garantindo que os usuários possam trabalhar no idioma com o qual se sintam mais confortáveis.

- [Español](README_es.md)
- [English](README.md)
- [Portuguese](README_pt.md)
- [Pусский](README_ru.md)
- [Français](README_fr.md)
- [Deutsch](README_de.md)
- [Chinese](README_zh.md)

---

## Índice
- [Sobre o Muon SSH](#sobre-o-muon-ssh)
- [Recursos](#recursos)
- [Instalação](#instalação)
    - [Pacote Snap (Recomendado)](#pacote-snap-recomendado)
    - [Pacote Deb](#pacote-deb)
- [Público-alvo](#público-alvo)
- [Download](#download)
- [Compilação a partir do código-fonte](#compilação-a-partir-do-código-fonte)
- [Registro de alterações](#registro-de-alterações)
- [Licença e bibliotecas de terceiros](#licença-e-bibliotecas-de-terceiros)
- [Roteiro](#roteiro)
- [Documentação](#documentação)
- [Desenvolvimento](#desenvolvimento)

---

## Sobre o Muon SSH
O Muon SSH é um cliente SSH gráfico rico em recursos que fornece uma interface intuitiva para gerenciar servidores remotos. Ele inclui ferramentas como:
- Navegador de arquivos SFTP aprimorado
- Emulador de terminal SSH
- Gerenciador de recursos/processos remotos
- Analisador de espaço em disco do servidor
- Editor de texto remoto com realce de sintaxe
- Visualizador de arquivos de log grandes
- E muito mais

O Muon SSH elimina a necessidade de instalações no servidor, pois opera inteiramente via SSH a partir da sua máquina local. Ele é compatível com Linux, Mac e Windows e foi testado em vários servidores Linux/UNIX, incluindo Ubuntu, CentOS, RHEL, OpenSUSE, FreeBSD, OpenBSD, NetBSD e HP-UX.

---

## Recursos
- **Interface gráfica para operações de arquivos**: Simplifica tarefas comuns de arquivos com uma interface intuitiva.
- **Editor de texto integrado**: Edite arquivos remotamente com realce de sintaxe e suporte para sudo.
- **Visualizador de arquivos de log/texto**: Visualize e pesquise rapidamente em arquivos de log ou texto grandes.
- **Pesquisa de arquivos e conteúdo**: Aproveite o poder do comando `find` para pesquisas rápidas.
- **Emulador de terminal**: Execute comandos facilmente usando o terminal integrado.
- **Gerenciador de tarefas**: Monitore e gerencie processos remotos.
- **Analisador de espaço em disco**: Visualize o uso do disco com uma ferramenta gráfica.
- **Gerenciamento de chaves SSH**: Gerencie facilmente as chaves SSH.
- **Ferramentas de rede**: Acesse um conjunto de utilitários de rede.
- **Suporte multilíngue**: Use o Muon SSH no seu idioma preferido.

---

## Instalação

### Pacote Snap (Recomendado)
[![Instale o Muon SSH Terminal usando Snap](https://snapcraft.io/muon-ssh/badge.svg)](https://snapcraft.io/muon-ssh)

O Muon SSH está disponível como um pacote Snap, garantindo uma instalação e atualização fáceis em várias distribuições Linux.

Para instalar, execute:
```sh  
sudo snap install muon-ssh  
```

Para a versão mais recente (pode ser instável):
```sh  
sudo snap install muon-ssh --edge    
```

### Pacote Deb
O Muon SSH também está disponível como um pacote Debian. Instale-o usando:
```sh  
sudo dpkg -i muon_package.deb   
```

---

## Público-alvo
O Muon SSH é projetado para:

**Desenvolvedores Web/Backend:** Simplifique a implantação e depuração em servidores remotos.

**Administradores de sistemas:** Gerencie vários servidores remotos de forma eficiente sem depender de comandos complexos de terminal.

---

## Download
**Nota**: Java 11 ou superior é necessário.

<table>
  <tr>
    <th>Versão</th>
    <th>Windows</th>
    <th>Ubuntu/Mint/Debian</th>
    <th>Mac OSes</th>
    <th>Portátil</th>
  </tr>
  <tr>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.deb">v2.4.0</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.exe">Arquivo Exe</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.deb">Instalador DEB</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.dmg">Instalador DMG</a>
    </td>
    <td>
      <a href="https://github.com/devlinx9/muon-ssh/releases/download/v2.4.0/muonssh_2.4.0.jar">JAR portátil (Java 11)</a>
    </td>
  </tr>
</table>

Para outras versões, visite a <a href="https://github.com/devlinx9/muon-ssh/releases">página de lançamentos</a>.

---

## Compilação a partir do código-fonte
O Muon SSH é um projeto padrão do Maven. Para compilar a partir do código-fonte, certifique-se de ter Java e Maven instalados, depois execute:
```sh  
mvn clean install  
```

O arquivo JAR compilado estará localizado no diretório `target`.

---

## Roteiro
Consulte [ROADMAP.md](ROADMAP.md)

---

## Documentação
Para documentação detalhada, visite a <a href="https://github.com/devlinx9/muon-ssh/wiki">Wiki do Muon SSH</a>.

---

## Desenvolvimento
**Branch principal**: <a href="https://github.com/devlinx9/muon-ssh">Versão estável</a>

**Branch de desenvolvimento**: <a href="https://github.com/devlinx9/muon-ssh/tree/develop">Desenvolvimento ativo</a>

---

## Licença e bibliotecas de terceiros
**Muon SSH**: consulte [LICENSE](LICENSE)

**JSch**: consulte [LICENSE-Jsch](LICENSE-Jsch)

---

## Registro de alterações
Para uma lista detalhada de alterações, correções de bugs e novos recursos, consulte o [CHANGELOG.md](CHANGELOG.md)