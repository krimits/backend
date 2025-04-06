import java.io.*;
import java.net.*;
import java.util.*;

public class ReducerActions extends Thread {
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Socket connection;
    private ArrayList<Integer> ids;
    private ArrayList<ArrayList<Store>> idFilters;
    private Object lock;
    private ArrayList<Integer> ids1;
    private ArrayList<Map<String, Integer>> idDict;
    private Object lock1;

    // search - aggregation
    private Map<String, List<Store>> finalSearchResult = new HashMap<>();
    private Map<String, Integer> finalAggResult = new HashMap<>();

    private boolean isSearchMode = false;
    private boolean isAggregationMode = false;

    public ReducerActions(Socket connection,
                          ArrayList<Integer> ids,
                          ArrayList<ArrayList<Store>> idFilters,
                          Object lock,
                          ArrayList<Integer> ids1,
                          ArrayList<Map<String, Integer>> idDict,
                          Object lock1) {
        try {
            this.connection = connection;
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
            this.ids = ids;
            this.idFilters = idFilters;
            this.lock = lock;
            this.ids1 = ids1;
            this.idDict = idDict;
            this.lock1 = lock1;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mergeSearch(Map<String, List<Store>> base, Map<String, List<Store>> toMerge) {
        for (Map.Entry<String, List<Store>> entry : toMerge.entrySet()) {
            String key = entry.getKey();
            List<Store> listToMerge = entry.getValue();
            if (base.containsKey(key)) {
                base.get(key).addAll(listToMerge);
            } else {
                base.put(key, new ArrayList<>(listToMerge));
            }
        }
    }

    private void mergeAggregation(Map<String, Integer> base, Map<String, Integer> toMerge) {
        for (Map.Entry<String, Integer> entry : toMerge.entrySet()) {
            String key = entry.getKey();
            int value = entry.getValue();
            base.merge(key, value, Integer::sum);
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("ReducerActions: Νήμα ξεκίνησε για σύνδεση " + connection);

            while (true) {
                Object obj = in.readObject();
                if (obj instanceof String) {
                    String msg = (String) obj;
                    if ("DONE".equalsIgnoreCase(msg)) {
                        System.out.println("ReducerActions: Λάβαμε DONE, τερματισμός λήψης αποτελεσμάτων.");
                        break;
                    }
                }
                else if (obj instanceof Map) {
                    if (!isSearchMode && !isAggregationMode) {
                        Map<?, ?> firstMap = (Map<?, ?>) obj;
                        if (!firstMap.isEmpty()) {
                            Object firstValue = firstMap.values().iterator().next();
                            if (firstValue instanceof List) {
                                isSearchMode = true;
                                System.out.println("ReducerActions: Περνάμε σε SEARCH MODE.");
                            } else if (firstValue instanceof Integer) {
                                isAggregationMode = true;
                                System.out.println("ReducerActions: Περνάμε σε AGGREGATION MODE.");
                            } else {
                                System.out.println("ReducerActions: Λάβαμε άγνωστο τύπο value: " + firstValue.getClass());
                                continue;
                            }
                        } else {
                            isSearchMode = true;
                            System.out.println("ReducerActions: Άδειο map, θεωρούμε SEARCH MODE.");
                        }
                    }

                    if (isSearchMode) {
                        @SuppressWarnings("unchecked")
                        Map<String, List<Store>> partial = (Map<String, List<Store>>) obj;
                        System.out.println("ReducerActions: (SEARCH) Λάβαμε partial result: " + partial);
                        mergeSearch(finalSearchResult, partial);
                    } else if (isAggregationMode) {
                        @SuppressWarnings("unchecked")
                        Map<String, Integer> partial = (Map<String, Integer>) obj;
                        System.out.println("ReducerActions: (AGGREGATION) Λάβαμε partial result: " + partial);
                        mergeAggregation(finalAggResult, partial);
                    }
                }
                else {
                    System.out.println("ReducerActions: Λάβαμε άγνωστο αντικείμενο: " + obj.getClass());
                }
            }

            if (isSearchMode) {
                System.out.println("ReducerActions: (SEARCH) Τελικό αποτέλεσμα -> " + finalSearchResult);
                out.writeObject(finalSearchResult);
            } else if (isAggregationMode) {
                System.out.println("ReducerActions: (AGGREGATION) Τελικό αποτέλεσμα -> " + finalAggResult);
                out.writeObject(finalAggResult);
            } else {
                System.out.println("ReducerActions: Δεν λάβαμε κανένα partial result, στέλνουμε άδειο αποτέλεσμα.");
                out.writeObject(finalSearchResult);
            }
            out.flush();

        } catch (EOFException eof) {
            System.out.println("ReducerActions: Σύνδεση έκλεισε από τον Client/Worker (EOF).");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)  in.close();
                if (out != null) out.close();
                if (connection != null && !connection.isClosed()) connection.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
