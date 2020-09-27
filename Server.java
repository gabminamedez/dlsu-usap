import java.io.*;
import java.util.*;
import java.net.*;
import java.text.SimpleDateFormat;
import static java.lang.System.out;

public class Server{

    Vector<String> users = new Vector<String>();
    Vector<HandleClient> clients = new Vector<HandleClient>();
    Vector<Socket> sockets = new Vector<Socket>();

    private static File chatlogs = new File("chatlogs.txt");
    private static int logsCount = 0;

    public void process() throws Exception{
        ServerSocket server = new ServerSocket(8080, 2);
        out.println("Server started!");
        out.println("IP Address: localhost");
        out.println("Port: 8080");

        while(true){
            Socket client = server.accept();
            sockets.add(client);
            HandleClient c = new HandleClient(client);
            clients.add(c);
        }
    }

    public static void main(String ... args) throws Exception{
        new Server().process();
        chatlogs.createNewFile();
    }

    public void broadcast(String user, String message) throws IOException{
        for(HandleClient c: clients){
            if(!c.getUsername().equals(user)){
                c.sendMessage(user, message);
            }
            else{
                c.sendMessage(user, message);
            }
        }

        if(message != null){
            char[] msg = message.toCharArray(); 
            String time = new SimpleDateFormat("MM-dd-yyyy_HH:mm:ss").format(Calendar.getInstance().getTime());
            String other = "";
            for(HandleClient c: clients){
                if(!c.getUsername().equals(user)){
                    other = c.getUsername();
                }
            }
            switch(msg[1]) {
                case '1':
                    out.println("[" + user + "] has logged in at " + time);
                    break;
                case '2':
                    out.println("[" + user + "] has disconnected at " + time);
                    int toRemove = users.indexOf(user);
                    users.remove(new String(user));
                    clients.remove(toRemove);
                    sockets.remove(toRemove);
                    break;
                case '3':
                    out.println("[" + user + "] has sent a message to " + other + " at " + time);
                    break;
                case '4':
                    out.println("[" + user + "] has sent a file to " + other + " at " + time);
                    break;
                default:
                    out.println("Case not found " + time);
            }
        }

    }

    class HandleClient extends Thread{
        String name = "";
        BufferedReader input;
        PrintWriter output;

        public HandleClient(Socket client) throws Exception{
            // get input and output streams
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new PrintWriter(client.getOutputStream(), true);
            // read name
            name = input.readLine();
            users.add(name); // add to vector
            start();
        }

        public void sendMessage(String username, String msg) throws IOException{
            String line = username + ": " + msg.substring(3);
            output.println(line);

            if(clients.size() == 1){
                Writer output;
                String time = new SimpleDateFormat("MM-dd-yyyy_HH:mm:ss").format(Calendar.getInstance().getTime());
                output = new BufferedWriter(new FileWriter(chatlogs, true));
                output.append("[" + time + "] " + line + "\n");
                output.close();
            }
            else if(clients.size() == 2 && logsCount == 0){
                Writer output;
                String time = new SimpleDateFormat("MM-dd-yyyy_HH:mm:ss").format(Calendar.getInstance().getTime());
                output = new BufferedWriter(new FileWriter(chatlogs, true));
                output.append("[" + time + "] " + line + "\n");
                output.close();
                logsCount = 1;
            }
            else if(clients.size() == 2 && logsCount == 1){
                logsCount = 0;
            }
        }
        
        public String getUsername(){  
            return name;
        }

        public void run(){
            String line;

            try{
                while(true){
                    line = input.readLine();
                    broadcast(name, line);
                }
            } catch(Exception ex){
                System.out.println(ex.getMessage());
            }
        }
    }
}