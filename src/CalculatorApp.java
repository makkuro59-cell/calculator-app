import javax.swing.SwingUtilities;

/**
 * 電卓アプリケーションのエントリーポイントとなるクラス
 * モデル・ビュー・コントローラーを生成し、アプリケーションを起動する
 */
public class CalculatorApp {

    /**
     * CalculatorApp のコンストラクタ
     * Swingのイベントディスパッチスレッド上で、モデル・ビュー・コントローラーの生成、および画面表示を行う
     */
    public CalculatorApp() {
        SwingUtilities.invokeLater(() -> {
            // 電卓の内部データや計算ロジックを管理するmodelのインスタンスを生成
            CalculatorModel model = new CalculatorModel();

            // 電卓のUIを表示するviewのインスタンスを生成
            CalculatorFrame view = new CalculatorFrame();

            // モデルとビューを仲介するコントローラーを生成し、viewにイベント処理を紐付け
            new CalculatorController(model, view);

            // 生成したウィンドウviewを画面上に表示
            view.setVisible(true);
        });
    }


    public static void main(String[] args) {
        //アプリケーションを起動
        new CalculatorApp();
    }
}