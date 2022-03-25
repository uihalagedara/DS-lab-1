package chatserver;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;


public class ChatServer {

    
    private static final int PORT = 9001;

    
    private static HashSet<String> names = new HashSet<String>();

    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    
    private static HashMap<String, PrintWriter> writersAndNames = new HashMap<String, PrintWriter>();
    
    private static HashSet<String> clients = new HashSet<>();
    
    
    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
            	Socket socket  = listener.accept();
                Thread handlerThread = new Thread(new Handler(socket));
                handlerThread.start();
            }
        } finally {
            listener.close();
        }
    }

    
    private static class Handler implements Runnable {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        
        public void run() {
            try {

               
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                
                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    
                    
                    synchronized(names) {
                    	 if (!names.contains(name)) {
                             names.add(name);
                             break;
                         }
                     
                    }
                   
                 }

               
                out.println("NAMEACCEPTED");
                writers.add(out);
                
                
                writersAndNames.put(name,out);

                
                
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    
                    
                    for(String key : writersAndNames.keySet()) {
                    	if(key.equals(name)) {
                    		for(String name: names) {
                    			writersAndNames.get(key).println("NEWNAME" +name);
                    		}
                    	}else {
                    		writersAndNames.get(key).println("NAME" +name);
                    		}
                    	}
                   
                    while(true) {
                    	String input1 = in.readLine();
                    	if(input1.startsWith("MSG")) {
                    		for(String string : clients) {
                    			for(String key : writersAndNames.keySet()) {
                    				if(string.equals(key)) {
                    					writersAndNames.get(key).println("MESSAGE " + name + ": " + input);
                    				}
                    			}
                    		}clients.clear();
                    	}else if(input1.startsWith("CHECK")) {
                    		for(PrintWriter writer: writers) {
                    			writer.println("MESSAGE" +name);
                    		}
                    	}else {
                    		clients.add(input1);
                    	}
                    }
                    
                }
            }
            catch(java.net.SocketException e) {
            	System.out.println("User logged out of the chat");
            }
            catch (IOException e) {
                System.out.println(e);
            } finally {
                
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}