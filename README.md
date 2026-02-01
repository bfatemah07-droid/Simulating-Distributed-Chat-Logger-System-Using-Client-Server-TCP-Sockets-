# Distributed Chat Logger System

A Java-based client-server chat system that supports multiple clients, real-time message broadcasting, logging, and administrative commands (/users, /log, /alert) using TCP sockets.

## Features

- Multi-client support with concurrent connections
- Real-time message broadcasting
- Logging of all messages, commands, connections, and disconnections
- Administrative commands: `/users`, `/log`, `/alert`
- Thread-safe and synchronized operations
- CLI-based client interface

## Technical Details

- **Language:** Java 11
- **Communication:** TCP sockets
- **Concurrency:** Multithreading for clients
- **Logging:** `chat_log.txt` with timestamps
- **Execution:** Command-line interface

## Authors

- Fatimah Saleh Baothman 
- Rafal Abdullah Riri 
- Maryam Turki Kabbani 
- Sidrah Faisal Alyamani 


## Usage

### Server
```bash
javac ChatServer.java ClientHandler.java
java ChatServer
