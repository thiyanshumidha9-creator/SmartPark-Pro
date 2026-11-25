package model;

import java.time.LocalDateTime;
import util.DateTimeHelper;

/**
 * Represents a parking ticket issued when a vehicle enters a mall slot.
 */
public class ParkingTicket implements Comparable<ParkingTicket> {

    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSlot slot;
    private final String attendantName;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private boolean active;

    public ParkingTicket(
            String ticketId,
            Vehicle vehicle,
            ParkingSlot slot,
            String attendantName,
            LocalDateTime entryTime) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.slot = slot;
        this.attendantName = attendantName;
        this.entryTime = entryTime;
        this.exitTime = null;
        this.active = true;
    }

    public String getTicketId() {
        return ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSlot getSlot() {
        return slot;
    }

    public String getAttendantName() {
        return attendantName;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public boolean isActive() {
        return active;
    }

    public void markExited(LocalDateTime exitTime) {
        this.exitTime = exitTime;
        this.active = false;
    }

    @Override
    public int compareTo(ParkingTicket other) {
        int timeCompare = this.entryTime.compareTo(other.entryTime);
        if (timeCompare != 0) {
            return timeCompare;
        }
        return this.ticketId.compareTo(other.ticketId);
    }

    @Override
    public String toString() {
        return String.format(
                "Ticket: %s | Vehicle: %s (%s) | Slot: %s | Entry: %s | Attendant: %s | Status: %s",
                ticketId,
                vehicle.getVehicleNumber(),
                vehicle.getVehicleType(),
                slot.getSlotId(),
                DateTimeHelper.format(entryTime),
                attendantName,
                active ? "ACTIVE" : "CLOSED");
    }
}
