# 🎵 MP3 Offline Music Player (JavaFX)

![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-UI-blue?style=for-the-badge&logo=java)
![Maven](https://img.shields.io/badge/Maven-Build-red?style=for-the-badge&logo=apache-maven)

A sleek, offline desktop music player built with JavaFX. It features a custom, visually appealing vinyl-inspired UI, local file system browsing, album art extraction, and smooth animations.

<img width="1366" height="768" alt="screenshot_2026-11-10-214922" src="https://github.com/user-attachments/assets/5521e9ea-bd31-4200-8cea-7ca11435538d" />


## Features

- **Offline Playback:** Play local `.mp3` files without an internet connection.
- **File System Browser:** Navigate through your folders directly from the app. The list automatically sorts directories and tracks alphabetically.
- **Metadata Extraction:** Automatically reads ID3 tags to display the current song's title and album cover using `JAudioTagger`.
- **Vinyl Animation:** The album art rotates smoothly while the music is playing.
- **Modern UI:** Custom transparent controls, animated sliders, and a responsive layout that scales with the window.
- **Keyboard Shortcuts:** Control playback without touching the mouse.
- **Playback Modes:** Supports sequential playback, shuffle, and loop.

## Tech Stack

- **Language:** Java 17
- **GUI Framework:** JavaFX
- **Build Tool:** Maven
- **Metadata:** JAudioTagger
- **Icons:** Ikonli (FontAwesome 5 pack)

## License

This project is licensed under the **GNU General Public License v3.0 (GPL-3.0)**. See the [LICENSE](LICENSE) file for the full text.

## How to Run

### Option 1: Download the Installer (Recommended for users)
Go to the **[Releases](../../releases)** tab and download the latest `.deb` package (for Debian/Ubuntu-based Linux).
1. Double-click the downloaded `.deb` file.
2. Install it using your system's software installer.
3. Search for "MP3Player" in your applications menu and enjoy!

*For other operating systems, download the `.jar` file from the Releases and run it via terminal:*
`java -jar reproductor-mp3-1.0.0.jar`

### Option 2: Build from Source (For developers)
If you want to compile the project yourself, you will need **Java 17+** and **Maven** installed.

```bash
# 1. Clone the repository
git clone https://github.com/TuUsuario/TuRepositorio.git

# 2. Navigate to the project folder
cd TuRepositorio

# 3. Run the application
mvn clean javafx:run
