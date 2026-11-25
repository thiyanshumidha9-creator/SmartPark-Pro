package manager;

import exception.NoAvailableSlotException;
import java.time.LocalDateTime;
import model.AvailabilitySnapshot;
import model.ParkingSlot;
import model.Vehicle;
import util.ParkingConstants;

/**
 * Manages the 2D array of mall parking slots (3 floors x 10 slots per floor).
 */
public class ParkingLayout {

    private final ParkingSlot[][] grid;

    public ParkingLayout() {
        this.grid = new ParkingSlot[ParkingConstants.TOTAL_FLOORS][ParkingConstants.SLOTS_PER_FLOOR];
        initializeSlots();
    }

    private void initializeSlots() {
        for (int floorIdx = 0; floorIdx < grid.length; floorIdx++) {
            int floorNumber = floorIdx + 1;
            for (int slotIdx = 0; slotIdx < grid[floorIdx].length; slotIdx++) {
                int slotNumber = slotIdx + 1;
                String slotType = determineSlotType(slotNumber);
                String prefix = getTypePrefix(slotType);
                String slotId = String.format("F%d-%s%02d", floorNumber, prefix, slotNumber);
                grid[floorIdx][slotIdx] = new ParkingSlot(slotId, floorNumber, slotNumber, slotType);
            }
        }
    }

    /**
     * Configures each 10-slot floor:
     * Slots 1-3: BIKE, Slots 4-7: CAR, Slots 8-9: TRUCK, Slot 10: GENERAL.
     */
    private String determineSlotType(int slotNumber) {
        if (slotNumber <= 3) {
            return ParkingConstants.SLOT_TYPE_BIKE;
        }
        if (slotNumber <= 7) {
            return ParkingConstants.SLOT_TYPE_CAR;
        }
        if (slotNumber <= 9) {
            return ParkingConstants.SLOT_TYPE_TRUCK;
        }
        return ParkingConstants.SLOT_TYPE_GENERAL;
    }

    private String getTypePrefix(String slotType) {
        if (ParkingConstants.SLOT_TYPE_BIKE.equals(slotType)) {
            return "B";
        }
        if (ParkingConstants.SLOT_TYPE_CAR.equals(slotType)) {
            return "C";
        }
        if (ParkingConstants.SLOT_TYPE_TRUCK.equals(slotType)) {
            return "T";
        }
        return "G";
    }

    /**
     * Finds the first available compatible slot.
     * Dedicated slots matching the vehicle's preferred type are checked before GENERAL slots.
     */
    public ParkingSlot findAvailableSlot(Vehicle vehicle) throws NoAvailableSlotException {
        if (vehicle == null) {
            throw new NoAvailableSlotException("Cannot assign a slot to a null vehicle.");
        }

        // First pass: prefer dedicated slot matching vehicle type
        String preferred = vehicle.getPreferredSlotType();
        for (int f = 0; f < grid.length; f++) {
            for (int s = 0; s < grid[f].length; s++) {
                ParkingSlot slot = grid[f][s];
                if (!slot.isOccupied() && preferred.equals(slot.getSlotType())) {
                    return slot;
                }
            }
        }

        // Second pass: fall back to GENERAL slot
        for (int f = 0; f < grid.length; f++) {
            for (int s = 0; s < grid[f].length; s++) {
                ParkingSlot slot = grid[f][s];
                if (!slot.isOccupied() && slot.isCompatibleWith(vehicle)) {
                    return slot;
                }
            }
        }

        throw new NoAvailableSlotException(
                "No available compatible slot for " + vehicle.getVehicleType() + " (" + vehicle.getVehicleNumber() + ").");
    }

    public ParkingSlot findSlotById(String slotId) {
        if (slotId == null) {
            return null;
        }
        String target = slotId.trim().toUpperCase();
        for (int f = 0; f < grid.length; f++) {
            for (int s = 0; s < grid[f].length; s++) {
                if (grid[f][s].getSlotId().equalsIgnoreCase(target)) {
                    return grid[f][s];
                }
            }
        }
        return null;
    }

    public int getFloorCount() {
        return grid.length;
    }

    public int getSlotsPerFloor() {
        return grid[0].length;
    }

    public ParkingSlot getSlot(int floorIndex, int slotIndex) {
        return grid[floorIndex][slotIndex];
    }

    /**
     * Builds an immutable availability snapshot for safe read-only reporting.
     */
    public AvailabilitySnapshot createSnapshot(int activeTicketCount) {
        int totalSlots = 0;
        int occupiedSlots = 0;
        int freeBike = 0;
        int freeCar = 0;
        int freeTruck = 0;
        int freeGeneral = 0;
        int[] freePerFloor = new int[grid.length];
        int[] totalPerFloor = new int[grid.length];

        for (int f = 0; f < grid.length; f++) {
            totalPerFloor[f] = grid[f].length;
            for (int s = 0; s < grid[f].length; s++) {
                ParkingSlot slot = grid[f][s];
                totalSlots++;
                if (slot.isOccupied()) {
                    occupiedSlots++;
                } else {
                    freePerFloor[f]++;
                    String type = slot.getSlotType();
                    if (ParkingConstants.SLOT_TYPE_BIKE.equals(type)) {
                        freeBike++;
                    } else if (ParkingConstants.SLOT_TYPE_CAR.equals(type)) {
                        freeCar++;
                    } else if (ParkingConstants.SLOT_TYPE_TRUCK.equals(type)) {
                        freeTruck++;
                    } else if (ParkingConstants.SLOT_TYPE_GENERAL.equals(type)) {
                        freeGeneral++;
                    }
                }
            }
        }

        return new AvailabilitySnapshot(
                LocalDateTime.now(),
                totalSlots,
                occupiedSlots,
                freePerFloor,
                totalPerFloor,
                freeBike,
                freeCar,
                freeTruck,
                freeGeneral,
                activeTicketCount);
    }
}
