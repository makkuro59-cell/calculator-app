import java.math.BigDecimal;

/**
 * 電卓の内部データ、計算ロジック、および入力状態を管理するクラス
 * {@link BigDecimal} を使用
 */
public class CalculatorModel {
    /** 左辺の値、またはこれまでの計算結果を保持する変数 */
    private BigDecimal leftOperand;
    /** 現在ユーザーが入力中の文字列を保持する変数 */
    private StringBuilder currentInput;

    /**
     * 四則演算の演算子を定義する列挙型
     */
    public enum Operator {
        /** 加算（+） */
        ADD,
        /** 減算（-） */
        SUB,
        /** 乗算（×） */
        MUL,
        /** 除算（÷） */
        DIV;

        /**
         * 画面上のボタン文字に対応する演算子の列挙型を返す
         *
         * @param cmd 演算子を表す文字列（"+", "-", "×", "÷"）
         * @return 対応する Operator 列挙型
         * @throws IllegalArgumentException （"+", "-", "×", "÷"）以外の演算子文字列が渡された場合
         */
        public static Operator fromString(String cmd) {
            return switch (cmd) {
                case "+" -> ADD;
                case "-" -> SUB;
                case "×" -> MUL;
                case "÷" -> DIV;
                default -> throw new IllegalArgumentException("未知の演算子: " + cmd);
            };
        }
    }

    /** 次に数字が入力されたとき、またはイコールが押されたときに適用される保留中の演算子 */
    private Operator pendingOP;

    /**
     * 電卓の入力・計算状態を表す列挙型
     */
    public enum InputState {
        /** 初期状態、またはクリア直後・計算完了後の状態 */
        READY,
        /** ユーザーが数値を入力している最中の状態 */
        INPUT_NUMBER,
        /** 演算子が入力され、次の数値入力を待っている状態 */
        INPUT_OPERATOR,
        /** ゼロ除算などの計算エラーが発生した状態 */
        ERROR
    }

    /** 電卓の現在の状態 */
    private InputState state;
    /** 入力および計算で許容される最大桁数（8桁） */
    private final int maxDigits = 8;

    /**
     * CalculatorModel のコンストラクタ
     * 内部データを初期化し、電卓を READY 状態にする
     */
    public CalculatorModel() {
        // これまでの計算結果や左辺の値を保持する leftOperand を「0（ZERO）」にリセット
        this.leftOperand = BigDecimal.ZERO;

        // ユーザーが現在キーパッドで入力している数字の文字列を溜めておくためのバッファ（StringBuilder）を生成
        this.currentInput = new StringBuilder();

        // 電卓の現在の入力状態（state）を、操作を受け付けられる「READY（準備完了）」状態に設定
        this.state = InputState.READY;
    }

    /**
     * 入力された数字（0～9）を現在の入力文字列に追加
     * 符号と小数点を除いた純粋な数字の桁数が maxDigits（8桁）未満の場合のみ追加する
     *
     * @param ch 入力された数字の文字
     */
    public void appendDigit(char ch) {
        // 現在の電卓の状態が「ERROR」だった場合の処理
        if (state == InputState.ERROR)
            // これ以降の処理を何もせず終了
            return;

        // 現在の状態が「READY」または「INPUT_OPERATOR」だった場合の処理
        if (state == InputState.READY || state == InputState.INPUT_OPERATOR) {
            // 新しい数字の入力を新しく始めるため、入力用バッファ（currentInput）を桁数0にリセット
            currentInput.setLength(0);

            // 電卓の状態を「INPUT_NUMBER」へ切り替え
            state = InputState.INPUT_NUMBER;
        }

        // 桁数を数えるためのカウンター
        int count = 0;

        // 現在入力されている文字列を、1文字目から順番に最後の文字までループ処理で確認
        for (int i = 0; i < currentInput.length(); i++) {
            // 取り出した文字が、小数点「.」ではなく、かつマイナス記号「-」でもない場合の処理
            if (currentInput.charAt(i) != '.' && currentInput.charAt(i) != '-') {
                // 小数点「.」ではなく、かつマイナス記号「-」でもない場合桁数のカウントを 1 増やす
                count++;
            }
        }

        // 数えた数字の桁数が、最大桁数（maxDigits）より小さいか確認
        if (count < maxDigits) {
            // 8桁未満であれば、入力用バッファの末尾に新しく押された数字（ch）を追加（8桁以上のときは無視する）
            currentInput.append(ch);
        }
    }

    /**
     * 現在の入力文字列に小数点を追加
     * すでに小数点が含まれている場合は追加しない
     * 新規入力の最初に押された場合は自動的に "0." から開始
     */
    public void appendDot() {
        if (state == InputState.ERROR)
            return;
        if (state == InputState.READY || state == InputState.INPUT_OPERATOR) {
            currentInput.setLength(0);
            currentInput.append("0");
            state = InputState.INPUT_NUMBER;
        }
        if (currentInput.indexOf(".") == -1) {
            currentInput.append(".");
        }
    }

    /**
     * 演算子が入力されたときの処理を行います。
     * すでに数値が入力されている場合は、保留中の計算を先に実行（連鎖計算）した上で、
     * 新しい演算子を保留状態（pendingOP）にします。
     *
     * @param op 入力された演算子（Operator型）
     */
    public void inputOperator(Operator op) {
        if (state == InputState.ERROR)
            return;
        if (currentInput.length() > 0) {
            equalsOp();
        }
        pendingOP = op;
        state = InputState.INPUT_OPERATOR;
    }

    /**
     * イコール（=）が押されたとき、または演算子の連鎖時に、保留中の計算を実行します。
     * 計算結果は左辺（leftOperand）に格納され、入力バッファはクリアされます。
     */
    public void equalsOp() {
        if (state == InputState.ERROR)
            return;
        if (currentInput.length() == 0) {
            return;
        }
        BigDecimal rightOperand = new BigDecimal(currentInput.toString());
        if (pendingOP == null) {
            leftOperand = rightOperand;
        } else {
            try {
                leftOperand = calculate(leftOperand, rightOperand, pendingOP);
            } catch (ArithmeticException e) {
                state = InputState.ERROR;
                ErrorHandler errorHandler = new ErrorHandler();
                errorHandler.handle(e);
                return;
            }
        }
        pendingOP = null;
        currentInput.setLength(0);
        state = InputState.READY;
    }

    /**
     * すべての内部状態をクリアし、初期状態（値は0、READY状態）に戻します。
     * オールクリア（C）ボタンが押された際に呼び出されます。
     */
    public void clearAll() {
        leftOperand = BigDecimal.ZERO;
        currentInput.setLength(0);
        pendingOP = null;
        state = InputState.READY;
    }

    /**
     * 画面のテキスト表示用に、現在の適切な表示文字列（入力中の数値、または計算結果）を組み立てて返します。
     * 計算結果の表示時には {@link FormatterUtil} を使用して適切なフォーマットや指数変換を行います。
     *
     * @return 画面に表示すべきフォーマット済みの文字列
     */
    public String getDisplayText() {
        if (state == InputState.ERROR) {
            return "エラー";
        }
        StringBuilder display = new StringBuilder();
        if (currentInput.length() > 0) {
            display.append(currentInput.toString());
        } else {
            display.append(FormatterUtil.formatForDisplay(leftOperand, maxDigits));
        }
        if (pendingOP != null && state == InputState.INPUT_OPERATOR) {
            display.append(" ");
            display.append(operatorToSymbol(pendingOP));
        }
        return display.toString();
    }

    /**
     * 2つの値（左辺、右辺）と演算子を元に、実際の四則演算を実行します。
     * ゼロ除算が発生した場合は、状態を ERROR に変更し、0 を返します。
     *
     * @param left  左辺の値
     * @param right 右辺の値
     * @param op    実行する演算子
     * @return 計算結果の BigDecimal
     */
    private BigDecimal calculate(BigDecimal left, BigDecimal right, Operator op) {
        switch (op) {
            case ADD:
                return left.add(right);
            case SUB:
                return left.subtract(right);
            case MUL:
                return left.multiply(right);
            case DIV:
                return left.divide(right, maxDigits, java.math.RoundingMode.HALF_UP);
            default:
                return BigDecimal.ZERO;
        }
    }

    /**
     * 現在、電卓が操作を受け付けられる状態かどうかを判定します。
     *
     * @return 操作可能な場合は true、エラー状態の場合は false
     */
    private boolean isActionAllowed() {
        if (state == InputState.ERROR) {
            return false;
        }
        return true;
    }

    /**
     * 内部の Operator 列挙型を、画面表示用の記号文字列に変換します。
     *
     * @param op 変換対象の Operator
     * @return 演算子記号の文字列 ("+", "-", "×", "÷")
     */
    private String operatorToSymbol(Operator op) {
        return switch (op) {
            case ADD -> "+";
            case SUB -> "-";
            case MUL -> "×";
            case DIV -> "÷";
        };
    }
}