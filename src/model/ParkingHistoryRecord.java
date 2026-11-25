package model;

import java.time.LocalDateTime;
import util.ConsoleFormatter;
import util.DateTimeHelper;

/**
 * Completed parking record stored in history and persisted to disk.
 */
public class ParkingHistoryRecord implements Comparable<ParkingHistoryRecord> {

    private final String ticketId;
    private final String vehicleNumber;
    private final String vehicleType;
    private final String slotId;
    private final LocalDateTime entryTime;
    private final LocalDateTime exitTime;
    private final long billedHours;
    private final double feePaid;

    public ParkingHistoryRecord(
            String ticketId,
            String vehicleNumber,
            String vehicleType,
            String slotId,
            LocalDateTime entryTime,
            LocalDateTime exitTime,
            long billedHours,
            double feePaid) {
        this.ticketId = ticketId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.slotId = slotId;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.billedHours = billedHours;
        this.feePaid = feePaid;
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

    public long getBilledHours() {
        return billedHours;
    }

    public double getFeePaid() {
        return feePaid;
    }

    /**
     * Natural ordering places the most recent exit first.
     */
    @Override
    public int compareTo(ParkingHistoryRecord other) {
        int timeCompare = other.exitTime.compareTo(this.exitTime);
        if (timeCompare != 0) {
            return timeCompare;
        }
        return other.ticketId.compareTo(this.ticketId);
    }

    @Override
    public String toString() {
        return String.format(
                "%-9s | %-12s | %-5s | %-7s | %-19s -> %-19s | %2d hr(s) | %s",
                ticketId,
                vehicleNumber,
                vehicleType,
                slotId,
                DateTimeHelper.format(entryTime),
                DateTimeHelper.format(exitTime),
                billedHours,
                ConsoleFormatter.formatCurrency(feePaid));
    }
}
