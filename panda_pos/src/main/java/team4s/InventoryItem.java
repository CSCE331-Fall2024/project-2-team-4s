package team4s;

public class InventoryItem {
    private int ingredient_id;
    private String ingredient_name;
    private int current_stock;
    private double price;
    private String unit;
    private int min_stock;
    private boolean selected; // To store whether the item is selected
    private int amount; // To store the chosen amount

    /**
     * Constructs an InventoryItem with the specified details.
     *
     * @param ingredient_id   the unique ID of the ingredient
     * @param ingredient_name the name of the ingredient
     * @param current_stock   the current stock of the ingredient
     * @param price           the price of the ingredient
     * @param unit            the unit of measurement for the ingredient
     * @param min_stock       the minimum stock level for the ingredient
     */
    public InventoryItem(int ingredient_id, String ingredient_name, int current_stock, double price, String unit,
            int min_stock) {
        this.ingredient_id = ingredient_id;
        this.ingredient_name = ingredient_name;
        this.current_stock = current_stock;
        this.price = price;
        this.unit = unit;
        this.min_stock = min_stock;
        this.selected = false; // Default not selected
        this.amount = 1; // Default amount 1
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
     * Gets the current stock of the ingredient.
     *
     * @return current_stock the current stock of the ingredient
     */
    public int getCurrentStock() {
        return current_stock;
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
     * Gets the unit of measurement for the ingredient.
     *
     * @return unit the unit of measurement for the ingredient
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Gets the minimum stock level for the ingredient.
     *
     * @return min_stock the minimum stock level for the ingredient
     */
    public int getMinStock() {
        return min_stock;
    }

    /**
     * Gets whether the ingredient is selected.
     * 
     * @return selected whether the ingredient is selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Gets the amount of the ingredient used.
     * 
     * @return amount the amount of the ingredient used
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Sets the ID of the ingredient.
     *
     * @param ingredient_id the new ID of the ingredient
     */
    public void setIngredientID(int ingredient_id) {
        this.ingredient_id = ingredient_id;
    }

    /**
     * Sets the name of the ingredient.
     *
     * @param ingredient_name the new name of the ingredient
     */
    public void setIngredientName(String ingredient_name) {
        this.ingredient_name = ingredient_name;
    }

    /**
     * Sets the current stock of the ingredient.
     *
     * @param current_stock the new current stock of the ingredient
     */
    public void setCurrentStock(int current_stock) {
        this.current_stock = current_stock;
    }

    /**
     * Sets the price of the ingredient.
     *
     * @param price the new price of the ingredient
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Sets the unit of measurement for the ingredient.
     *
     * @param unit the new unit of measurement for the ingredient
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Sets the minimum stock level for the ingredient.
     *
     * @param min_stock the new minimum stock level for the ingredient
     */
    public void setMinStock(int min_stock) {
        this.min_stock = min_stock;
    }

    /**
     * Sets whether the ingredient is selected.
     * 
     * @param selected whether the ingredient is selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Sets the amount of the ingredient used.
     * 
     * @param amount the amount of the ingredient used
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }
}
