import java.io.*;
import java.net.*;

public class Acceptor {
    
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    private void connectSocket() {
        try {
            socket = new Socket("127.0.0.1", 6666);
            this.out = new PrintWriter(socket.getOutputStream());
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch(Exception e) {
            System.out.println("Connecting socket to proposer exception: " + e.getMessage());
        }

    }


    public static void main(String args[]) {

        Acceptor m2 = new Acceptor();
        Acceptor m3 = new Acceptor();
        Acceptor m4 = new Acceptor();
        Acceptor m5 = new Acceptor();

        m2.connectSocket();
        m3.connectSocket();
        // m4.connectSocket();
        // m5.connectSocket();

        m2.out.write("Hello");
        m2.out.flush();
        m3.out.write("Hello");
        m3.out.flush();

    }

}

// Prepare
// Promise
// Accept
// Accepted