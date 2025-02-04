package com.riki.electrical_offers_software;

import com.riki.electrical_offers_software.database.DatabaseConfiguration;
import com.riki.electrical_offers_software.offers.materialinsertion.MaterialInsertDB;
import com.riki.electrical_offers_software.users.UserCreation;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        DatabaseConfiguration.connect();
        UserCreation.createAdminUser();
        MaterialInsertDB.insertData();
        launch(args);
    }

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
