package model;

import util.ParkingConstants;

/**
 * Abstract parent class for all vehicles supported by SmartPark Pro.
 */
public abstract class Vehicle implements Comparable<Vehicle> {

    private final String vehicleNumber;
    private String ownerName;
    private String ownerPhone;
    private final String vehicleType;

    protected Vehicle(String vehicleNumber, String ownerName, String ownerPhone, String vehicleType) {
        this.vehicleNumber = vehicleNumber;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.vehicleType = vehicleType;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public abstract double getHourlyRate();

    public abstract String getPreferredSlotType();

    public abstract String getSpecificDetail();

    /**
     * Vehicles can park in their dedicated slot type or in a GENERAL slot.
     */
    public boolean canParkIn(String slotType) {
        return getPreferredSlotType().equals(slotType)
                || ParkingConstants.SLOT_TYPE_GENERAL.equals(slotType);
    }

    @Override
    public int compareTo(Vehicle other) {
        return this.vehicleNumber.compareTo(other.vehicleNumber);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Vehicle)) {
            return false;
        }
        Vehicle other = (Vehicle) obj;
        return this.vehicleNumber.equals(other.vehicleNumber);
    }

    @Override
    public int hashCode() {
        return vehicleNumber.hashCode();
    }

    @Override
    public String toString() {
        return vehicleNumber + " [" + vehicleType + "] Owner: " + ownerName + " (" + getSpecificDetail() + ")";
    }
}
