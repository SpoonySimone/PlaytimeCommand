# Playtime Command

Playtime Command is a very simple mod that lets you see your own or other online players' playtime.

The mod provides some unique features, such as:
- **Plug and play**: Requires zero configuration, in fact there isn't even one!
- **Accurate playtime**: This mod uses the same playtime that's tracked by Minecraft itself, ensuring accurate data without relying on other methods!
- **Playtime leaderboard**: Create a leaderboard which shows your most active online players!
- **Full Geyser support**: Use Playtime Command with Minecraft Bedrock usernames just like you would with Minecraft Java usernames!
- **Lightweight and simple code**: Uses almost zero resources and is designed to allow you to update your server without needing to update Playtime Command (except some rare occurrences)!
- **Automatic update checker**: Playtime Command automatically checks for updates on startup and will let you know via a message in console if there is one.

# Mod showcase
**Chat message**

![Chat Showcase](https://raw.githubusercontent.com/SpoonySimone/PlaytimeCommand/refs/heads/main/images/chat_showcase.png)

**Leaderboard**

![Leaderboard](https://raw.githubusercontent.com/SpoonySimone/PlaytimeCommand/refs/heads/main/images/leaderboard.png)


# Commands
- `/playtime` - See your own playtime
- `/playtime <username>` - See another online player's playtime
- `/playtime --top <page>` - See online players' playtime leaderboard
- `/playtime --about` - See mod information
- `/playtime --help` - See command usage

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
