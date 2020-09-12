import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static java.lang.System.out;

public class Client extends JFrame implements ActionListener{

    private static final long serialVersionUID = 3156748499018424211L;
    String username;
    PrintWriter pw;
    BufferedReader br;
    JTextArea  taMessages;
    JTextField tfInput;
    JButton btnSendMessage, btnSendFile, btnLogout;
    Socket client;
    JFileChooser fc = new JFileChooser();

    public Client(String username, String ip, int port) throws Exception{
        super(username + " - De La Salle Usap");
        this.username = username;
        client = new Socket(ip, port);
        br = new BufferedReader(new InputStreamReader(client.getInputStream()));
        pw = new PrintWriter(client.getOutputStream(), true);
        pw.println(username); // send name to server
        buildInterface();
        new MessagesThread().start(); // create thread for listening for messages
        pw.println("[1] " + username + " has joined the chat!");
    }

    public void buildInterface() {
        taMessages = new JTextArea();
        taMessages.setRows(20);
        taMessages.setColumns(50);
        taMessages.setEditable(false);
        JScrollPane sp = new JScrollPane(taMessages, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(sp, "Center");

        tfInput = new JTextField(50);
        btnSendMessage = new JButton("Send");
        btnSendFile = new JButton("Send File");
        btnLogout = new JButton("Logout");

        JPanel bp = new JPanel(new FlowLayout());
        bp.add(tfInput);
        bp.add(btnSendMessage);
        bp.add(btnSendFile);
        bp.add(btnLogout);
        add(bp, "South");

        btnSendMessage.addActionListener(this);
        btnSendFile.addActionListener(this);
        btnLogout.addActionListener(this);

        setSize(500, 500);
        setVisible(true);
        pack();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSendMessage) {
            pw.println("[3] " + tfInput.getText());
        } else if (e.getSource() == btnSendFile) {
            int returnVal = fc.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                pw.println("[4] Sent the file " + file.getName() + ".");
            }
        } else if (e.getSource() == btnLogout) {
            pw.println("[2] " + username + " has left the chat.");
            String onExit = JOptionPane.showInputDialog(null,
                    "Would you like to save the chat logs? (Enter 'YES' to save)", "", JOptionPane.PLAIN_MESSAGE);
            if (onExit.equals("YES")) {
                // save chat logs
            }
            System.exit(0);
        }
    }

    public static void main(String... args) throws Exception {
        JTextField ip = new JTextField();
        JTextField port = new JTextField();
        JTextField username = new JTextField();

        Object[] message = {
            "Server IP Address:", ip,
            "Server Port Number:", port,
            "Username:", username
        };

        while(true){
            int option = JOptionPane.showConfirmDialog(null, message, "Welcome to De La Salle Usap!", JOptionPane.OK_CANCEL_OPTION);
            if(option == JOptionPane.OK_OPTION){
                if(ip.getText().equals("localhost") && port.getText().equals("8080") && !username.getText().isEmpty()){
                    out.println("Login successful!");
                    int portNum = Integer.parseInt(port.getText());
                    new Client(username.getText(), ip.getText(), portNum);
                    break;
                }
                else{
                    out.println("login failed!");
                    continue;
                }
            }
            else{
                out.println("Login cancelled!");
                break;
            }
        }
    }

    class MessagesThread extends Thread{
        public void run(){
            String line;
            try{
                while(true){
                    line = br.readLine();
                    taMessages.append(line + "\n");
                }
            } catch(Exception ex){}
        }
    }
}