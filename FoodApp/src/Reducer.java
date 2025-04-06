import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Map;

public class Reducer {
    public static void main(String[] args) throws UnknownHostException {
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<ArrayList<Store>> idFilters = new ArrayList<>();

        ArrayList<Integer> ids1 = new ArrayList<>();
        ArrayList<Map<String, Integer>> idDict = new ArrayList<>();


        Object lock = new Object();
        Object lock1 = new Object();

        new Reducer().openServer(ids, idFilters, lock, ids1, idDict, lock1);
    }

    ServerSocket providerSocket;
    Socket connection = null;

    void openServer(ArrayList<Integer> ids, ArrayList<ArrayList<Store>> idFilters, Object lock, ArrayList<Integer> ids1, ArrayList<Map<String, Integer>> idDict, Object lock1) {
        try {
            providerSocket = new ServerSocket(4325, 10);

            while (true) {
                connection = providerSocket.accept();


                Thread t = new ReducerActions(connection, ids, idFilters, lock, ids1, idDict, lock1);
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
