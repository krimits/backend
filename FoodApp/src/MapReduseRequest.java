import java.io.Serializable;
import java.util.List;

public class MapReduceRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private double clientLatitude;
    private double clientLongitude;
    private List<String> foodCategories;
    private int minStars;
    private String priceCategory;
    private double radius;

    public MapReduceRequest(double clientLatitude, double clientLongitude, List<String> foodCategories, int minStars, String priceCategory, double radius) {
        this.clientLatitude = clientLatitude;
        this.clientLongitude = clientLongitude;
        this.foodCategories = foodCategories;
        this.minStars = minStars;
        this.priceCategory = priceCategory;
        this.radius = radius;
    }

    // Getters
    public double getClientLatitude() { return clientLatitude; }
    public double getClientLongitude() { return clientLongitude; }
    public List<String> getFoodCategories() { return foodCategories; }
    public int getMinStars() { return minStars; }
    public String getPriceCategory() { return priceCategory; }
    public double getRadius() { return radius; }
}