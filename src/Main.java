import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new BudgetTrackerUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}







