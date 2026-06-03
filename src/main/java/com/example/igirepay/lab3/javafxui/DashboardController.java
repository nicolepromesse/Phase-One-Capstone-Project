package com.example.igirepay.lab3.javafxui;

import com.example.igirepay.lab1.model.*;
import com.example.igirepay.lab3.exception.*;
import com.example.igirepay.lab3.report.TransactionReportGenerator;
import com.example.igirepay.lab3.service.*;
import com.example.igirepay.lab1.model.Loan;
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
    private final AccountService accountService   = new AccountService();
    private final TransactionService transactionService = new TransactionService();
    private final TransactionReportGenerator reportGen  = new TransactionReportGenerator();
    private final LoanService loanService = new LoanService();

    public void initData(Customer customer) {
        this.currentCustomer = customer;
        welcomeLabel.setText("Welcome, " + customer.getFullName());
        showDashboard();
        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setMaximized(true);
        });
    }

    @FXML private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/igirepay_payment_gateway_project/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setMaximized(false);
            stage.setScene(new Scene(root, 500, 600));
            stage.setTitle("IgirePay — Login");
        } catch (Exception e) { showError("Logout error: " + e.getMessage()); }
    }

    @FXML private void showDashboard()    { contentPane.getChildren().setAll(buildDashboardPanel()); }
    @FXML private void showCustomers()    { contentPane.getChildren().setAll(buildCustomersPanel()); }
    @FXML private void showAccounts()     { contentPane.getChildren().setAll(buildAccountsPanel()); }
    @FXML private void showDeposit()       { contentPane.getChildren().setAll(buildSingleTransactionPanel("Deposit",  "➕", "#f0fdf4", "#16a34a", false)); }
    @FXML private void showWithdraw()      { contentPane.getChildren().setAll(buildSingleTransactionPanel("Withdraw", "➖", "#fff7ed", "#ea580c", false)); }
    @FXML private void showTransfer()      { contentPane.getChildren().setAll(buildSingleTransactionPanel("Transfer", "⇄",  "#f0f9ff", "#0369a1", true)); }
    @FXML private void showTransactions()  { contentPane.getChildren().setAll(buildTransactionsPanel()); }
    @FXML private void showDailySummary()  { contentPane.getChildren().setAll(buildDailySummaryPanel()); }
    @FXML private void showStatement()     { contentPane.getChildren().setAll(buildStatementPanel()); }
    @FXML private void showExportCsv()     { runExportCsv(); }
    @FXML private void showChangePin()     { contentPane.getChildren().setAll(buildChangePinPanel()); }
    @FXML private void showRequestLoan()    { contentPane.getChildren().setAll(buildRequestLoanPanel()); }

    private VBox buildDashboardPanel() {
        VBox root = new VBox(24);
        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");

        HBox stats = new HBox(16);
        stats.setAlignment(Pos.CENTER_LEFT);
        try {
            List<Account> accounts = accountService.getAll().stream()
                    .filter(a -> a.getCustomer().getId() == currentCustomer.getId()).toList();
            double totalBalance = accounts.stream().mapToDouble(Account::getBalance).sum();
            long txCount = transactionService.getAll().stream()
                    .filter(t -> accounts.stream().anyMatch(a -> a.getId() == t.getAccount().getId())).count();
            stats.getChildren().addAll(
                    statCard("💰 Total Balance", String.format("%.2f RWF", totalBalance), "#f0fdf4", "#16a34a"),
                    statCard("🏦 Accounts", String.valueOf(accounts.size()), "#f0f9ff", "#0369a1"),
                    statCard("📋 Transactions", String.valueOf(txCount), "#fefce8", "#ca8a04")
            );
        } catch (SQLException e) {
            stats.getChildren().add(new Label("Error loading stats."));
        }

        Label recentTitle = new Label("Recent Transactions");
        recentTitle.getStyleClass().add("section-title");

        root.getChildren().addAll(title, stats, recentTitle, buildTransactionsTable(true));
        return root;
    }

    private VBox statCard(String lbl, String val, String bg, String accent) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setPrefWidth(220);
        card.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:12;" +
                "-fx-border-color:" + accent + "20;-fx-border-radius:12;-fx-border-width:1;");
        Label l = new Label(lbl); l.getStyleClass().add("stat-label");
        Label v = new Label(val); v.getStyleClass().add("stat-value");
        v.setStyle("-fx-text-fill:" + accent + ";");
        card.getChildren().addAll(l, v);
        return card;
    }

    private VBox buildCustomersPanel() {
        VBox root = new VBox(16);
        Label title = new Label("Customer Management");
        title.getStyleClass().add("page-title");

        TitledPane regPane = new TitledPane("➕ Register New Customer", buildRegisterForm());
        regPane.setCollapsible(true); regPane.setExpanded(false);
        regPane.setStyle("-fx-background-color:white;-fx-background-radius:10;");

        TitledPane editPane = new TitledPane("✏ Update My Information", buildUpdateCustomerForm());
        editPane.setCollapsible(true); editPane.setExpanded(false);
        editPane.setStyle("-fx-background-color:white;-fx-background-radius:10;");

        TableView<Customer> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPrefHeight(300);

        TableColumn<Customer, Integer> cId   = col("ID", d -> new SimpleIntegerProperty(d.getValue().getId()).asObject(), 60);
        TableColumn<Customer, String>  cName = colS("Full Name", d -> new SimpleStringProperty(d.getValue().getFullName()), 200);
        TableColumn<Customer, String>  cMail = colS("Email", d -> new SimpleStringProperty(d.getValue().getEmail()), 220);
        TableColumn<Customer, String>  cPh   = colS("Phone Number", d -> new SimpleStringProperty(d.getValue().getPhoneNumber()), 160);
        table.getColumns().addAll(cId, cName, cMail, cPh);

        Button refreshBtn = new Button("⟳ Refresh"); refreshBtn.getStyleClass().add("btn-secondary");
        Runnable load = () -> { try { table.setItems(FXCollections.observableArrayList(customerService.getAll())); } catch (SQLException e) { showError(e.getMessage()); } };
        load.run();
        refreshBtn.setOnAction(e -> load.run());

        root.getChildren().addAll(title, regPane, editPane, refreshBtn, table);
        return root;
    }

    private VBox buildUpdateCustomerForm() {
        VBox form = new VBox(10); form.setPadding(new Insets(16));

        Label info = new Label("Editing your own profile: " + currentCustomer.getFullName());
        info.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;");

        TextField nameF = field("Full Name");
        nameF.setText(currentCustomer.getFullName());
        TextField mailF = field("Email");
        mailF.setText(currentCustomer.getEmail());
        TextField phF   = field("Phone Number");
        phF.setText(currentCustomer.getPhoneNumber());

        Label msg = new Label(); msg.setWrapText(true);
        Button save = new Button("Save Changes"); save.getStyleClass().add("btn-primary");
        save.setOnAction(e -> {
            if (nameF.getText().isBlank() || mailF.getText().isBlank() || phF.getText().isBlank()) {
                styled(msg, "All fields are required.", false); return;
            }
            try {
                currentCustomer.setFullName(nameF.getText().trim());
                currentCustomer.setEmail(mailF.getText().trim());
                currentCustomer.setPhoneNumber(phF.getText().trim());
                customerService.update(currentCustomer);
                styled(msg, "✓ Profile updated successfully.", true);
            } catch (SQLException ex) { styled(msg, "Error: " + ex.getMessage(), false); }
        });
        form.getChildren().addAll(info, label("Full Name"), nameF, label("Email"), mailF, label("Phone Number"), phF, save, msg);
        return form;
    }

    private VBox buildRegisterForm() {
        VBox form = new VBox(10); form.setPadding(new Insets(16));
        TextField nameF = field("Full Name");
        TextField mailF = field("Email");
        TextField phF   = field("Phone Number (e.g. 0788123456)");
        PasswordField pinF = new PasswordField(); pinF.setPromptText("PIN — 4 to 6 digits"); pinF.getStyleClass().add("input-field");
        Label msg = new Label(); msg.setWrapText(true);
        Button save = new Button("Register Customer"); save.getStyleClass().add("btn-primary");
        save.setOnAction(e -> {
            if (nameF.getText().isBlank() || mailF.getText().isBlank() || phF.getText().isBlank()) {
                styled(msg, "All fields are required.", false); return;
            }
            if (!pinF.getText().matches("\\d{4,6}")) { styled(msg, "PIN must be 4–6 digits.", false); return; }
            try {
                customerService.register(new Customer(0, nameF.getText().trim(), mailF.getText().trim(), phF.getText().trim(), pinF.getText()));
                styled(msg, "✓ Customer registered successfully.", true);
                nameF.clear(); mailF.clear(); phF.clear(); pinF.clear();
            } catch (SQLException ex) { styled(msg, "Error: " + ex.getMessage(), false); }
        });
        form.getChildren().addAll(label("Full Name"), nameF, label("Email"), mailF, label("Phone Number"), phF, label("PIN"), pinF, save, msg);
        return form;
    }

    private VBox buildAccountsPanel() {
        VBox root = new VBox(16);
        Label title = new Label("Account Management");
        title.getStyleClass().add("page-title");

        TitledPane createPane = new TitledPane("➕ Open New Account", buildCreateAccountForm());
        createPane.setCollapsible(true); createPane.setExpanded(false);

        HBox tools = new HBox(12);
        tools.setAlignment(Pos.CENTER_LEFT);

        VBox balCard = new VBox(10);
        balCard.setPadding(new Insets(16));
        balCard.setStyle("-fx-background-color:white;-fx-background-radius:10;-fx-border-color:#e5e7eb;-fx-border-radius:10;");
        balCard.setMinWidth(280);
        Label balTitle = new Label("💰 My Account Balances"); balTitle.getStyleClass().add("section-title");
        Label balResult = new Label(); balResult.setWrapText(true);
        Button checkBtn = new Button("⟳ Refresh Balances"); checkBtn.getStyleClass().add("btn-secondary");
        Runnable loadBal = () -> {
            try {
                List<Account> accs = accountService.getByCustomerId(currentCustomer.getId());
                if (accs.isEmpty()) { styled(balResult, "No accounts found. Open one below.", false); return; }
                StringBuilder sb = new StringBuilder();
                for (Account a : accs) {
                    sb.append(String.format("[%s] Account #%d%n  Balance: %.2f RWF%n%n", a.getAccountType(), a.getId(), a.getBalance()));
                }
                styled(balResult, sb.toString().trim(), true);
            } catch (SQLException ex) { styled(balResult, "Error: " + ex.getMessage(), false); }
        };
        loadBal.run();
        checkBtn.setOnAction(e -> loadBal.run());
        balCard.getChildren().addAll(balTitle, checkBtn, balResult);

        VBox delCard = new VBox(10);
        delCard.setPadding(new Insets(16));
        delCard.setStyle("-fx-background-color:white;-fx-background-radius:10;-fx-border-color:#e5e7eb;-fx-border-radius:10;");
        delCard.setMinWidth(280);
        Label delTitle = new Label("🗑 Delete Inactive Account"); delTitle.getStyleClass().add("section-title");
        Label delNote = new Label("To delete an account, its balance must be 0. Withdraw all funds first, then delete.");
        delNote.setStyle("-fx-font-size:11px;-fx-text-fill:#6b7280;");
        delNote.setWrapText(true);
        Label delResult = new Label(); delResult.setWrapText(true);
        ComboBox<String> delAccCombo = new ComboBox<>();
        delAccCombo.setPromptText("Select account to delete");
        delAccCombo.setMaxWidth(Double.MAX_VALUE);
        Button findAccBtn = new Button("⟳ Refresh Accounts"); findAccBtn.getStyleClass().add("btn-secondary");

        final List<Account>[] foundAccHolder = new List[]{null};

        Runnable loadDelAccs = () -> {
            try {
                List<Account> accs = accountService.getByCustomerId(currentCustomer.getId());
                if (accs.isEmpty()) { styled(delResult, "No accounts found.", false); return; }
                foundAccHolder[0] = accs;
                delAccCombo.getItems().clear();
                for (Account a : accs) {
                    delAccCombo.getItems().add(String.format("ID:%d  %s  %.2f RWF", a.getId(), a.getAccountType(), a.getBalance()));
                }
                delAccCombo.getSelectionModel().selectFirst();
                styled(delResult, "Select the account below and click Delete.", true);
            } catch (SQLException ex) { styled(delResult, "Error: " + ex.getMessage(), false); }
        };
        loadDelAccs.run();
        findAccBtn.setOnAction(e -> loadDelAccs.run());

        Button delBtn = new Button("Delete Account"); delBtn.getStyleClass().add("btn-primary");
        delBtn.setStyle("-fx-background-color:#dc2626;");
        delBtn.setOnAction(e -> {
            int idx = delAccCombo.getSelectionModel().getSelectedIndex();
            if (foundAccHolder[0] == null || idx < 0) { styled(delResult, "Find accounts first and select one.", false); return; }
            Account chosen = foundAccHolder[0].get(idx);
            try {
                accountService.deleteInactive(chosen.getId());
                styled(delResult, "✓ Account deleted successfully.", true);
                delAccCombo.getItems().clear();
                foundAccHolder[0] = null;
            } catch (IllegalStateException ex) {
                double bal = foundAccHolder[0].get(idx).getBalance();
                styled(delResult, String.format(
                        "✗ This account still has %.2f RWF. Please withdraw all funds first, then try deleting again.", bal), false);
            } catch (SQLException ex) {
                styled(delResult, "Error: " + ex.getMessage(), false);
            }
        });
        delCard.getChildren().addAll(delTitle, delNote, findAccBtn, delAccCombo, delBtn, delResult);

        tools.getChildren().addAll(balCard, delCard);

        TableView<Account> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPrefHeight(280);

        TableColumn<Account, Integer> cId   = col("ID", d -> new SimpleIntegerProperty(d.getValue().getId()).asObject(), 60);
        TableColumn<Account, String>  cName = colS("Account Holder", d -> new SimpleStringProperty(currentCustomer.getFullName()), 180);
        TableColumn<Account, String>  cType = colS("Type", d -> new SimpleStringProperty(d.getValue().getAccountType()), 110);
        TableColumn<Account, Double>  cBal  = new TableColumn<>("Balance (RWF)");
        cBal.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getBalance()).asObject());
        cBal.setPrefWidth(160);
        TableColumn<Account, String>  cDate = colS("Created At", d -> new SimpleStringProperty(d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().toLocalDate().toString() : ""), 130);
        table.getColumns().addAll(cId, cName, cType, cBal, cDate);

        Button refreshBtn = new Button("⟳ Refresh"); refreshBtn.getStyleClass().add("btn-secondary");
        Runnable load = () -> {
            try {
                table.setItems(FXCollections.observableArrayList(accountService.getByCustomerId(currentCustomer.getId())));
            } catch (SQLException e) { showError(e.getMessage()); }
        };
        load.run();
        refreshBtn.setOnAction(e -> load.run());

        root.getChildren().addAll(title, createPane, tools, refreshBtn, table);
        return root;
    }

    private VBox buildCreateAccountForm() {
        VBox form = new VBox(12); form.setPadding(new Insets(16));

        TextField phF = field("Your Phone Number (e.g. 0788123456)");
        Label custResult = new Label(); custResult.setWrapText(true);
        final Customer[] custHolder = {null};
        Button lookupBtn = new Button("🔍 Find My Account"); lookupBtn.getStyleClass().add("btn-secondary");
        lookupBtn.setOnAction(e -> {
            String ph = phF.getText().trim();
            if (ph.isBlank()) { styled(custResult, "Enter your phone number.", false); return; }
            try {
                Customer c = customerService.getCustomerByPhone(ph);
                if (c == null) { styled(custResult, "No customer found for that phone number. Please register first.", false); custHolder[0] = null; return; }
                custHolder[0] = c;
                styled(custResult, "✓ Found: " + c.getFullName(), true);
            } catch (SQLException ex) { styled(custResult, "Error: " + ex.getMessage(), false); }
        });

        Label typeTitle = new Label("Choose Account Type");
        typeTitle.getStyleClass().add("field-label");

        ToggleGroup typeGroup = new ToggleGroup();

        VBox walletOption = accountTypeCard(
                "💳 Wallet Account",
                "Best for everyday payments.\n• Instant deposits & transfers\n• Daily transfer limit applies\n• Small transaction fee per withdrawal",
                "#f0f9ff", "#0369a1"
        );
        RadioButton walletRb = new RadioButton(); walletRb.setToggleGroup(typeGroup); walletRb.setSelected(true);
        walletOption.getChildren().add(0, walletRb);

        VBox savingsOption = accountTypeCard(
                "🏦 Savings Account",
                "Best for saving money.\n• Earns interest on balance\n• Maximum 5 withdrawals per month\n• Must keep a minimum balance",
                "#f0fdf4", "#16a34a"
        );
        RadioButton savingsRb = new RadioButton(); savingsRb.setToggleGroup(typeGroup);
        savingsOption.getChildren().add(0, savingsRb);

        HBox typeRow = new HBox(12, walletOption, savingsOption);

        walletOption.setOnMouseClicked(e -> walletRb.setSelected(true));
        savingsOption.setOnMouseClicked(e -> savingsRb.setSelected(true));

        TextField balF = field("Initial Deposit Amount (RWF) — optional, leave blank for 0");

        Label msg = new Label(); msg.setWrapText(true);
        Button create = new Button("Open Account"); create.getStyleClass().add("btn-primary");

        create.setOnAction(e -> {
            if (custHolder[0] == null) { styled(msg, "Please find your customer profile first.", false); return; }
            String balStr = balF.getText().trim();
            double bal = 0;
            if (!balStr.isBlank()) {
                try {
                    bal = Double.parseDouble(balStr);
                    if (bal < 0) { styled(msg, "Initial deposit cannot be negative.", false); return; }
                } catch (NumberFormatException ex) {
                    styled(msg, "Enter a valid number for the deposit amount.", false); return;
                }
            }
            try {
                if (walletRb.isSelected()) {
                    accountService.createAccount(new WalletAccount(0, custHolder[0], bal, LocalDateTime.now(), 500000, 100, true, 0, null));
                } else {
                    accountService.createAccount(new SavingsAccount(0, custHolder[0], "SAVINGS", bal, LocalDateTime.now(), 0.05, 1000, 5, 0, null));
                }
                String depositMsg = bal > 0 ? String.format(" with %.2f RWF deposited", bal) : " (you can deposit money later)";
                styled(msg, "✓ Account opened successfully" + depositMsg + "!", true);
                phF.clear(); balF.clear(); custHolder[0] = null; custResult.setText("");
                walletRb.setSelected(true);
            } catch (SQLException ex) {
                styled(msg, "Error: " + ex.getMessage(), false);
            }
        });

        form.getChildren().addAll(
                label("Your Phone Number"), phF, lookupBtn, custResult,
                typeTitle, typeRow,
                label("Initial Deposit (RWF) — optional"), balF,
                create, msg
        );
        return form;
    }

    private VBox accountTypeCard(String title, String description, String bg, String accent) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.setPrefWidth(260);
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:10;" +
                "-fx-border-color:" + accent + ";-fx-border-radius:10;-fx-border-width:1.5;");
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:" + accent + ";");
        Label desc = new Label(description);
        desc.setStyle("-fx-font-size:12px;-fx-text-fill:#374151;");
        desc.setWrapText(true);
        card.getChildren().addAll(lbl, desc);
        return card;
    }

    private VBox buildTransactionsPanel() {
        VBox root = new VBox(16);
        Label title = new Label("Transaction History");
        title.getStyleClass().add("page-title");

        Label sub = new Label("Your personal transaction records");
        sub.setStyle("-fx-text-fill:#6b7280;-fx-font-size:13px;");

        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(12));
        filterBar.setStyle("-fx-background-color:white;-fx-background-radius:10;-fx-border-color:#e5e7eb;-fx-border-radius:10;");

        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("All Types", "DEPOSIT", "WITHDRAW", "TRANSFER_OUT", "TRANSFER_IN");
        typeFilter.getSelectionModel().selectFirst();
        typeFilter.setPromptText("Filter by type");

        TextField searchRef = field("Search by reference ID…");
        searchRef.setPrefWidth(220);

        Button applyBtn = new Button("🔍 Search"); applyBtn.getStyleClass().add("btn-secondary");
        Button clearBtn = new Button("✕ Clear");   clearBtn.getStyleClass().add("btn-secondary");

        filterBar.getChildren().addAll(new Label("Type:"), typeFilter, searchRef, applyBtn, clearBtn);

        TableView<Transaction> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPrefHeight(380);

        TableColumn<Transaction, Integer> cId   = col("ID", d -> new SimpleIntegerProperty(d.getValue().getId()).asObject(), 60);
        TableColumn<Transaction, Integer> cAcc  = col("Account", d -> new SimpleIntegerProperty(d.getValue().getAccount().getId()).asObject(), 80);
        TableColumn<Transaction, String>  cType = colS("Type", d -> new SimpleStringProperty(d.getValue().getTransactionType()), 140);
        TableColumn<Transaction, Double>  cAmt  = new TableColumn<>("Amount (RWF)");
        cAmt.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getAmount()).asObject()); cAmt.setPrefWidth(140);
        TableColumn<Transaction, String>  cTime = colS("Timestamp", d -> new SimpleStringProperty(d.getValue().getTimestamp().toString()), 200);
        TableColumn<Transaction, String>  cRef  = colS("Reference ID", d -> new SimpleStringProperty(d.getValue().getReferenceId()), 280);
        table.getColumns().addAll(cId, cAcc, cType, cAmt, cTime, cRef);

        Runnable loadAll = () -> {
            try {
                List<Account> myAccs = accountService.getByCustomerId(currentCustomer.getId());
                java.util.Set<Integer> myAccIds = new java.util.HashSet<>();
                for (Account a : myAccs) myAccIds.add(a.getId());
                List<Transaction> all = transactionService.getAll().stream()
                        .filter(t -> myAccIds.contains(t.getAccount().getId()))
                        .collect(java.util.stream.Collectors.toList());

                String typeVal = typeFilter.getValue();
                if (typeVal != null && !typeVal.equals("All Types")) {
                    all = all.stream().filter(t -> t.getTransactionType().equalsIgnoreCase(typeVal))
                            .collect(java.util.stream.Collectors.toList());
                }
                String refVal = searchRef.getText().trim();
                if (!refVal.isBlank()) {
                    final String lower = refVal.toLowerCase();
                    all = all.stream().filter(t -> t.getReferenceId().toLowerCase().contains(lower))
                            .collect(java.util.stream.Collectors.toList());
                }
                table.setItems(FXCollections.observableArrayList(all));
            } catch (SQLException e) { showError(e.getMessage()); }
        };

        loadAll.run();
        applyBtn.setOnAction(e -> loadAll.run());
        clearBtn.setOnAction(e -> {
            typeFilter.getSelectionModel().selectFirst();
            searchRef.clear();
            loadAll.run();
        });

        root.getChildren().addAll(title, sub, filterBar, table);
        return root;
    }

    private VBox buildTransactionCard(String type, String icon, String bg, String accent, boolean isTransfer) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20)); card.setMinWidth(280);
        card.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:#e5e7eb;-fx-border-radius:12;");

        Label ic  = new Label(icon); ic.setStyle("-fx-font-size:24px;");
        Label lbl = new Label(type + " Money"); lbl.getStyleClass().add("section-title");

        TextField phoneF   = field("Your Phone Number");   phoneF.setPrefWidth(220);
        TextField toPhoneF = field("Recipient Phone Number"); toPhoneF.setPrefWidth(220);
        toPhoneF.setVisible(isTransfer); toPhoneF.setManaged(isTransfer);
        TextField amtF = field("Amount (RWF)"); amtF.setPrefWidth(220);

        Label msg = new Label(); msg.getStyleClass().add("success-label"); msg.setWrapText(true); msg.setMaxWidth(240);

        Button btn = new Button(type); btn.getStyleClass().add("btn-primary");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color:" + accent + ";");

        btn.setOnAction(e -> {
            msg.setText("");
            String ph = phoneF.getText().trim(); String amtTxt = amtF.getText().trim();
            if (ph.isBlank())     { styled(msg, "Enter your phone number.", false); return; }
            if (amtTxt.isBlank()) { styled(msg, "Enter an amount.", false); return; }
            try {
                double amt = Double.parseDouble(amtTxt);
                Customer from = customerService.getCustomerByPhone(ph);
                if (from == null) { styled(msg, "No account found for that phone number.", false); return; }
                List<Account> fromAccs = accountService.getAll().stream()
                        .filter(a -> a.getCustomer().getId() == from.getId()).toList();
                if (fromAccs.isEmpty()) { styled(msg, "You have no accounts. Please open one first.", false); return; }
                int fromId = fromAccs.get(0).getId();
                String ref;
                if ("Deposit".equals(type)) {
                    ref = transactionService.deposit(fromId, amt);
                } else if ("Withdraw".equals(type)) {
                    ref = transactionService.withdraw(fromId, amt);
                } else {
                    String toPh = toPhoneF.getText().trim();
                    if (toPh.isBlank()) { styled(msg, "Enter the recipient's phone number.", false); return; }
                    Customer to = customerService.getCustomerByPhone(toPh);
                    if (to == null) { styled(msg, "Recipient phone number not found.", false); return; }
                    List<Account> toAccs = accountService.getAll().stream()
                            .filter(a -> a.getCustomer().getId() == to.getId()).toList();
                    if (toAccs.isEmpty()) { styled(msg, "Recipient has no accounts.", false); return; }
                    ref = transactionService.transfer(fromId, toAccs.get(0).getId(), amt);
                }
                styled(msg, "✓ Done! Ref: " + ref.substring(0, 8) + "…", true);
                phoneF.clear(); amtF.clear(); if (isTransfer) toPhoneF.clear();
            } catch (InsufficientBalanceException | InvalidAmountException | AccountNotFoundException ex) {
                styled(msg, "✗ " + ex.getMessage(), false);
            } catch (NumberFormatException ex) {
                styled(msg, "✗ Enter a valid amount.", false);
            } catch (Exception ex) {
                styled(msg, "✗ Error: " + ex.getMessage(), false);
            }
        });

        card.getChildren().addAll(ic, lbl, label("Your Phone Number"), phoneF);
        if (isTransfer) card.getChildren().addAll(label("Recipient Phone Number"), toPhoneF);
        card.getChildren().addAll(label("Amount (RWF)"), amtF, btn, msg);
        return card;
    }

    private VBox buildSingleTransactionPanel(String type, String icon, String bg, String accent, boolean isTransfer) {
        VBox root = new VBox(24);
        Label title = new Label(type + " Money"); title.getStyleClass().add("page-title");
        VBox card = buildTransactionCard(type, icon, bg, accent, isTransfer);
        root.getChildren().addAll(title, card);
        return root;
    }

    private VBox buildTransactionsTable(boolean limitRows) {
        TableView<Transaction> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPrefHeight(limitRows ? 200 : 320);

        TableColumn<Transaction, Integer> cId   = col("ID", d -> new SimpleIntegerProperty(d.getValue().getId()).asObject(), 60);
        TableColumn<Transaction, Integer> cAcc  = col("Account", d -> new SimpleIntegerProperty(d.getValue().getAccount().getId()).asObject(), 80);
        TableColumn<Transaction, String>  cType = colS("Type", d -> new SimpleStringProperty(d.getValue().getTransactionType()), 130);
        TableColumn<Transaction, Double>  cAmt  = new TableColumn<>("Amount (RWF)");
        cAmt.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getAmount()).asObject()); cAmt.setPrefWidth(140);
        TableColumn<Transaction, String>  cTime = colS("Timestamp", d -> new SimpleStringProperty(d.getValue().getTimestamp().toString()), 200);
        table.getColumns().addAll(cId, cAcc, cType, cAmt, cTime);

        try {
            List<Account> myAccs = accountService.getByCustomerId(currentCustomer.getId());
            java.util.Set<Integer> myAccIds = new java.util.HashSet<>();
            for (Account a : myAccs) myAccIds.add(a.getId());
            List<Transaction> all = transactionService.getAll().stream()
                    .filter(t -> myAccIds.contains(t.getAccount().getId()))
                    .collect(java.util.stream.Collectors.toList());
            if (limitRows && all.size() > 5) all = all.subList(0, 5);
            table.setItems(FXCollections.observableArrayList(all));
        } catch (SQLException e) { showError(e.getMessage()); }
        return new VBox(table);
    }

    private VBox buildDailySummaryPanel() {
        VBox root = new VBox(16);
        Label title = new Label("Daily Summary"); title.getStyleClass().add("page-title");
        Label sub = new Label("Your transactions — " + java.time.LocalDate.now());
        sub.setStyle("-fx-text-fill:#6b7280;-fx-font-size:13px;");

        HBox stats = new HBox(16);
        stats.setAlignment(Pos.CENTER_LEFT);

        TableView<Transaction> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPrefHeight(340);
        TableColumn<Transaction, Integer> cId   = col("ID", d -> new SimpleIntegerProperty(d.getValue().getId()).asObject(), 55);
        TableColumn<Transaction, Integer> cAcc  = col("Account", d -> new SimpleIntegerProperty(d.getValue().getAccount().getId()).asObject(), 80);
        TableColumn<Transaction, String>  cType = colS("Type", d -> new SimpleStringProperty(d.getValue().getTransactionType()), 140);
        TableColumn<Transaction, Double>  cAmt  = new TableColumn<>("Amount (RWF)");
        cAmt.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getAmount()).asObject()); cAmt.setPrefWidth(150);
        TableColumn<Transaction, String>  cTime = colS("Timestamp", d -> new SimpleStringProperty(d.getValue().getTimestamp().toString()), 200);
        TableColumn<Transaction, String>  cRef  = colS("Reference", d -> new SimpleStringProperty(d.getValue().getReferenceId().substring(0,8)+"…"), 100);
        table.getColumns().addAll(cId, cAcc, cType, cAmt, cTime, cRef);

        Label status = new Label(); status.setWrapText(true);
        try {
            List<Account> myAccs = accountService.getByCustomerId(currentCustomer.getId());
            java.util.Set<Integer> myAccIds = new java.util.HashSet<>();
            for (Account a : myAccs) myAccIds.add(a.getId());

            List<Transaction> all = transactionService.getAll().stream()
                    .filter(t -> myAccIds.contains(t.getAccount().getId()))
                    .collect(java.util.stream.Collectors.toList());

            double totalIn  = all.stream().filter(t -> t.getTransactionType().equals("DEPOSIT") || t.getTransactionType().equals("TRANSFER_IN")).mapToDouble(Transaction::getAmount).sum();
            double totalOut = all.stream().filter(t -> t.getTransactionType().equals("WITHDRAW") || t.getTransactionType().equals("TRANSFER_OUT")).mapToDouble(Transaction::getAmount).sum();
            double net = totalIn - totalOut;

            stats.getChildren().addAll(
                    statCard("📥 Total In",  String.format("%.2f RWF", totalIn),  "#f0fdf4", "#16a34a"),
                    statCard("📤 Total Out", String.format("%.2f RWF", totalOut), "#fff7ed", "#ea580c"),
                    statCard("📊 Net Flow",  String.format("%.2f RWF", net),      net >= 0 ? "#f0f9ff" : "#fef2f2", net >= 0 ? "#0369a1" : "#dc2626"),
                    statCard("🔢 Count",     String.valueOf(all.size()),           "#fefce8", "#ca8a04")
            );

            table.setItems(FXCollections.observableArrayList(all));
            styled(status, "✓ " + all.size() + " transaction(s) loaded.", true);
        } catch (SQLException ex) {
            styled(status, "Error: " + ex.getMessage(), false);
        }
        root.getChildren().addAll(title, sub, stats, table, status);
        return root;
    }

    private VBox buildStatementPanel() {
        VBox root = new VBox(16);
        Label title = new Label("Account Statement"); title.getStyleClass().add("page-title");

        VBox form = new VBox(10);
        form.setPadding(new Insets(16));
        form.setStyle("-fx-background-color:white;-fx-background-radius:10;-fx-border-color:#e5e7eb;-fx-border-radius:10;");
        form.setMaxWidth(420);

        ComboBox<String> accCombo = new ComboBox<>();
        accCombo.setPromptText("Select account"); accCombo.setMaxWidth(Double.MAX_VALUE);
        Label msg = new Label(); msg.setWrapText(true);
        Button viewBtn = new Button("📄 View Statement"); viewBtn.getStyleClass().add("btn-primary");
        form.getChildren().addAll(label("Select Your Account"), accCombo, viewBtn, msg);

        TableView<Transaction> table = new TableView<>();
        table.getStyleClass().add("table-view"); table.setPrefHeight(320);
        table.setVisible(false); table.setManaged(false);
        TableColumn<Transaction, Integer> sId   = col("ID", d -> new SimpleIntegerProperty(d.getValue().getId()).asObject(), 55);
        TableColumn<Transaction, Integer> sAcc  = col("Account", d -> new SimpleIntegerProperty(d.getValue().getAccount().getId()).asObject(), 80);
        TableColumn<Transaction, String>  sType = colS("Type", d -> new SimpleStringProperty(d.getValue().getTransactionType()), 140);
        TableColumn<Transaction, Double>  sAmt  = new TableColumn<>("Amount (RWF)");
        sAmt.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getAmount()).asObject()); sAmt.setPrefWidth(150);
        TableColumn<Transaction, String>  sTime = colS("Timestamp", d -> new SimpleStringProperty(d.getValue().getTimestamp().toString()), 200);
        TableColumn<Transaction, String>  sRef  = colS("Reference ID", d -> new SimpleStringProperty(d.getValue().getReferenceId()), 280);
        table.getColumns().addAll(sId, sAcc, sType, sAmt, sTime, sRef);

        final List<Account>[] holder = new List[]{null};

        try {
            List<Account> accs = accountService.getByCustomerId(currentCustomer.getId());
            if (accs.isEmpty()) {
                styled(msg, "You have no accounts yet. Open one from Accounts.", false);
            } else {
                holder[0] = accs;
                for (Account a : accs)
                    accCombo.getItems().add(String.format("Account #%d — %s  (%.2f RWF)", a.getId(), a.getAccountType(), a.getBalance()));
                accCombo.getSelectionModel().selectFirst();
                styled(msg, "Select an account and click View Statement.", true);
            }
        } catch (SQLException ex) { styled(msg, "Error loading accounts: " + ex.getMessage(), false); }

        viewBtn.setOnAction(e -> {
            int idx = accCombo.getSelectionModel().getSelectedIndex();
            if (holder[0] == null || idx < 0) { styled(msg, "Select an account first.", false); return; }
            try {
                int accountId = holder[0].get(idx).getId();
                List<Transaction> txs = transactionService.getAll().stream()
                        .filter(t -> t.getAccount().getId() == accountId)
                        .collect(java.util.stream.Collectors.toList());
                table.setItems(FXCollections.observableArrayList(txs));
                table.setVisible(true); table.setManaged(true);

                double totalIn  = txs.stream().filter(t -> t.getTransactionType().equals("DEPOSIT") || t.getTransactionType().equals("TRANSFER_IN")).mapToDouble(Transaction::getAmount).sum();
                double totalOut = txs.stream().filter(t -> t.getTransactionType().equals("WITHDRAW") || t.getTransactionType().equals("TRANSFER_OUT")).mapToDouble(Transaction::getAmount).sum();
                styled(msg, String.format("✓ %d transaction(s) | In: %.2f RWF | Out: %.2f RWF | Net: %.2f RWF",
                        txs.size(), totalIn, totalOut, totalIn - totalOut), true);
            } catch (Exception ex) { styled(msg, "Error: " + ex.getMessage(), false); }
        });

        root.getChildren().addAll(title, form, table);
        return root;
    }

    private void runExportCsv() {
        FileChooser fc = new FileChooser(); fc.setTitle("Save My Transactions as CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fc.setInitialFileName("my_transactions_" + currentCustomer.getFullName().replaceAll("\\s+", "_") + ".csv");
        File file = fc.showSaveDialog(contentPane.getScene().getWindow());
        if (file != null) {
            try {
                List<Account> myAccs = accountService.getByCustomerId(currentCustomer.getId());
                List<Integer> myAccIds = myAccs.stream().map(Account::getId).collect(java.util.stream.Collectors.toList());
                reportGen.exportForCustomer(myAccIds, file.getAbsolutePath());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful"); alert.setHeaderText(null);
                alert.setContentText("✓ Exported " + myAccIds.size() + " account(s) to:\n" + file.getAbsolutePath());
                alert.showAndWait();
            } catch (Exception ex) { showError("Export error: " + ex.getMessage()); }
        }
    }

    private VBox buildChangePinPanel() {
        VBox root = new VBox(16); root.setMaxWidth(420);
        Label title = new Label("Change PIN"); title.getStyleClass().add("page-title");

        VBox form = new VBox(10); form.setPadding(new Insets(24));
        form.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:#e5e7eb;-fx-border-radius:12;");

        PasswordField oldPin  = new PasswordField(); oldPin.setPromptText("Current PIN");         oldPin.getStyleClass().add("input-field");
        PasswordField newPin  = new PasswordField(); newPin.setPromptText("New PIN (4–6 digits)"); newPin.getStyleClass().add("input-field");
        PasswordField confPin = new PasswordField(); confPin.setPromptText("Confirm New PIN");     confPin.getStyleClass().add("input-field");

        Label msg = new Label(); msg.setWrapText(true);
        Button save = new Button("Update PIN"); save.getStyleClass().add("btn-primary");
        save.setOnAction(e -> {
            if (!newPin.getText().equals(confPin.getText())) { styled(msg, "New PINs do not match.", false); return; }
            if (!newPin.getText().matches("\\d{4,6}"))       { styled(msg, "PIN must be 4–6 digits.", false); return; }
            try {
                customerService.changePin(currentCustomer, oldPin.getText(), newPin.getText());
                styled(msg, "✓ PIN updated successfully.", true);
                oldPin.clear(); newPin.clear(); confPin.clear();
            } catch (InvalidPinException | AccountLockedException ex) {
                styled(msg, "✗ " + ex.getMessage(), false);
            } catch (SQLException ex) { styled(msg, "Error: " + ex.getMessage(), false); }
        });

        form.getChildren().addAll(label("Current PIN"), oldPin, label("New PIN"), newPin, label("Confirm New PIN"), confPin, save, msg);
        root.getChildren().addAll(title, form);
        return root;
    }

    private VBox buildRequestLoanPanel() {
        VBox root = new VBox(24);
        Label title = new Label("Request a Loan");
        title.getStyleClass().add("page-title");

        VBox formCard = new VBox(12);
        formCard.setPadding(new Insets(24));
        formCard.setMaxWidth(480);
        formCard.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-border-color:#e5e7eb;-fx-border-radius:12;");

        Label formTitle = new Label("📋 New Loan Request");
        formTitle.getStyleClass().add("section-title");

        Label infoNote = new Label("Loan requests are reviewed within 24 hours. Standard interest rate is 5% flat. " +
                "You will be notified once approved.");
        infoNote.setStyle("-fx-font-size:12px;-fx-text-fill:#6b7280;");
        infoNote.setWrapText(true);

        TextField phoneF = field("Your Phone Number");
        Label custResult = new Label(); custResult.setWrapText(true);
        final Customer[] custHolder = {null};
        final Account[] accHolder   = {null};

        Button lookupBtn = new Button("🔍 Find My Account");
        lookupBtn.getStyleClass().add("btn-secondary");
        lookupBtn.setOnAction(e -> {
            String ph = phoneF.getText().trim();
            if (ph.isBlank()) { styled(custResult, "Enter your phone number.", false); return; }
            try {
                Customer c = customerService.getCustomerByPhone(ph);
                if (c == null) { styled(custResult, "No customer found for that phone number.", false); custHolder[0] = null; return; }
                java.util.List<Account> accs = accountService.getAll().stream()
                        .filter(a -> a.getCustomer().getId() == c.getId()).toList();
                if (accs.isEmpty()) { styled(custResult, "You have no accounts. Please open one first.", false); return; }
                custHolder[0] = c;
                accHolder[0]  = accs.get(0);
                styled(custResult, "✓ Found: " + c.getFullName() + " (Account ID: " + accs.get(0).getId() + ")", true);
            } catch (java.sql.SQLException ex) { styled(custResult, "Error: " + ex.getMessage(), false); }
        });

        TextField amtF = field("Loan Amount (RWF)");

        Label msg = new Label(); msg.setWrapText(true);

        Button submitBtn = new Button("Submit Loan Request");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setStyle("-fx-background-color:#7c3aed;");

        TableView<Loan> loanHistTable = buildLoanTable();
        try {
            loanHistTable.setItems(javafx.collections.FXCollections.observableArrayList(
                    loanService.getLoansForCustomer(currentCustomer.getId())
            ));
        } catch (java.sql.SQLException ex) {

        }

        submitBtn.setOnAction(e -> {
            if (custHolder[0] == null || accHolder[0] == null) {
                styled(msg, "Please find your customer profile first.", false); return;
            }
            String amtTxt = amtF.getText().trim();
            if (amtTxt.isBlank()) { styled(msg, "Enter the loan amount.", false); return; }
            try {
                double amt = Double.parseDouble(amtTxt);
                if (amt <= 0) { styled(msg, "Loan amount must be greater than 0.", false); return; }
                Loan loan = loanService.requestLoan(accHolder[0], custHolder[0], amt);
                styled(msg, "✓ Loan request submitted! Reference: " + loan.getReferenceId().substring(0, 8) + "…\n" +
                        "Status: PENDING — you will be notified once reviewed.", true);
                amtF.clear(); phoneF.clear(); custResult.setText(""); custHolder[0] = null; accHolder[0] = null;
                try {
                    loanHistTable.setItems(javafx.collections.FXCollections.observableArrayList(
                            loanService.getLoansForCustomer(currentCustomer.getId())
                    ));
                } catch (java.sql.SQLException ignored) {

                }
            } catch (NumberFormatException ex) {
                styled(msg, "Enter a valid number for the amount.", false);
            } catch (com.example.igirepay.lab3.exception.InvalidAmountException ex) {
                styled(msg, "✗ " + ex.getMessage(), false);
            } catch (java.sql.SQLException ex) {
                styled(msg, "Error: " + ex.getMessage(), false);
            }
        });

        formCard.getChildren().addAll(
                formTitle, infoNote,
                label("Your Phone Number"), phoneF, lookupBtn, custResult,
                label("Loan Amount (RWF)"), amtF,
                submitBtn, msg
        );

        HBox histHeader = new HBox(12);
        histHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label histTitle = new Label("My Loan History");
        histTitle.getStyleClass().add("section-title");
        Button refreshLoansBtn = new Button("⟳ Refresh");
        refreshLoansBtn.getStyleClass().add("btn-secondary");
        refreshLoansBtn.setOnAction(e -> {
            try {
                loanHistTable.setItems(javafx.collections.FXCollections.observableArrayList(
                        loanService.getLoansForCustomer(currentCustomer.getId())
                ));
            } catch (java.sql.SQLException ex) {
                showError("Could not refresh loans: " + ex.getMessage());
            }
        });
        histHeader.getChildren().addAll(histTitle, refreshLoansBtn);

        root.getChildren().addAll(title, formCard, histHeader, loanHistTable);
        return root;
    }

    private TableView<Loan> buildLoanTable() {
        TableView<Loan> table = new TableView<>();
        table.getStyleClass().add("table-view");
        table.setPrefHeight(260);

        javafx.scene.control.TableColumn<Loan, Integer> cId =
                col("ID", d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getId()).asObject(), 50);
        javafx.scene.control.TableColumn<Loan, Double> cReq = new javafx.scene.control.TableColumn<>("Requested (RWF)");
        cReq.setCellValueFactory(d -> new javafx.beans.property.SimpleDoubleProperty(d.getValue().getRequestedAmount()).asObject());
        cReq.setPrefWidth(150);
        javafx.scene.control.TableColumn<Loan, Double> cApproved = new javafx.scene.control.TableColumn<>("Approved (RWF)");
        cApproved.setCellValueFactory(d -> new javafx.beans.property.SimpleDoubleProperty(d.getValue().getApprovedAmount()).asObject());
        cApproved.setPrefWidth(140);
        javafx.scene.control.TableColumn<Loan, String> cStatus =
                colS("Status", d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus()), 100);
        javafx.scene.control.TableColumn<Loan, String> cRate =
                colS("Interest %", d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getInterestRate() + "%"), 90);
        javafx.scene.control.TableColumn<Loan, String> cDate =
                colS("Requested At", d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getRequestedAt().toString()), 200);
        javafx.scene.control.TableColumn<Loan, String> cRef =
                colS("Reference", d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getReferenceId().substring(0, 8) + "…"), 100);

        table.getColumns().addAll(cId, cReq, cApproved, cStatus, cRate, cDate, cRef);
        return table;
    }

    private TextField field(String prompt) {
        TextField tf = new TextField(); tf.setPromptText(prompt); tf.getStyleClass().add("input-field"); return tf;
    }
    private Label label(String text) {
        Label l = new Label(text); l.getStyleClass().add("field-label"); return l;
    }
    private void styled(Label lbl, String text, boolean ok) {
        lbl.setStyle(ok ? "-fx-text-fill:#16a34a;" : "-fx-text-fill:#dc2626;"); lbl.setText(text);
    }
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK); a.setTitle("Error"); a.showAndWait();
    }
    private <T> TableColumn<T, Integer> col(String name, javafx.util.Callback<TableColumn.CellDataFeatures<T, Integer>, javafx.beans.value.ObservableValue<Integer>> fn, int w) {
        TableColumn<T, Integer> c = new TableColumn<>(name); c.setCellValueFactory(fn); c.setPrefWidth(w); return c;
    }
    private <T> TableColumn<T, String> colS(String name, javafx.util.Callback<TableColumn.CellDataFeatures<T, String>, javafx.beans.value.ObservableValue<String>> fn, int w) {
        TableColumn<T, String> c = new TableColumn<>(name); c.setCellValueFactory(fn); c.setPrefWidth(w); return c;
    }
}