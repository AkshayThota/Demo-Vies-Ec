package in.juspay.viesdemo.util;

import android.text.Editable;
import android.text.TextWatcher;

public class CardTextWatcher implements TextWatcher {
    private final char divider;
    private final int dividerPosition;

    public CardTextWatcher(char divider, int dividerPosition) {
        this.divider = divider;
        this.dividerPosition = dividerPosition;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() == 0) {
            return;
        }

        if (!isInputCorrect(s)) {
            s.replace(0, s.length(), buildCorrectString(getDigitArray(s)));
        }

        if (s.charAt(s.length() - 1) == divider) {
            s.replace(s.length() - 1, s.length(), "");
        }
    }

    private boolean isInputCorrect(Editable s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if ((i + 1) % (dividerPosition + 1) == 0) {
                if (c != divider) {
                    return false;
                } else {
                    continue;
                }
            }

            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

    private String buildCorrectString(char[] digits) {
        final StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < digits.length; i++) {
            if (digits[i] != 0) {
                formatted.append(digits[i]);
                if (i > 0 && i < (digits.length - 1) && ((i + 1) % dividerPosition) == 0) {
                    formatted.append(divider);
                }
            }
        }

        return formatted.toString();
    }

    private char[] getDigitArray(final Editable s) {
        char[] digits = new char[s.length()];

        for (int i = 0, index = 0; i < s.length(); i++) {
            char current = s.charAt(i);
            if (Character.isDigit(current)) {
                digits[index] = current;
                index++;
            }
        }

        return digits;
    }
}
