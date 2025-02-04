CREATE TABLE IF NOT EXISTS users (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     first_name TEXT NOT NULL,
                                     last_name TEXT NOT NULL,
                                     email TEXT UNIQUE NOT NULL,
                                     password TEXT NOT NULL,
                                     phone_number TEXT UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS categories(
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS materials (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         name TEXT NOT NULL,
                                         category_id INTEGER NOT NULL,
                                         FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS offer_pdfs (
                                          id INTEGER PRIMARY KEY AUTOINCREMENT,
                                          last_name TEXT NOT NULL,
                                          pdf BLOB NOT NULL,
                                          file_path TEXT NOT NULL,
                                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                          FOREIGN KEY (last_name) REFERENCES users(last_name) ON DELETE CASCADE
);