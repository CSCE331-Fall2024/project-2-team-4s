-- 12 queries total, 3 per person, write comment for the intent of each query

-- transactions during a date
SELECT 'The total number of transactions during ' || transaction_date || ' is: ' || COUNT(transaction_id) AS result
FROM transaction
WHERE transaction_date = '2024-01-27'
GROUP BY transaction_date;

-- transactions during a specific hour of a specific month
SELECT 'The total number of transactions during hour ' || EXTRACT(HOUR FROM transaction_time) || ' in month ' || EXTRACT(MONTH FROM transaction_date) || ' is: ' || COUNT(transaction_id) AS result
FROM transaction
WHERE EXTRACT(MONTH FROM transaction_date) = 1
AND EXTRACT(HOUR FROM transaction_time) = 11
GROUP BY EXTRACT(HOUR FROM transaction_time), EXTRACT(MONTH FROM transaction_date);

-- SPECIAL QUERY 1
SELECT 'Week ' || week_number || ' had ' || COUNT(transaction_id) || ' transactions' AS result
FROM transaction
WHERE week_number = 5
GROUP BY week_number;

-- SPECIAL QUERY 2
SELECT 'The total number of transactions during hour ' || EXTRACT(HOUR FROM transaction_time) || ' is: ' || COUNT(transaction_id) || ', totaling $' || SUM(total_cost) AS result
FROM transaction
WHERE EXTRACT(HOUR FROM transaction_time) = 11
GROUP BY EXTRACT(HOUR FROM transaction_time);

-- SPECIAL QUERY 3
SELECT '2024-01-27 has $' || SUM(total_cost) || ' in top sales' AS result
FROM (
    SELECT total_cost
    FROM transaction
    WHERE transaction_date = '2024-01-27'
    ORDER BY total_cost DESC
    LIMIT 10
) AS top_orders;


-- Eshwar Reddy Gadi
-- amount of money each customer has spent
SELECT c.customer_id, c.first_name, c.last_name, SUM(t.total_cost) AS total_spent
FROM customer c
JOIN transaction t ON c.customer_id = t.customer_id
GROUP BY c.customer_id, c.first_name, c.last_name 
ORDER BY c.customer_id;

-- number of transactions each employee has done
SELECT e.employee_id, e.first_name, e.last_name, COUNT(t.transaction_id) AS number_of_transactions
FROM employee e
JOIN transaction t ON e.employee_id = t.employee_id
GROUP BY e.employee_id, e.first_name, e.last_name 
ORDER BY e.employee_id;

-- amount of revenue each item has generated
SELECT mi.menu_item_id, mi.item_name, SUM(mt.item_quantity * mi.item_price) AS total_revenue
FROM menu_item mi
JOIN menu_item_transaction mt ON mi.menu_item_id = mt.menu_item_id
JOIN transaction t ON mt.transaction_id = t.transaction_id 
WHERE mi.menu_item_id BETWEEN 15 AND 22
GROUP BY mi.menu_item_id, mi.item_name;


--Landon Uelsmann
-- number of transactions associated with each payment method and the total revenue for each method 
SELECT transaction.transaction_type, COUNT(transaction.transaction_id) AS transaction_count, SUM(transaction.total_cost) AS total_revenue
FROM transaction
GROUP BY transaction.transaction_type
ORDER BY transaction_count DESC;

-- top 8 used ingredients in all transactions
SELECT CONCAT('The ingredient ', inventory.ingredient_name, ' was used a total of ', SUM(inventory_menu_item.ingredient_amount * menu_item_transaction.item_quantity), ' times.') AS result
FROM inventory_menu_item
JOIN inventory ON inventory_menu_item.ingredient_id = inventory.ingredient_id
JOIN  menu_item_transaction ON inventory_menu_item.menu_item_id = menu_item_transaction.menu_item_id
GROUP BY inventory.ingredient_name
ORDER BY SUM(inventory_menu_item.ingredient_amount * menu_item_transaction.item_quantity) DESC
LIMIT 8;

--Ryan Tran
-- inventory items sorted by closest to being understocked
SELECT i.ingredient_name, i.current_stock, i.min_stock, i.current_stock - i.min_stock AS difference 
FROM inventory AS i 
ORDER BY difference ASC;

-- employee with the most transactions done
SELECT e.employee_id, e.first_name, e.last_name, COUNT(t.transaction_id) AS transaction_count 
FROM employee AS e 
INNER JOIN transaction AS t ON e.employee_id = t.employee_id 
GROUP BY e.employee_id, e.first_name, e.last_name 
ORDER BY transaction_count DESC
LIMIT 1;