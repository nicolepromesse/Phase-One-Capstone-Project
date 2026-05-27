package com.example.igirepay.lab3.report;

import com.example.igirepay.lab1.model.Transaction;
import com.example.igirepay.lab2.daoimpl.TransactionDAOImpl;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionReportGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TransactionDAOImpl transactionDAO = new TransactionDAOImpl();

    public String exportAllToCSV(String outputPath) throws SQLException, IOException {
        List<Transaction> transactions = transactionDAO.getAll();
        writeCSV(outputPath, transactions);
        return outputPath;
    }

    public String exportAccountToCSV(int accountId, String outputPath) throws SQLException, IOException {
        List<Transaction> transactions = transactionDAO.getByAccountId(accountId);
        writeCSV(outputPath, transactions);
        return outputPath;
    }

    private void writeCSV(String path, List<Transaction> transactions) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.println("ID,AccountID,ReferenceID,Type,Amount,Timestamp");
            for (Transaction t : transactions) {
                pw.printf("%d,%d,%s,%s,%.2f,%s%n",
                        t.getId(),
                        t.getAccount().getId(),
                        t.getReferenceId(),
                        t.getTransactionType(),
                        t.getAmount(),
                        t.getTimestamp().format(DATETIME_FMT));
            }
        }
    }

    public String getDailySummary() throws SQLException {
        List<Transaction> all = transactionDAO.getAll();

        Map<LocalDate, List<Transaction>> byDate = all.stream()
                .collect(Collectors.groupingBy(t -> t.getTimestamp().toLocalDate()));

        StringBuilder sb = new StringBuilder();
        sb.append("=== Daily Transaction Summary ===\n\n");

        byDate.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, List<Transaction>>comparingByKey().reversed())
                .forEach(entry -> {
                    LocalDate date = entry.getKey();
                    List<Transaction> dayTxs = entry.getValue();
                    double totalIn  = dayTxs.stream()
                            .filter(t -> t.getTransactionType().equals("DEPOSIT") ||
                                         t.getTransactionType().equals("TRANSFER_IN"))
                            .mapToDouble(Transaction::getAmount).sum();
                    double totalOut = dayTxs.stream()
                            .filter(t -> t.getTransactionType().equals("WITHDRAW") ||
                                         t.getTransactionType().equals("TRANSFER_OUT"))
                            .mapToDouble(Transaction::getAmount).sum();

                    sb.append(String.format("Date: %s | Count: %d | In: %.2f RWF | Out: %.2f RWF | Net: %.2f RWF%n",
                            date.format(DATE_FMT), dayTxs.size(), totalIn, totalOut, totalIn - totalOut));
                });

        return sb.toString();
    }


    public String getAccountStatement(int accountId) throws SQLException {
        List<Transaction> txs = transactionDAO.getByAccountId(accountId);

        StringBuilder sb = new StringBuilder();
        sb.append("=== Account Statement — Account ID: ").append(accountId).append(" ===\n\n");
        sb.append(String.format("%-6s %-14s %-12s %12s  %-20s%n",
                "ID", "Type", "Ref (short)", "Amount (RWF)", "Timestamp"));
        sb.append("-".repeat(70)).append("\n");

        for (Transaction t : txs) {
            String shortRef = t.getReferenceId().length() > 12
                    ? t.getReferenceId().substring(0, 12) + "…"
                    : t.getReferenceId();
            sb.append(String.format("%-6d %-14s %-12s %12.2f  %-20s%n",
                    t.getId(),
                    t.getTransactionType(),
                    shortRef,
                    t.getAmount(),
                    t.getTimestamp().format(DATETIME_FMT)));
        }

        double net = txs.stream().mapToDouble(t ->
                (t.getTransactionType().equals("DEPOSIT") || t.getTransactionType().equals("TRANSFER_IN"))
                        ? t.getAmount() : -t.getAmount()
        ).sum();

        sb.append("-".repeat(70)).append("\n");
        sb.append(String.format("Total Transactions: %d | Net Flow: %.2f RWF%n", txs.size(), net));
        return sb.toString();
    }
}
