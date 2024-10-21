package team4s;

public class RestockInventoryItem {
    private int ingredient_id;
    private String ingredient_name;
    private int quantity;
    private double price;
    private String unit;
    private double total_price;

    /**
     * Constructs a RestockInventoryItem with the specified details.
     * 
     * @param ingredient_id
     * @param ingredient_name
     * @param quantity
     * @param price
     * @param unit
     * @param total_price
     */
    public RestockInventoryItem(int ingredient_id, String ingredient_name, int quantity, double price, String unit,
            double total_price) {
        this.ingredient_id = ingredient_id;
        this.ingredient_name = ingredient_name;
        this.quantity = quantity;
        this.price = price;
        this.unit = unit;
        this.total_price = total_price;
    }

    /**
     * Gets the ID of the ingredient.
     * 
     * @return ingredient_id the ID of the ingredient
     */
    public int getIngredientID() {
        return ingredient_id;
    }

    /**
     * Gets the name of the ingredient.
     * 
     * @return ingredient_name the name of the ingredient
     */
    public String getIngredientName() {
        return ingredient_name;
    }

    /**
     * Gets the quantity of the ingredient in the order.
     * 
     * @return quantity the quantity of the ingredient in the order
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Gets the price of the ingredient.
     * 
     * @return price the price of the ingredient
     */
    public double getPrice() {
        return price;
    }

    /**
     * Gets the unit of the ingredient.
     * 
     * @return unit the unit of the ingredient
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Gets the total price of the ingredient in the order.
     * 
     * @return total_price the total price of the ingredient in the order
     */
    public double getTotalPrice() {
        return total_price;
    }

    /**
     * Sets the ID of the ingredient.
     * 
     * @param ingredient_id the ID of the ingredient
     */
    public void setIngredientID(int ingredient_id) {
        this.ingredient_id = ingredient_id;
    }

    /**
     * Sets the name of the ingredient.
     * 
     * @param ingredient_name the name of the ingredient
     */
    public void setIngredientName(String ingredient_name) {
        this.ingredient_name = ingredient_name;
    }

    /**
     * Sets the quantity of the ingredient in the order.
     * 
     * @param quantity the quantity of the ingredient in the order
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Sets the price of the ingredient.
     * 
     * @param price the price of the ingredient
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Sets the unit of the ingredient.
     * 
     * @param unit the unit of the ingredient
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Sets the total price of the ingredient in the order.
     * 
     * @param total_price the total price of the ingredient in the order
     */
    public void setTotalPrice(double total_price) {
        this.total_price = total_price;
    }
}
