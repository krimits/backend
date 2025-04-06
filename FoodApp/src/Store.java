import java.io.Serializable;
import java.util.List;
import java.util.*;

public class Store implements Serializable {

    private String storeName;
    private double latitude;
    private double longitude;
    private String category;
    private int stars;
    private int noOfReviews;
    private ArrayList<Product> products;
    private ArrayList<Purchase> purchases;

    public Store(String storeName, double latitude, double longitude, String category, int stars, int noOfReviews, String storeLogoPath, ArrayList<Product> products) {
        this.storeName = storeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.category = category;
        this.stars = stars;
        this.noOfReviews = noOfReviews;
        this.products = new ArrayList<>();
        this.purchases = new ArrayList<>();
    }

//    public void addProduct(Product product) {
//        products.add(product);
//    }
//
//    public void removeProduct(String productName) {
//        products.removeIf(product -> product.getName().equals(productName));
//    }
//
//    public void updateInventory(String productName, int quantity) {
//        for (Product product : products) {
//            if (product.getName().equals(productName)) {
//                product.setQuantity(quantity);
//                break;
//            }
//        }
//    }

    public String getStoreName() {
        return storeName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCategory() {
        return category;
    }

    public int getStars() {
        return stars;
    }

    public int getNoOfReviews() {
        return noOfReviews;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public void setNoOfReviews(int noOfReviews) {
        this.noOfReviews = noOfReviews;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public ArrayList<Purchase> getPurchases() {
        return purchases;
    }

    public String calculatePriceCategory() {
        double totalPrice = 0;
        for (Product product : products) {
            totalPrice += product.getPrice();
        }
        double avgPrice = totalPrice / products.size();
        if (avgPrice <= 5) return "$";
        if (avgPrice <= 15) return "$$";
        return "$$$";
    }

    @Override
    public String toString() {
        return "Store Name: " + storeName + "\nCategory: " + category + "\nStars: " + stars + "\nReviews: " + noOfReviews;
    }
}
