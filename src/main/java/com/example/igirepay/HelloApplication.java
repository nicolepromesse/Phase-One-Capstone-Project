package com.example.igirepay;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/example/igirepay_payment_gateway_project/login-view.fxml")
        );
        Parent root = loader.load();
        Scene scene = new Scene(root, 500, 600);

        stage.setTitle("IgirePay — Digital Wallet Gateway");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
