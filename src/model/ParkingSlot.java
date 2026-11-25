package model;

/**
 * Represents a single physical parking slot on a specific floor in the mall.
 */
public class ParkingSlot {

    private final String slotId;
    private final int floorNumber;
    private final int slotNumber;
    private final String slotType;
    private boolean occupied;
    private String currentVehicleNumber;

    public ParkingSlot(String slotId, int floorNumber, int slotNumber, String slotType) {
        this.slotId = slotId;
        this.floorNumber = floorNumber;
        this.slotNumber = slotNumber;
        this.slotType = slotType;
        this.occupied = false;
        this.currentVehicleNumber = null;
    }

    public String getSlotId() {
        return slotId;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public String getSlotType() {
        return slotType;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public String getCurrentVehicleNumber() {
        return currentVehicleNumber;
    }

    public boolean isCompatibleWith(Vehicle vehicle) {
        return vehicle != null && vehicle.canParkIn(this.slotType);
    }

    public void assignVehicle(String vehicleNumber) {
        this.occupied = true;
        this.currentVehicleNumber = vehicleNumber;
    }

    public void release() {
        this.occupied = false;
        this.currentVehicleNumber = null;
    }

    @Override
    public String toString() {
        String state = occupied ? ("OCCUPIED by " + currentVehicleNumber) : "FREE";
        return slotId + " [" + slotType + "] - " + state;
    }
}
