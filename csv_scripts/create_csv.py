import csv
import random
import datetime as dt

# TODO: write function to add items in order based on meal type (e.g. a bowl will have itself added, plus the entree and side associated with it)
# TODO: write function to generate the transaction/junction table data and save to csv file


# constants
# NOTE: can add more prices as needed, this is just a starting point
MENU_ITEMS = {
    "bowl": 8.30,
    "plate": 9.80,
    "bigger_plate": 11.30,
    "appetizer": 2.00,
    "entree_a_la_carte": 5.20,
    "appetizer_a_la_carte": 4.40,
    "drink": 2.10
}

# NOTE: subject to change based on actual menu_item table
MENU_ITEM_IDS = {
    "bowl": 1,
    "plate": 2,
    "bigger_plate": 3,
    "entrees": list(range(4, 10)),
    "sides": list(range(10, 14)),
    "appetizers": list(range(14, 18)),
    "drinks": 18
}

TRANSACTION_HEADER = ["transaction_id", "total_cost", "transaction_time",
                      "transaction_date", "transaction_type", "customer_id", "employee_id"]

MENU_ITEM_HEADER = ["menu_item_id", "current_servings",
                    "item_name", "item_price", "item_category"]

MENU_TRANSACTION_JOIN_HEADER = [
    "menu_item_id", "transaction_id", "item_quantity"]

TRANSACTION_TYPES = ["Maroon Meal",
                     "Dining Dollars", "Credit/Debit", "Gift Card"]

TRANSACTION_COUNT = 65000  # NOTE: not sure if this is needed right now
TOTAL_REVENUE = 750000
CUSTOMER_ID_RANGE = range(1, 101)
EMPLOYEE_ID_RANGE = range(1, 31)
START_DATE = dt.datetime(2024, 1, 1)
END_DATE = dt.datetime(2024, 9, 27)
START_TIME = dt.timedelta(hours=10)
END_TIME = dt.timedelta(hours=22)


# helper functions
def random_time(start_time, end_time):
    """generates a random time between the start and end time"""
    delta = end_time - start_time
    random_seconds = random.randint(0, delta.seconds)
    random_time = start_time + dt.timedelta(seconds=random_seconds)

    # format: HH:MM:SS
    return str(random_time)[:8]


def random_date(start_date, end_date):
    """generates a random date between the start and end date"""
    delta = end_date - start_date
    random_days = random.randint(0, delta.days)
    random_date = start_date + dt.timedelta(days=random_days)

    # format: YYYY-MM-DD
    return str(random_date)[:10]


def get_transaction_type(total_cost):
    """generates a random transaction type"""
    if total_cost <= 9:
        return random.choice(TRANSACTION_TYPES)

    # maroon meals are not available for transactions over $9
    return random.choice(TRANSACTION_TYPES[1:])


def get_transaction_items(meal_type):
    """generates a list of menu items based on the meal type"""
    items = []

    if meal_type == "bowl":
        items.append(MENU_ITEM_IDS["bowl"])
        items.append(random.choice(MENU_ITEM_IDS["entrees"]))
        items.append(random.choice(MENU_ITEM_IDS["sides"]))
    elif meal_type == "plate":
        items.append(MENU_ITEM_IDS["plate"])
        items.append(random.choice(MENU_ITEM_IDS["entrees"]))
        items.append(random.choice(MENU_ITEM_IDS["entrees"]))
        items.append(random.choice(MENU_ITEM_IDS["sides"]))
    elif meal_type == "bigger_plate":
        items.append(MENU_ITEM_IDS["bigger_plate"])
        items.append(random.choice(MENU_ITEM_IDS["entrees"]))
        items.append(random.choice(MENU_ITEM_IDS["entrees"]))
        items.append(random.choice(MENU_ITEM_IDS["entrees"]))
        items.append(random.choice(MENU_ITEM_IDS["sides"]))
    else:
        items.append(MENU_ITEM_IDS[meal_type])

    return items
