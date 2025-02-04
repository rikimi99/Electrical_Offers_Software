package com.riki.electrical_offers_software.offers.materialinsertion;

import com.riki.electrical_offers_software.database.DatabaseConfiguration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MaterialInsertDB {
    private static final String CATEGORIES_FILE = "categories.txt";
    private static final String MATERIALS_FILE = "materials.txt";

    public static void insertData() {
        try (Connection connection = DriverManager.getConnection(DatabaseConfiguration.getDatabaseUrl())) {
            if (connection == null) {
                System.out.println("❌ Αποτυχία σύνδεσης με τη βάση δεδομένων.");
                return;
            }

            Map<Integer, Integer> categoryMap = insertCategories(connection);

            insertMaterials(connection, categoryMap);

            System.out.println("✅ Τα δεδομένα εισήχθησαν με επιτυχία!");
        } catch (Exception e) {
            System.out.println("❌ Σφάλμα κατά την εισαγωγή δεδομένων: " + e.getMessage());
        }
    }

    /**
     * Εισάγει κατηγορίες από το αρχείο στη βάση δεδομένων.
     * @param connection Η σύνδεση με τη βάση δεδομένων
     * @return Χάρτης με τα αναγνωριστικά των κατηγοριών
     * @throws IOException Αν υπάρχει πρόβλημα με το αρχείο
     * @throws SQLException Αν υπάρχει σφάλμα SQL
     */
    private static Map<Integer, Integer> insertCategories(Connection connection) throws IOException, SQLException {
        Map<Integer, Integer> categoryMap = new HashMap<>();
        String query = "INSERT INTO categories (id, name) VALUES (?, ?)";

        try (BufferedReader reader = new BufferedReader(new FileReader(CATEGORIES_FILE));
             PreparedStatement statement = connection.prepareStatement(query)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 2) continue;

                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();

                statement.setInt(1, id);
                statement.setString(2, name);
                statement.executeUpdate();

                categoryMap.put(id, id);
            }
        }
        return categoryMap;
    }

    /**
     * Εισάγει υλικά από το αρχείο στη βάση δεδομένων.
     * @param connection Η σύνδεση με τη βάση δεδομένων
     * @param categoryMap Χάρτης με τις κατηγορίες για έλεγχο εγκυρότητας
     * @throws IOException Αν υπάρχει πρόβλημα με το αρχείο
     * @throws SQLException Αν υπάρχει σφάλμα SQL
     */
    private static void insertMaterials(Connection connection, Map<Integer, Integer> categoryMap) throws IOException, SQLException {
        String query = "INSERT INTO materials (id, name, category_id) VALUES (?, ?, ?)";

        try (BufferedReader reader = new BufferedReader(new FileReader(MATERIALS_FILE));
             PreparedStatement statement = connection.prepareStatement(query)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3) continue;

                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();
                int categoryId = Integer.parseInt(parts[2].trim());

                if (!categoryMap.containsKey(categoryId)) continue;

                statement.setInt(1, id);
                statement.setString(2, name);
                statement.setInt(3, categoryId);
                statement.executeUpdate();
            }
        }
    }
}
