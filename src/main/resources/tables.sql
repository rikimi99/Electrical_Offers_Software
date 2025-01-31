CREATE TABLE IF NOT EXISTS users (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     first_name TEXT NOT NULL,
                                     last_name TEXT NOT NULL,
                                     email TEXT UNIQUE NOT NULL,
                                     password TEXT NOT NULL,
                                     phone_number TEXT UNIQUE NOT NULL
);