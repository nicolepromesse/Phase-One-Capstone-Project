module com.example.igirepay_payment_gateway_project {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    requires java.sql;

    opens com.example.igirepay_payment_gateway_project to javafx.fxml;
    exports com.example.igirepay;
}