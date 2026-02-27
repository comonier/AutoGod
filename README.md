# 🛡️ AutoGod - Minecraft Plugin (1.21+)
**Robust and lightweight solution for God and Fly modes with persistence and multi-language support.**

Tested on **Paper/Spigot 1.21.1**.

---

## ✨ Features

- **Auto-Login:** Automatically enables God and Fly modes when authorized players join.
- **Independent Logic:** Separate permissions for God and Fly auto-activation.
- **Persistence:** Manual states are saved in `data.yml` and restored after server restarts.
- **Fall Protection:** Forced flight on join to prevent fall damage during login.
- **Vault Integration:** Automatic detection for administrative and moderator groups.
- **Multi-Language:** Supports **English (en)** and **Portuguese (pt)** via `config.yml`.
- **Hot-Reload:** Update configurations in real-time with `/autogod reload`.

---

## 🛠️ Commands & Permissions


| Command | Description | Permission | Default |
| :--- | :--- | :--- | :--- |
| `/godme` | Toggle your own God mode | `autogod.command.god` | **OP** |
| `/flyme` | Toggle your own Flight mode | `autogod.command.fly` | **OP** |
| `/autogod reload` | Reload configuration and messages | `auto.god.admin` | **OP** |
| **(Auto-God)** | Receive God mode automatically on login | `auto.god.login` | **OP** |
| **(Auto-Fly)** | Receive Fly mode automatically on login | `auto.fly.login` | **OP** |

---

## ⚙️ Configuration

### Language Support
Change the `language` key in `config.yml` to switch between English and Portuguese:
- `language: en` (Default)
- `language: pt` (Portuguese)

### Auto-Activation Groups
By default, the plugin recognizes these groups:
- Owners: `own`, `owner`
- Admins: `adm`, `admin`, `administrator`, `administrador`
- Staff: `mod`, `moderator`, `moderador`

---

## 🚀 Installation

1. Ensure you have Vault and a permission plugin (like [LuckPerms](https://luckperms.net)) installed.
2. Download the `AutoGod.jar`.
3. Place it in your `plugins/` folder.
4. Restart your server.

---

## 👨‍💻 Author
Developed by **comonier**.
