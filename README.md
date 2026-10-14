# LintVision

**Your code. Your inspections. Right where you need them.**

LintVision displays errors, warnings, and other inspection messages inline at the end of each line in IntelliJ IDEA, inspired by the VS Code Error Lens extension.
![LintVision running in IntelliJ IDEA](.github/assets/hero.png)
[![JetBrains Marketplace](https://img.shields.io/jetbrains/plugin/v/35068-lintvision?style=for-the-badge&label=Marketplace)](https://plugins.jetbrains.com/plugin/35068-lintvision)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/35068-lintvision?style=for-the-badge)](https://plugins.jetbrains.com/plugin/35068-lintvision)
[![License](https://img.shields.io/github/license/Gurwi30/lint-vision?style=for-the-badge)](LICENSE)

## Overview

LintVision brings IntelliJ's inspection results directly into your editor, so you can spot issues without constantly checking the Problems panel or hovering over highlighted code.

### Features

- **Inline diagnostics** — See inspection messages at the end of the affected line.
- **Severity filtering** — Choose which errors, warnings, weak warnings, and informational messages appear.
- **Custom appearance** — Configure severity colors, backgrounds, transparency, spacing, border radius, and text styling.
- **Message templates** — Customize how inline messages are displayed using template variables.
- **Message controls** — Limit message length, show additional issue counts, and configure when those counts appear.
- **Multiple inspections** — Display messages from different inspection sources.
- **Configurable behavior** — Adjust display delays and exclude messages using regular expressions.

## Installation

1. Open **Settings | Plugins | Marketplace** in your JetBrains IDE.
2. Search for **LintVision**.
3. Click **Install**.
4. Restart the IDE if prompted.

You can also install it from the [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/35068-lintvision).

## Configuration

Open **Settings | Tools | LintVision** to configure the plugin.

| Setting                | Description                                                    |
|------------------------|----------------------------------------------------------------|
| Enabled                | Enable or disable inline inspection messages.                  |
| Severity filters       | Choose which inspection severities to display.                 |
| Severity colors        | Customize colors for individual severity levels.               |
| Background and alpha   | Configure the message background and its transparency.         |
| Border radius and gap  | Adjust message appearance and spacing.                         |
| Italic and bold        | Customize message text styling.                                |
| Message template       | Define the format used to display messages.                    |
| Severity labels        | Customize labels associated with each severity.                |
| Maximum length         | Limit the length of displayed messages.                        |
| Extra count            | Show how many additional messages affect the same line.        |
| Minimum extra messages | Set the threshold for displaying the additional message count. |
| Regex exclusion        | Hide messages matching a regular expression.                   |
| Delay                  | Configure the delay before messages appear.                    |

### Message templates

Use these variables to customize the inline message format:

| Variable    | Description                                      |
|-------------|--------------------------------------------------|
| `$message`  | The inspection message.                          |
| `$count`    | The number of messages associated with the line. |
| `$severity` | The inspection severity.                         |
| `$source`   | The inspection source.                           |
| `$code`     | The inspection code, when available.             |

## Compatibility

LintVision targets IntelliJ Platform build **243 and newer**, corresponding to IntelliJ IDEA 2024.3 and later. Compatibility with specific IDEs and future platform releases may vary.

Check the [JetBrains Marketplace listing](https://plugins.jetbrains.com/plugin/35068-lintvision) for the latest compatibility information.

## Contributing

Contributions, bug reports, and feature suggestions are welcome!

- [Report a bug or request a feature](https://github.com/Gurwi30/lint-vision/issues)
- [Browse the source code](https://github.com/Gurwi30/lint-vision)

## License

LintVision is distributed under the [MIT License](LICENSE).

---

<p>Made with care by <a href="https://github.com/Gurwi30">Gurwi30</a>.</p>