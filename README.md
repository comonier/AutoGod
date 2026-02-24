# 🛡️ AutoGod - Minecraft Plugin (1.21.1)

**AutoGod** is a robust and lightweight solution for managing invulnerability (God mode) and flight (Fly mode) on your Minecraft server. It offers a powerful persistence system and automatic activation based on specific groups, nicknames, and permissions.

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


| Command | Description | Permission |
| :--- | :--- | :--- |
| `/godme` | Toggle your own God mode. | `autogod.command.god` |
| `/flyme` | Toggle your own Fly mode. | `autogod.command.fly` |
| `/autogod reload` | Reload plugin configuration. | `auto.god.admin` |

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

god-players:
  - "comonier"
