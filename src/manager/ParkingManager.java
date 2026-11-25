package manager;

import exception.DataStorageException;
import exception.InvalidTimeException;
import exception.InvalidVehicleException;
import exception.NoAvailableSlotException;
import exception.TicketNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import model.AvailabilitySnapshot;
import model.ParkingAttendant;
import model.ParkingHistoryRecord;
import model.ParkingReceipt;
import model.ParkingSlot;
import model.ParkingTicket;
import model.User;
import model.Vehicle;
import service.FeeCalculator;
import service.FileStorageService;
import service.SampleDataLoader;
import util.InputValidator;
import util.TicketIdGenerator;

/**
 * Central coordinator for SmartPark Pro parking operations.
 * Uses ArrayList as primary storage and HashMap for fast active-ticket lookup by ID.
 * Synchronizes all shared reads and writes to prevent race conditions with ParkingMonitor.
 */
public class ParkingManager {

    private final ArrayList<User> users;
    private final ArrayList<Vehicle> registeredVehicles;
    private final ArrayList<ParkingTicket> activeTickets;
    private final ArrayList<ParkingHistoryRecord> parkingHistory;
    private final HashMap<String, ParkingTicket> activeTicketById;

    private final ParkingLayout layout;
    private final FeeCalculator feeCalculator;
    private final FileStorageService storageService;
    private ParkingAttendant activeAttendant;

    public ParkingManager() {
        this.users = new ArrayList<>();
        this.registeredVehicles = new ArrayList<>();
        this.activeTickets = new ArrayList<>();
        this.parkingHistory = new ArrayList<>();
        this.activeTicketById = new HashMap<>();
        this.layout = new ParkingLayout();
        this.feeCalculator = new FeeCalculator();
        this.storageService = new FileStorageService();
        this.activeAttendant = new ParkingAttendant("USR-100", "Default Gate Attendant", "9800000000", "G1", "Day");
    }

    public synchronized void addUser(User user) {
        if (user != null) {
            users.add(user);
        }
    }

    public synchronized void setActiveAttendant(ParkingAttendant attendant) {
        if (attendant != null) {
            this.activeAttendant = attendant;
        }
    }

    public synchronized ParkingAttendant getActiveAttendant() {
        return activeAttendant;
    }

    public synchronized void addRegisteredVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            return;
        }
        Vehicle existing = findRegisteredVehicleInternal(vehicle.getVehicleNumber());
        if (existing == null) {
            registeredVehicles.add(vehicle);
        } else {
            existing.setOwnerName(vehicle.getOwnerName());
            existing.setOwnerPhone(vehicle.getOwnerPhone());
        }
    }

    public synchronized void addHistoryRecord(ParkingHistoryRecord record) {
        if (record != null) {
            parkingHistory.add(record);
        }
    }

    /**
     * Used during initial sample-data seeding with explicit historical entry times.
     */
    public synchronized void seedActiveParking(Vehicle vehicle, LocalDateTime entryTime) {
        try {
            addRegisteredVehicle(vehicle);
            ParkingSlot slot = layout.findAvailableSlot(vehicle);
            slot.assignVehicle(vehicle.getVehicleNumber());
            String ticketId = TicketIdGenerator.nextTicketId();
            ParkingTicket ticket = new ParkingTicket(
                    ticketId, vehicle, slot, activeAttendant.getName(), entryTime);
            activeTickets.add(ticket);
            activeTicketById.put(ticketId, ticket);
        } catch (NoAvailableSlotException ignored) {
            // Sample seeding assumes sufficient initial capacity
        }
    }

    /**
     * Critical section: validates vehicle, assigns a compatible slot, and creates an active ticket.
     */
    public synchronized ParkingTicket registerAndParkVehicle(Vehicle vehicle)
            throws InvalidVehicleException, NoAvailableSlotException {
        if (vehicle == null || !InputValidator.isValidVehicleNumber(vehicle.getVehicleNumber())) {
            throw new InvalidVehicleException("Valid vehicle details are required.");
        }
        if (!InputValidator.isValidVehicleType(vehicle.getVehicleType())) {
            throw new InvalidVehicleException("Unsupported vehicle type: " + vehicle.getVehicleType());
        }

        ParkingTicket alreadyParked = findActiveTicketByVehicleInternal(vehicle.getVehicleNumber());
        if (alreadyParked != null) {
            throw new InvalidVehicleException(
                    "Vehicle " + vehicle.getVehicleNumber()
                            + " is already parked at slot " + alreadyParked.getSlot().getSlotId()
                            + " (Ticket " + alreadyParked.getTicketId() + ").");
        }

        // Check slot availability BEFORE mutating ticket/registry state
        ParkingSlot slot = layout.findAvailableSlot(vehicle);

        addRegisteredVehicle(vehicle);
        slot.assignVehicle(vehicle.getVehicleNumber());

        String ticketId = TicketIdGenerator.nextTicketId();
        ParkingTicket ticket = new ParkingTicket(
                ticketId, vehicle, slot, activeAttendant.getName(), LocalDateTime.now());
        activeTickets.add(ticket);
        activeTicketById.put(ticketId, ticket);
        return ticket;
    }

    /**
     * Finds an active ticket by either Ticket ID (via HashMap) or Vehicle Number (via ArrayList).
     */
    public synchronized ParkingTicket findActiveTicket(String query) throws TicketNotFoundException {
        if (query == null || query.trim().isEmpty()) {
            throw new TicketNotFoundException("Search query cannot be empty.");
        }
        String normalized = query.trim().toUpperCase();
        ParkingTicket byId = activeTicketById.get(normalized);
        if (byId != null && byId.isActive()) {
            return byId;
        }

        String vehicleNorm = InputValidator.normalizeVehicleNumber(normalized);
        ParkingTicket byVehicle = findActiveTicketByVehicleInternal(vehicleNorm);
        if (byVehicle != null) {
            return byVehicle;
        }

        throw new TicketNotFoundException("No active parking ticket found for '" + query.trim() + "'.");
    }

    /**
     * Critical section: calculates parking fee, frees the slot, closes the ticket,
     * and records the completed trip in history.
     */
    public synchronized ParkingReceipt exitVehicle(String ticketOrVehicleQuery)
            throws TicketNotFoundException, InvalidTimeException {
        ParkingTicket ticket = findActiveTicket(ticketOrVehicleQuery);
        LocalDateTime exitTime = LocalDateTime.now();

        ParkingReceipt receipt = feeCalculator.calculateAndBuildReceipt(
                ticket.getTicketId(),
                ticket.getVehicle(),
                ticket.getSlot().getSlotId(),
                ticket.getEntryTime(),
                exitTime);

        ticket.markExited(exitTime);
        ticket.getSlot().release();
        removeActiveTicketInternal(ticket);

        ParkingHistoryRecord historyRecord = new ParkingHistoryRecord(
                ticket.getTicketId(),
                ticket.getVehicle().getVehicleNumber(),
                ticket.getVehicle().getVehicleType(),
                ticket.getSlot().getSlotId(),
                ticket.getEntryTime(),
                exitTime,
                receipt.getBilledHours(),
                receipt.getTotalFee());
        parkingHistory.add(historyRecord);
        return receipt;
    }

    public synchronized Vehicle findRegisteredVehicle(String vehicleNumber) {
        return findRegisteredVehicleInternal(InputValidator.normalizeVehicleNumber(vehicleNumber));
    }

    /**
     * Read-only synchronized snapshot used by both ConsoleUI and ParkingMonitor.
     */
    public synchronized AvailabilitySnapshot getAvailabilitySnapshot() {
        return layout.createSnapshot(activeTickets.size());
    }

    public synchronized ParkingLayout getLayout() {
        return layout;
    }

    public synchronized ArrayList<ParkingTicket> getActiveTicketsCopy() {
        return new ArrayList<>(activeTickets);
    }

    public synchronized ArrayList<ParkingHistoryRecord> getParkingHistoryCopy() {
        return new ArrayList<>(parkingHistory);
    }

    public synchronized ArrayList<Vehicle> getRegisteredVehiclesCopy() {
        return new ArrayList<>(registeredVehicles);
    }

    public synchronized ArrayList<User> getUsersCopy() {
        return new ArrayList<>(users);
    }

    public synchronized void saveAllRecords() throws DataStorageException {
        storageService.saveAll(registeredVehicles, activeTickets, parkingHistory);
    }

    /**
     * Loads saved CSV records if present; otherwise loads initial sample data.
     * Returns any non-fatal warnings encountered while parsing malformed lines.
     */
    public synchronized ArrayList<String> initializeData() throws DataStorageException {
        ArrayList<String> warnings = new ArrayList<>();
        if (!storageService.hasAnySavedFiles()) {
            new SampleDataLoader().loadSampleData(this);
            return warnings;
        }

        ArrayList<Vehicle> loadedVehicles = storageService.loadVehicles(warnings);
        for (int i = 0; i < loadedVehicles.size(); i++) {
            addRegisteredVehicle(loadedVehicles.get(i));
        }

        ArrayList<ParkingTicket> loadedTickets =
                storageService.loadActiveTickets(layout, registeredVehicles, warnings);
        for (int i = 0; i < loadedTickets.size(); i++) {
            ParkingTicket t = loadedTickets.get(i);
            activeTickets.add(t);
            activeTicketById.put(t.getTicketId(), t);
        }

        ArrayList<ParkingHistoryRecord> loadedHistory = storageService.loadHistory(warnings);
        parkingHistory.addAll(loadedHistory);

        // Ensure default attendant exists in user list
        if (users.isEmpty()) {
            users.add(activeAttendant);
        }
        return warnings;
    }

    private Vehicle findRegisteredVehicleInternal(String vehicleNumber) {
        for (int i = 0; i < registeredVehicles.size(); i++) {
            Vehicle v = registeredVehicles.get(i);
            if (v.getVehicleNumber().equals(vehicleNumber)) {
                return v;
            }
        }
        return null;
    }

    private ParkingTicket findActiveTicketByVehicleInternal(String vehicleNumber) {
        for (int i = 0; i < activeTickets.size(); i++) {
            ParkingTicket t = activeTickets.get(i);
            if (t.isActive() && t.getVehicle().getVehicleNumber().equals(vehicleNumber)) {
                return t;
            }
        }
        return null;
    }

    private void removeActiveTicketInternal(ParkingTicket ticket) {
        activeTicketById.remove(ticket.getTicketId());
        Iterator<ParkingTicket> iterator = activeTickets.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getTicketId().equals(ticket.getTicketId())) {
                iterator.remove();
                break;
            }
        }
    }
}
