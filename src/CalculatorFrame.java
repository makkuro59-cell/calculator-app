import javax.swing.*;
import java.awt.*;

/**
 * 電卓のUIを表示するメインウィンドウクラス
 * 画面のレイアウト、ボタンの配置、およびボタン押下時のイベントハンドリングを行う
 */
public class CalculatorFrame extends JFrame {
    /** 計算結果や入力中の数値を表示するラベル */
    private JLabel displayLabel;
    /** ボタン（キーパッド）を配置するパネル */
    private JPanel keypadPanel;

    /**
     * CalculatorFrame のコンストラクタ
     * ウィンドウタイトルの設定、レイアウトの初期化、およびボタンの配置
     */
    public CalculatorFrame() {
        // ウィンドウのタイトルを "Calculator" に設定
        setTitle("Calculator");

        // ウィンドウの「×」ボタンが押されたときに終了させる処理
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ウィンドウの初期サイズを、横幅400ピクセル、縦幅600ピクセルに設定
        setSize(400, 600);

        // 画面全体の配置方法を BorderLayout（上下左右中央に分けて配置する形式）に設定
        // それぞれの隙間を10ピクセルに設定（横方向、縦方向）
        setLayout(new BorderLayout(10, 10));

        // ウィンドウの表示位置をパソコン画面の中央に設定
        setLocationRelativeTo(null);

        // 計算結果や入力値を表示するためのラベル（JLabel）を生成
        // 初期テキストは "0" で、右詰め（SwingConstants.RIGHT）で表示
        this.displayLabel = new JLabel("0", SwingConstants.RIGHT);

        // 生成したディスプレイ用ラベルを、ウィンドウの最上部（BorderLayout.NORTH）に配置
        add(this.displayLabel, BorderLayout.NORTH);

        // フォントを「Arial」、太字（Font.BOLD）、サイズ「36」に設定
        displayLabel.setFont(new Font("Arial", Font.BOLD, 36));

        // ディスプレイの文字の周囲に余白（上・左・下・右に各20ピクセル）を設定
        displayLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ボタンを並べるためのパネル（JPanel）を、GridLayout（格子状のレイアウト）を使って生成
        // 電卓用に「5行 × 4列」のマス目を作り、ボタン同士の隙間を縦横5ピクセルに設定しています
        this.keypadPanel = new JPanel(new GridLayout(5, 4, 5, 5));

        // 電卓に配置するボタンの文字を、左上から順番に並べた文字列の配列を定義
        String[] buttons = {
                "7", "8", "9", "÷",
                "4", "5", "6", "×",
                "1", "2", "3", "-",
                "0", ".", "=", "+",
                "C"
        };

        // 上記で定義したボタンの文字配列を、拡張for文を使って1つずつ取り出して処理
        for (String text : buttons) {
            // 取り出した文字や演算子が書かれたボタン（JButton）を生成
            JButton button = new JButton(text);

            // ボタンに表示される文字のフォントを「Arial」、太字（Font.BOLD）、サイズ「24」に設定
            button.setFont(new Font("Arial", Font.BOLD, 24));

            // 生成してデザインを整えたボタンを、格子状のレイアウトパネル（keypadPanel）に順番に追加
            this.keypadPanel.add(button);
        }

        // すべてのボタンが詰まったパネルを、ウィンドウの中央エリア（BorderLayout.CENTER）に配置
        add(this.keypadPanel, BorderLayout.CENTER);
    }

    /**
     * 電卓のディスプレイ表示文字列を更新
     *
     * @param text ディスプレイに表示する文字列
     */
    public void setDisplay(String text) {
        this.displayLabel.setText(text);
    }

    /**
     * 各ボタンに対して、コントローラーを紐付け
     *
     * @param c イベントを処理する CalculatorController のインスタンス
     */
    public void bindController(CalculatorController c) {
        // keypadPanel（ボタンが配置されているパネル）の中にあるすべてのコンポーネント（数字や演算子）を1つずつ取り出してループ処理
        for (Component comp : keypadPanel.getComponents()) {

            // 取り出した部品（comp）が「ボタン（JButton）」ではない場合の処理
            // instanceof を使って型を判定し、ボタン以外（パネル内の余白など）であればスキップ
            if (!(comp instanceof JButton))
                // これ以降の処理を行わず、次の処理へ進む
                continue;

            // 取り出した部品をComponent型から JButton型に変換
            JButton button = (JButton) comp;

            // ボタンがクリックされたときの動作を登録
            // e.getActionCommand() でボタンに書かれた文字（数字 や 演算子 など）を取得し、それを handleButtonClickメソッドに渡す
            button.addActionListener(e -> handleButtonClick(e.getActionCommand(), c));
        }
    }

    /**
     * クリックされたボタンの種類（コマンド）を判定し、対応するコントローラーのメソッドを呼び出す
     *
     * @param cmd クリックされたボタンのテキスト文字列（数字や演算子など）
     * @param c   イベントの通知先となる CalculatorController のインスタンス
     */
    public void handleButtonClick(String cmd, CalculatorController c) {
        switch (cmd) {
            case "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" -> c.onDigit(cmd.charAt(0));
            case "." -> c.onDot();
            case "+", "-", "×", "÷" -> c.onOperator(CalculatorModel.Operator.fromString(cmd));
            case "=" -> c.onEquals();
            case "C" -> c.onClear();
        }
    }
}