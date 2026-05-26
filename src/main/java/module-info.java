module com.example.igirepay_payment_gateway_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.igirepay to javafx.fxml;
    opens com.example.igirepay.lab1.model to javafx.fxml;
    opens com.example.igirepay.lab1.storage to javafx.fxml;
    opens com.example.igirepay.lab2.dao to javafx.fxml;
    opens com.example.igirepay.lab2.daoimpl to javafx.fxml;
    opens com.example.igirepay.lab2.database to javafx.fxml;
    opens com.example.igirepay.lab3.exception to javafx.fxml;
    opens com.example.igirepay.lab3.service to javafx.fxml;
    opens com.example.igirepay.lab3.report to javafx.fxml;
    opens com.example.igirepay.lab3.javafxui to javafx.fxml;
    opens com.example.igirepay.lab3.console to javafx.fxml;

    exports com.example.igirepay;
    exports com.example.igirepay.lab1.model;
    exports com.example.igirepay.lab1.storage;
    exports com.example.igirepay.lab2.dao;
    exports com.example.igirepay.lab2.daoimpl;
    exports com.example.igirepay.lab2.database;
    exports com.example.igirepay.lab3.exception;
    exports com.example.igirepay.lab3.service;
    exports com.example.igirepay.lab3.report;
    exports com.example.igirepay.lab3.javafxui;
    exports com.example.igirepay.lab3.console;
}