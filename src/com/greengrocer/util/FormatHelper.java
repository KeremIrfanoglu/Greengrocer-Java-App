package com.greengrocer.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility class for consistent number and currency formatting across the
 * application.
 */
public class FormatHelper {

    private static final String CURRENCY_SYMBOL = " TL";
    private static final DecimalFormat currencyFormat;
    private static final DecimalFormat numberFormat;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');

        // Use 2 decimal places for currency/prices (e.g., 12.50 TL)
        currencyFormat = new DecimalFormat("#,##0.00' TL'", symbols);

        // Use 3 decimal places for quantities/weights (e.g., 1.500 kg)
        numberFormat = new DecimalFormat("#,##0.000", symbols);
    }

    /**
     * Formats a double value as currency (e.g., 1,234.56 TL).
     */
    public static String formatCurrency(double value) {
        return currencyFormat.format(value);
    }

    /**
     * Formats a double value with a prefix (e.g., -50.00 TL).
     */
    public static String formatCurrencyWithPrefix(double value, String prefix) {
        if (value < 0) {
            return "-" + currencyFormat.format(Math.abs(value));
        }
        return prefix + currencyFormat.format(value);
    }

    /**
     * Formats a quantity/number (e.g., 1.5).
     */
    public static String formatNumber(double value) {
        return numberFormat.format(value);
    }

    /**
     * Get the currency symbol being used.
     */
    public static String getCurrencySymbol() {
        return CURRENCY_SYMBOL;
    }
}
