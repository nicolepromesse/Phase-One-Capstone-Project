package com.example.igirepay.lab3.report;

import com.example.igirepay.lab1.model.Transaction;

import java.util.List;

public class TransactionReport {

    private final List<Transaction> transactions;
    private final String title;

    public TransactionReport(String title, List<Transaction> transactions) {
        this.title = title;
        this.transactions = transactions;
    }

    public void printSummary() {
        System.out.println("\n============================================");
        System.out.println("  REPORT: " + title);
        System.out.println("============================================");

        if (transactions.isEmpty()) {
            System.out.println("  No transactions found.");
            System.out.println("============================================\n");
            return;
        }

        double totalDeposits = 0;
        double totalWithdrawals = 0;
        double totalTransfersIn = 0;
        double totalTransfersOut = 0;

        for (Transaction tx : transactions) {
            switch (tx.getTransactionType()) {
                case "DEPOSIT"      -> totalDeposits += tx.getAmount();
                case "WITHDRAW"     -> totalWithdrawals += tx.getAmount();
                case "TRANSFER_IN"  -> totalTransfersIn += tx.getAmount();
                case "TRANSFER_OUT" -> totalTransfersOut += tx.getAmount();
            }
        }

        System.out.printf("  Total Transactions : %d%n", transactions.size());
        System.out.printf("  Total Deposits     : %.2f RWF%n", totalDeposits);
        System.out.printf("  Total Withdrawals  : %.2f RWF%n", totalWithdrawals);
        System.out.printf("  Transfers In       : %.2f RWF%n", totalTransfersIn);
        System.out.printf("  Transfers Out      : %.2f RWF%n", totalTransfersOut);
        System.out.println("============================================\n");
    }

    public void printDetailed() {
        System.out.println("\n============================================");
        System.out.println("  DETAILED REPORT: " + title);
        System.out.println("============================================");

        if (transactions.isEmpty()) {
            System.out.println("  No transactions found.");
        } else {
            transactions.forEach(tx ->
                    System.out.printf("  [%s] %s | %.2f RWF | Ref: %s | Time: %s%n",
                            tx.getTransactionType(),
                            "Account #" + tx.getAccount().getId(),
                            tx.getAmount(),
                            tx.getReferenceId(),
                            tx.getTimestamp()
                    )
            );
        }

        System.out.println("============================================\n");
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
