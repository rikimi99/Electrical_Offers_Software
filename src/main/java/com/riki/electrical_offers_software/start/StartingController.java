package com.riki.electrical_offers_software.start;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class StartingController {

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label messageLabel;

    @FXML
    private Label percentageLabel;

    private double progress = 0.0;
    private final Timeline timeline = new Timeline();

    @FXML
    public void initialize() {
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(50), event -> updateProgress()));
        timeline.setCycleCount(100);
        timeline.play();
    }

    /**
     * Updates the progress bar, percentage label, and message label smoothly.
     */
    private void updateProgress() {
        progress += 0.01; // Increment by 1%
        progressBar.setProgress(progress);
        percentageLabel.setText((int) (progress * 100) + "%");
        updateMessageLabel((int) (progress * 100));
        if (progress >= 1.0) {
            timeline.stop();
            loadLoginScreen();
        }
    }

    /**
     * Updates the message label based on the progress percentage.
     * @param percentage The current progress percentage.
     */
    private void updateMessageLabel(int percentage) {
        if (percentage <= 15) {
            messageLabel.setText("Σύνδεση στη βάση δεδομένων...");
        } else if (percentage <= 50) {
            messageLabel.setText("Προετοιμασία του συστήματος...");
        } else if (percentage <= 80) {
            messageLabel.setText("Έλεγχος πληροφοριών...");
        } else {
            messageLabel.setText("Σύνδεση...");
        }
    }

    /**
     * Loads the login screen after progress reaches 100%.
     */
    private void loadLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/riki/electrical_offers_software/login/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) progressBar.getScene().getWindow();
            stage.setScene(new Scene(root, 700, 400));
            stage.setTitle("Electrical Offers Software - Login");
            stage.setResizable(false);
        } catch (IOException e) {
            System.out.println("Error loading login screen: " + e.getMessage());
        }
    }
}
