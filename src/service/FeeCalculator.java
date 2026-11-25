package service;

import exception.InvalidTimeException;
import java.time.LocalDateTime;
import model.ParkingReceipt;
import model.Vehicle;
import util.DateTimeHelper;
import util.TicketIdGenerator;

/**
 * Calculates parking fees based on vehicle hourly rate and elapsed time.
 * Any started hour is billed as a full hour.
 */
public class FeeCalculator {

    public long calculateBilledHours(long durationMinutes) throws InvalidTimeException {
        if (durationMinutes < 0) {
            throw new InvalidTimeException("Parking duration cannot be negative.");
        }
        if (durationMinutes == 0) {
            return 1;
        }
        return (durationMinutes + 59) / 60;
    }

    public ParkingReceipt calculateAndBuildReceipt(
            String ticketId,
            Vehicle vehicle,
            String slotId,
            LocalDateTime entryTime,
            LocalDateTime exitTime) throws InvalidTimeException {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null during fee calculation.");
        }

        long totalMinutes = DateTimeHelper.calculateDurationMinutes(entryTime, exitTime);
        long billedHours = calculateBilledHours(totalMinutes);
        double hourlyRate = vehicle.getHourlyRate();
        double totalFee = billedHours * hourlyRate;

        return new ParkingReceipt(
                TicketIdGenerator.nextReceiptId(),
                ticketId,
                vehicle.getVehicleNumber(),
                vehicle.getVehicleType(),
                slotId,
                entryTime,
                exitTime,
                totalMinutes,
                billedHours,
                hourlyRate,
                totalFee);
    }
}
