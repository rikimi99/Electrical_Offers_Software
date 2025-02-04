module com.riki.electrical_offers_software {
    requires javafx.fxml;
    requires javafx.web;

    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires jbcrypt;
    requires itextpdf;
    requires com.dlsc.pdfviewfx;
    requires io.github.cdimascio.dotenv.java;
    requires java.mail;

    opens com.riki.electrical_offers_software to javafx.fxml;
    exports com.riki.electrical_offers_software;
    exports com.riki.electrical_offers_software.database;
    opens com.riki.electrical_offers_software.database to javafx.fxml;
    exports com.riki.electrical_offers_software.login;
    opens com.riki.electrical_offers_software.login to javafx.fxml;
    exports com.riki.electrical_offers_software.start;
    opens com.riki.electrical_offers_software.start;
    exports com.riki.electrical_offers_software.offers to javafx.fxml;
    opens com.riki.electrical_offers_software.offers;
}