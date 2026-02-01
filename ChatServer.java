package group4_rtm_fall2025;

/**
  CPCS371 Project — Group 4
  Team:
     Fatimah Saleh Baothman (2307298)
     Rafal Abdullah Riri (2308220)
     Sidrah Faisal Alyamani (2311603)
     Maryam Turki Kabbani (2306217)
*/
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * ChatServer
 * 
 * A multi-client chat server implemented using TCP sockets.
 * 
 * Features:
 *  - Accepts multiple clients using threads.
 *  - Broadcasts normal chat messages to all connected clients.
 *  - Supports commands:
 *        /users  → list connected clients
 *        /log    → number of entries stored in the log file
 *        /alert  → broadcast alert message to all clients
 *  - Logs every event (messages, connections, commands) into chat_log.txt
 */
public class ChatServer {

    private static final int PORT = 5000;
    private static final String LOG_FILE_NAME = "chat_log.txt";

    // List that stores all active client handlers
    private static final ArrayList<ClientHandler> clients = new ArrayList<>();

    /**
     * ClientHandler:
     * A runnable class that manages communication with one connected client.
     * Each client will be handled in a separate thread.
     */
    private static class ClientHandler implements Runnable {

        private final Socket socket;
        private final BufferedReader reader;
        private final PrintWriter writer;
        private final String clientName;

        public ClientHandler(Socket socket, int clientNumber) throws IOException {
            this.socket = socket;

            // Input stream: data coming from client
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Output stream: sending data to client
            this.writer = new PrintWriter(socket.getOutputStream(), true);

            // Assign default name to each client (Client-1, Client-2, …)
            this.clientName = "Client-" + clientNumber;

            // Send welcome message to the client
            sendMessage("Welcome " + clientName + "! You are connected to the chat server.");

            // Log connection event
            log("Client connected: " + socket.getInetAddress() + " (User: " + clientName + ")");

            // Notify other clients
            broadcast("SERVER: " + clientName + " has joined the chat.");
        }

        public String getClientName() {
            return clientName;
        }

        /**
         * Sends a message only to this specific client.
         */
        public void sendMessage(String msg) {
            writer.println(msg);
        }

        /**
         * Main loop for each client:
         * - Reads messages from the client
         * - Distinguishes between commands and normal chat messages
         */
        @Override
        public void run() {
            try {
                String line;

                while ((line = reader.readLine()) != null) {

                    // Commands always start with "/"
                    if (line.startsWith("/")) {
                        handleCommand(line);

                    } else {
                        // Normal chat message
                        handleNormalMessage(line);
                    }
                }

            } catch (IOException e) {
                System.err.println("Connection lost with " + clientName);

            } finally {

                // Remove from active clients list
                removeClient(this);

                // Notify all clients about the disconnection
                broadcast("SERVER: " + clientName + " has left the chat.");

                // Log this event
                log("Client disconnected: " + socket.getInetAddress() + " (User: " + clientName + ")");

                closeResources();
            }
        }

        /**
         * Handles a normal chat message.
         * This message is broadcasted to all clients and logged.
         */
        private void handleNormalMessage(String message) {

            String formatted = clientName + ": " + message;

            // Send message to all connected clients
            broadcast(formatted);

            // Log the chat activity
            log("Message received from " + socket.getInetAddress() + " (" + clientName + "): " + message);

        }

        /**
         * Handles supported commands:
         * /users, /log, /alert <msg>
         */
        private void handleCommand(String cmd) {

            if (cmd.equals("/users")) {

                log("Command /users requested by "  + socket.getInetAddress() + " (" + clientName + ")");

                sendMessage("Connected users: " + getUsersList());


            } else if (cmd.equals("/log")) {

                log("Command /log requested by " + socket.getInetAddress() + " (" + clientName + ")");


                // Count number of log entries stored in the log file
                int count = getLogLineCount();

                sendMessage("Total messages logged: " + count);


            } else if (cmd.startsWith("/alert ")) {

                String msg = cmd.substring(7).trim();

                if (msg.isEmpty()) {
                    sendMessage("SERVER: Usage: /alert <message>");
                    return;
                }

                // Send alert message to all clients
                broadcast("ALERT: " + msg);

                log("Command /alert requested by " + socket.getInetAddress() + " (" + clientName + ")");

               log("Alert broadcasted: '" + msg + "' by "+ socket.getInetAddress() + " (" + clientName + ")");



            } else {
                sendMessage("SERVER: Unknown command.");
            }
        }

        /**
         * Safely closes all network resources after disconnect.
         */
        private void closeResources() {
            try { reader.close(); } catch (IOException ignored) {}
            writer.close();
            try { socket.close(); } catch (IOException ignored) {}
        }
    }


    // -------------------- SERVER UTILITIES --------------------

    /**
     * Writes a log entry into chat_log.txt and prints it on server console.
     * Each event (commands, connections, messages, alerts) is recorded.
     */
    private static synchronized void log(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = "[" + sdf.format(new Date()) + "] ";
        String logLine = timestamp + message;

        System.out.println(logLine);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE_NAME, true))) {
            bw.write(logLine);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to log file.");
        }
    }

    /**
     * Adds a client to the active clients list.
     */
    private static synchronized void addClient(ClientHandler c) {
        clients.add(c);
    }

    /**
     * Removes a client from the active clients list.
     */
    private static synchronized void removeClient(ClientHandler c) {
        clients.remove(c);
    }

    /**
     * Sends a message to all connected clients.
     */
    private static synchronized void broadcast(String msg) {
        for (ClientHandler c : clients) {
            c.sendMessage(msg);
        }
    }

    /**
     * Returns a comma-separated list of connected clients.
     */
    private static synchronized String getUsersList() {
        if (clients.isEmpty()) return "No connected users.";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clients.size(); i++) {
            sb.append(clients.get(i).getClientName());
            if (i < clients.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    /**
     * Counts the number of log entries (lines) stored in chat_log.txt.
     * 
     * This ensures that the /log command returns the exact number
     * of recorded events, matching the project requirement.
     */
    private static synchronized int getLogLineCount() {
        int count = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(LOG_FILE_NAME))) {
            while (br.readLine() != null) {
                count++;
            }
        } catch (IOException e) {
            System.err.println("Error reading log file.");
        }

        return count;
    }

    /**
     * Starts the server and waits for incoming client connections.
     */
    public static void main(String[] args) {
        System.out.println("Chat server starting on port " + PORT + "...");

        int clientCounter = 1;

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            log("Server started and listening on port " + PORT);

            while (true) {

                // Accept a new client
                Socket clientSocket = serverSocket.accept();

                System.out.println("New client connected: " + clientSocket.getInetAddress());

                try {
                    // Assign a new handler and start its thread
                    ClientHandler handler = new ClientHandler(clientSocket, clientCounter++);
                    addClient(handler);
                    new Thread(handler).start();

                } catch (IOException e) {
                    log("Error creating client handler: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            log("Server error: " + e.getMessage());
        }
    }
}
