#include <iostream>
#include <fstream>
#include <string>
#include <cstdlib>
#include <ctime>

using namespace std;

int main()
{
    int total_transactions = 65000;
    int total_payment_options = 4;
    int total_order_types = 6;
    int num_of_sides = 2;
    int num_of_entrees = 6;
    int getting_meal = 24;
    int total_quantity = 28;
    int total_months = 9;
    int total_hours = 13;
    int total_minutes = 60;

    srand(time(0));
    
    string transaction_types[] = {"Maroon Meal", "Dining Dollars", "Card", "Gift Card"};
    string order_types[] = {"Bowl", "Plate", "Bigger plate", "Appetizer", "Drink", "Ala cart"};
    string meal_items[] = {"chow mein", "fried rice", "teriyaki chicken", "mushroom chicken", "orange chicken", "broccoli beef", "black pepper chicken", "beijing beef"};
    double meal_costs[] = {8.50, 10, 12.50, 4.50};
    
    int select_meal[] = {0,0,0,0,0,0,1,1,1,1,1,1,1,1,2,2,2,2,2,2,3,3,3,3};
    int quantity_bought[] = {1,1,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,3,3,3,3,3,3,4,4};
    double* costs_of_meal = new double[total_transactions];
    int months[] = {1,2,3,4,5,6,7,8,9};
    int days[] = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31};
    int hours[] = {10,11,12,1,2,3,4,5,6,7,8,9,10};
    int minute[] = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59};

    ofstream transaction_file("transactions.csv");
    transaction_file << "transaction_id, total_cost, transaction_time, transaction_date, transaction_type, customer_id, employee_id";
    for(int i = 1; i < total_transactions; i++){
        int index  = rand() % total_payment_options; 

        transaction_file << i << ", ";
        
        if(index == 0){
            transaction_file << "$8.50" << ", ";
        }
        else{
            index = rand() % getting_meal;
            transaction_file << meal_costs[index] << ","; 
        } 
        index = rand() % total_hours;
        int index2 = rand() % total_minutes;
        if(index == 0){
            transaction_file << hours[index] << ": ";  
        }
        transaction_file << hours[index] << ": ";  
    }
    
    ofstream meal_file("meal-transaction.csv");
    meal_file << "menu_item_id, transaction_id, item_quantity";
    for(int i = 1; i < total_transactions; i++){
        int index  = rand() % total_payment_options; 

        meal_file << i << ", "; 
        meal_file << index << ", "; 
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