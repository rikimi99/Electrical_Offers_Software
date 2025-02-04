package com.riki.electrical_offers_software.offers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;

public class MainPageController {

    @FXML
    private AnchorPane contentPage;

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            AnchorPane pane = loader.load();
            AnchorPane.setTopAnchor(pane, 0.0);
            AnchorPane.setBottomAnchor(pane, 0.0);
            AnchorPane.setLeftAnchor(pane, 0.0);
            AnchorPane.setRightAnchor(pane, 0.0);
            contentPage.getChildren().setAll(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onNewOfferClick() {
        loadPage("/com/riki/electrical_offers_software/offers/newOffer.fxml");
    }

    @FXML
    private void onOfferHistoryClick() {
        loadPage("/com/riki/electrical_offers_software/offers/allOffers.fxml");
    }

    @FXML
    private void onMaterialsClick() {
        loadPage("/com/riki/electrical_offers_software/offers/materialsPage.fxml");
    }

    @FXML
    private void onUserClick() {
        loadPage("/com/riki/electrical_offers_software/offers/userPage.fxml");
    }
}
