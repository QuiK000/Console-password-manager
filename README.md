# Password Manager

A secure, terminal-based password manager written in Java. All vault data is encrypted locally — nothing ever leaves your machine.

---

## Features

- **AES-256-GCM encryption** for all stored data
- **PBKDF2WithHmacSHA256** key derivation (600,000 iterations) from your master password
- **TOTP / 2FA support** — stores and generates time-based one-time passwords
- **Password generator** — cryptographically random, configurable length and character sets
- **Password history** — keeps the last 2 previous passwords per entry
- **Auto-lock** — vault locks automatically after a configurable period of inactivity
- **Clipboard integration** — copies passwords to clipboard and clears them after 15 seconds
- **Encrypted backups** — create and restore full vault backups
- **Master password change** — re-encrypts the entire vault with a new key
- **Atomic saves** — writes to a temp file first, then atomically replaces the vault file

---

## Requirements

- Java 22+
- Maven 3.8+

---

## Getting Started

### Build

```bash
mvn clean package
```

### Run

```bash
java -jar target/PasswordManager-1.0-SNAPSHOT.jar
```

On first launch you will be prompted to create a master password. On subsequent runs, enter it to unlock the vault.

---

## Commands

| Command | Description |
|---|---|
| `add` | Add a new password entry |
| `list [filter]` | List all entries, optionally filtered by site / login / notes |
| `search <query>` | Search entries by site, login, or notes |
| `get <id>` | Show details of an entry and copy password to clipboard |
| `update <id>` | Update an existing entry |
| `delete <id>` | Delete an entry |
| `lock` | Lock the vault and return to the login prompt |
| `timeout [value]` | Show or set the auto-lock timeout (e.g. `30s`, `5m`, `1h`) |
| `change-master` | Change the master password and re-encrypt the vault |
| `backup [path]` | Create an encrypted backup (default: `backups/vault-<timestamp>.bak`) |
| `restore <path>` | Restore a vault from an encrypted backup |
| `clear` | Clear the terminal screen |
| `help` | Show the help message |
| `exit` | Exit the program |

---

## How It Works

### Encryption

The vault file (`vault.json`) is never stored in plaintext. On every save:

1. The vault is serialized to JSON.
2. JSON bytes are encrypted with AES-256-GCM using a random 12-byte IV.
3. The 16-byte salt is prepended to the ciphertext.
4. The result is written atomically to disk.

The encryption key is never stored — it is derived fresh from your master password and the stored salt each time you log in.

### Key Derivation

```
key = PBKDF2WithHmacSHA256(masterPassword, salt, iterations=600_000, keyLength=256)
```

### Vault File Layout

```
[ 16 bytes: salt ][ 12 bytes: GCM IV ][ N bytes: AES-GCM ciphertext + 16-byte auth tag ]
```

---

## Project Structure

```
src/main/java/
├── Main.java                        # Entry point
├── cli/
│   ├── CommandHandler.java          # Command dispatch and logic
│   ├── ConsoleUI.java               # REPL loop and auto-lock timer
│   ├── SessionSettings.java         # Auto-lock timeout settings
│   └── commands/                    # Individual command classes
├── crypto/
│   ├── ICryptoService.java
│   └── impl/CryptoServiceImpl.java  # AES-GCM + PBKDF2 implementation
├── model/
│   ├── Entry.java                   # Password entry with history
│   └── Vault.java                   # Entry collection
├── service/
│   ├── IAuthService.java
│   ├── IVaultService.java
│   └── impl/
│       ├── AuthServiceImpl.java     # First-run detection, login
│       └── VaultServiceImpl.java    # Core vault operations
├── storage/
│   ├── IStorageService.java
│   ├── IVaultSerializer.java
│   └── impl/
│       ├── FileStorage.java         # Atomic file I/O
│       └── JsonVaultSerializer.java # Jackson-based serialization
└── util/
    ├── ConsoleUtils.java            # Input, clipboard, screen
    ├── PasswordGenerator.java       # Secure random password generator
    ├── TableUtils.java              # ASCII table rendering
    └── TotpUtils.java               # TOTP code generation
```

---

## Dependencies

| Library | Purpose |
|---|---|
| `jackson-databind` + `jackson-datatype-jsr310` | JSON serialization / deserialization |
| `googleauth` | TOTP / 2FA code generation |
| `asciitable` | Terminal table rendering |
| `lombok` | Boilerplate reduction (`@Builder`, `@Getter`, etc.) |

---

## Security Notes

- Passwords are stored as `char[]` and zeroed from memory after use.
- The master password is never stored or logged anywhere.
- Auto-lock wipes the decrypted vault and key from memory.
- The GCM authentication tag ensures any tampering with the ciphertext is detected on decryption.
- Clipboard contents are cleared after 15 seconds.

---

## License

MIT
