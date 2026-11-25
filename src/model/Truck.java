package model;

import util.ParkingConstants;

/**
 * Heavy commercial vehicle subclass of Vehicle.
 */
public class Truck extends Vehicle {

    private final double loadCapacityTons;

    public Truck(String vehicleNumber, String ownerName, String ownerPhone, double loadCapacityTons) {
        super(vehicleNumber, ownerName, ownerPhone, ParkingConstants.VEHICLE_TYPE_TRUCK);
        this.loadCapacityTons = loadCapacityTons;
    }

    public double getLoadCapacityTons() {
        return loadCapacityTons;
    }

    @Override
    public double getHourlyRate() {
        return ParkingConstants.TRUCK_HOURLY_RATE;
    }

    @Override
    public String getPreferredSlotType() {
        return ParkingConstants.SLOT_TYPE_TRUCK;
    }

    @Override
    public String getSpecificDetail() {
        return String.format("Capacity: %.1f tons", loadCapacityTons);
    }
}
