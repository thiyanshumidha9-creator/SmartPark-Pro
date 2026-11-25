package model;

import java.time.LocalDateTime;
import service.Reportable;
import util.ConsoleFormatter;
import util.DateTimeHelper;

/**
 * Detailed billing receipt generated when a vehicle exits the mall parking.
 */
public class ParkingReceipt implements Reportable {

    private final String receiptId;
    private final String ticketId;
    private final String vehicleNumber;
    private final String vehicleType;
    private final String slotId;
    private final LocalDateTime entryTime;
    private final LocalDateTime exitTime;
    private final long totalMinutes;
    private final long billedHours;
    private final double hourlyRate;
    private final double totalFee;

    public ParkingReceipt(
            String receiptId,
            String ticketId,
            String vehicleNumber,
            String vehicleType,
            String slotId,
            LocalDateTime entryTime,
            LocalDateTime exitTime,
            long totalMinutes,
            long billedHours,
            double hourlyRate,
            double totalFee) {
        this.receiptId = receiptId;
        this.ticketId = ticketId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.slotId = slotId;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.totalMinutes = totalMinutes;
        this.billedHours = billedHours;
        this.hourlyRate = hourlyRate;
        this.totalFee = totalFee;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public String getSlotId() {
        return slotId;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public long getTotalMinutes() {
        return totalMinutes;
    }

    public long getBilledHours() {
        return billedHours;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public double getTotalFee() {
        return totalFee;
    }

    @Override
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(ConsoleFormatter.subHeader("SMARTPARK PRO - EXIT RECEIPT")).append('\n');
        sb.append("  Receipt ID   : ").append(receiptId).append('\n');
        sb.append("  Ticket ID    : ").append(ticketId).append('\n');
        sb.append("  Vehicle      : ").append(vehicleNumber).append(" (").append(vehicleType).append(")\n");
        sb.append("  Slot Freed   : ").append(slotId).append('\n');
        sb.append("  Entry Time   : ").append(DateTimeHelper.format(entryTime)).append('\n');
        sb.append("  Exit Time    : ").append(DateTimeHelper.format(exitTime)).append('\n');
        sb.append("  Duration     : ").append(totalMinutes).append(" minute(s)\n");
        sb.append("  Billed Hours : ").append(billedHours).append(" hour(s) (started hour billed full)\n");
        sb.append("  Hourly Rate  : ").append(ConsoleFormatter.formatCurrency(hourlyRate)).append("/hr\n");
        sb.append("  Total Fee    : ").append(ConsoleFormatter.formatCurrency(totalFee)).append('\n');
        sb.append(ConsoleFormatter.divider('-'));
        return sb.toString();
    }
}
