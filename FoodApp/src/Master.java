import java.io.*;
import java.net.*;
import java.util.*;


public class Master {

    public static void main(String[] args) throws UnknownHostException {

        String[][] workers = new String [args.length/2][2];
        HashMap<Integer, ObjectOutputStream> connectionsOut = new HashMap<>();


        for (int i = 0; i < args.length/2; i++) {
            workers[i][0] = args[i*2];
            workers[i][1] = args[i*2 + 1];
        }

        new Master().openServer(workers, connectionsOut);
    }

    ServerSocket providerSocket;
    Socket connection = null;
    int counterID = 0;

    void openServer(String [][] workers, HashMap<Integer, ObjectOutputStream> connectionsOut) {
        try {
            providerSocket = new ServerSocket(4321, 10);

            while (true) {
                connection = providerSocket.accept();
                counterID++;

                Thread t = new Actions(connection, workers, connectionsOut, counterID);
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



