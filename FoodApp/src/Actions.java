import java.io.*;
import java.net.*;
import java.text.ParseException;
import java.util.*;

public class Actions extends Thread {
    ObjectInputStream in;
    ObjectOutputStream out;
    String[][] workers;
    HashMap<Integer, ObjectOutputStream> connectionsOut;
    int counterID;

    public Actions(Socket connection, String[][] workers, HashMap<Integer, ObjectOutputStream> connectionsOut, int counterID) {
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            this.workers = workers;
            this.connectionsOut = connectionsOut;
            this.counterID = counterID;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String role = (String) in.readObject();

            if (role.equals("manager")) {
                // Receive the store list
                ArrayList<Store> stores = (ArrayList<Store>) in.readObject();
                int successCount = 0;

                for (Store store : stores) {
                    Socket workerSocket = null;
                    ObjectOutputStream outWorker = null;
                    ObjectInputStream inWorker = null;

                    try {
                        // Hash-based assignment to worker
                        int workerId = Math.abs(store.getStoreName().hashCode()) % workers.length;
                        String workerIP = workers[workerId][0];
                        int workerPort = Integer.parseInt(workers[workerId][1]);

                        workerSocket = new Socket(workerIP, workerPort);
                        outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                        inWorker = new ObjectInputStream(workerSocket.getInputStream());

                        outWorker.writeObject("manager");
                        outWorker.flush();

                        outWorker.writeObject(store);
                        outWorker.flush();

                        String response = (String) inWorker.readObject();
                        if ("Store(s) added successfully".equals(response)) {
                            successCount++;
                        }

                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (inWorker != null) inWorker.close();
                            if (outWorker != null) outWorker.close();
                            if (workerSocket != null) workerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (successCount == stores.size()) {
                    out.writeObject("All stores added successfully");
                } else {
                    out.writeObject("Some stores failed to add");
                }
                out.flush();


            }else if (role.equals("product")) {
                String storeName = (String) in.readObject();       // Get store name
                Product product = (Product) in.readObject();        // Get product to add

                Socket workerSocket = null;
                ObjectOutputStream outWorker = null;
                ObjectInputStream inWorker = null;

                try {
                    int workerId = Math.abs(storeName.hashCode()) % workers.length;
                    String workerIP = workers[workerId][0];
                    int workerPort = Integer.parseInt(workers[workerId][1]);

                    workerSocket = new Socket(workerIP, workerPort);
                    outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                    inWorker = new ObjectInputStream(workerSocket.getInputStream());

                    // Send operation and data
                    outWorker.writeObject("product");
                    outWorker.flush();

                    outWorker.writeObject(storeName);
                    outWorker.flush();

                    outWorker.writeObject(product);
                    outWorker.flush();

                    // Get response from worker and pass to manager
                    String response = (String) inWorker.readObject();
                    out.writeObject(response);
                    out.flush();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inWorker != null) inWorker.close();
                        if (outWorker != null) outWorker.close();
                        if (workerSocket != null) workerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }else if (role.equals("remove")) {
                String storeName = (String) in.readObject();                 // Get the store name
                Map<String, Integer> productUpdates = (Map<String, Integer>) in.readObject();

                Socket workerSocket = null;
                ObjectOutputStream outWorker = null;
                ObjectInputStream inWorker = null;

                try {
                    // Determine the worker responsible for this store
                    int workerId = Math.abs(storeName.hashCode()) % workers.length;
                    String workerIP = workers[workerId][0];
                    int workerPort = Integer.parseInt(workers[workerId][1]);

                    // Connect to the appropriate worker
                    workerSocket = new Socket(workerIP, workerPort);
                    outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                    inWorker = new ObjectInputStream(workerSocket.getInputStream());

                    // Send command and data to worker
                    outWorker.writeObject("remove");
                    outWorker.flush();

                    outWorker.writeObject(storeName);
                    outWorker.flush();

                    outWorker.writeObject(productUpdates);
                    outWorker.flush();

                    // Wait for confirmation and forward it to manager
                    String response = (String) inWorker.readObject();
                    out.writeObject(response);
                    out.flush();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (inWorker != null) inWorker.close();
                        if (outWorker != null) outWorker.close();
                        if (workerSocket != null) workerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }else if (role.equals("storeType")) {
                String storeType = (String) in.readObject();  // e.g., "pizzeria"

                Map<String, Integer> finalResult = new HashMap<>();

                for (int i = 0; i < workers.length; i++) {
                    Socket workerSocket = null;
                    ObjectOutputStream outWorker = null;
                    ObjectInputStream inWorker = null;

                    try {
                        String workerIP = workers[i][0];
                        int workerPort = Integer.parseInt(workers[i][1]);

                        workerSocket = new Socket(workerIP, workerPort);
                        outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                        inWorker = new ObjectInputStream(workerSocket.getInputStream());

                        // Send operation type and filter
                        outWorker.writeObject("storeType");
                        outWorker.flush();

                        outWorker.writeObject(storeType);
                        outWorker.flush();

                        Map<String, Integer> partialResult = (Map<String, Integer>) inWorker.readObject();

                        // Merge into final result
                        for (Map.Entry<String, Integer> entry : partialResult.entrySet()) {
                            finalResult.merge(entry.getKey(), entry.getValue(), Integer::sum);
                        }

                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (inWorker != null) inWorker.close();
                            if (outWorker != null) outWorker.close();
                            if (workerSocket != null) workerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Send final merged result back to Manager
                out.writeObject(finalResult);
                out.flush();


            }else if (role.equals("productCategory")) {
                String productCategory = (String) in.readObject(); // e.g., "pizza"

                Map<String, Integer> finalResult = new HashMap<>();

                for (int i = 0; i < workers.length; i++) {
                    Socket workerSocket = null;
                    ObjectOutputStream outWorker = null;
                    ObjectInputStream inWorker = null;

                    try {
                        String workerIP = workers[i][0];
                        int workerPort = Integer.parseInt(workers[i][1]);

                        workerSocket = new Socket(workerIP, workerPort);
                        outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                        inWorker = new ObjectInputStream(workerSocket.getInputStream());

                        // Send operation type and category to filter
                        outWorker.writeObject("productCategory");
                        outWorker.flush();

                        outWorker.writeObject(productCategory);
                        outWorker.flush();

                        Map<String, Integer> partialResult = (Map<String, Integer>) inWorker.readObject();

                        // Merge partial result into final
                        for (Map.Entry<String, Integer> entry : partialResult.entrySet()) {
                            finalResult.merge(entry.getKey(), entry.getValue(), Integer::sum);
                        }

                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (inWorker != null) inWorker.close();
                            if (outWorker != null) outWorker.close();
                            if (workerSocket != null) workerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Send final result to the Manager
                out.writeObject(finalResult);
                out.flush();


            }else if (role.equals("client")) {

                MapReduceRequest request = (MapReduceRequest) in.readObject();

                ArrayList <Store> finalResult = new ArrayList<> () ;
                for (int i = 0; i < workers.length; i++) {
                    Socket workerSocket = null;
                    ObjectOutputStream outWorker = null;
                    ObjectInputStream inWorker = null;

                    try {
                        String workerIP = workers[i][0];
                        int workerPort = Integer.parseInt(workers[i][1]);

                        workerSocket = new Socket(workerIP, workerPort);
                        outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                        inWorker = new ObjectInputStream(workerSocket.getInputStream());

                        // Send operation type and category to filter
                        outWorker.writeObject("client");
                        outWorker.flush();

                        outWorker.writeObject(request);
                        outWorker.flush();

                        finalResult = (ArrayList<Store>) inWorker.readObject();


                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (inWorker != null) inWorker.close();
                            if (outWorker != null) outWorker.close();
                            if (workerSocket != null) workerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    // Send final result to the Manager
                    out.writeObject(finalResult);
                    out.flush();

                }



            }else if (role.equals("filter")) {

                MapReduceRequest request = (MapReduceRequest) in.readObject();

                ArrayList<Store> results = new ArrayList<>();

                for (int i = 0; i < workers.length; i++) {
                    Socket workerSocket = null;
                    ObjectOutputStream outWorker = null;
                    ObjectInputStream inWorker = null;

                    try {
                        String workerIP = workers[i][0];
                        int workerPort = Integer.parseInt(workers[i][1]);

                        workerSocket = new Socket(workerIP, workerPort);
                        outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                        inWorker = new ObjectInputStream(workerSocket.getInputStream());

                        // Send operation type and category to filter
                        outWorker.writeObject("filter");
                        outWorker.flush();

                        outWorker.writeObject(request);
                        outWorker.flush();

                        results = (ArrayList<Store>) inWorker.readObject();


                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (inWorker != null) inWorker.close();
                            if (outWorker != null) outWorker.close();
                            if (workerSocket != null) workerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Send final result to the Manager
                out.writeObject(results);
                out.flush();


            }else if (role.equals("fetchProducts")) {

                String store = (String) in.readObject();

                ArrayList<Product> results = new ArrayList<>();

                for (int i = 0; i < workers.length; i++) {
                    Socket workerSocket = null;
                    ObjectOutputStream outWorker = null;
                    ObjectInputStream inWorker = null;

                    try {
                        String workerIP = workers[i][0];
                        int workerPort = Integer.parseInt(workers[i][1]);

                        workerSocket = new Socket(workerIP, workerPort);
                        outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                        inWorker = new ObjectInputStream(workerSocket.getInputStream());

                        // Send operation type and category to filter
                        outWorker.writeObject("fetchProducts");
                        outWorker.flush();

                        outWorker.writeObject(store);
                        outWorker.flush();

                        results = (ArrayList<Product>) inWorker.readObject();


                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (inWorker != null) inWorker.close();
                            if (outWorker != null) outWorker.close();
                            if (workerSocket != null) workerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Send final result to the Manager
                out.writeObject(results);
                out.flush();


            }else if (role.equals("purchase")) {

                Purchase pur = (Purchase) in.readObject();
                String results = null;

                for (int i = 0; i < workers.length; i++) {
                    Socket workerSocket = null;
                    ObjectOutputStream outWorker = null;
                    ObjectInputStream inWorker = null;

                    try {
                        String workerIP = workers[i][0];
                        int workerPort = Integer.parseInt(workers[i][1]);

                        workerSocket = new Socket(workerIP, workerPort);
                        outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                        inWorker = new ObjectInputStream(workerSocket.getInputStream());

                        // Send operation type and category to filter
                        outWorker.writeObject("purchase");
                        outWorker.flush();

                        outWorker.writeObject(pur);
                        outWorker.flush();

                       results = (String) inWorker.readObject();


                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (inWorker != null) inWorker.close();
                            if (outWorker != null) outWorker.close();
                            if (workerSocket != null) workerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Send final result to the Manager
                out.writeObject(results);
                out.flush();


            }else if (role.equals("rate")) {

                String store = (String) in.readObject();
                int rating = (int) in.readObject();

                String results = null;

                for (int i = 0; i < workers.length; i++) {
                    Socket workerSocket = null;
                    ObjectOutputStream outWorker = null;
                    ObjectInputStream inWorker = null;

                    try {
                        String workerIP = workers[i][0];
                        int workerPort = Integer.parseInt(workers[i][1]);

                        workerSocket = new Socket(workerIP, workerPort);
                        outWorker = new ObjectOutputStream(workerSocket.getOutputStream());
                        inWorker = new ObjectInputStream(workerSocket.getInputStream());

                        // Send operation type and category to filter
                        outWorker.writeObject("purchase");
                        outWorker.flush();

                        outWorker.writeObject(store);
                        outWorker.flush();

                        outWorker.writeObject(rating);
                        outWorker.flush();

                        results = (String) inWorker.readObject();


                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (inWorker != null) inWorker.close();
                            if (outWorker != null) outWorker.close();
                            if (workerSocket != null) workerSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Send final result to the Manager
                out.writeObject(results);
                out.flush();
            }








//            } else if (role.equals("reducer")) {
//                // Συγχώνευση αποτελεσμάτων από Workers (Reduce)
//                int id = in.readInt();
//                Map<String, List<Store>> result = (Map<String, List<Store>>) in.readObject();
//
//                connectionsOut.get(id).writeObject(result);
//                connectionsOut.get(id).flush();
//
//            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
       }
    }
}
