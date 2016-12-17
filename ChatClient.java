//Colette and Jacob Chat Room

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatClient {

    BufferedReader in;
    PrintWriter out;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(40);
    JTextArea messageArea = new JTextArea(8, 40);
    String name;

    public ChatClient() {

        // Layout GUI
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, "North");
        frame.getContentPane().add(new JScrollPane(messageArea), "Center");
        frame.pack();

        // Add Listeners
        textField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String message = textField.getText();
                //user wants to leave chat room
                if(message.startsWith("/exit")){
                    out.println("7 \r\n");


                }
                //message sent to everyone
                else if(message.startsWith("/all")){
                    out.println("3 " + message.substring(4) + "\r\n");
                }
                //private message
                else if (message.startsWith("/private")){
                    out.println("4"  + message.substring(8) + "\r\n");
                }
               // out.println(textField.getText());
                textField.setText("");
            }
        });
    }


    /**
     * Prompt for and return the desired screen name.
     */
    private String getName() {
        return JOptionPane.showInputDialog(
                frame,
                "Choose a screen name:",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);

    }

    /**
     * Connects to the server then enters the processing loop.
     */
    private void run() throws IOException {

        // Make connection and initialize streams
        String serverAddress = "localhost";
        Socket socket = new Socket(serverAddress, 1337);
        in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Process all messages from server, according to the protocol.
        while (true) {
            String line = in.readLine();
        //get user name
            if (line.startsWith("SUBMITNAME")) {
                name = getName();
                out.println(name);
            } else if (line.startsWith("NAMEACCEPTED")) {
                textField.setEditable(true);
            }

            //accept user name or decline user name if name is taken
            else if (line.startsWith("1") || line.startsWith("2")) {
                messageArea.append(line.substring(2) + "\r\n");
            }
            //general or private message from server
            else if (line.startsWith("5")) {
                messageArea.append(line.substring(2) + "\r\n");

            }
            else if (line.startsWith("6")){
                String temp = line.substring(2);
                System.out.println("whole line "+ temp);
                int firstSpace = temp.indexOf(" ");
                System.out.println("firstspace "+ firstSpace);
                String fromName = temp.substring(0, firstSpace);
                System.out.println("fromname"+ fromName);
                String message = temp.substring(firstSpace, temp.length());
                System.out.println("message "+ message);
                messageArea.append("from: " + fromName + " "+ " to: "+ message + "\r\n");
            }
            //disconnect request received from server and close streams and exit user
            else if (line.startsWith("8")){
                messageArea.append(line.substring(2)+ "\r\n");
                out.close();
                in.close();
                System.exit(0);
            }
            //sent message saying user has entered chat room if 10 and exited chat room if 9
            else if (line.startsWith("9")|| line.startsWith("10")){
                messageArea.append(line.substring(2)+ "\r\n");
            }
        }
    }

    /**
     * Runs the client as an application with a closeable frame.
     */
    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}
