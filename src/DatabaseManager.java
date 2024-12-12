import java.sql.*;
import java.util.*;

public class DatabaseManager {
    //Used to get the database URL on sqLITE
    private static final String DB_URL = "jdbc:sqlite:budget_tracker.db";

    //Calls the database creation, I can also populate the database threw it
    // I used this method just to make it easier for me to work threw the project
    public DatabaseManager() {

        createDatabase();
        //    dbManager.deleteAllTransactions();
        //    dbManager.populateSampleData();
    }

    //Database creation
    private void createDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                // Inital Database creation
                String createTableQuery = "CREATE TABLE IF NOT EXISTS transactions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "amount REAL NOT NULL, " +
                        "category TEXT NOT NULL, " +
                        "type TEXT NOT NULL, " +
                        "date TEXT NOT NULL, " +
                        "description TEXT)";
                Statement stmt = conn.createStatement();
                stmt.execute(createTableQuery);

                // Added a description feature in the middle of the project here
                ResultSet rs = conn.getMetaData().getColumns(null, null, "transactions", "description");
                if (!rs.next()) {
                    String alterTableQuery = "ALTER TABLE transactions ADD COLUMN description TEXT DEFAULT ''";
                    stmt.execute(alterTableQuery);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Used to get the final balance for the annual chart
    public double getFinalBalanceForPreviousYear(String year) {
        String query = "SELECT strftime('%Y-%m', date) AS month, " +
                "SUM(CASE WHEN type = 'Income' THEN amount ELSE 0 END) AS income, " +
                "SUM(CASE WHEN type = 'Expense' THEN amount ELSE 0 END) AS expense " +
                "FROM transactions WHERE strftime('%Y', date) < ? GROUP BY month ORDER BY month ASC";

        double cumulativeBalance = 0.0; // Initial balance starts at 0

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, year); // Gets all the data for the previous year
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String month = rs.getString("month");
                double income = rs.getDouble("income");
                double expense = rs.getDouble("expense");

                // Updating the cumulative balance
                cumulativeBalance += (income - expense);

                // logs
                System.out.println(String.format(
                        "Month: %s | Income: %.2f | Expense: %.2f | Cumulative Balance: %.2f",
                        month, income, expense, cumulativeBalance
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Final cumulative balance up to year " + (Integer.parseInt(year) - 1) + ": " + cumulativeBalance);
        return cumulativeBalance;
    }

    //Add a new transaction to the database
    public void addTransaction(double amount, String category, String type, String date, String description) {
        String insertQuery = "INSERT INTO transactions (amount, category, type, date, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, category);
            pstmt.setString(3, type);
            pstmt.setString(4, date);
            pstmt.setString(5, description);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Used to delete a selected transaction
    public void deleteTransaction(String date, String description) {
        String deleteQuery = "DELETE FROM transactions WHERE date = ? AND description = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
            pstmt.setString(1, date);
            pstmt.setString(2, description);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Used to display the transaction list
    public ResultSet getTransactions() {
        String selectQuery = "SELECT * FROM transactions ORDER BY date DESC";
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(selectQuery);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Used for the filtering
    public ResultSet filterTransactions(String category, String type, String startDate, String endDate) {
        String filterQuery = "SELECT * FROM transactions WHERE 1=1";
        if (category != null) {
            filterQuery += " AND category = ?";
        }
        if (type != null) {
            filterQuery += " AND type = ?";
        }
        if (startDate != null) {
            filterQuery += " AND date >= ?";
        }
        if (endDate != null) {
            filterQuery += " AND date <= ?";
        }

        filterQuery += " ORDER BY date DESC";
        
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(filterQuery);
            int paramIndex = 1;
            if (category != null) {
                pstmt.setString(paramIndex++, category);
            }
            if (type != null) {
                pstmt.setString(paramIndex++, type);
            }
            if (startDate != null) {
                pstmt.setString(paramIndex++, startDate);
            }
            if (endDate != null) {
                pstmt.setString(paramIndex++, endDate);
            }

            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Used to clear the data for dev
    public void deleteAllTransactions() {
        String deleteQuery = "DELETE FROM transactions";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            int rowsDeleted = stmt.executeUpdate(deleteQuery);
            System.out.println(rowsDeleted + " transactions deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Used to create an example
    public void populateSampleData() {
        String[] categories = {"Food", "Entertainment", "Transportation", "Utilities", "Healthcare", "Education", "Other"};
        String[] descriptions = {
                "Grocery shopping", "Restaurant", "Cinema", "Concert", "Bus ticket", "Electricity bill",
                "Doctor appointment", "Course fee", "Fuel", "Gym membership", "Internet bill", "Clothing", "Gift"
        };

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false); // Start a transaction
            String insertQuery = "INSERT INTO transactions (amount, category, type, date, description) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertQuery);

            // Loop through the years
            for (int year = 2022; year <= 2024; year++) {
                // Loop through the months
                for (int month = 1; month <= 12; month++) {
                    // Introduce salary fluctuation (e.g., bonuses or deductions)
                    double baseSalary = 2200.0;
                    double salaryFluctuation = Math.round((Math.random() * 300 - 150) * 100.0) / 100.0; // ±150€
                    double salary = baseSalary + salaryFluctuation;

                    pstmt.setDouble(1, salary);
                    pstmt.setString(2, "Salary");
                    pstmt.setString(3, "Income");
                    pstmt.setString(4, String.format("%d-%02d-01", year, month));
                    pstmt.setString(5, "Monthly salary");
                    pstmt.addBatch();

                    // Generate a realistic number of expenses for the month
                    int expensesCount = 5 + (int)(Math.random() * 6); // Between 5 and 10
                    for (int i = 0; i < expensesCount; i++) {
                        // Add variation to the expenses
                        double baseExpense = 50 + Math.random() * 450; // Base range for expenses
                        double seasonalMultiplier = getSeasonalMultiplier(month); // Apply seasonal factor
                        double expenseAmount = Math.round((baseExpense * seasonalMultiplier) * 100.0) / 100.0;

                        String category = categories[(int)(Math.random() * categories.length)];
                        String description = descriptions[(int)(Math.random() * descriptions.length)];
                        String date = String.format("%d-%02d-%02d", year, month, 1 + (int)(Math.random() * 28));

                        pstmt.setDouble(1, expenseAmount);
                        pstmt.setString(2, category);
                        pstmt.setString(3, "Expense");
                        pstmt.setString(4, date);
                        pstmt.setString(5, description);
                        pstmt.addBatch();
                    }
                }
            }

            pstmt.executeBatch();
            conn.commit(); // Commit the transaction
            System.out.println("Sample data with fluctuations inserted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper method to simulate seasonal variations
    private double getSeasonalMultiplier(int month) {
        switch (month) {
            case 12: // December (holiday season)
            case 1:  // January (post-holiday expenses)
                return 1.2; // 20% higher spending
            case 6:  // June (vacation planning)
            case 7:  // July (peak vacation time)
            case 8:  // August (vacation expenses)
                return 1.15; // 15% higher spending
            default:
                return 1.0; // Normal spending
        }
    }

    //Used for the monthly chart
    public Map<String, Double> getTotalExpensesByCategoryForMonth(String month) {
        String query = "SELECT category, SUM(amount) AS total FROM transactions " +
                "WHERE type = 'Expense' AND strftime('%Y-%m', date) = ? GROUP BY category";
        Map<String, Double> data = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, month);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                data.put(rs.getString("category"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return data;
    }

    //Calculate the ratio
    public double getMonthlyBalance(String month) {
        String query = "SELECT " +
                "SUM(CASE WHEN type = 'Income' THEN amount ELSE 0 END) - " +
                "SUM(CASE WHEN type = 'Expense' THEN amount ELSE 0 END) AS balance " +
                "FROM transactions WHERE strftime('%Y-%m', date) = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, month);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    //Used for the CSV transoformation
    public List<String[]> getAllTransactions() {
        String query = "SELECT amount, category, type, date, description FROM transactions";
        List<String[]> transactions = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String amount = String.valueOf(rs.getDouble("amount"));
                String category = rs.getString("category");
                String type = rs.getString("type");
                String date = rs.getString("date");
                String description = rs.getString("description");
                transactions.add(new String[]{amount, category, type, date, description});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    //Used for the annual evolution chart
    public Map<String, double[]> getAnnualEvolution(String year, double initialBalance) {
        String query = "SELECT strftime('%Y-%m', date) AS month, " +
                "SUM(CASE WHEN type = 'Income' THEN amount ELSE 0 END) AS income, " +
                "SUM(CASE WHEN type = 'Expense' THEN amount ELSE 0 END) AS expense " +
                "FROM transactions WHERE strftime('%Y', date) = ? GROUP BY month ORDER BY month";

        Map<String, double[]> data = new LinkedHashMap<>();
        double cumulativeBalance = initialBalance;

        System.out.println("=== Starting Annual Evolution Calculation for Year: " + year + " ===");
        System.out.println("Initial Cumulative Balance from Previous Year: " + cumulativeBalance);

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, year);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String month = rs.getString("month");
                double income = rs.getDouble("income");
                double expense = rs.getDouble("expense");

                // Update cumulative balance
                cumulativeBalance += (income - expense);

                // Add the data to the results
                data.put(month, new double[]{income, expense, cumulativeBalance});

                // Logs
                System.out.println(String.format(
                        "Month: %s | Income: %.2f | Expense: %.2f | Cumulative Balance: %.2f",
                        month, income, expense, cumulativeBalance
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("=== Finished Annual Evolution Calculation for Year: " + year + " ===");
        return data;
    }

    //get the accumulated balance for the last month (used for the monthly chart)
    public double getFinalBalanceForLatestMonth() {
        String query = "SELECT strftime('%Y-%m', MAX(date)) AS latestMonth, " +
                "SUM(CASE WHEN type = 'Income' THEN amount ELSE 0 END) - " +
                "SUM(CASE WHEN type = 'Expense' THEN amount ELSE 0 END) AS balance " +
                "FROM transactions";

        double cumulativeBalance = 0.0;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                cumulativeBalance = rs.getDouble("balance");

                // Log pour le débogage
                System.out.println(String.format(
                        "Latest Month: %s | Final Balance: %.2f",
                        rs.getString("latestMonth"), cumulativeBalance
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cumulativeBalance;
    }

}