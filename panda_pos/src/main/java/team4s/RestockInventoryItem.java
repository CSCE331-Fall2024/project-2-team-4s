package team4s;

public class RestockInventoryItem {
    private int ingredient_id;
    private String ingredient_name;
    private int quantity;
    private double price;
    private String unit;
    private double total_price;

    public RestockInventoryItem(int ingredient_id, String ingredient_name, int quantity, double price, String unit,
            double total_price) {
        this.ingredient_id = ingredient_id;
        this.ingredient_name = ingredient_name;
        this.quantity = quantity;
        this.price = price;
        this.unit = unit;
        this.total_price = total_price;
    }

    public int getIngredientID() {
        return ingredient_id;
    }

    public String getIngredientName() {
        return ingredient_name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getUnit() {
        return unit;
    }

    public double getTotalPrice() {
        return total_price;
    }

    public void setIngredientID(int ingredient_id) {
        this.ingredient_id = ingredient_id;
    }

    public void setIngredientName(String ingredient_name) {
        this.ingredient_name = ingredient_name;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setTotalPrice(double total_price) {
        this.total_price = total_price;
    }
}
