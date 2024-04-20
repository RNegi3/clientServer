
package Projects_Chat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.SimpleDateFormat;

public class ChatServer {
    private int port;
    private ServerSocket server;
    private Map<String, PrintWriter> clientConnection;
    private ExecutorService pool;
    private Queue<String> userOrder;
    private String coordinator;
    private Map<String, Long> activeTime;

    public ChatServer(int port) {
        // Initialize server parameters
        this.port = port;
        this.userOrder = new LinkedList<>();
        this.clientConnection = new HashMap<>();
        this.coordinator = null;
        this.activeTime = new HashMap<>();
    }

    public void startServer() {
        // Start the server and handle client connections
        try {
            server = new ServerSocket(port);
            System.out.println("Server is listening on port " + port);
            pool = Executors.newCachedThreadPool();
            while (true) {
                Socket socket = server.accept();
                // Handle each client in a separate thread
                ConnectionHandler handler = new ConnectionHandler(socket);
                pool.execute(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Create and start the server
        ChatServer server = new ChatServer(1234);
        server.startServer();
    }

    // Inner class to handle client connections
    public class ConnectionHandler implements Runnable {
        public Socket clientSocket;
        public PrintWriter out;
        private BufferedReader in;
        public String username;

        public ConnectionHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                // Read username from client
                this.username = in.readLine();
                // Process the username
                if (validName(username)) {
                    // Send validation message to client
                    out.println("Valid");
                    // Add client to connection map and user queue
                    clientConnection.put(username, out);
                    userOrder.add(username);
                    activeTime.put(username, System.currentTimeMillis());
                    // Assign coordinator if first client, otherwise notify client of coordinator
                    if (userOrder.size() == 1) {
                        coordinator = username;
                        out.println("Welcome, " + username + ". You have successfully joined the Server on port " + port);
                        sendCoordinatorMessage(username);
                    } else {
                        out.println("Welcome, " + username + ". You have successfully joined the Server on port " + port);
                        out.println("The Coordinator for this server is: " + coordinator);
                        broadcast(username + " has joined the chat.", "Server");
                    }
                    System.out.println(username + " has joined the chat.");
                } else {
                    // Send invalid message to client and close connection
                    out.println("Invalid");
                    closeConnection();
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // Listen for messages from client
                while (!clientSocket.isClosed()) {
                    String message = in.readLine();
                    if (message == null) {
                        break;
                    } else if (message.equals("/quit")) {
                        clientExit();
                    } else if (message.startsWith("/users")) {
                        // Process user status request
                        message.trim();
                        String[] parts = message.split(" ", 2);
                        String requestingUser = parts[1];
                        requestStatus(requestingUser);
                    } else {
                        // Process regular message
                        processMessage(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void clientExit() {
            // Handle client exit
            if (isCoordinator(username)) {
                // If coordinator exits, assign new coordinator
                String oldCoordinator = coordinator;
                userOrder.remove(username);
                if (!userOrder.isEmpty()) {
                    coordinator = userOrder.peek();
                    System.out.println("The coordinator " + oldCoordinator + " has left the chat.\n New coordinator is " + coordinator);
                    broadcast("The coordinator " + oldCoordinator + " left the chat. New Coordinator is " + coordinator + ".", "Server");
                    sendCoordinatorMessage(coordinator);
                } else {
                    System.out.println("Previous coordinator " + oldCoordinator + ". Everyone left this server");
                    coordinator = null;
                }
            } else {
                // Broadcast user exit
                broadcast(username + " has left the chat!", "Server");
                userOrder.remove(username);
                System.out.println(username + " has left the chat.");
            }
            clientConnection.remove(username);
            closeConnection();
        }

        public void processMessage(String message) {
            // Process regular message or special commands
            if (message.startsWith("/pm")) {
                // Send private message
                String[] parts = message.split(" ", 3);
                String recipient = parts[1];
                String privateMessage = parts[2];
                sendPrivateMessage(privateMessage, recipient);
            } else if (message.startsWith("/Approved")) {
                // Process user detail request approval
                String[] parts = message.split(" ", 2);
                String requestingUser = parts[1];
                PrintWriter requestingUserOut = clientConnection.get(requestingUser);
                if (requestingUserOut != null) {
                    showUserDetails(requestingUserOut);
                }
            } else if (message.startsWith("/Rejected")) {
                // Process user detail request rejection
                String[] parts = message.split(" ", 2);
                String requestingUser = parts[1];
                PrintWriter requestingUserOut = clientConnection.get(requestingUser);
                if (requestingUserOut != null) {
                    requestingUserOut.println("Request rejected");
                }
            } else {
                // Broadcast regular message
                updateActiveTime(username);
                broadcast(message, username);
            }
        }

        public void sendPrivateMessage(String message, String recipient) {
            // Send private message to recipient
            PrintWriter recipientOut = clientConnection.get(recipient);
            if (recipientOut != null) {
                String timeStampMessage = "[" + getTimeStamp() + "] " + "Private message from " + username + ": " + message;
                recipientOut.println(timeStampMessage);
            } else {
                out.println("User " + recipient + " not found.");
            }
        }

        private void closeConnection() {
            // Close connection with client
            try {
                in.close();
                out.close();
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean isCoordinator(String user) {
            // Check if user is coordinator
            return (user.equals(coordinator));
        }

        public String getTimeStamp() {
            // Get current timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            return dateFormat.format(new Date());
        }

        private void updateActiveTime(String user) {
            // Update user's last active time
            activeTime.put(user, System.currentTimeMillis());
        }

        private void sendCoordinatorMessage(String coordinator) {
            // Notify coordinator of their role
            PrintWriter newCoordinator = clientConnection.get(coordinator);
            if (newCoordinator != null) {
                newCoordinator.println("You are the coordinator!");
            }
        }

        public void broadcast(String message, String sender) {
            // Broadcast message to all clients
            for (PrintWriter userOut : clientConnection.values()) {
                userOut.println("[" + getTimeStamp() + "] " + sender + ": " + message);
            }
        }

        private void showUserDetails(PrintWriter requestingUserOut) {
            // Send user details to requester
            StringBuilder userDetails = new StringBuilder();
            userDetails.append("User Details\n");
            if (coordinator != null) {
                userDetails.append(String.format("Coordinator: %s, Port: %d, ipAddress: %s\n", coordinator, port, "127.0.0.1"));
            }
            for (String user : userOrder) {
                if (user != null) {
                    if (user != null && !user.equals(coordinator) && isActive(user)) {
                        userDetails.append(String.format("(Active) User: %s, Port: %d, ipAddress: %s\n", user, port, "127.0.0.1"));
                    }
                }
            }
            for (String user : userOrder) {
                if (user != null) {
                    if (user != null && !user.equals(coordinator) && !isActive(user)) {
                        userDetails.append(String.format("(Inactive)User: %s, Port: %d, ipAddress: %s\n", user, port, "127.0.0.1"));
                    }
                }
            }
            requestingUserOut.println(userDetails.toString());
        }

        private boolean validName(String username) {
            // Check if username is valid
            String trimUsername = username.trim();
            return !clientConnection.containsKey(trimUsername);
        }

        private void requestStatus(String requestingUser) {
            // Handle user status request
            PrintWriter requestingUserOut = clientConnection.get(requestingUser);
            PrintWriter coordinatorOut = clientConnection.get(coordinator);
            if (requestingUserOut != null) {
                if (requestingUserOut.equals(coordinatorOut)) {
                    showUserDetails(requestingUserOut);
                } else {
                    coordinatorOut.println("/pop " + requestingUser);
                }
            }
        }
    }

    // Get current timestamp
    public String getTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return dateFormat.format(new Date());
    }
}
