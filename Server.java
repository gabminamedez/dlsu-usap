import java.io.*;
import java.util.*;
import java.net.*;
import static java.lang.System.out;

public class Server{

    Vector<String> users = new Vector<String>();
    Vector<HandleClient> clients = new Vector<HandleClient>();

    private static File chatlogs = new File("chatlogs.txt");

    public void process() throws Exception{
        ServerSocket server = new ServerSocket(8080, 2);
        out.println("Server started!");
        out.println("IP Address: localhost");
        out.println("Port: 8080");

        while(true){
            Socket client = server.accept();
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
            switch(msg[1]) {
                case '1':
                    out.println("[" + user + "] has logged in");
                    break;
                case '2':
                    out.println("[" + user + "] has disconnected");
                    break;
                case '3':
                    out.println("[" + user + "] has sent message");
                    break;
                case '4':
                    out.println("[" + user + "] has sent a file");
                    break;
                default:
                    out.println("Case not found");
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

            Writer output;
            output = new BufferedWriter(new FileWriter(chatlogs, true));
            output.append(line + "\n");
            output.close();
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