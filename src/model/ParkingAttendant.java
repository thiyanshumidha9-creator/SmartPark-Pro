package model;

/**
 * Represents a mall parking gate attendant who issues and closes parking tickets.
 */
public class ParkingAttendant extends User {

    private final String gateNumber;
    private final String shiftName;

    public ParkingAttendant(String userId, String name, String phone, String gateNumber, String shiftName) {
        super(userId, name, phone);
        this.gateNumber = gateNumber;
        this.shiftName = shiftName;
    }

    public String getGateNumber() {
        return gateNumber;
    }

    public String getShiftName() {
        return shiftName;
    }

    @Override
    public String getRoleDescription() {
        return "Parking Attendant (Gate " + gateNumber + ", " + shiftName + " Shift)";
    }
}
