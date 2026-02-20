package io.github.bmb0136.maestro;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        URL resource = Objects.requireNonNull(getClass().getResource("/MainWindow.fxml"));
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root, 1280, 720);
        stage.setScene(scene);
        stage.setTitle("Maestro");
        stage.show();
    }
}
