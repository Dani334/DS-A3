import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Proposer {
    private int n;
    
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private ServerSocket proposerSocket;

    private Proposer() {
        this.n = 1;
    }

    private void createConnection() {
        try {
            this.proposerSocket = new ServerSocket(6666);
            while(true) {
                socket = proposerSocket.accept();
                Thread connectionThread = new CommunicationHandler(socket);
                connectionThread.start();
            }
        } catch(Exception e) {
            System.out.println("Creating proposer exception: " + e.getMessage());
        }
    }

    private String createPrepare() {

        String prepare = "Prepare(" + n + ")";
        n++;
        return prepare;
    }


    public static void main(String args[]) {

        Proposer m1 = new Proposer();
        m1.createConnection();

    }
    
}
