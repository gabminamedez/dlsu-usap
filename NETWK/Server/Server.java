package sample;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/***
 * This class represents the server class.
 * Accepts 2 clients that can send and receive chat or files.
 * Implements Runnable to multi-thread server.
 *
 * @author Roxas, Ronell John
 * @author Ang, Charlene
 */
public class Server implements Runnable{

    //Address and port for server
    protected String address;
    protected int port;

    //IOStreams to communicate with clients
    protected ObjectInputStream[] oisReader = new ObjectInputStream[2];
    protected ObjectOutputStream[] oosWriter = new ObjectOutputStream[2];

    //Username of the 2 clients
    protected String[] username = new String[2];

    //Socket for the clients
    protected Socket[] serverEndPoint = new Socket[2];

    //boolean to know if clients disconnected
    protected boolean[] disconnected = new boolean[2];

    protected ServerSocket serverSocket = null;

    //Boolean to check if server is stopped manually
    protected boolean isStopped = false;
    protected Thread thread = null;

    //True if there are updates in the logs for the UI thread to change.
    protected boolean update = false;

    //Arraylist of text Logs
    protected ArrayList<String> arrLogs = new ArrayList<String>();

    public Server(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public void run() {

        synchronized (this) {
            thread = Thread.currentThread();
        }
        openServer();

        //get connections
        try {
            arrLogs.add("Listening at port " + port);
            update = true;
            acceptClient();
        } catch (IOException | ClassNotFoundException e) {
            if (isStopped)
                return;
            throw new RuntimeException("Error accepting client.", e);
        }

        //Listen to clients' chats
        listenClients();
    }


    /***
     * This method listens for incoming clients.
     * This loops twice so 2 users can connect.
     *
     * @throws IOException When there was an error with establishing connection with the client
     * @throws ClassNotFoundException  When Message class is not found
     */
    public void acceptClient() throws IOException, ClassNotFoundException {
        for(int i = 0; i < serverEndPoint.length; i++) {
            //temp socket to listen to clients
            Socket tempserverEndPoint = serverSocket.accept();

            //Passes the Socket with client connected and initializes the respective IO Streams
            serverEndPoint[i] = tempserverEndPoint;
            oisReader[i] = new ObjectInputStream(tempserverEndPoint.getInputStream());
            oosWriter[i] = new ObjectOutputStream(tempserverEndPoint.getOutputStream());

            //Set username of the client
            Message messageInput = (Message) oisReader[i].readObject();
            username[i] = messageInput.getSource();

            //Adds a log when a client connects
            String tempusername = username[i];
            arrLogs.add("Client " + tempusername + " Connected. (Address: " + tempserverEndPoint.getRemoteSocketAddress() + ")");
            update = true;

            //Sends a message to the client
            Message messageOutput = new Message("[SERVER]", "Welcome " + tempusername + "! Waiting for other user to connect.");
            oosWriter[i].writeObject(messageOutput);
            disconnected[i] = false;
        }
        if (serverEndPoint.length == 2) {  //Both users connected
            //Message for client to enable chat text field
            for (int i = 0; i < oosWriter.length; i++) {
                oosWriter[i].writeObject(new Message("[SERVER]", "[CHAT-ENABLE]"));
                oosWriter[i].writeObject(new Message("[SERVER]", "You are now talking to " + username[(i + 1) % 2] + ", Say hi!"));
            }
        }
    }

    /***
     * Used to listen to client updates. This method is threaded in the listenClients method.
     *
     * @param index index of the client listening to.
     * @throws IOException When there is an error with the IO streams between server and client
     * @throws ClassNotFoundException When Message class is not found
     */
    public void listenTo(int index) throws IOException, ClassNotFoundException {
        while (!isStopped) {
            try {
                Message message = (Message) oisReader[index].readObject();

                if (message != null) {
                    if(message.getMessage() != null) {
                        arrLogs.add(message.getSource() + ": " + message.getMessage());
                    }
                    else
                        arrLogs.add(message.getSource() + ": Sent a file [" + message.getFile().getName() + "](" + message.getFile().length() + " bytes)");
                    update = true;

                    oosWriter[(index + 1) % 2].writeObject(message);
                }

                Thread.sleep(1000);
            } catch (IOException | InterruptedException e) {
                disconnected[index] = true;
                Message disconnect = new Message("[SERVER]", "[DISCONNECTED]");
                arrLogs.add(username[index] + " disconnected. Waiting to reconnect.");
                update = true;
                if(!disconnected[(index + 1) % 2])
                    oosWriter[(index + 1) % 2].writeObject(disconnect);
                else stop();


                if(!isStopped)
                    reconnect(index);
                return;
            }

        }
    }

    /***
     * This method initializes the threads to listen to both clients.
     */
    public void listenClients() {
        Runnable listen = new Runnable() {
            @Override
            public void run() {
                try {
                    listenTo(0);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable listen2 = new Runnable() {
            @Override
            public void run() {
                try {
                    listenTo(1);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread cl1 = new Thread(listen);
        Thread cl2 = new Thread(listen2);

        cl1.setDaemon(true);
        cl2.setDaemon(true);
        cl1.start();
        cl2.start();
    }

    /***
     * This method stops the server. It closes the Sockets.
     */
    public synchronized void stop() {
        this.isStopped = true;
        try {
            this.serverSocket.close();
            for(int i = 0; i < serverEndPoint.length; i++)
                serverEndPoint[i].close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }


    /***
     * This threaded method is called when a client disconnects first.
     * Allows a client to reconnect using the same username.
     *
     * @param index index of the disconnected client.
     */
    private void reconnect(int index) {
        Runnable reconnect = new Runnable() {
            @Override
            public void run() {
                //Boolean to check if the right user connected already based on username
                boolean recon = false;
                try {
                    Socket tempserverEndPoint = null;
                    String tempusername = null;

                    //listen for client connected while server is not stopped or user has not reconnected
                    while (!recon && !isStopped) {
                        tempserverEndPoint = serverSocket.accept();

                        serverEndPoint[index] = tempserverEndPoint;
                        oisReader[index] = new ObjectInputStream(tempserverEndPoint.getInputStream());
                        oosWriter[index] = new ObjectOutputStream(tempserverEndPoint.getOutputStream());


                        Message messageInput = (Message) oisReader[index].readObject();
                        tempusername = messageInput.getSource();
                        if(tempusername.equals(username[index]))
                            recon = true;
                    }

                    //If user reconnected
                    if(recon) {
                        disconnected[index] = false;
                        //add to logs user reconnected
                        arrLogs.add("Client " + tempusername + " reconnected. (Address: " + tempserverEndPoint.getRemoteSocketAddress() + ")");
                        update = true;

                        //Tells partner client about the reconnection
                        oosWriter[(index + 1)%2].writeObject(new Message("[SERVER]",  username[index] + " reconnected."));

                        //Message to reconnected client
                        oosWriter[index].writeObject(new Message("[SERVER]", "You are now talking to " + username[(index + 1) % 2] + ", Welcome back!"));

                        //re-enables chat for both clients
                        for (int i = 0; i < oosWriter.length; i++) {
                            oosWriter[i].writeObject(new Message("[SERVER]", "[CHAT-ENABLE]"));
                        }

                        //listen to reconnected client
                        listenTo(index);
                    }

                }
                catch (IOException | ClassNotFoundException e) {
                    return;
                }
            }
        };

        Thread reconThread = new Thread(reconnect);
        reconThread.setDaemon(true);
        reconThread.start();


    }

    /***
     * Method used to open the server
     */
    private void openServer() {
        try {
            serverSocket = new ServerSocket(port);
            arrLogs.add("Server started listening at port " + port);
        }
        catch (IOException e) {
            Alert err = new Alert(Alert.AlertType.ERROR, "Unable to start Server at port " + port, ButtonType.OK);
            err.show();
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /***
     * gets number of connected clients based on disconnected[] and serverEndPoint[].
     * @return int number of connected clients.
     */
    public int getConnections() {
        int open = 0;
        for (int i = 0; i < disconnected.length; i++) {
            if(!disconnected[i] && serverEndPoint[i] != null)
                open++;
        }
        return open;
    }


    public boolean getisStopped() {
        return  isStopped;
    }

    public boolean getUpdate() {
        return update;
    }

    public void isUpdated() {
        update = false;
    }

    public ArrayList<String> getArrLogs() {
        return arrLogs;
    }
}
