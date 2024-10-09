package team4s;

public class InventoryItem {
    private int ingredient_id;
    private String ingredient_name;
    private int current_stock;
    private double price;
    private String unit;
    private int min_stock;

    // Constructor
    public InventoryItem(int ingredient_id, String ingredient_name, int current_stock, double price, String unit, int min_stock) {
        this.ingredient_id = ingredient_id;
        this.ingredient_name = ingredient_name;
        this.current_stock = current_stock;
        this.price = price;
        this.unit = unit;
        this.min_stock = min_stock;
    }

    // Getters
    public int getIngredientId() { return ingredient_id; }
    public String getIngredientName() { return ingredient_name; }
    public int getCurrentStock() { return current_stock; }
    public double getPrice() { return price; }
    public String getUnit() { return unit; }
    public int getMinStock() { return min_stock; }

    // Setters
    public void setIngredientId(int ingredient_id) { this.ingredient_id = ingredient_id; }
    public void setIngredientName(String ingredient_name) { this.ingredient_name = ingredient_name; }
    public void setCurrentStock(int current_stock) { this.current_stock = current_stock; }
    public void setPrice(double price) { this.price = price; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setMinStock(int min_stock) { this.min_stock = min_stock; }
}