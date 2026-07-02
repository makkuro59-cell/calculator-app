public class ErrorHandler {
    public void handle(Exception e) {
        // エラー処理の実装
        System.err.println("An error occurred: " + e.getMessage());
        e.printStackTrace();
    }
}
