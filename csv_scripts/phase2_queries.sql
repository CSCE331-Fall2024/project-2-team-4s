-- 12 queries total, 3 per person, write comment for the intent of each query-- transactions during a date
SELECT 'The total amount of transactions during ' || MAX(transaction_date) || ' is: ' || COUNT(*) AS result
FROM transaction
WHERE transaction_date = '2024-01-27';
-- Transaction during a specific hour during a specific month
SELECT 
    'The total amount of transactions during hour ' || EXTRACT(HOUR FROM transaction_time) || 
    ' in month ' || EXTRACT(MONTH FROM transaction_date) || 
    ' is: ' || COUNT(*) AS result
FROM transaction
WHERE EXTRACT(MONTH FROM transaction_date) = 1
AND EXTRACT(HOUR FROM transaction_time) = 11
GROUP BY EXTRACT(HOUR FROM transaction_time), EXTRACT(MONTH FROM transaction_date);
-- SPECIAL QUERY 1
SELECT 'Week ' || MAX(week_number) || ' is: ' || COUNT(*) AS result
FROM transaction
WHERE week_number = 5;
-- SPECIAL QUERY 2
SELECT 
    'The total amount of transactions during hour ' || EXTRACT(HOUR FROM transaction_time) || 
    ' is: ' || COUNT(*) || ', totaling $' || SUM(total_cost) AS result
FROM transaction
WHERE EXTRACT(HOUR FROM transaction_time) = 11
GROUP BY EXTRACT(HOUR FROM transaction_time);
-- SPECIAL QUERY 3
SELECT 
    '2024-01-27 has $' || SUM(total_cost) AS result
FROM (
    SELECT total_cost
    FROM transaction
    WHERE transaction_date = '2024-01-27'
    ORDER BY total_cost DESC
    LIMIT 10
) AS top_orders;


-- Eshwar Reddy Gadi
-- Amount of money each customer has spent:
SELECT c.Customer_ID, c.First_Name, c.Last_Name, SUM(t.Total_Cost) AS TotalSpent
FROM Customer c
JOIN Transaction t ON c.Customer_ID = t.Customer_ID
GROUP BY c.Customer_ID, c.First_Name, c.Last_Name ORDER BY c.Customer_ID;

-- Number of Transactions each employee has done:
SELECT e.Employee_ID, e.First_Name, e.Last_Name, COUNT(t.Transaction_ID) AS Number_Of_Transactions
FROM Employee e
JOIN Transaction t ON e.Employee_ID = t.Employee_ID
GROUP BY e.Employee_ID, e.First_Name, e.Last_Name ORDER BY e.Employee_ID;

-- Amount of Revenue Each Item has generated:
SELECT mi.Menu_Item_ID, mi.Item_Name, SUM(mt.Item_Quantity * mi.Item_Price) AS Total_Revenue
FROM Menu_Item mi
JOIN Menu_Item_Transaction mt ON mi.Menu_Item_ID = mt.Menu_Item_ID
JOIN Transaction t ON mt.Transaction_ID = t.Transaction_ID WHERE mi.Menu_Item_ID BETWEEN 15 AND 22
GROUP BY mi.Menu_Item_ID, mi.Item_Name;