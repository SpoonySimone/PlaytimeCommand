# Playtime Command

Playtime Command is a very simple mod that lets you see your own or other online players' playtime.

The playtime the mod fetches is the same one that you see in the Statistics menu, which is stored server-side.

The mod has no configuration and is entirely plug and play, simply drop this in your mods folder and you're good to go.

# Mod showcase
**Chat message**

![Chat Showcase](https://raw.githubusercontent.com/SpoonySimone/PlaytimeCommand/refs/heads/main/images/chat_showcase.png)


# Commands
- `/playtime` - See your own playtime
- `/playtime <username>` - See another online player's playtime

# Credits
Thanks to [Lucide](https://lucide.dev/) for providing the message icon

# Download
You can download the mod [here](https://modrinth.com/project/PlaytimeCommand).

# Development
> [!WARNING]
> This section is meant for developers who want to contribute to the mod or build it locally.
> 
> If you just want to use the mod, you can ignore this and [download the mod](https://github.com/SpoonySimone/PlaytimeCommand?tab=readme-ov-file#download) instead.

> [!NOTE]
> You need to have [Git](https://git-scm.com/) & [JDK 21](https://adoptium.net/temurin/releases?version=21&os=any&arch=any) installed on your computer.

To build the mod locally, you have to:
1. Clone the repository and navigate to it

   ```
   git clone https://github.com/SpoonySimone/PlaytimeCommand; cd PlaytimeCommand
   ``` 
2. Build the mod using Gradle

    **Linux**
    ```
    ./gradlew build
    ```
    **Windows**
    ```
    gradlew.bat build
    ```
3. The built mod will be located in `build/libs/`
