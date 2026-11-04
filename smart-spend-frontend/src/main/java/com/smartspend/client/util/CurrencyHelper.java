package com.smartspend.client.util;

import com.smartspend.client.config.AppConfig;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyHelper {

    private static final DecimalFormat INDIAN_CURRENCY_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("en", "IN"));
        symbols.setCurrencySymbol(AppConfig.CURRENCY_SYMBOL);
        INDIAN_CURRENCY_FORMAT = new DecimalFormat("##,##,##0.00", symbols);
    }

    public static String format(BigDecimal amount) {
        if (amount == null) {
            return AppConfig.CURRENCY_SYMBOL + "0.00";
        }
        return AppConfig.CURRENCY_SYMBOL + INDIAN_CURRENCY_FORMAT.format(amount);
    }

    public static String format(double amount) {
        return format(BigDecimal.valueOf(amount));
    }
}
