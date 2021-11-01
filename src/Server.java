import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(555);
            while (true) {
                Socket socket = ss.accept();
                System.out.println("A new client is connected.");

                // create a thread to allow simultaneous connections
                Worker w = new Worker(socket);
                w.start();
            }
        } catch (Exception e) {
            System.out.println("Some kind of error has occurred.");
        }
    }
}