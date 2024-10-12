package team4s;

public class MenuItem {
    private int menu_item_id;
    private int current_servings;
    private String item_name;
    private float item_price;
    private String item_category;
    private String ingredients;

    // Constructor
    public MenuItem(int menu_item_id, int current_servings, String item_name, float item_price, String item_category, String ingredients) {
        this.menu_item_id = menu_item_id;
        this.current_servings = current_servings;
        this.item_name = item_name;
        this.item_price = item_price;
        this.item_category = item_category;
        this.ingredients = ingredients;  
    }

    // Getters (method names follow property names)
    public int getMenu_item_id() { return menu_item_id; }
    public int getCurrent_servings() { return current_servings; }
    public String getItem_name() { return item_name; }
    public float getItem_price() { return item_price; }
    public String getItem_category() { return item_category; }
    public String getIngredients() { return ingredients; }

    // Setters
    public void setMenu_item_id(int menu_item_id) { this.menu_item_id = menu_item_id; }
    public void setCurrent_servings(int current_servings) { this.current_servings = current_servings; }
    public void setItem_name(String item_name) { this.item_name = item_name; }
    public void setItem_price(float item_price) { this.item_price = item_price; }
    public void setItem_category(String item_category) { this.item_category = item_category; }
}
