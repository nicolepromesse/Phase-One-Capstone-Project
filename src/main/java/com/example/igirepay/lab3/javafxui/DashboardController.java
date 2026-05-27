package com.example.igirepay.lab3.javafxui;

import com.example.igirepay.lab1.model.*;
import com.example.igirepay.lab3.exception.*;
import com.example.igirepay.lab3.report.TransactionReportGenerator;
import com.example.igirepay.lab3.service.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private StackPane contentPane;

    private Customer currentCustomer;
    private final CustomerService customerService = new CustomerService();
    private final AccountService accountService = new AccountService();
    private final TransactionService transactionService = new TransactionService();
    private final TransactionReportGenerator reportGen = new TransactionReportGenerator();

    public void initData(Customer customer) {
        this.currentCustomer = customer;
        welcomeLabel.setText("Welcome, " + customer.getFullName());
        showDashboard();
    }

    @FXML private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/igirepay_payment_gateway_project/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 600));
            stage.setTitle("IgirePay — Login");
        } catch (Exception e) { showError("Logout error: " + e.getMessage()); }
    }

    // ── Navigation

    @FXML private void showDashboard()    { contentPane.getChildren().setAll(buildDashboardPanel()); }
    @FXML private void showCustomers()    { contentPane.getChildren().setAll(buildCustomersPanel()); }
    @FXML private void showAccounts()     { contentPane.getChildren().setAll(buildAccountsPanel()); }
    @FXML private void showTransactions() { contentPane.getChildren().setAll(buildTransactionsPanel()); }
    @FXML private void showReports()      { contentPane.getChildren().setAll(buildReportsPanel()); }
    @FXML private void showChangePin()    { contentPane.getChildren().setAll(buildChangePinPanel()); }

    // ── DASHBOARD PANEL ───────────────────────────────────────────

    private VBox buildDashboardPanel() {
        VBox root = new VBox(24);

        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");

        // Stat cards row
        HBox stats = new HBox(16);
        stats.setAlignment(Pos.CENTER_LEFT);

        try {
            List<Account> accounts = accountService.getAll().stream()
                .filter(a -> a.getCustomer().getId() == currentCustomer.getId())
                .toList();

            double totalBalance = accounts.stream().mapToDouble(Account::getBalance).sum();
            long txCount = transactionService.getAll().stream()
                .filter(t -> accounts.stream().anyMatch(a -> a.getId() == t.getAccount().getId()))
                .count();

            stats.getChildren().addAll(
                buildStatCard("💰 Total Balance", String.format("%.2f RWF", totalBalance), "#f0fdf4", "#16a34a"),
                buildStatCard("🏦 Accounts", String.valueOf(accounts.size()), "#f0f9ff", "#0369a1"),
                buildStatCard("📋 Transactions", String.valueOf(txCount), "#fefce8", "#ca8a04")
            );
        } catch (SQLException e) {
            stats.getChildren().add(new Label("Error loading stats."));
        }

        // Recent transactions
        Label recentTitle = new Label("Recent Transactions");
        recentTitle.getStyleClass().add("section-title");

        VBox recentBox = buildTransactionsTable(true);

        root.getChildren().addAll(title, stats, recentTitle, recentBox);
        return root;
    }

    private VBox buildStatCard(String label, String value, String bg, String accent) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setPrefWidth(220);
        card.setStyle("-fx-background-color:" + bg + ";" +
                      "-fx-background-radius:12;" +
                      "-fx-border-color:" + accent + "20;" +
                      "-fx-border-radius:12;" +
                      "-fx-border-width:1;");

        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");
        Label val = new Label(value);
        val.getStyleClass().add("stat-value");
        val.setStyle("-fx-text-fill:" + accent + ";");
        card.getChildren().addAll(lbl, val);
        return card;
    }


    private VBox buildCustomersPanel() {
        VBox root = new VBox(16);

        Label title = new Label("Customer Management");
        title.getStyleClass().add("page-title");

        // Register form
        TitledPane regPane = new TitledPane("Register New Customer", buildRegisterForm());
        regPane.setCollapsible(true);
        regPane.setExpanded(false);
        regPane.setStyle("-fx-background-color:white; -fx-background-radius:10;");

        // Customer table
        TableView<Customer> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPrefHeight(320);

        TableColumn<Customer, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        colId.setPrefWidth(60);

        TableColumn<Customer, String> colName = new TableColumn<>("Full Name");
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        colName.setPrefWidth(200);

        TableColumn<Customer, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colEmail.setPrefWidth(220);

        TableColumn<Customer, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhoneNumber()));
        colPhone.setPrefWidth(150);

        table.getColumns().addAll(colId, colName, colEmail, colPhone);

        Button refreshBtn = new Button("⟳ Refresh");
        refreshBtn.getStyleClass().add("btn-secondary");

        Runnable loadCustomers = () -> {
            try {
                table.setItems(FXCollections.observableArrayList(customerService.getAll()));
            } catch (SQLException e) { showError(e.getMessage()); }
        };
        loadCustomers.run();
        refreshBtn.setOnAction(e -> loadCustomers.run());

        root.getChildren().addAll(title, regPane, refreshBtn, table);
        return root;
    }

    private VBox buildRegisterForm() {
        VBox form = new VBox(10);
        form.setPadding(new Insets(16));

        TextField nameF  = field("Full Name");
        TextField emailF = field("Email");
        TextField phoneF = field("Phone Number");
        PasswordField pinF = new PasswordField(); pinF.setPromptText("PIN (4–6 digits)"); pinF.getStyleClass().add("input-field");

        Label msg = new Label(); msg.getStyleClass().add("success-label");

        Button save = new Button("Register Customer"); save.getStyleClass().add("btn-primary");
        save.setOnAction(e -> {
            try {
                if (!pinF.getText().matches("\\d{4,6}")) { msg.setStyle("-fx-text-fill:#dc2626;"); msg.setText("PIN must be 4–6 digits."); return; }
                customerService.register(new Customer(0, nameF.getText(), emailF.getText(), phoneF.getText(), pinF.getText()));
                msg.setStyle("-fx-text-fill:#16a34a;"); msg.setText("✓ Customer registered successfully.");
                nameF.clear(); emailF.clear(); phoneF.clear(); pinF.clear();
            } catch (SQLException ex) { msg.setStyle("-fx-text-fill:#dc2626;"); msg.setText("Error: " + ex.getMessage()); }
        });

        form.getChildren().addAll(
            label("Full Name"), nameF,
            label("Email"), emailF,
            label("Phone"), phoneF,
            label("PIN"), pinF,
            save, msg
        );
        return form;
    }


    private VBox buildAccountsPanel() {
        VBox root = new VBox(16);

        Label title = new Label("Account Management");
        title.getStyleClass().add("page-title");

        // Create account form
        TitledPane createPane = new TitledPane("Open New Account", buildCreateAccountForm());
        createPane.setCollapsible(true); createPane.setExpanded(false);

        // Accounts table
        TableView<Account> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPrefHeight(300);

        TableColumn<Account, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        colId.setPrefWidth(60);

        TableColumn<Account, Integer> colCust = new TableColumn<>("Customer ID");
        colCust.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCustomer().getId()).asObject());
        colCust.setPrefWidth(100);

        TableColumn<Account, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAccountType()));
        colType.setPrefWidth(100);

        TableColumn<Account, Double> colBal = new TableColumn<>("Balance (RWF)");
        colBal.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getBalance()).asObject());
        colBal.setPrefWidth(160);

        table.getColumns().addAll(colId, colCust, colType, colBal);

        Button refreshBtn = new Button("⟳ Refresh"); refreshBtn.getStyleClass().add("btn-secondary");
        Runnable load = () -> {
            try { table.setItems(FXCollections.observableArrayList(accountService.getAll())); }
            catch (SQLException e) { showError(e.getMessage()); }
        };
        load.run();
        refreshBtn.setOnAction(e -> load.run());

        root.getChildren().addAll(title, createPane, refreshBtn, table);
        return root;
    }

    private VBox buildCreateAccountForm() {
        VBox form = new VBox(10);
        form.setPadding(new Insets(16));

        TextField custIdF = field("Customer ID");
        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton walletRb  = new RadioButton("Wallet Account");  walletRb.setToggleGroup(typeGroup); walletRb.setSelected(true);
        RadioButton savingsRb = new RadioButton("Savings Account"); savingsRb.setToggleGroup(typeGroup);
        HBox typeRow = new HBox(16, walletRb, savingsRb);

        TextField balF   = field("Initial Balance (RWF)");
        TextField extraF = field("Daily Limit / Interest Rate");
        TextField extra2F = field("Fee / Minimum Balance");

        Label msg = new Label(); msg.getStyleClass().add("success-label");
        Button create = new Button("Create Account"); create.getStyleClass().add("btn-primary");

        create.setOnAction(e -> {
            try {
                int cid = Integer.parseInt(custIdF.getText().trim());
                Customer c = customerService.getById(cid);
                if (c == null) { msg.setStyle("-fx-text-fill:#dc2626;"); msg.setText("Customer not found."); return; }
                double bal = Double.parseDouble(balF.getText().trim());

                if (walletRb.isSelected()) {
                    double limit = Double.parseDouble(extraF.getText().trim());
                    double fee = Double.parseDouble(extra2F.getText().trim());
                    accountService.createAccount(new WalletAccount(0, c, bal, LocalDateTime.now(), limit, fee, true, 0, null));
                } else {
                    double rate = Double.parseDouble(extraF.getText().trim());
                    double minBal = Double.parseDouble(extra2F.getText().trim());
                    accountService.createAccount(new SavingsAccount(0, c, "SAVINGS", bal, LocalDateTime.now(), rate, minBal, 5, 0, null));
                }
                msg.setStyle("-fx-text-fill:#16a34a;"); msg.setText("✓ Account created.");
                custIdF.clear(); balF.clear(); extraF.clear(); extra2F.clear();
            } catch (Exception ex) { msg.setStyle("-fx-text-fill:#dc2626;"); msg.setText("Error: " + ex.getMessage()); }
        });

        form.getChildren().addAll(
            label("Customer ID"), custIdF,
            label("Account Type"), typeRow,
            label("Initial Balance"), balF,
            label("Daily Limit / Interest Rate"), extraF,
            label("Transaction Fee / Min Balance"), extra2F,
            create, msg
        );
        return form;
    }


    private VBox buildTransactionsPanel() {
        VBox root = new VBox(16);

        Label title = new Label("Transaction Management");
        title.getStyleClass().add("page-title");

        // Action cards row
        HBox actions = new HBox(12);

        VBox depCard  = buildActionCard("Deposit",  "➕", "#f0fdf4", "#16a34a", false);
        VBox witCard  = buildActionCard("Withdraw", "➖", "#fff7ed", "#ea580c", false);
        VBox trfCard  = buildActionCard("Transfer", "⇄",  "#f0f9ff", "#0369a1", true);
        actions.getChildren().addAll(depCard, witCard, trfCard);

        // All transactions table
        Label histTitle = new Label("All Transactions"); histTitle.getStyleClass().add("section-title");
        VBox tableBox = buildTransactionsTable(false);

        root.getChildren().addAll(title, actions, histTitle, tableBox);
        return root;
    }

    private VBox buildActionCard(String type, String icon, String bg, String accent, boolean hasTwo) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setMinWidth(260);
        card.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:#e5e7eb; -fx-border-radius:12;");

        Label ic = new Label(icon); ic.setStyle("-fx-font-size:24px;");
        Label lbl = new Label(type + " Money"); lbl.getStyleClass().add("section-title");

        TextField acctF = field("Account ID"); acctF.setPrefWidth(200);
        TextField amtF  = field("Amount (RWF)"); amtF.setPrefWidth(200);
        TextField toF   = field("To Account ID"); toF.setPrefWidth(200); toF.setVisible(hasTwo); toF.setManaged(hasTwo);

        Label msg = new Label(); msg.getStyleClass().add("success-label"); msg.setWrapText(true); msg.setMaxWidth(220);

        Button btn = new Button(type); btn.getStyleClass().add("btn-primary"); btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color:" + accent + ";");

        btn.setOnAction(e -> {
            try {
                int aid = Integer.parseInt(acctF.getText().trim());
                double amt = Double.parseDouble(amtF.getText().trim());
                String ref;
                if ("Deposit".equals(type))  ref = transactionService.deposit(aid, amt);
                else if ("Withdraw".equals(type)) ref = transactionService.withdraw(aid, amt);
                else { int to = Integer.parseInt(toF.getText().trim()); ref = transactionService.transfer(aid, to, amt); }
                msg.setStyle("-fx-text-fill:#16a34a;"); msg.setText("✓ Done! Ref: " + ref.substring(0, 8) + "…");
                acctF.clear(); amtF.clear(); if (hasTwo) toF.clear();
            } catch (InsufficientBalanceException | InvalidAmountException | AccountNotFoundException ex) {
                msg.setStyle("-fx-text-fill:#dc2626;"); msg.setText("✗ " + ex.getMessage());
            } catch (Exception ex) { msg.setStyle("-fx-text-fill:#dc2626;"); msg.setText("✗ Error: " + ex.getMessage()); }
        });

        card.getChildren().addAll(ic, lbl, label("Account ID"), acctF);
        if (hasTwo) card.getChildren().addAll(label("To Account"), toF);
        card.getChildren().addAll(label("Amount"), amtF, btn, msg);
        return card;
    }

    private VBox buildTransactionsTable(boolean limitRows) {
        TableView<Transaction> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPrefHeight(limitRows ? 200 : 320);

        TableColumn<Transaction, Integer> cId = new TableColumn<>("ID");
        cId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        cId.setPrefWidth(60);

        TableColumn<Transaction, Integer> cAcc = new TableColumn<>("Account");
        cAcc.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getAccount().getId()).asObject());
        cAcc.setPrefWidth(80);

        TableColumn<Transaction, String> cType = new TableColumn<>("Type");
        cType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTransactionType()));
        cType.setPrefWidth(130);

        TableColumn<Transaction, Double> cAmt = new TableColumn<>("Amount (RWF)");
        cAmt.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getAmount()).asObject());
        cAmt.setPrefWidth(140);

        TableColumn<Transaction, String> cTime = new TableColumn<>("Timestamp");
        cTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTimestamp().toString()));
        cTime.setPrefWidth(180);

        table.getColumns().addAll(cId, cAcc, cType, cAmt, cTime);

        try {
            List<Transaction> all = transactionService.getAll();
            if (limitRows && all.size() > 5) all = all.subList(0, 5);
            table.setItems(FXCollections.observableArrayList(all));
        } catch (SQLException e) { showError(e.getMessage()); }

        VBox box = new VBox(table);
        return box;
    }

    private VBox buildReportsPanel() {
        VBox root = new VBox(16);

        Label title = new Label("Reports & Exports");
        title.getStyleClass().add("page-title");

        TextArea output = new TextArea();
        output.getStyleClass().add("text-area");
        output.setPrefHeight(400);
        output.setEditable(false);
        output.setWrapText(true);

        Button dailyBtn = new Button("📅 Daily Summary"); dailyBtn.getStyleClass().add("btn-secondary");
        TextField acctStatF = field("Account ID");
        Button acctBtn = new Button("📄 Account Statement"); acctBtn.getStyleClass().add("btn-secondary");
        Button exportBtn = new Button("⬇ Export CSV"); exportBtn.getStyleClass().add("btn-primary");

        dailyBtn.setOnAction(e -> {
            try { output.setText(reportGen.getDailySummary()); }
            catch (SQLException ex) { output.setText("Error: " + ex.getMessage()); }
        });

        acctBtn.setOnAction(e -> {
            try {
                int id = Integer.parseInt(acctStatF.getText().trim());
                output.setText(reportGen.getAccountStatement(id));
            } catch (Exception ex) { output.setText("Error: " + ex.getMessage()); }
        });

        exportBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Save CSV Export");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fc.setInitialFileName("transactions_export.csv");
            File file = fc.showSaveDialog(contentPane.getScene().getWindow());
            if (file != null) {
                try {
                    reportGen.exportAllToCSV(file.getAbsolutePath());
                    output.setText("✓ Exported to: " + file.getAbsolutePath());
                } catch (Exception ex) { output.setText("Export error: " + ex.getMessage()); }
            }
        });

        HBox btnRow = new HBox(12, dailyBtn,
            new HBox(6, acctStatF, acctBtn),
            exportBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        root.getChildren().addAll(title, btnRow, output);
        return root;
    }

    private VBox buildChangePinPanel() {
        VBox root = new VBox(16);
        root.setMaxWidth(420);

        Label title = new Label("Change PIN");
        title.getStyleClass().add("page-title");

        VBox form = new VBox(10);
        form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-border-color:#e5e7eb; -fx-border-radius:12;");

        PasswordField oldPin = new PasswordField(); oldPin.setPromptText("Current PIN"); oldPin.getStyleClass().add("input-field");
        PasswordField newPin = new PasswordField(); newPin.setPromptText("New PIN (4–6 digits)"); newPin.getStyleClass().add("input-field");
        PasswordField confPin = new PasswordField(); confPin.setPromptText("Confirm New PIN"); confPin.getStyleClass().add("input-field");

        Label msg = new Label(); msg.setWrapText(true);

        Button save = new Button("Update PIN"); save.getStyleClass().add("btn-primary");
        save.setOnAction(e -> {
            if (!newPin.getText().equals(confPin.getText())) {
                msg.setStyle("-fx-text-fill:#dc2626;"); msg.setText("New PINs do not match.");
                return;
            }
            if (!newPin.getText().matches("\\d{4,6}")) {
                msg.setStyle("-fx-text-fill:#dc2626;"); msg.setText("PIN must be 4–6 digits.");
                return;
            }
            try {
                customerService.changePin(currentCustomer, oldPin.getText(), newPin.getText());
                msg.setStyle("-fx-text-fill:#16a34a;"); msg.setText("✓ PIN updated successfully.");
                oldPin.clear(); newPin.clear(); confPin.clear();
            } catch (InvalidPinException | AccountLockedException ex) {
                msg.setStyle("-fx-text-fill:#dc2626;"); msg.setText("✗ " + ex.getMessage());
            } catch (SQLException ex) {
                msg.setStyle("-fx-text-fill:#dc2626;"); msg.setText("Error: " + ex.getMessage());
            }
        });

        form.getChildren().addAll(
            label("Current PIN"), oldPin,
            label("New PIN"), newPin,
            label("Confirm New PIN"), confPin,
            save, msg
        );

        root.getChildren().addAll(title, form);
        return root;
    }


    private TextField field(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("input-field");
        return tf;
    }

    private Label label(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("field-label");
        return l;
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle("Error");
        a.showAndWait();
    }
}
