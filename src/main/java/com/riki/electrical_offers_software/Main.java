package com.riki.electrical_offers_software;

import com.riki.electrical_offers_software.database.DatabaseConfiguration;
import com.riki.electrical_offers_software.offers.materialinsertion.MaterialInsertDB;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * The Main class serves as the entry point for the Electrical Offers Software application.
 * It initializes the database connection, inserts initial data, and launches the JavaFX UI.
 */
public class Main extends Application {

    /**
     * The main method is the entry point of the application.
     * It establishes a connection to the database, inserts initial data, and starts the JavaFX application.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        DatabaseConfiguration.connect();
        MaterialInsertDB.insertData();
        launch(args);
    }

    /**
     * The start method initializes and displays the primary stage of the JavaFX application.
     *
     * @param primaryStage The primary stage of the JavaFX application.
     * @throws IOException If the FXML file cannot be loaded.
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/riki/electrical_offers_software/start/start.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700, 400);
        primaryStage.setTitle("Electrical Offers Software - Login");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
