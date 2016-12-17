//Colette and Jacob Server class for Chat Server
import java.net.ServerSocket;

public class ChatServer {

    private static final int PORT = 1337;

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }
}
