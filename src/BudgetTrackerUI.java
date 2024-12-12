import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import org.jfree.data.category.DefaultCategoryDataset;


public class BudgetTrackerUI {
    private DatabaseManager dbManager;
    private JFrame frame;
    private JTable transactionTable;
    private DefaultTableModel tableModel;

    //Creation of the database and call of the display program
    public BudgetTrackerUI() {
        dbManager = new DatabaseManager();

        initializeUI();
    }

    //Display program
    private void initializeUI() {
        frame = new JFrame("Personal Budget Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);

        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(128, 128, 128)); // Background color for main panel (light blue)

        // West panel with uniform color
        JPanel westPanel = new JPanel();
        westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.Y_AXIS));
        westPanel.setBackground(new Color(128, 128, 128)); // Uniform color for left side

        // Title for "Transaction Adding"
        JLabel transactionTitle = new JLabel("Transaction Adding");
        transactionTitle.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        transactionTitle.setFont(new Font("Futura", Font.BOLD, 16));
        transactionTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        westPanel.add(transactionTitle);

        // Form for adding transactions
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(new Color(128, 128, 128)); // Same color as westPanel

        JTextField amountField = new JTextField();
        amountField.setMaximumSize(new Dimension(200, 30));
        formPanel.add(new JLabel("Amount:"));
        formPanel.add(amountField);

        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"Food", "Entertainment", "Transportation", "Utilities", "Healthcare", "Education","Salary", "Other"});
        categoryCombo.setMaximumSize(new Dimension(200, 30));
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryCombo);

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Income", "Expense"});
        typeCombo.setMaximumSize(new Dimension(200, 30));
        formPanel.add(new JLabel("Type:"));
        formPanel.add(typeCombo);

        JTextField dateField = new JTextField("YYYY-MM-DD");
        dateField.setMaximumSize(new Dimension(200, 30));
        formPanel.add(new JLabel("Date:"));
        formPanel.add(dateField);

        JTextField descriptionField = new JTextField();
        descriptionField.setMaximumSize(new Dimension(200, 30));
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descriptionField);

        JButton addButton = new JButton("Add Transaction");
        addButton.setMaximumSize(new Dimension(200, 30));
        addButton.setBackground(new Color(0, 128, 128)); // Powder blue for buttons
        addButton.addActionListener(e -> {
            try {
                double amount = validateAmount(amountField.getText());
                String date = validateDate(dateField.getText());
                String category = (String) categoryCombo.getSelectedItem();
                String type = (String) typeCombo.getSelectedItem();
                String description = descriptionField.getText();

                dbManager.addTransaction(amount, category, type, date, description);
                updateTransactionTable(null, null, null, null);

                amountField.setText("");
                dateField.setText("YYYY-MM-DD");
                descriptionField.setText("");
                JOptionPane.showMessageDialog(frame, "Transaction added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        formPanel.add(addButton);
        westPanel.add(formPanel);

        // Title for "Filtering"
        JLabel filteringTitle = new JLabel("Filtering");
        filteringTitle.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        filteringTitle.setFont(new Font("Futura", Font.BOLD, 16));
        filteringTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        westPanel.add(filteringTitle);

// Panel for filtering options
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBackground(new Color(128, 128, 128)); // Same color as westPanel

        JComboBox<String> filterCategoryCombo = new JComboBox<>(new String[]{"All", "Food", "Entertainment", "Transportation", "Utilities", "Healthcare", "Education","Salary", "Other"});
        filterCategoryCombo.setMaximumSize(new Dimension(200, 30));
        filterPanel.add(new JLabel("Category:"));
        filterPanel.add(filterCategoryCombo);

        JComboBox<String> filterTypeCombo = new JComboBox<>(new String[]{"All", "Income", "Expense"});
        filterTypeCombo.setMaximumSize(new Dimension(200, 30));
        filterPanel.add(new JLabel("Type:"));
        filterPanel.add(filterTypeCombo);

        JTextField filterStartDateField = new JTextField("YYYY-MM-DD");
        filterStartDateField.setMaximumSize(new Dimension(200, 30));
        filterPanel.add(new JLabel("Start Date:"));
        filterPanel.add(filterStartDateField);

        JTextField filterEndDateField = new JTextField("YYYY-MM-DD");
        filterEndDateField.setMaximumSize(new Dimension(200, 30));
        filterPanel.add(new JLabel("End Date:"));
        filterPanel.add(filterEndDateField);

        JButton filterButton = new JButton("Filter Transactions");
        filterButton.setMaximumSize(new Dimension(200, 30));
        filterButton.setBackground(new Color(0, 128, 128));
        filterButton.addActionListener(e -> {
            try {
                String category = filterCategoryCombo.getSelectedItem().equals("All") ? null : (String) filterCategoryCombo.getSelectedItem();
                String type = filterTypeCombo.getSelectedItem().equals("All") ? null : (String) filterTypeCombo.getSelectedItem();
                String startDate = filterStartDateField.getText().equals("YYYY-MM-DD") ? null : validateDate(filterStartDateField.getText());
                String endDate = filterEndDateField.getText().equals("YYYY-MM-DD") ? null : validateDate(filterEndDateField.getText());

                updateTransactionTable(category, type, startDate, endDate);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        filterPanel.add(filterButton);
        westPanel.add(filterPanel);


        // Title for "Actions"
        JLabel actionsTitle = new JLabel("Actions");
        actionsTitle.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        actionsTitle.setFont(new Font("Futura", Font.BOLD, 16));
        actionsTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        westPanel.add(actionsTitle);

        // Buttons for actions


        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBackground(new Color(128, 128, 128));

        JButton chartButton = new JButton("Show Current Month Expense Chart");
        chartButton.setMaximumSize(new Dimension(200, 30));
        chartButton.setBackground(new Color(0, 128, 128));
        chartButton.addActionListener(e -> showCurrentMonthExpenseChart());
        actionPanel.add(chartButton);

        JButton deleteButton = new JButton("Delete Selected Transaction");
        deleteButton.setMaximumSize(new Dimension(200, 30));
        deleteButton.setBackground(new Color(0, 128, 128)); // Light red for delete
        deleteButton.addActionListener(e -> deleteSelectedTransaction());
        actionPanel.add(deleteButton);

        JButton exportButton = new JButton("Export Transactions to CSV");
        exportButton.setMaximumSize(new Dimension(200, 30));
        exportButton.setBackground(new Color(0, 128, 128));
        exportButton.addActionListener(e -> exportTransactionsToCSV());
        actionPanel.add(exportButton);

        JButton annualChartButton = new JButton("Show Annual Evolution Chart");
        annualChartButton.setMaximumSize(new Dimension(200, 30));
        annualChartButton.setBackground(new Color(0, 128, 128));
        annualChartButton.addActionListener(e -> showAnnualEvolutionChart());
        actionPanel.add(annualChartButton);

       /* JButton forecastButton = new JButton("Show Forecast");
        forecastButton.setMaximumSize(new Dimension(200, 30));
        forecastButton.setBackground(new Color(176, 224, 230));
        forecastButton.addActionListener(e -> showForecastChart());
        actionPanel.add(forecastButton);*/


        westPanel.add(actionPanel);

        // Table to display transactions
        tableModel = new DefaultTableModel(new String[]{"Amount", "Category", "Type", "Date", "Description"}, 0);
        transactionTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(transactionTable);

        // Add components to main panel
        mainPanel.add(westPanel, BorderLayout.WEST); // All features on the left
        mainPanel.add(scrollPane, BorderLayout.CENTER); // Transactions table on the right

        frame.add(mainPanel);
        frame.setVisible(true);

        updateTransactionTable(null, null, null, null);
    }

    //Annual Evolution Program
    private void showAnnualEvolutionChart() {
        String year = JOptionPane.showInputDialog(frame, "Enter the year (YYYY):", "Select Year", JOptionPane.QUESTION_MESSAGE);

        if (year == null || !year.matches("\\d{4}")) {
            JOptionPane.showMessageDialog(frame, "Invalid year format. Please enter a valid year (YYYY).", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Calls the method to get the cumulated balance depending on the year
        double initialBalance = dbManager.getFinalBalanceForPreviousYear(year);

        // Store the annual Evolution in a hashmap
        Map<String, double[]> annualData = dbManager.getAnnualEvolution(year, initialBalance);

        // Preapre the data for the chart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, double[]> entry : annualData.entrySet()) {
            String month = entry.getKey();
            double[] values = entry.getValue();
            dataset.addValue(values[0], "Income", month); // Income
            dataset.addValue(values[1], "Expense", month); // Expense
            dataset.addValue(values[2], "Cumulative Balance", month); // Cumulative Balance
        }

        // Create the chart
        JFreeChart chart = ChartFactory.createLineChart(
                "Annual Financial Evolution - " + year,
                "Month",
                "Amount (€)",
                dataset
        );

        JFrame chartFrame = new JFrame("Annual Evolution Chart - " + year);
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.setSize(800, 600);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartFrame.add(chartPanel);
        chartFrame.setVisible(true);
    }

    //Update the transaction table for the list, this program is called everytime we do a an action on InitializeUI
    private void updateTransactionTable(String category, String type, String startDate, String endDate) {
        tableModel.setRowCount(0);
        try (ResultSet rs = (category == null && type == null && startDate == null && endDate == null)
                ? dbManager.getTransactions()
                : dbManager.filterTransactions(category, type, startDate, endDate)) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getString("type"),
                        rs.getString("date"),
                        rs.getString("description")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //used to validate the entered amount
    private double validateAmount(String amountText) throws IllegalArgumentException {
        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero.");
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Please enter a valid numeric amount.");
        }
    }

    //used to enter a valid date
    private String validateDate(String dateText) throws IllegalArgumentException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false); // Strict date parsing
        try {
            dateFormat.parse(dateText); // Validate the date format
            return dateText;
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Please enter a valid date in the format YYYY-MM-DD.");
        }
    }

    //used to delete a selected transaction by cliking on the list, then calling the database deletion program
    private void deleteSelectedTransaction() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "No transaction selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this transaction?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String date = tableModel.getValueAt(selectedRow, 3).toString();
            String description = tableModel.getValueAt(selectedRow, 4).toString();
            dbManager.deleteTransaction(date, description); // Delete from database
            updateTransactionTable(null, null, null, null); // Refresh table
            JOptionPane.showMessageDialog(frame, "Transaction deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    //Shows the current month chart with the Jfree chart package
    private void showCurrentMonthExpenseChart() {
        String currentMonth = new SimpleDateFormat("yyyy-MM").format(new java.util.Date());
        Map<String, Double> data = dbManager.getTotalExpensesByCategoryForMonth(currentMonth);
        DefaultPieDataset dataset = new DefaultPieDataset();

        // Calculer le ratio mensuel et la balance cumulée
        double ratio = dbManager.getMonthlyBalance(currentMonth);
        double cumulativeBalance = dbManager.getFinalBalanceForLatestMonth();

        // Remplir le dataset pour le graphique
        for (Map.Entry<String, Double> entry : data.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        // Créer le graphique
        JFreeChart chart = ChartFactory.createPieChart(
                "Current Month Expenses: " + currentMonth,
                dataset,
                true,
                true,
                false
        );

        // Préparer la fenêtre du graphique
        JFrame chartFrame = new JFrame("Current Month Expense Chart");
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        chartFrame.setSize(600, 400);

        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.add(new ChartPanel(chart), BorderLayout.CENTER);

        // Formater les balances
        String formattedRatio = String.format("Ratio of the month: %.2f", ratio);
        String formattedCumulativeBalance = String.format("Cumulative Balance: %.2f", cumulativeBalance);

        // Ajouter les deux balances sous le graphique
        JPanel balancePanel = new JPanel();
        balancePanel.setLayout(new BoxLayout(balancePanel, BoxLayout.Y_AXIS));
        balancePanel.add(new JLabel(formattedRatio, SwingConstants.CENTER));
        balancePanel.add(new JLabel(formattedCumulativeBalance, SwingConstants.CENTER));

        chartPanel.add(balancePanel, BorderLayout.SOUTH);

        // Afficher la fenêtre
        chartFrame.add(chartPanel);
        chartFrame.setVisible(true);
    }

    //exports our transaction into a CSV by using JFile and FileWritee packages
    private void exportTransactionsToCSV() {
        // get all the transactions
        List<String[]> transactions = dbManager.getAllTransactions();

        // choose a file to store the data
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Transactions to CSV");
        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();

            try (FileWriter csvWriter = new FileWriter(filePath)) {
                // First line to explain what are the features
                csvWriter.append("Amount,Category,Type,Date,Description\n");

                // simple loop to write transactions
                for (String[] transaction : transactions) {
                    csvWriter.append(String.join(",", transaction));
                    csvWriter.append("\n");
                }

                JOptionPane.showMessageDialog(frame, "Transactions exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "An error occurred while exporting transactions.", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}