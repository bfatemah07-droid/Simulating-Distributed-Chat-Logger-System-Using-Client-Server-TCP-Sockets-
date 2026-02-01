
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
import java.net.*;
import java.util.Scanner;

public class ChatClient {

    private String serverIP;
    private int serverPort;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Scanner scanner;

    public ChatClient(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public void start() {
        try {
            // Connect to the server (on the same machine)
            socket = new Socket(serverIP, serverPort);
            System.out.println("Connected to server.");

            // Input from server
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Output to server
            writer = new PrintWriter(socket.getOutputStream(), true);

            // User input
            scanner = new Scanner(System.in);

            // Thread that listens for messages from the server
            new Thread(() -> {
                try {
                    String response;
                    while ((response = reader.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            }).start();

            // Main loop: sending messages to the server
            while (true) {
                String message = scanner.nextLine();
                writer.println(message);

                if (message.equalsIgnoreCase("/exit")) {
                    break;
                }
            }

            socket.close();
        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("127.0.0.1", 5000);
        client.start();
    }
}

