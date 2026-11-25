package util;

/**
 * Thread-safe generator for unique ticket, receipt, and user IDs using static counters.
 */
public final class TicketIdGenerator {

    private static int ticketSequence = 1000;
    private static int receiptSequence = 5000;
    private static int userSequence = 100;

    private TicketIdGenerator() {
    }

    public static synchronized String nextTicketId() {
        ticketSequence++;
        return "TKT-" + ticketSequence;
    }

    public static synchronized String nextReceiptId() {
        receiptSequence++;
        return "RCP-" + receiptSequence;
    }

    public static synchronized String nextUserId() {
        userSequence++;
        return "USR-" + userSequence;
    }

    /**
     * Advances the static ticket counter when records are loaded from disk
     * so newly generated IDs never collide with existing tickets.
     */
    public static synchronized void syncTicketSequence(String existingTicketId) {
        int parsed = extractNumericSuffix(existingTicketId, "TKT-");
        if (parsed > ticketSequence) {
            ticketSequence = parsed;
        }
    }

    public static synchronized void syncUserSequence(String existingUserId) {
        int parsed = extractNumericSuffix(existingUserId, "USR-");
        if (parsed > userSequence) {
            userSequence = parsed;
        }
    }

    private static int extractNumericSuffix(String id, String prefix) {
        if (id == null || !id.startsWith(prefix)) {
            return -1;
        }
        try {
            return Integer.parseInt(id.substring(prefix.length()).trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
