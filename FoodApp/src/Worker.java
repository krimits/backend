import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Worker {

    public static void main(String[] args) throws UnknownHostException{
        int port = Integer.parseInt(args[0]);

        ArrayList<Store> stores = new ArrayList<>();
        Object lock1 = new Object();

        new Worker().openServer(port, stores, lock1);
    }

    ServerSocket providerSocket;
    Socket connection = null;

    void openServer(int port, ArrayList<Store> stores, Object lock1) {
        try {
            providerSocket = new ServerSocket(port, 10);

            while (true) {
                connection = providerSocket.accept();


                Thread t = new WorkerActions(connection, stores, lock1);
                t.start();

            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

}