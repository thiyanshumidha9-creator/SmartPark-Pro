package util;

/**
 * Formats banners, section headings, dividers, and currency values using StringBuilder.
 */
public final class ConsoleFormatter {

    private static final int LINE_WIDTH = 66;

    private ConsoleFormatter() {
    }

    public static String divider(char symbol) {
        StringBuilder sb = new StringBuilder(LINE_WIDTH);
        for (int i = 0; i < LINE_WIDTH; i++) {
            sb.append(symbol);
        }
        return sb.toString();
    }

    public static String header(String title) {
        StringBuilder sb = new StringBuilder();
        sb.append('\n').append(divider('=')).append('\n');
        sb.append("  ").append(title).append('\n');
        sb.append(divider('='));
        return sb.toString();
    }

    public static String subHeader(String title) {
        StringBuilder sb = new StringBuilder();
        sb.append('\n').append(divider('-')).append('\n');
        sb.append("  ").append(title).append('\n');
        sb.append(divider('-'));
        return sb.toString();
    }

    public static String formatCurrency(double amount) {
        return String.format("%s%.2f", ParkingConstants.CURRENCY_SYMBOL, amount);
    }
}
