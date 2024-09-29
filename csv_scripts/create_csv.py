import csv
import random
import datetime as dt

# TODO: look into using choice data type to give more weight to certain number of items in a transaction or to give weight to none for customer id
# TODO: figure out how to limit transactions to not exceed total revenue by a lot

# constants
# NOTE: can add more prices as needed, this is just a starting point
MENU_ITEMS = {
    "bowl": 8.30,
    "plate": 9.80,
    "bigger_plate": 11.30,
    "alc_entree": 5.20,
    "alc_side": 4.40,
    "appetizer": 2.00,
    "fountain_drink": 2.10,
    "bottled_drink": 3.00
}

# NOTE: subject to change based on actual menu_item table
MENU_ITEM_IDS = {
    "bowl": 17,
    "plate": 18,
    "bigger_plate": 19,
    "appetizer": 20,
    "alc_side": 21,
    "alc_entree": 22,
    "entree": [5, 6, 7, 8, 9, 10],
    "side": [1, 2, 3, 4],
    "appetizers": [11, 12, 13, 14],
    "fountain_drink": 15,
    "bottled_drink": 16
}

MENU_ITEM_WEIGHTS = {
    "bowl": 5,
    "plate": 5,
    "bigger_plate": 3,
    "alc_entree": 1,
    "alc_side": 1,
    "appetizer": 2,
    "fountain_drink": 3,
    "bottled_drink": 1
}

ITEM_QUANTITY_WEIGHTS = {
    "bowl": {1: 16, 2: 3, 3: 1},
    "plate": {1: 20, 2: 4, 3: 1},
    "bigger_plate": {1: 14, 2: 1, 3: 1},
    "alc_entree": {1: 3, 2: 2, 3: 1},
    "alc_side": {1: 2, 2: 1},
    "appetizer": {1: 5, 2: 3, 3: 2},
    "fountain_drink": {1: 13, 2: 1},
    "bottled_drink": {1: 6, 2: 1}
}

CUSTOMER_ID_WEIGHTS = {
    "customer_id": 3,
    "NULL": 5
}

TRANSACTION_HEADER = ["transaction_id", "total_cost", "transaction_time",
                      "transaction_date", "transaction_type", "customer_id", "employee_id"]

MENU_ITEM_HEADER = ["menu_item_id", "current_servings",
                    "item_name", "item_price", "item_category"]

MENU_TRANSACTION_JOIN_HEADER = [
    "menu_item_id", "transaction_id", "item_quantity"]

TRANSACTION_TYPES = ["Maroon Meal",
                     "Dining Dollars", "Credit/Debit", "Gift Card"]

TRANSACTION_COUNT = 65000
TOTAL_REVENUE = 755432
CUSTOMER_ID_RANGE = list(range(1, 1001))
EMPLOYEE_ID_RANGE = list(range(1, 14))  # FIXME: update to exclude chefs
START_DATE = dt.datetime(2024, 1, 1)
END_DATE = dt.datetime(2024, 9, 27)
START_TIME = dt.timedelta(hours=10)
END_TIME = dt.timedelta(hours=22)


# helper functions
def weighted_random_choice(weights_dict):
    """returns a random key from a dictionary based on the weights provided"""
    total_weight = sum(weights_dict.values())
    rand_value = random.uniform(0, total_weight)
    cumulative_weight = 0

    for item, weight in weights_dict.items():
        cumulative_weight += weight

        if rand_value <= cumulative_weight:
            return item

    return list(weights_dict.keys())[0]


def generate_random_time(start_time, end_time):
    """generates a random time between the start and end time"""
    delta = end_time - start_time
    random_seconds = random.randint(0, delta.seconds)
    random_time = start_time + dt.timedelta(seconds=random_seconds)

    # format: HH:MM:SS
    return str(random_time)[:8]


def generate_random_date(start_date, end_date):
    """generates a random date between the start and end date"""
    delta = end_date - start_date
    random_days = random.randint(0, delta.days)
    random_date = start_date + dt.timedelta(days=random_days)

    # format: YYYY-MM-DD
    return str(random_date)[:10]


def generate_transaction_type(total_cost):
    """generates a random transaction type"""
    if total_cost <= 9:
        return random.choice(TRANSACTION_TYPES)

    # maroon meals are not available for transactions over $9
    return random.choice(TRANSACTION_TYPES[1:])


def generate_transaction_items(item_type):
    """generates a list of menu items based on the meal type"""
    items = []

    item_quantity = weighted_random_choice(ITEM_QUANTITY_WEIGHTS[item_type])

    for i in range(item_quantity):
        if item_type == "bowl":
            items.append(MENU_ITEM_IDS["bowl"])
            items.append(random.choice(MENU_ITEM_IDS["entree"]))
            items.append(random.choice(MENU_ITEM_IDS["side"]))
        elif item_type == "plate":
            items.append(MENU_ITEM_IDS["plate"])
            items.append(random.choice(MENU_ITEM_IDS["entree"]))
            items.append(random.choice(MENU_ITEM_IDS["entree"]))
            items.append(random.choice(MENU_ITEM_IDS["side"]))
        elif item_type == "bigger_plate":
            items.append(MENU_ITEM_IDS["bigger_plate"])
            items.append(random.choice(MENU_ITEM_IDS["entree"]))
            items.append(random.choice(MENU_ITEM_IDS["entree"]))
            items.append(random.choice(MENU_ITEM_IDS["entree"]))
            items.append(random.choice(MENU_ITEM_IDS["side"]))
        elif item_type == "alc_entree":
            items.append(MENU_ITEM_IDS["alc_entree"])
            items.append(random.choice(MENU_ITEM_IDS["entree"]))
        elif item_type == "alc_side":
            items.append(MENU_ITEM_IDS["alc_side"])
            items.append(random.choice(MENU_ITEM_IDS["side"]))
        elif item_type == "appetizer":
            items.append(MENU_ITEM_IDS["appetizer"])
            items.append(random.choice(MENU_ITEM_IDS["appetizers"]))
        else:
            items.append(MENU_ITEM_IDS[item_type])

    return items, item_quantity


def generate_transaction_history():
    """generates transaction and menu_item_transaction data for csv files"""
    current_revenue = 0
    transactions = []
    menu_item_transactions = []

    for i in range(1, TRANSACTION_COUNT):
        if current_revenue >= TOTAL_REVENUE:
            break

        date = generate_random_date(START_DATE, END_DATE)
        time = generate_random_time(START_TIME, END_TIME)
        customer_choice = weighted_random_choice(CUSTOMER_ID_WEIGHTS)
        customer_id = random.choice(
            CUSTOMER_ID_RANGE) if customer_choice == "customer_id" else ""
        employee_id = random.choice(EMPLOYEE_ID_RANGE)

        num_items = random.randint(1, 3)
        all_item_ids = []  # item ids for junction table
        all_item_types = []  # types for transaction cost

        # generate transaction items (currently only 1-3 items per transaction)
        for n in range(num_items):
            item_type = weighted_random_choice(MENU_ITEM_WEIGHTS)
            generated_items = generate_transaction_items(item_type)
            item_ids = generated_items[0]

            all_item_ids.extend(item_ids)

            for m in range(generated_items[1]):
                all_item_types.append(item_type)

        total_cost = round(sum([MENU_ITEMS[item]
                           for item in all_item_types]), 2)

        current_revenue += total_cost

        transaction_type = generate_transaction_type(total_cost)

        # format of row: [transaction_id, total_cost, transaction_time, transaction_date, transaction_type, customer_id, employee_id]
        transactions.append([i, total_cost, time,
                             date, transaction_type, customer_id, employee_id])

        # generate junction table data
        for item_id in set(all_item_ids):
            # format of row: [menu_item_id, transaction_id, item_quantity]
            menu_item_transactions.append(
                [item_id, i, all_item_ids.count(item_id)])

    return (transactions, menu_item_transactions)


if __name__ == "__main__":
    transaction_history = generate_transaction_history()
    # write to transaction.csv
    with open("csv_scripts/transaction.csv", "w", newline='') as f:
        writer = csv.writer(f)
        writer.writerow(TRANSACTION_HEADER)
        transactions = transaction_history[0]
        writer.writerows(transactions)

    # write to menu_item_transaction.csv
    with open("csv_scripts/menu_item_transaction.csv", "w", newline='') as f:
        writer = csv.writer(f)
        writer.writerow(MENU_TRANSACTION_JOIN_HEADER)
        menu_item_transactions = transaction_history[1]
        writer.writerows(menu_item_transactions)
