package model;

import java.time.LocalDateTime;
import service.Reportable;
import util.DateTimeHelper;

/**
 * Immutable, thread-safe snapshot of current parking availability.
 * Returned by ParkingManager so ParkingMonitor can read status without holding locks or mutating state.
 */
public class AvailabilitySnapshot implements Reportable {

    private final LocalDateTime capturedAt;
    private final int totalSlots;
    private final int occupiedSlots;
    private final int freeSlots;
    private final int[] freePerFloor;
    private final int[] totalPerFloor;
    private final int freeBikeSlots;
    private final int freeCarSlots;
    private final int freeTruckSlots;
    private final int freeGeneralSlots;
    private final int activeTicketCount;

    public AvailabilitySnapshot(
            LocalDateTime capturedAt,
            int totalSlots,
            int occupiedSlots,
            int[] freePerFloor,
            int[] totalPerFloor,
            int freeBikeSlots,
            int freeCarSlots,
            int freeTruckSlots,
            int freeGeneralSlots,
            int activeTicketCount) {
        this.capturedAt = capturedAt;
        this.totalSlots = totalSlots;
        this.occupiedSlots = occupiedSlots;
        this.freeSlots = totalSlots - occupiedSlots;
        this.freePerFloor = freePerFloor.clone();
        this.totalPerFloor = totalPerFloor.clone();
        this.freeBikeSlots = freeBikeSlots;
        this.freeCarSlots = freeCarSlots;
        this.freeTruckSlots = freeTruckSlots;
        this.freeGeneralSlots = freeGeneralSlots;
        this.activeTicketCount = activeTicketCount;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public int getOccupiedSlots() {
        return occupiedSlots;
    }

    public int getFreeSlots() {
        return freeSlots;
    }

    public int[] getFreePerFloor() {
        return freePerFloor.clone();
    }

    public int[] getTotalPerFloor() {
        return totalPerFloor.clone();
    }

    public int getFreeBikeSlots() {
        return freeBikeSlots;
    }

    public int getFreeCarSlots() {
        return freeCarSlots;
    }

    public int getFreeTruckSlots() {
        return freeTruckSlots;
    }

    public int getFreeGeneralSlots() {
        return freeGeneralSlots;
    }

    public int getActiveTicketCount() {
        return activeTicketCount;
    }

    public String toCompactLine() {
        return String.format(
                "[%s] Free: %d/%d | Bike:%d Car:%d Truck:%d Gen:%d | Active Tickets:%d",
                DateTimeHelper.format(capturedAt),
                freeSlots,
                totalSlots,
                freeBikeSlots,
                freeCarSlots,
                freeTruckSlots,
                freeGeneralSlots,
                activeTicketCount);
    }

    @Override
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("  Captured At   : ").append(DateTimeHelper.format(capturedAt)).append('\n');
        sb.append("  Total Slots   : ").append(totalSlots)
                .append("  (Occupied: ").append(occupiedSlots)
                .append(", Available: ").append(freeSlots).append(")\n");
        sb.append("  By Slot Type  : Bike Free=").append(freeBikeSlots)
                .append(", Car Free=").append(freeCarSlots)
                .append(", Truck Free=").append(freeTruckSlots)
                .append(", General Free=").append(freeGeneralSlots).append('\n');
        for (int i = 0; i < freePerFloor.length; i++) {
            sb.append("  Floor ").append(i + 1).append("       : ")
                    .append(freePerFloor[i]).append("/").append(totalPerFloor[i]).append(" available\n");
        }
        return sb.toString();
    }
}
