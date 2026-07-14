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
            // 渡された文字列（cmd）の値に応じて、対応する Operator 列挙型を返す
            return switch (cmd) {
                case "+" -> ADD;
                case "-" -> SUB;
                case "×" -> MUL;
                case "÷" -> DIV;
                default -> throw new IllegalArgumentException("演算子以外: " + cmd);
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

    private boolean justCleared = true;

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
    public boolean appendDigit(char ch) {
        // 現在の電卓の状態が「ERROR」だった場合の処理
        if (state == InputState.ERROR)
            // これ以降の処理を何もせず終了
            return false;

        // 現在の状態が「READY」または「INPUT_OPERATOR」だった場合の処理
        if (state == InputState.READY || state == InputState.INPUT_OPERATOR) {
            // 新しい数字の入力を新しく始めるため、入力用バッファ（currentInput）を桁数0にリセット
            currentInput.setLength(0);

            // 電卓の状態を「INPUT_NUMBER」へ切り替え
            state = InputState.INPUT_NUMBER;
        }

        // 桁数を数えるためのカウンター
        int count = 0;

        if (currentInput.length() == 0 && ch == '0') {
            return false;

        }

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
            justCleared = false;
        }
        return true;
    }

    /**
     * 現在の入力文字列に小数点を追加
     * すでに小数点が含まれている場合は追加しない
     * 新規入力の最初に押された場合は自動的に "0." から開始
     */
    public boolean appendDot() {
        if (justCleared == true) {
            return false;
        }
        // 現在の電卓の状態が「ERROR」だった場合の処理
        if (state == InputState.ERROR)
            // これ以降の処理を何もせず終了
            return  false;

        // 現在の状態が「READY」または「INPUT_OPERATOR」だった場合の処理
        if (state == InputState.READY || state == InputState.INPUT_OPERATOR) {
            return false;

        }

        // 入力用バッファの中にすでに小数点「.」が含まれているかを確認
        if (currentInput.indexOf(".") == -1) {
            // 小数点が含まれていない場合のみ、入力用バッファの末尾に小数点を追加
            currentInput.append(".");
            return true;
        }
        return false;
    }

    /**
     * 演算子が入力されたときの処理を行います。
     * すでに数値が入力されている場合は、保留中の計算を先に実行（連鎖計算）した上で、
     * 新しい演算子を保留状態（pendingOP）にします。
     *
     * @param op 入力された演算子（Operator型）
     */
    public void inputOperator(Operator op) {
        // 現在の電卓の状態が「ERROR」だった場合の処理
        if (state == InputState.ERROR)
            // これ以降の処理を何もせず終了
            return;

        if (op == Operator.SUB && currentInput.length() == 1 && currentInput.charAt(0) == '-') {
            return;
        }

        if (currentInput.length() == 0 && op == Operator.SUB && justCleared == true) {
            currentInput.append("-");
            state = InputState.INPUT_NUMBER;
            justCleared = false;
            return;

        }

        if (currentInput.length() == 0 && pendingOP == null && justCleared) {
            // まだ何も入力・計算していない状態での演算子入力は無視
            return;
        }

        // 入力用バッファに数値が入力されているかを確認
        if (currentInput.length() > 0) {
            // 数値が入力されている場合は、保留中の計算を先に実行
            equalsOp();
        }

        // 新しく押された演算子を保留中の演算子として保持
        pendingOP = op;

        // 電卓の状態を「INPUT_OPERATOR」へ切り替え
        state = InputState.INPUT_OPERATOR;
    }

    /**
     * イコール（=）が押されたとき、または演算子の連鎖時に、保留中の計算を実行します。
     * 計算結果は左辺（leftOperand）に格納され、入力バッファはクリアされます。
     */
    public void equalsOp() {
        // 現在の電卓の状態が「ERROR」だった場合の処理
        if (state == InputState.ERROR)
            // これ以降の処理を何もせず終了
            return;

        // 入力用バッファが空（右辺が未入力）かどうかを確認
        if (currentInput.length() == 0) {
            // 右辺が未入力の場合は、これ以降の処理を行わず終了
            return;
        }

        // 入力用バッファの文字列を BigDecimal型に変換し、右辺の値として取得
        BigDecimal rightOperand = new BigDecimal(currentInput.toString());

        // 保留中の演算子（pendingOP）が設定されていないかどうかを確認
        if (pendingOP == null) {
            // 演算子が未設定の場合は、右辺の値をそのまま左辺（leftOperand）に格納
            leftOperand = rightOperand;
        } else {
            try {
                // 保留中の演算子を使って、左辺と右辺の計算を実行し、結果を左辺（leftOperand）に格納
                leftOperand = apply(leftOperand, rightOperand, pendingOP);
            } catch (ArithmeticException e) {
                // ゼロ除算などの計算エラーが発生した場合、状態を「ERROR」に変更
                state = InputState.ERROR;

                // エラー処理を行うための ErrorHandler のインスタンスを生成
                ErrorHandler errorHandler = new ErrorHandler();

                // 発生した例外をエラーハンドラーに渡して処理
                errorHandler.handle(e);

                // エラー発生時はこれ以降の処理を行わず終了
                return;
            }
        }

        // 計算が完了したため、保留中の演算子をリセット
        pendingOP = null;

        // 入力用バッファをリセットし、次の入力に備える
        currentInput.setLength(0);

        // 電卓の状態を「READY」へ切り替え
        state = InputState.READY;

        justCleared = false;
    }

    /**
     * すべての内部状態をクリアし、初期状態（値は0、READY状態）に戻します。
     * オールクリア（C）ボタンが押された際に呼び出されます。
     */
    public void clearAll() {
        justCleared = true;
        // 左辺の値（leftOperand）を「0（ZERO）」にリセット
        leftOperand = BigDecimal.ZERO;

        // 入力用バッファ（currentInput）をリセット
        currentInput.setLength(0);

        // 保留中の演算子（pendingOP）をリセット
        pendingOP = null;

        // 電卓の状態を「READY（準備完了）」に戻す
        state = InputState.READY;
    }

    /**
     * 画面のテキスト表示用に、現在の適切な表示文字列（入力中の数値、または計算結果）を組み立てて返す。
     * 計算結果の表示時には {@link FormatterUtil} を使用して適切なフォーマットや指数変換を行う
     *
     * @return 画面に表示すべきフォーマット済みの文字列
     */
    public String getDisplayText() {
        // 現在の電卓の状態が「ERROR」だった場合の処理
        if (state == InputState.ERROR) {
            // エラー状態の場合は「エラー」という文字列を返す
            return "エラー";
        }

        // 表示用の文字列を組み立てるための StringBuilder を生成
        StringBuilder display = new StringBuilder();
        if (pendingOP == null) {
            if (currentInput.length() == 0) {
                display.append(FormatterUtil.formatForDisplay(leftOperand, maxDigits));
            } else {
                display.append(currentInput.toString());

            }
        }

        // 保留中の演算子があるかを確認
        if (pendingOP != null) {

            if (currentInput.length() == 0) {
                display.append(getDisplayString());
            }

            if (currentInput.length() > 0) {
                display.append(getDisplayString());
                // 演算子の前に半角スペースを追加
                display.append(" ");
                // 入力中の文字列がある場合は、そのまま表示用に追加
                display.append(currentInput.toString());
            }

        }

        // 組み立てた表示用の文字列を返す
        return display.toString();
    }

    /**
     * 2つの値（左辺、右辺）と演算子を元に、実際の四則演算を実行
     * ゼロ除算が発生した場合は、状態を ERROR に変更し0 を返す。
     *
     * @param left  左辺の値
     * @param right 右辺の値
     * @param op    実行する演算子
     * @return 計算結果の BigDecimal
     */
    public BigDecimal apply(BigDecimal left, BigDecimal right, Operator op) {
        // 演算子の種類に応じて処理を分岐
        switch (op) {
            case ADD:
                // 加算（+）の場合、左辺と右辺を足した結果を返す
                return left.add(right);
            case SUB:
                // 減算（-）の場合、左辺から右辺を引いた結果を返す
                return left.subtract(right);
            case MUL:
                // 乗算（×）の場合、左辺と右辺を掛けた結果を返す
                return left.multiply(right);
            case DIV:
                // 除算（÷）の場合、左辺を右辺で割った結果を返す
                return left.divide(right, maxDigits, java.math.RoundingMode.HALF_UP);
            default:
                // 想定外の演算子だった場合は「0」を返す
                return BigDecimal.ZERO;
        }
    }

    /**
     * 内部の Operator 列挙型を、画面表示用の記号文字列に変換します。
     *
     * @param op 変換対象の Operator
     * @return 演算子記号の文字列 ("+", "-", "×", "÷")
     */
    private String operatorToSymbol(Operator op) {
        // 内部の演算子（Operator）を対応する記号文字列に変換して返す
        return switch (op) {
            case ADD -> "+";
            case SUB -> "-";
            case MUL -> "×";
            case DIV -> "÷";
        };
    }

    private String getDisplayString() {
        StringBuilder display = new StringBuilder();
        // 入力中の文字列が空の場合は、左辺の値を表示用に追加
        display.append(FormatterUtil.formatForDisplay(leftOperand, maxDigits));
        // 演算子の前に半角スペースを追加
        display.append(" ");
        // 保留中の演算子を記号文字列に変換して表示用に追加
        display.append(operatorToSymbol(pendingOP));
        return display.toString();
    }
}