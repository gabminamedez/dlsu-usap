package sample;


import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

/***
 * This class represents the client.
 *
 * @author Roxas, Ronell John
 * @author Ang, Charlene
 */
public class Client {

    //Address and port of the server to connect to
    private String serverAddress;
    private int serverPort;

    //Username of this client
    private String username;

    //Socket for this client
    private Socket clientEndPoint = null;

    //boolean containing value if user is disconnected
    private boolean disconnect = false;

    //Boolean to check if chat is enabled
    private boolean chatEnable = false;

    //latest update or chat from the server or the client. Used by the UI Controller to listen to UI updates.
    private Message update = null;

    //IO Streams
    protected ObjectInputStream oisReader;
    protected ObjectOutputStream oosWriter;


    /***
     * Client constructor.
     *
     * @param serverAddress address of the server.
     * @param serverPort port of the server.
     * @param username name of this client.
     */
    public Client(String serverAddress, int serverPort, String username) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.username = username;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getUsername() {
        return username;
    }

    public boolean isDisconnect() {
        return disconnect;
    }

    public boolean getChatEnable() {
        return chatEnable;
    }

    /***
     * Connects to the server.
     * @throws IOException If failed to connect to server.
     */
    public void joinServer() throws IOException {
        try {
            clientEndPoint = new Socket(serverAddress, serverPort);                 //Connect to server
            oosWriter = new ObjectOutputStream(clientEndPoint.getOutputStream());   //Output stream to the server
            oisReader = new ObjectInputStream(clientEndPoint.getInputStream());     //Input stream of the server
            oosWriter.writeObject(new Message(username, "CONNECT"));        //tell server this client connected

            update = (Message) oisReader.readObject();                              //send update message to UI

            //listen to server updates
            listenServerUpdate();
        }
        catch (IOException | ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    /***
     * Thread that listens to server updates.
     */
    public void listenServerUpdate() {
        Runnable listen = new Runnable() {
            @Override
            public void run() {
                Message message = null;

                while(!disconnect)
                    try {
                        message = (Message) oisReader.readObject();

                        //enables chat
                        if(message.getSource().equals("[SERVER]") && message.getMessage().equals("[CHAT-ENABLE]")) {
                            chatEnable = true;
                            continue;
                        }

                        //disables chat and informs that other client disconnected
                        if(message.getSource().equals("[SERVER]") && message.getMessage().equals("[DISCONNECTED]")) {
                            chatEnable = false;
                            update = new Message("[SERVER]", "Other user disconnected. Waiting to reconnect...");
                        }
                        else
                            //Send message to UI
                            update = message;

                        Thread.sleep(1000);
                    } catch (IOException | InterruptedException | ClassNotFoundException e) {
                        return;
                    }
            }
        };

        Thread serverListener = new Thread(listen);
        serverListener.setDaemon(true);
        serverListener.start();
    }

    /***
     * Sends chat from text field to server.
     * @param chat message or chat from text field.
     */
    public void sendChat(Message chat) {
        try {
            oosWriter.writeObject(chat);
        }
        catch (IOException e) {
            Alert err = new Alert(Alert.AlertType.ERROR, "Failed to send message. Please try again.", ButtonType.OK);
            err.show();
        }

    }

    /***
     * Sends file to server to forward to client.
     * @param file File to be sent to other client.
     */
    public void sendFile(File file) {
        try {
            oosWriter.writeObject(new Message(username, file));
        }
        catch (IOException e) {
            Alert err = new Alert(Alert.AlertType.ERROR, "Failed to send file. Please try again.", ButtonType.OK);
            err.show();
        }
    }

    /***
     * Disconnect from server.
     * @throws IOException if error on closing Socket
     */
    public void disconnect() throws IOException{
        disconnect = true;
        clientEndPoint.close();
    }


    public Message getUpdate() {
        return update;
    }

    /***
     * Set update to null. Used when UI is updated.
     */
    public void updated() {
        update = null;
    }
}
