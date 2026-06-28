/**
 * 電卓のビュー（画面）とモデル（ロジック）の仲介を行うクラス
 * 画面からのイベントを受け取ってモデルの状態を更新し、その結果を再び画面に反映（描画）
 */
public class CalculatorController {
    /** 制御対象のモデル */
    private CalculatorModel model;
    /** 制御対象のフレーム（ビュー） */
    public CalculatorFrame view;

    /**
     * CalculatorController のコンストラクタ
     * モデルとビューを保持し、ビューに対して自身をイベントの通知先として登録
     *
     * @param model 使用する CalculatorModel のインスタンス
     * @param view  使用する CalculatorFrame のインスタンス
     */
    public CalculatorController(CalculatorModel model, CalculatorFrame view) {
        this.model = model;
        this.view = view;
        view.bindController(this);
    }

    /**
     * モデルから最新の表示用テキストを取得し、ビューのディスプレイを更新
     */
    private void updateView() {
        view.setDisplay(model.getDisplayText());
    }

    /**
     * 数字ボタンが押されたときにappendDigitを呼び出し、ビューを更新
     *
     * @param ch 押された数字の文字
     */
    public void onDigit(char ch) {
        model.appendDigit(ch);
        updateView();
    }

    /**
     * 小数点（.）ボタンが押されたときにappendDotを呼び出し、ビューを更新
     */
    public void onDot() {
        model.appendDot();
        updateView();
    }

    /**
     * 各種演算子（+, -, ×, ÷）ボタンが押されたときにinputOperatorを呼び出し、ビューを更新
     *
     * @param op 押された演算子の種類（Operator型）
     */
    public void onOperator(CalculatorModel.Operator op) {
        model.inputOperator(op);
        updateView();
    }

    /**
     * イコール（=）ボタンが押されたときにequalsOpを呼び出し、ビューを更新
     */
    public void onEquals() {
        model.equalsOp();
        updateView();
    }

    /**
     * クリア（C）ボタンが押されたときにclearAllを呼び出し、ビューを更新
     */
    public void onClear() {
        model.clearAll();
        updateView();
    }
}