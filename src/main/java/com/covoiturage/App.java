package com.covoiturage;

import com.covoiturage.db.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Application JavaFX de Covoiturage.
 * Point d'entrée principal.
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // Initialize database on startup
        DatabaseManager.getInstance();

        scene = new Scene(loadFXML("login"), 960, 650);
        stage.setTitle("CovoiturApp — Plateforme de Covoiturage");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    @Override
    public void stop() {
        // Close database connection on exit
        DatabaseManager.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch();
    }
}