package Projects_Chat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
 
public class ClientGUI {
    private JFrame loginFrame;
    private JTextField nameField;
    private JTextField portField;
    private JButton loginButton;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
    public static void main(String[] args) {
        // Launch the GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // Initialize the login window
            new ClientGUI().initializeLogin();
        });
    }
    
    // Set up the login window
    private void initializeLogin() {
        loginFrame = new JFrame("Login");
        loginFrame.setSize(500, 230);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Create the login panel
        JPanel loginPanel = new JPanel();    
        loginFrame.add(loginPanel);
        modifyFrame(loginPanel); // Add components to the login panel
        loginFrame.setVisible(true);
    }
    
    // Customize the login frame
    private void modifyFrame(JPanel panel) {
        panel.setLayout(null);
        
        // Username label and text field
        JLabel nameLabel = new JLabel("Username: ");
        nameLabel.setBounds(50, 40, 80, 25);
        panel.add(nameLabel);
        nameField = new JTextField(20);
        nameField.setBounds(150, 40, 265, 25);
        panel.add(nameField);
        
        // Port number label and text field
        JLabel portLabel = new JLabel("Port Number: ");
        portLabel.setBounds(50, 70, 80, 25);
        panel.add(portLabel);
        portField = new JTextField(20);
        portField.setBounds(150, 70, 265, 25);
        panel.add(portField);
        
        // Login button
        loginButton = new JButton("Login");
        loginButton.setBounds(230, 100, 80, 25);
        panel.add(loginButton);
        loginFrame.getRootPane().setDefaultButton(loginButton); // Enable pressing Enter to trigger login
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Handle login button click
                String username = nameField.getText();
                String portStr = portField.getText();
                
                // Validate username and port
                username.trim();
                if (username.isEmpty() && portStr.isEmpty()) {
                    JOptionPane.showMessageDialog(loginFrame, "Please enter username and port number.");
                    return;
                }
                
                // Check for invalid username (containing spaces or starting with '/')
                if (username.contains(" ")) {
                    JOptionPane.showMessageDialog(loginFrame, "Don't add spaces in the username!");
                    return;
                } else if (username.startsWith("/")) {
                    JOptionPane.showMessageDialog(loginFrame, "Username cannot start with '/'");
                    return;
                }
                
                int port;
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(loginFrame, "Port number must be an integer.");
                    return;
                }
                
                try {
                    // Establish connection to the server
                    socket = new Socket("127.0.0.1", port);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out.println(username); // Send username to server
                    
                    // Receive response from server
                    String response = in.readLine();
                    if (response.equals("Valid")) {
                        loginFrame.dispose(); // Close the login window
                        initializeChatGUI(); // Initialize the chat GUI
                    } else {
                        JOptionPane.showMessageDialog(loginFrame, "Username already taken. Enter a different username");
                    }
                    
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(loginFrame, "Error connecting to the server.");
                    ex.printStackTrace();
                }
            }
        });
    }
    
    // Set up the chat GUI
    private void initializeChatGUI() {
        JFrame frame = new JFrame("Client");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        JTextField userInputField = new JTextField();
        
        // Create buttons for sending messages, showing users, and quitting
        JButton sendButton = new JButton("Send");
        JButton detailButton = new JButton("Show Users");
        JButton quitButton = new JButton("Quit");
        
        // Enable pressing Enter to send messages
        frame.getRootPane().setDefaultButton(sendButton);
        
        // Create panels to organize components
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(userInputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(detailButton, BorderLayout.NORTH);
        buttonPanel.add(quitButton, BorderLayout.SOUTH);
        
        // Add components to the frame
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);
        frame.add(buttonPanel, BorderLayout.EAST);
        frame.setVisible(true); // Display the frame
        
        // Action listeners for buttons
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Send message when "Send" button is clicked
                sendMessage(userInputField.getText());
                userInputField.setText(""); // Clear the input field
            }
        });
        
        detailButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Show user details when "Show Users" button is clicked
                String requestingUser = JOptionPane.showInputDialog("Enter your username for confirmation: ");
                requestingUser.trim();
                if (requestingUser != null && !requestingUser.isEmpty()) {
                    userDetail(requestingUser);
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid username");
                }
            }
        });
        
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Quit the server when "Quit" button is clicked
                quitServer();
                frame.dispose(); // Close the frame
            }
        });
        
        // Listen for messages from the server
       
        new Thread(() -> {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String serverMessage;
                
                // Listen for messages from the server
                while ((serverMessage = in.readLine()) != null) {
                    serverMessage.trim();
                    
                    if (serverMessage.startsWith("/pop ")) {
                        // Handle user detail requests
                        String[] parts = serverMessage.split(" ", 2);
                        String requestingUser = parts[1];
                        
                        // Display a prompt to approve user detail requests
                        int option = JOptionPane.showConfirmDialog(null, "\nApprove this request from " + requestingUser, "User Detail Request", JOptionPane.YES_NO_OPTION);
                        if (option == JOptionPane.YES_OPTION) {
                            out.println("/Approved " + requestingUser); // Send approval to the server
                        }
                    } else {
                        // Display chat messages in the text area
                        chatArea.append(serverMessage + "\n");
                        chatArea.setCaretPosition(chatArea.getDocument().getLength()); // Scroll to the bottom
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start(); // Start the thread
    }
    
    // Send a message to the server
    private void sendMessage(String message) {
        out.println(message);
    }
    
    // Send a request for user details to the server
    private void userDetail(String requestingUser) {
        out.println("/users " + requestingUser);
    }
    
    // Send a /quit message to the server to disconnect
    private void quitServer() {
        out.println("/quit");
    }
}
