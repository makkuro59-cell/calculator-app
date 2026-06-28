import java.math.BigDecimal;
import java.util.Locale;

public class FormatterUtil {
    public static String formatDisplayText(String text) {
        if (text == null || text.isEmpty()) {
            return "0";
        }
        return text;
    }

    /**
     * 計算結果を画面表示用にフォーマットします。
     * 符号と小数点を除く純粋な数字の桁数が maxDigits を超える場合は指数表記に変換します。
     *
     * @param v         フォーマット対象の BigDecimal
     * @param maxDigits 許容される最大桁数（8桁）
     * @return フォーマット済みの文字列
     */
    public static String formatForDisplay(BigDecimal v, int maxDigits) {
        if (v == null) {
            return "0";
        }

        // 不要な末尾のゼロを削除 (例: 1.2300 -> 1.23)
        BigDecimal stripped = v.stripTrailingZeros();

        // 値が0の場合はシンプルに "0" を返す
        if (stripped.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }

        String plainStr = stripped.toPlainString();

        // 符号(-)や小数点(.)を除いた、純粋な数字の桁数をカウント
        int digitCount = 0;
        for (int i = 0; i < plainStr.length(); i++) {
            char ch = plainStr.charAt(i);
            if (ch >= '0' && ch <= '9') {
                digitCount++;
            }
        }

        // 桁数が最大桁数（8桁）を超える場合は指数表記に変換
        if (digitCount > maxDigits) {
            // 例: maxDigitsが8の場合、"%.7e" で「整数1桁＋小数7桁＝合計8桁」の係数にする
            String s = String.format(Locale.US, "%." + (maxDigits - 1) + "e", stripped);

            // Javaのデフォルトの指数表記（e+02, e-02など）から先行する不要なゼロを除去 (e-02 -> e-2)
            if (s.contains("e")) {
                String[] parts = s.split("e");
                String coeff = parts[0];
                String exp = parts[1]; // 例: "+02", "-02"
                if (exp.length() > 1) {
                    char sign = exp.charAt(0); // '+' または '-'
                    String numStr = exp.substring(1);
                    int expNum = Integer.parseInt(numStr);
                    s = coeff + "e" + sign + expNum;
                }
            }
            return s;
        }

        return plainStr;
    }
}
