#include <iostream>
#include <fstream>
#include <string>
#include <cstdlib>
#include <ctime>
using namespace std;
int main()
{
    int total_transactions = 65000;
    int total_payment_options = 3;
    int total_order_types = 6;
    int num_of_sides = 2;
    int num_of_entrees = 6;
    srand(time(0));
    string meal_types[] = {"Maroon Meal", "Dining Dollars", "Card"};
    string order_types[] = {"Bowl", "Plate", "Bigger plate", "Appetizer", "Drink", "Ala cart"};
    string meal_items[] = {"chow mein", "fried rice", "teriyaki chicken", "mushroom chicken", "orange chicken", "broccoli beef", "black pepper chicken", "beijing beef"};
    double meal_costs[] = {8.50, 10, 12.50, 4.50};
    int select_meal[] = {0,0,0,0,0,0,1,1,1,1,1,1,1,1,2,2,2,2,2,2,3,3,3,3}
    double* costs_of_meal = new double[total_transactions];

    ofstream transactionFile("transactions.csv");
    transactionFile << "transaction_id, total_cost, transaction_time, transaction_date, transaction_type, customer_id, employee_id";
    for(int i = 1; i < total_transactions; i++){
        int index  = rand() % total_payment_options; 
        transactionFile << i << ", "; 
        transactionFile << index << ", ";  
    }
    
    ofstream mealFile("meal-transaction.csv");
    mealFile << "menu_item_id, transaction_id, item_quantity";
    for(int i = 1; i < total_transactions; i++){
        int index  = rand() % total_payment_options; 
        mealFile << i << ", "; 
        mealFile << index << ", "; 
    }



    ofstream joinFile("meal-transaction.csv");
    //The following for-loop sets up the CSV for the menuitem-transaction link table.
    joinFile << "menu_item_id, transaction_id, item_quantity";
    for(int i = 1; i < total_transactions; i++){
        int index  = rand() % total_payment_options; 
        joinFile << i << ", "; 
        joinFile << index << ", ";  
    }
    return 0;
}