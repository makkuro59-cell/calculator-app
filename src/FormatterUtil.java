import java.math.BigDecimal; // 精度の高い数値計算を行うためのBigDecimalクラスをインポート
import java.util.Locale; // 数値フォーマット時のロケール指定のためインポート

public class FormatterUtil {

    /**
     * 画面表示用のテキストをフォーマットする
     * null または空文字の場合は "0" を返す
     */
    public static String formatDisplayText(String text) {
        if (text == null || text.isEmpty()) { // textがnull、または空文字列かどうかを判定
            return "0"; // null/空文字の場合は "0" を返す
        }
        return text; // それ以外はそのまま返す
    }

    /**
     * 計算結果を画面表示用にフォーマット
     * 符号と小数点を除く数字の桁数が maxDigits を超える場合は指数表記に変換
     *
     * @param v         フォーマット対象の BigDecimal
     * @param maxDigits 許容される最大桁数（8桁）
     * @return フォーマット済みの文字列
     */
    public static String formatForDisplay(BigDecimal v, int maxDigits) {
        // 引数がnullかどうかをチェックしnullの場合は "0" を返す
        if (v == null) {
            return "0";
        }

        // 末尾の余分なゼロを取り除いた値を生成
        BigDecimal stripped = v.stripTrailingZeros();

        // 値が0の場合は "0" を返す
        if (stripped.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }

        // 指数表記を使わない通常の文字列表現に変換
        String plainStr = stripped.toPlainString();

        // 数字桁数をカウントする変数を初期化
        int digitCount = 0;
        // 符号(-)や小数点(.)を除いた、数字の桁数をカウント
        for (int i = 0; i < plainStr.length(); i++) {
            char ch = plainStr.charAt(i);
            if (ch >= '0' && ch <= '9') {
                digitCount++;
            }
        }

        // 桁数が上限を超えているかを判定し、桁数が最大桁数（8桁）を超える場合は指数表記に変換
        if (digitCount > maxDigits) {
            // 例: maxDigitsが8の場合、"%.7e" で「整数1桁＋小数7桁＝合計8桁」の係数にする
            String s = String.format(Locale.US, "%." + (maxDigits - 1) + "e", stripped); // 指数表記の文字列を生成（ロケールUS固定）

            // 生成された文字列に "e" が含まれるか確認
            if (s.contains("e")) {
                // "e" を区切り文字として係数部と指数部に分割
                String[] parts = s.split("e");
                // 係数部分（例: "1.2345678"）を取得
                String coeff = parts[0];
                // 指数部分（例: "+02" や "-02"）を取得
                String exp = parts[1];

                if (exp.length() > 1) { // 指数部が符号＋数字で構成されているか確認

                    char sign = exp.charAt(0); // 指数の符号（+ か -）を取得

                    String numStr = exp.substring(1); // 符号を除いた数字部分を取得

                    int expNum = Integer.parseInt(numStr); // 数字部分を整数に変換（先頭ゼロが自動的に消える）

                    s = coeff + "e" + sign + expNum; // 先頭ゼロを除去した指数表記に組み立て直す

                }
            }
            return s; // 指数表記の文字列を返す
        }

        return plainStr; // 桁数が上限以下の場合は通常表記の文字列を返す
    }
}