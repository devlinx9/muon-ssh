# Changelog

## [v3.0.0] - 2025-04-03

### Added
- Local Terminal
- Change name in terminal tab
- Kubernetes context selector plugin

### Fixed
- Open in local file browser view
- Freeze muon window when open a new connection
- Russian language fixes (@K-Faktor)
- Retrieve the name when renaming option is selected in filebrowser

### Removed
- Normal transfer file option

### Developer Changes
- Updated libraries.
- Package reorganization and some code cleanup and optimization.
- Jediterm repo creation

## [v2.4.0] - 2025-02-26

### Added
- X11 support
- Download Folders from ssh menu

### Fixed
- Connection tree fix
- Import session from snowflake
- Export session zip file
- Passphrase and password saved for the session
- Master password security fix
- Open files on local machine
- Saved Settings
- Cursor fix after reconnection

### Developer Changes
- Updated libraries.
- Package reorganization and some code cleanup and optimization.

## [v2.3.0] - 2025-02-12

### Added
- **Language Support**: Added Chinese language support (@GWan1234).
- **File Browser**:
    - Added the word "local" in tab titles.
    - Option to show the local view in the left panel.
    - New arrangement of icons in the left panel.
    - Option to hide the left panel.
    - Reload file browser after upload or download in background mode (@Nymphxyz).
    - Delete shortcut in local view contextual menu (@Nymphxyz).
- **Windows**: Disabled additional command line at startup (@isycat).
- **Language Fixes**: Improvements in language files (@carloslockward, @JonathanCosta).
- **Terminal**: Defaulted to ANSI terminal due to incompatibilities with JEditerm and Xterm (vi and nano scrolling).

### Developer Changes
- Improved logging with Slf4j.
- Code cleanup and optimization.
- Updated libraries.

---

## [v2.2.0] - 2025-02-08

### Added
- **Language Support**: Added French and German language support.
- **Portable Version**: Added configuration path for portable version.

### Fixed
- **Private Key**: Bug fix for passphrase handling.

---

## [v2.1.0] - 2024-12-17

### Fixed
- **Processes and Services Tab**: Fixed sudo usage.
- **Symbolic Links**: Fixed handling of symbolic links for directories and files (@JonathanCosta).
- **Security**: Addressed CVE-2021-44228 (log4j vulnerability) (@Tomut0).
- **Processes Tab**: Enabled/disabled kill buttons.

### Other Changes
- Fixed download links in README pages.
- Added README page in Russian.
- Code cleanup in IDE (@Tomut0).
- Added Russian language support.

---

## [v2.0.0] - 2024-11-09

### Breaking Changes
- **Backup Recommended**: Changes may break settings from older versions. Users are advised to back up their configurations.

### Added
- **SSH Config Files**: Added support for importing SSH config files.
- **Settings**: Added two additional fields for sudo validation.
- **Main Screen**: Added the ability to select between SSH and FileBrowser as the default view.

### Fixed
- Resolved issue from Snowflake ticket #186.

### Changed
- Renamed the project from "Muon" to "MuonSSH" to avoid conflicts with an existing Ubuntu package.

---

## [v1.2.1] - 2024-11-01

### Changed
- Updated repositories to reflect the new repository (@devlinx9).

---

## [v1.2.0] - 2024-10-30

### Initial Release
- First release under the new repository (@devlinx9).

---

## [v1.1.0] - 2024-10-29

### Added
- **Project Renaming**: Explained the reason for renaming the project.

---

## [v1.0.0] - Initial Release
- Initial release of Muon SSH, forked from Snowflake.

---

For more details, visit the [Releases Page](https://github.com/devlinx9/muon-ssh/releases).