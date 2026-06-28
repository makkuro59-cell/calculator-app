import javax.swing.SwingUtilities;


public class CalculatorApp {


    public CalculatorApp() {
        SwingUtilities.invokeLater(() -> {
            CalculatorModel model = new CalculatorModel();
            CalculatorFrame view = new CalculatorFrame();
            new CalculatorController(model, view);
            view.setVisible(true);
        });
    }

    public static void main(String[] args) {
        new CalculatorApp();
    }
}