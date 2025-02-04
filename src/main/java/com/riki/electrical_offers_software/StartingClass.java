package com.riki.electrical_offers_software;

import javafx.application.Application;

/**
 * The StartingClass serves as an alternative entry point for launching the Electrical Offers Software application.
 * It delegates the launch process to the JavaFX Application class.
 */
public class StartingClass {

    /**
     * The main method starts the JavaFX application by launching the Main class.
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }
}
