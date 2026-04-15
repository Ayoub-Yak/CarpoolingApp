module com.covoiturage {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.covoiturage to javafx.fxml;
    opens com.covoiturage.controller to javafx.fxml;
    opens com.covoiturage.model to javafx.fxml;
    opens com.covoiturage.model.enums to javafx.fxml;

    exports com.covoiturage;
    exports com.covoiturage.model;
    exports com.covoiturage.model.enums;
    exports com.covoiturage.db;
    exports com.covoiturage.dao;
    exports com.covoiturage.service;
    exports com.covoiturage.controller;
}
