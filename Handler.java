//Jacob and Colette Handler class for Server
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

class Handler extends Thread {
    private String name;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    // The set of all names of clients in the chat room.
    private static HashSet<String> names = new HashSet<String>();
    private static Hashtable <String, PrintWriter> hash = new Hashtable<String, PrintWriter>();
    /**
     * The set of all the print writers for all the clients.
     */
    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    private static SimpleDateFormat sdf;
    /**
     * Constructs a handler thread, squirreling away the socket.
     * All the interesting work is done in the run method.
     */
    public Handler(Socket socket) {
        this.socket = socket;
    }


    public void run() {
        try {
           // Iterator<String> it = names.iterator();
            sdf = new SimpleDateFormat("HH:mm:ss");
            String time = sdf.format(new Date());
            // Create character streams for the socket.
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Request a name for user.  Keep requesting until
            // a name is submitted that is not already used.
            while (true) {
                out.println("SUBMITNAME");
                name = in.readLine();
                if (name == null) {
                    return;
                }
                synchronized (names) {

                    if (!names.contains(name)) {
                        names.add(name);
                        break;

                    }
                }
            }


            // add user to writers so user can receive broadcast messages.
            out.println("NAMEACCEPTED");


            //send welcome message to user and
            //user has connected message to everyone else
            out.println("1 Welcome List of Users " + names+"\r\n");
            for (PrintWriter writer : writers) {
                writer.println("10 "  + name +  " has connected \r\n");
            }
            writers.add(out);
            hash.put(name,out);

            // Accept messages from this client and broadcast them.

            while (true) {
                String input = in.readLine();
                if (input == null) {
                    return;
                }
                //general message sent to everyone
                if(input.startsWith("3")) {
                    for (PrintWriter writer : writers) {
                        writer.println("5 " + time + " " + name + ": " + input.substring(2)+"\r\n");
                    }
                }
                // if message is private
                else if(input.startsWith("4")){
                    System.out.println("whole input: "+ input);
                    int temp = input.indexOf(" ",2);
                    String tempWord = input.substring(temp+1, input.length());
                    int firstSpace = tempWord.indexOf(" ");
                    String toUser = tempWord.substring(0, firstSpace);
                    String message = tempWord.substring(firstSpace, tempWord.length());
                    hash.get(toUser).println("6 " + name + " "+ toUser + " " + time + " " + message + "\r\n");
                    System.out.println("6 " + name + "1: "+ toUser + "2: " + time + "3: " + message);
                    }



                // user wants to leave chat room send goodbye message to user
                // and disconnect notice to everyone else
                else if(input.startsWith("7")){
                    out.println("8 Goodbye!\r\n");
                    for (PrintWriter writer : writers) {
                        writer.println("9 "+ name + " has disconnected \r\n");
                    }
                    names.remove(name);
                    writers.remove(out);


                }
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            // This client is going down!  Remove its name and its print
            // writer from the sets, and close its socket.
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
