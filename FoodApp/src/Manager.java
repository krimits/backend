import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;

public class Manager {
    public static void main(String[] args) throws ParseException, FileNotFoundException {
        Scanner sc = new Scanner(System.in);

        boolean flag = true;

        while (flag) {

            System.out.println("1.Add store");
            System.out.println("2.Add Product");
            System.out.println("3.Remove Product");
            System.out.println("4.Total sales by store type");
            System.out.println("5.Total sales by product category");
            System.out.println("6.Exit");
            System.out.println("Choose 1-2-3-4-5-6");
            String number = sc.nextLine();

            if (number.equals("1")) {
                ArrayList<Store> stores = new ArrayList<>();

                System.out.println("Give the json file of the store");
                String jsonPath = sc.nextLine();

                try (FileReader reader = new FileReader(jsonPath)) {

                    JSONParser parser = new JSONParser();

                    JSONArray jsonArray = (JSONArray) parser.parse(reader);


                    for (Object obj : jsonArray) {
                        JSONObject jsonObject = (JSONObject) obj;

                        String name = (String) jsonObject.get("StoreName");

                        double latitude = ((Number) jsonObject.get("Latitude")).doubleValue();

                        double longitude = ((Number) jsonObject.get("Longitude")).doubleValue();

                        String category = (String) jsonObject.get("FoodCategory");

                        int stars = ((Number) jsonObject.get("Stars")).intValue();

                        int reviews = ((Number) jsonObject.get("NoOfVotes")).intValue();

                        String storeLogoPath = (String) jsonObject.get("StoreLogo");

                        // Ανάγνωση των προϊόντων
                        ArrayList<Product> products = new ArrayList<>();
                        JSONArray productsArray = (JSONArray) jsonObject.get("Products");
                        for (Object prodObj : productsArray) {
                            JSONObject productJson = (JSONObject) prodObj;

                            String productName = (String) productJson.get("ProductName");
                            String productType = (String) productJson.get("ProductType");
                            int amount = ((Number) jsonObject.get("Available Amount")).intValue();
                            double productPrice = ((Number) productJson.get("Price")).doubleValue();

                            products.add(new Product(productName, productType, amount, productPrice));
                        }

                        Store s = new Store(name, latitude, longitude, category, stars, reviews, storeLogoPath, products);

                        System.out.println(s);

                        stores.add(s);

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (org.json.simple.parser.ParseException e) {
                    throw new RuntimeException(e);
                }


                Socket requestSocket = null;
                ObjectOutputStream out = null;
                ObjectInputStream in = null;
                try {
                    requestSocket = new Socket("127.0.0.1", 4321);
                    out = new ObjectOutputStream(requestSocket.getOutputStream());
                    in = new ObjectInputStream(requestSocket.getInputStream());

                    out.writeObject("manager");
                    out.flush();

                    out.writeObject(stores);
                    out.flush();

                    String res = (String) in.readObject();
                    System.out.println(res);
                    System.out.print("\n");


                } catch (UnknownHostException unknownHost) {
                    System.err.println("You are trying to connect to an unknown host!");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                        out.close();
                        requestSocket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }

            } else if (number.equals("2")) {
                String jsonPath = "store.json"; // Σταθερό όνομα αρχείου JSON

                try (FileReader reader = new FileReader(jsonPath)) {
                    JSONParser parser = new JSONParser();
                    JSONArray jsonArray = (JSONArray) parser.parse(reader);

                    System.out.println("Enter store name to add a product:");
                    String storeName = sc.nextLine();
                    Product product = null;

                    boolean storeFound = false;

                    for (Object obj : jsonArray) {
                        JSONObject jsonObject = (JSONObject) obj;

                        if (storeName.equalsIgnoreCase((String) jsonObject.get("StoreName"))) {
                            storeFound = true;

                            JSONArray productsArray = (JSONArray) jsonObject.get("Products");

                            System.out.println("Enter Product Name:");
                            String productName = sc.nextLine();

                            boolean productFound = false;

                            // Ελέγχουμε αν το προϊόν υπάρχει ήδη
                            for (Object productObj : productsArray) {
                                JSONObject productJson = (JSONObject) productObj;

                                String existingProductName = (String) productJson.get("ProductName");

                                if (productName.equalsIgnoreCase(existingProductName)) {
                                    productFound = true;

                                    System.out.println("Product already exists. How much would you like to add to the quantity?");
                                    int additionalAmount = Integer.parseInt(sc.nextLine());

                                    int currentAmount = ((Number) productJson.get("Available Amount")).intValue();
                                    product.setQuantity(currentAmount + additionalAmount);
                                    break;
                                }
                            }

                            if (!productFound) {

                                System.out.println("Enter Product Type:");
                                String productType = sc.nextLine();

                                System.out.println("Enter Available Amount:");
                                int amount = Integer.parseInt(sc.nextLine());

                                System.out.println("Enter Product Price:");
                                double productPrice = Double.parseDouble(sc.nextLine());

                                product = new Product(productName, productType, amount, productPrice);
                            }


                            Socket requestSocket = null;
                            ObjectOutputStream out = null;
                            ObjectInputStream in = null;
                            try {
                                requestSocket = new Socket("127.0.0.1", 4321);
                                out = new ObjectOutputStream(requestSocket.getOutputStream());
                                in = new ObjectInputStream(requestSocket.getInputStream());

                                out.writeObject("product");
                                out.flush();

                                out.writeObject(storeName);
                                out.flush();

                                out.writeObject(product);
                                out.flush();

                                System.out.println();



                                String res = (String) in.readObject();
                                System.out.println(res);
                                System.out.print("\n");


                            } catch (UnknownHostException unknownHost) {
                                System.err.println("You are trying to connect to an unknown host!");
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            } finally {
                                try {
                                    in.close();
                                    out.close();
                                    requestSocket.close();
                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                }
                            }
                        }
                    }

                    if (!storeFound) {
                        System.out.println("Store not found.");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (org.json.simple.parser.ParseException e) {
                    System.err.println("Error parsing JSON file: " + e.getMessage());
                }


            } else if (number.equals("3")) {
                String jsonPath = "store.json";
                Map<String, Integer> productQuantityMap = new HashMap<>();


                try (FileReader reader = new FileReader(jsonPath)) {
                    JSONParser parser = new JSONParser();
                    JSONArray jsonArray = (JSONArray) parser.parse(reader);

                    ArrayList<Product> products = new ArrayList<>();

                    System.out.println("Enter store name to remove a product:");
                    String storeName = sc.nextLine();

                    boolean storeFound = false;
                    boolean productFound = false;

                    for (Object obj : jsonArray) {
                        JSONObject jsonObject = (JSONObject) obj;

                        if (storeName.equalsIgnoreCase((String) jsonObject.get("StoreName"))) {
                            storeFound = true;

                            JSONArray productsArray = (JSONArray) jsonObject.get("Products");

                            System.out.println("Enter Product Name to remove:");
                            String productName = sc.nextLine();


                            System.out.println("1. Remove the product");
                            System.out.println("2. Decrease the quantity of the product");
                            System.out.println("Choose 1-2");
                            String num = sc.nextLine();

                            if (num.equals("1")) {
                                for (Object prodObj : productsArray) {
                                    JSONObject productJson = (JSONObject) prodObj;

                                    if (productName.equalsIgnoreCase((String) productJson.get("ProductName"))) {
                                        productJson.put("Hidden", true);
                                        productQuantityMap.put(productName, -1); // send -1 to hide
                                        productFound = true;
                                        break;
                                    }

                                }
                            } else if (num.equals("2")) {
                                for (Object prodObj : productsArray) {
                                    JSONObject productJson = (JSONObject) prodObj;

                                    int currentQuantity = ((Long) productJson.get("Quantity")).intValue();

                                    if (productName.equalsIgnoreCase((String) productJson.get("ProductName"))) {
                                        productFound = true;
                                    }

                                    if (productFound) {
                                        System.out.println("Enter quantity to subtract:");
                                        int subtractAmount = sc.nextInt();
                                        sc.nextLine(); // Consume newline

                                        if (subtractAmount > currentQuantity) {
                                            System.out.println("Error: Not enough stock!");
                                            System.out.println("The available stock is: " + currentQuantity);
                                        } else {
                                            productJson.put("Quantity", currentQuantity - subtractAmount);
                                            System.out.println("Updated Quantity: " + productJson.get("Quantity"));
                                            productQuantityMap.put(productName, currentQuantity - subtractAmount);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }



                    if (!storeFound) {
                        System.out.println("Store not found.");
                    } else if (!productFound) {
                        System.out.println("Product not found.");
                    }

                    try (FileWriter writer = new FileWriter(jsonPath)) {
                        writer.write(jsonArray.toJSONString());
                        writer.flush();
                        System.out.println("Changes saved to store.json.");
                    }


                    Socket requestSocket = null;
                    ObjectOutputStream out = null;
                    ObjectInputStream in = null;
                    try {
                        requestSocket = new Socket("127.0.0.1", 4321);
                        out = new ObjectOutputStream(requestSocket.getOutputStream());
                        in = new ObjectInputStream(requestSocket.getInputStream());

                        out.writeObject("remove");
                        out.flush();

                        out.writeObject(storeName);
                        out.flush();

                        out.writeObject(productQuantityMap);
                        out.flush();

                        String res = (String) in.readObject();
                        System.out.println(res);
                        System.out.print("\n");


                    } catch (UnknownHostException unknownHost) {
                        System.err.println("You are trying to connect to an unknown host!");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    } finally {
                        try {
                            in.close();
                            out.close();
                            requestSocket.close();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }



                } catch (IOException | org.json.simple.parser.ParseException e) {
                    throw new RuntimeException(e);
                }


            } else if (number.equals("4")) {
                System.out.println("Enter the store type (e.g., pizzeria, burger):");
                String storeType = sc.nextLine();

                Socket requestSocket = null;
                ObjectOutputStream out = null;
                ObjectInputStream in = null;
                try{
                    requestSocket = new Socket("127.0.0.1", 4321);
                    out = new ObjectOutputStream(requestSocket.getOutputStream());
                    in = new ObjectInputStream(requestSocket.getInputStream());


                    out.writeObject("storeType");
                    out.flush();

                    out.writeObject(storeType);
                    out.flush();


                    Map<String, Integer> result = (Map<String, Integer>) in.readObject();

                    int total = 0;
                    System.out.println("Sales by Store for type: " + storeType);
                    for (Map.Entry<String, Integer> entry : result.entrySet()) {
                        System.out.println("• " + entry.getKey() + ": " + entry.getValue());
                        total += entry.getValue();
                    }
                    System.out.println("Total Sales: " + total + "\n");

                } catch (UnknownHostException unknownHost) {
                    System.err.println("You are trying to connect to an unknown host!");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        in.close();
                        out.close();
                        requestSocket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }


            } else if (number.equals("5")) {
                System.out.println("Enter the product category (e.g., pizza, salad, burger):");
                String productCategory = sc.nextLine();

                Socket requestSocket = null;
                ObjectOutputStream out = null;
                ObjectInputStream in = null;
                try {
                    requestSocket = new Socket("127.0.0.1", 4321);
                    out = new ObjectOutputStream(requestSocket.getOutputStream());
                    in = new ObjectInputStream(requestSocket.getInputStream());

                    out.writeObject("productCategory");
                    out.flush();

                    out.writeObject(productCategory);
                    out.flush();

                    Map<String, Integer> result = (Map<String, Integer>) in.readObject();

                    int total = 0;
                    System.out.println("Sales by Store for product category: " + productCategory);
                    for (Map.Entry<String, Integer> entry : result.entrySet()) {
                        System.out.println("• " + entry.getKey() + ": " + entry.getValue());
                        total += entry.getValue();
                    }
                    System.out.println("Total Sales: " + total + "\n");



                } catch (UnknownHostException unknownHost) {
                    System.err.println("You are trying to connect to an unknown host!");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        in.close();
                        out.close();
                        requestSocket.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }


            } else if (number.equals("6")) {
                System.out.println("Exit");
                flag = false;
            } else {
                System.out.println("Wrong number. Try again");
            }
        }

    }
}