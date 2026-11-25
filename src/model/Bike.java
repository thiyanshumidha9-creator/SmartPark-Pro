package model;

import util.ParkingConstants;

/**
 * Two-wheeler subclass of Vehicle.
 */
public class Bike extends Vehicle {

    private final boolean hasHelmetStorage;

    public Bike(String vehicleNumber, String ownerName, String ownerPhone, boolean hasHelmetStorage) {
        super(vehicleNumber, ownerName, ownerPhone, ParkingConstants.VEHICLE_TYPE_BIKE);
        this.hasHelmetStorage = hasHelmetStorage;
    }

    public boolean hasHelmetStorage() {
        return hasHelmetStorage;
    }

    @Override
    public double getHourlyRate() {
        return ParkingConstants.BIKE_HOURLY_RATE;
    }

    @Override
    public String getPreferredSlotType() {
        return ParkingConstants.SLOT_TYPE_BIKE;
    }

    @Override
    public String getSpecificDetail() {
        return "Helmet Storage: " + (hasHelmetStorage ? "Yes" : "No");
    }
}
