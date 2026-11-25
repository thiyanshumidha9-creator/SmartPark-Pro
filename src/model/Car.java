package model;

import util.ParkingConstants;

/**
 * Four-wheeler car subclass of Vehicle.
 */
public class Car extends Vehicle {

    private final String bodyType;

    public Car(String vehicleNumber, String ownerName, String ownerPhone, String bodyType) {
        super(vehicleNumber, ownerName, ownerPhone, ParkingConstants.VEHICLE_TYPE_CAR);
        this.bodyType = bodyType;
    }

    public String getBodyType() {
        return bodyType;
    }

    @Override
    public double getHourlyRate() {
        return ParkingConstants.CAR_HOURLY_RATE;
    }

    @Override
    public String getPreferredSlotType() {
        return ParkingConstants.SLOT_TYPE_CAR;
    }

    @Override
    public String getSpecificDetail() {
        return "Body: " + bodyType;
    }
}
