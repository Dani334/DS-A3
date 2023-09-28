import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class CommunicationHandler extends Thread {
    
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public CommunicationHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
    
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            this.handle();

        } catch(Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                socket.close();
                out.close();
                in.close();
            } catch(IOException e) {
                System.out.println("Proposer close exception occured: " + e.getMessage());
            }
        }   

    }


    private void handle() throws Exception {
        String line, req;
        req = "";
        line = in.readLine();
        while(line != null && !line.isEmpty()) {
            req += line;
            line = in.readLine();
        }
        System.out.println(req);
    }


}
