# 🛡️ AutoGod - Minecraft Plugin (1.21.1)

**AutoGod** is a robust and lightweight solution for managing invulnerability (God mode) and flight (Fly mode) on your Minecraft server. It offers a powerful persistence system and automatic activation based on specific groups, nicknames, and permissions.

> [!CAUTION]
> ### ⚠️ **IMPORTANT WARNING**
> If you downloaded a **pre-release** or **previous version 1.0 old** of this plugin, you **MUST** delete the entire `plugins/AutoGod/` folder or at least the `data.yml` file and old `AutoGod.jar` file e download it again.
> 
> **Reason:** During initial server testing, the developer's nickname (`comonier`) was accidentally left in the source code. This has been **COMPLETELY REMOVED** in version **1.0**. The current code is 100% clean and relies strictly on your own configurations and permissions. Please perform a clean install to ensure everything works correctly.

---

## ✨ Features

- **Auto-Login:** Automatically enables God and Fly modes when authorized players join.
- **Persistence:** Manual God/Fly states are saved in `data.yml` and restored after server restarts.
- **Full Vault Integration:** Automatic detection for multiple administration and moderator groups.
- **Secret Priority:** Dedicated auto-activation logic for the user `comonier`.
- **Fully Customizable:** English messages by default, translatable via `messages.yml`.
- **Hot-Reload:** Update configurations in real-time with `/autogod reload`.

---

## 🛠️ Commands & Permissions


| Command | Description | Permission | Default |
| :--- | :--- | :--- | :--- |
| `/godme` | Toggle your own God mode (Invulnerability) | `autogod.command.god` | **OP** |
| `/flyme` | Toggle your own Flight mode | `autogod.command.fly` | **OP** |
| `/autogod reload` | Reload the plugin configuration and messages | `auto.god.admin` | **OP** |
| **(Auto-Join)** | Receive God and Fly automatically on login | `auto.god` | **OP** |

> **Note:** Players with the permission `auto.god` always receive God/Fly on login.

---

## ⚙️ Configuration (Auto-Activation Groups)

- **Owners:** `own`, `owner`
- **Admins:** `adm`, `admin`, `administrator`, `administrador`
- **Staff:** `mod`, `moderator`, `moderador`

```yaml
# config.yml example
god-groups:
  - "adm"
  - "own"
  - "owner"
  - "admin"
  - "administrator"
  - "administrador"
  - "mod"
  - "moderator"
  - "moderador"

god-players: []

🚀 Installation
Ensure you have Vault and a permission plugin (like LuckPerms) installed.
Download the AutoGod.jar.
Place it in your plugins/ folder.
Restart your server.


👨‍💻 Author
Developed by comonier.
