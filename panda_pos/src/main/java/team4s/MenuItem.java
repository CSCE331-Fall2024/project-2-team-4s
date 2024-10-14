package team4s;

public class MenuItem {
    private int menu_item_id;
    private int current_servings;
    private String item_name;
    private float item_price;
    private String item_category;
    private String ingredients;

    /**
     * Constructs a MenuItem with the specified details.
     * 
     * @param menu_item_id     the unique ID of the menu item
     * @param current_servings the current servings of the menu item
     * @param item_name        the name of the menu item
     * @param item_price       the price of the menu item
     * @param item_category    the category of the menu item
     * @param ingredients      the ingredients of the menu item
     */
    public MenuItem(int menu_item_id, int current_servings, String item_name, float item_price, String item_category,
            String ingredients) {
        this.menu_item_id = menu_item_id;
        this.current_servings = current_servings;
        this.item_name = item_name;
        this.item_price = item_price;
        this.item_category = item_category;
        this.ingredients = ingredients;
    }

    /**
     * Gets the ID of the menu item.
     * 
     * @return menu_item_id the ID of the menu item
     */
    public int getMenuItemID() {
        return menu_item_id;
    }

    /**
     * Gets the current servings of the menu item.
     * 
     * @return current_servings the current number of servings for the menu item
     */
    public int getCurrentServings() {
        return current_servings;
    }

    /**
     * Gets the name of the menu item.
     * 
     * @return item_name the name of the menu item
     */
    public String getItemName() {
        return item_name;
    }

    /**
     * Gets the price of the menu item.
     * 
     * @return item_price the price of the menu item
     */
    public float getItemPrice() {
        return item_price;
    }

    /**
     * Gets the category of the menu item.
     * 
     * @return item_category the category of the menu item
     */
    public String getItemCategory() {
        return item_category;
    }

    /**
     * Gets the ingredients of the menu item.
     * 
     * @return ingredients the ingredients of the menu item
     */
    public String getIngredients() {
        return ingredients;
    }

    /**
     * Sets the ID of the menu item.
     * 
     * @param menu_item_id the new ID of the menu item
     */
    public void setMenuItemID(int menu_item_id) {
        this.menu_item_id = menu_item_id;
    }

    /**
     * Sets the current servings of the menu item.
     * 
     * @param current_servings the new current number of servings for the menu item
     */
    public void setCurrentServings(int current_servings) {
        this.current_servings = current_servings;
    }

    /**
     * Sets the name of the menu item.
     * 
     * @param item_name the new name of the menu item
     */
    public void setItemName(String item_name) {
        this.item_name = item_name;
    }

    /**
     * Sets the price of the menu item.
     * 
     * @param item_price the new price of the menu item
     */
    public void setItemPrice(float item_price) {
        this.item_price = item_price;
    }

    /**
     * Sets the category of the menu item.
     * 
     * @param item_category the new category of the menu item
     */
    public void setItemCategory(String item_category) {
        this.item_category = item_category;
    }
}
