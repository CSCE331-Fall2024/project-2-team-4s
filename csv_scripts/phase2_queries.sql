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
