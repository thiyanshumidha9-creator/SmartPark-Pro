package service;

import exception.DataStorageException;
import exception.InvalidTimeException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import manager.ParkingLayout;
import model.Bike;
import model.Car;
import model.ParkingHistoryRecord;
import model.ParkingSlot;
import model.ParkingTicket;
import model.Truck;
import model.Vehicle;
import util.DateTimeHelper;
import util.InputValidator;
import util.ParkingConstants;
import util.TicketIdGenerator;

/**
 * Handles plain-text CSV persistence for vehicles, active tickets, and parking history
 * using File, FileReader, FileWriter, BufferedReader, and BufferedWriter.
 */
public class FileStorageService {

    public boolean hasAnySavedFiles() {
        return new File(ParkingConstants.VEHICLES_FILE).exists()
                || new File(ParkingConstants.ACTIVE_TICKETS_FILE).exists()
                || new File(ParkingConstants.HISTORY_FILE).exists();
    }

    public void saveAll(
            ArrayList<Vehicle> vehicles,
            ArrayList<ParkingTicket> activeTickets,
            ArrayList<ParkingHistoryRecord> history) throws DataStorageException {
        ensureDataDirectoryExists();
        saveVehicles(vehicles);
        saveActiveTickets(activeTickets);
        saveHistory(history);
    }

    private void ensureDataDirectoryExists() throws DataStorageException {
        File dir = new File(ParkingConstants.DATA_DIRECTORY);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new DataStorageException("Unable to create data directory: " + dir.getPath());
        }
    }

    private void saveVehicles(ArrayList<Vehicle> vehicles) throws DataStorageException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(ParkingConstants.VEHICLES_FILE));
            writer.write("vehicleNumber,vehicleType,ownerName,ownerPhone,detail");
            writer.newLine();
            for (int i = 0; i < vehicles.size(); i++) {
                Vehicle v = vehicles.get(i);
                writer.write(formatVehicleCsv(v));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new DataStorageException("Failed to save vehicles file.", e);
        } finally {
            closeWriter(writer);
        }
    }

    private void saveActiveTickets(ArrayList<ParkingTicket> activeTickets) throws DataStorageException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(ParkingConstants.ACTIVE_TICKETS_FILE));
            writer.write("ticketId,vehicleNumber,slotId,attendantName,entryTime");
            writer.newLine();
            for (int i = 0; i < activeTickets.size(); i++) {
                ParkingTicket t = activeTickets.get(i);
                if (!t.isActive()) {
                    continue;
                }
                String line = String.join(",",
                        t.getTicketId(),
                        t.getVehicle().getVehicleNumber(),
                        t.getSlot().getSlotId(),
                        sanitizeField(t.getAttendantName()),
                        DateTimeHelper.format(t.getEntryTime()));
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new DataStorageException("Failed to save active tickets file.", e);
        } finally {
            closeWriter(writer);
        }
    }

    private void saveHistory(ArrayList<ParkingHistoryRecord> history) throws DataStorageException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(ParkingConstants.HISTORY_FILE));
            writer.write("ticketId,vehicleNumber,vehicleType,slotId,entryTime,exitTime,billedHours,feePaid");
            writer.newLine();
            for (int i = 0; i < history.size(); i++) {
                ParkingHistoryRecord r = history.get(i);
                String line = String.join(",",
                        r.getTicketId(),
                        r.getVehicleNumber(),
                        r.getVehicleType(),
                        r.getSlotId(),
                        DateTimeHelper.format(r.getEntryTime()),
                        DateTimeHelper.format(r.getExitTime()),
                        String.valueOf(r.getBilledHours()),
                        String.format("%.2f", r.getFeePaid()));
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new DataStorageException("Failed to save parking history file.", e);
        } finally {
            closeWriter(writer);
        }
    }

    public ArrayList<Vehicle> loadVehicles(ArrayList<String> warnings) throws DataStorageException {
        ArrayList<Vehicle> loaded = new ArrayList<>();
        File file = new File(ParkingConstants.VEHICLES_FILE);
        if (!file.exists()) {
            return loaded;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (lineNum == 1 && line.startsWith("vehicleNumber,")) {
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                Vehicle v = parseVehicleLine(line, lineNum, warnings);
                if (v != null) {
                    loaded.add(v);
                }
            }
        } catch (IOException e) {
            throw new DataStorageException("Failed to read vehicles file.", e);
        } finally {
            closeReader(reader);
        }
        return loaded;
    }

    public ArrayList<ParkingTicket> loadActiveTickets(
            ParkingLayout layout,
            ArrayList<Vehicle> vehicles,
            ArrayList<String> warnings) throws DataStorageException {
        ArrayList<ParkingTicket> loaded = new ArrayList<>();
        File file = new File(ParkingConstants.ACTIVE_TICKETS_FILE);
        if (!file.exists()) {
            return loaded;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (lineNum == 1 && line.startsWith("ticketId,")) {
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                ParkingTicket ticket = parseActiveTicketLine(line, lineNum, layout, vehicles, warnings);
                if (ticket != null) {
                    loaded.add(ticket);
                    TicketIdGenerator.syncTicketSequence(ticket.getTicketId());
                }
            }
        } catch (IOException e) {
            throw new DataStorageException("Failed to read active tickets file.", e);
        } finally {
            closeReader(reader);
        }
        return loaded;
    }

    public ArrayList<ParkingHistoryRecord> loadHistory(ArrayList<String> warnings) throws DataStorageException {
        ArrayList<ParkingHistoryRecord> loaded = new ArrayList<>();
        File file = new File(ParkingConstants.HISTORY_FILE);
        if (!file.exists()) {
            return loaded;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (lineNum == 1 && line.startsWith("ticketId,")) {
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }
                ParkingHistoryRecord record = parseHistoryLine(line, lineNum, warnings);
                if (record != null) {
                    loaded.add(record);
                    TicketIdGenerator.syncTicketSequence(record.getTicketId());
                }
            }
        } catch (IOException e) {
            throw new DataStorageException("Failed to read parking history file.", e);
        } finally {
            closeReader(reader);
        }
        return loaded;
    }

    private String formatVehicleCsv(Vehicle v) {
        String detail;
        if (v instanceof Bike) {
            detail = String.valueOf(((Bike) v).hasHelmetStorage());
        } else if (v instanceof Car) {
            detail = sanitizeField(((Car) v).getBodyType());
        } else if (v instanceof Truck) {
            detail = String.format("%.1f", ((Truck) v).getLoadCapacityTons());
        } else {
            detail = "Standard";
        }
        return String.join(",",
                v.getVehicleNumber(),
                v.getVehicleType(),
                sanitizeField(v.getOwnerName()),
                sanitizeField(v.getOwnerPhone()),
                detail);
    }

    private Vehicle parseVehicleLine(String line, int lineNum, ArrayList<String> warnings) {
        String[] parts = line.split(",");
        if (parts.length < 5) {
            warnings.add("vehicles.txt line " + lineNum + ": skipped malformed row.");
            return null;
        }
        String number = InputValidator.normalizeVehicleNumber(parts[0]);
        String type = parts[1].trim().toUpperCase();
        String owner = parts[2].trim();
        String phone = parts[3].trim();
        String detail = parts[4].trim();

        if (!InputValidator.isValidVehicleNumber(number) || !InputValidator.isValidVehicleType(type)) {
            warnings.add("vehicles.txt line " + lineNum + ": invalid vehicle number or type.");
            return null;
        }

        try {
            if (ParkingConstants.VEHICLE_TYPE_BIKE.equals(type)) {
                return new Bike(number, owner, phone, Boolean.parseBoolean(detail));
            }
            if (ParkingConstants.VEHICLE_TYPE_CAR.equals(type)) {
                return new Car(number, owner, phone, detail.isEmpty() ? "Sedan" : detail);
            }
            double tons = Double.parseDouble(detail);
            return new Truck(number, owner, phone, tons > 0 ? tons : 2.0);
        } catch (NumberFormatException e) {
            warnings.add("vehicles.txt line " + lineNum + ": invalid numeric detail, skipped.");
            return null;
        }
    }

    private ParkingTicket parseActiveTicketLine(
            String line,
            int lineNum,
            ParkingLayout layout,
            ArrayList<Vehicle> vehicles,
            ArrayList<String> warnings) {
        String[] parts = line.split(",");
        if (parts.length < 5) {
            warnings.add("active_tickets.txt line " + lineNum + ": skipped malformed row.");
            return null;
        }
        String ticketId = parts[0].trim();
        String vehicleNumber = InputValidator.normalizeVehicleNumber(parts[1]);
        String slotId = parts[2].trim().toUpperCase();
        String attendant = parts[3].trim();

        Vehicle vehicle = findVehicleByNumber(vehicles, vehicleNumber);
        if (vehicle == null) {
            warnings.add("active_tickets.txt line " + lineNum + ": vehicle " + vehicleNumber + " not found.");
            return null;
        }
        ParkingSlot slot = layout.findSlotById(slotId);
        if (slot == null || slot.isOccupied() || !slot.isCompatibleWith(vehicle)) {
            warnings.add("active_tickets.txt line " + lineNum + ": slot " + slotId + " unavailable or incompatible.");
            return null;
        }

        try {
            LocalDateTime entryTime = DateTimeHelper.parse(parts[4]);
            slot.assignVehicle(vehicle.getVehicleNumber());
            return new ParkingTicket(ticketId, vehicle, slot, attendant, entryTime);
        } catch (InvalidTimeException e) {
            warnings.add("active_tickets.txt line " + lineNum + ": " + e.getMessage());
            return null;
        }
    }

    private ParkingHistoryRecord parseHistoryLine(String line, int lineNum, ArrayList<String> warnings) {
        String[] parts = line.split(",");
        if (parts.length < 8) {
            warnings.add("parking_history.txt line " + lineNum + ": skipped malformed row.");
            return null;
        }
        try {
            String ticketId = parts[0].trim();
            String vehicleNumber = InputValidator.normalizeVehicleNumber(parts[1]);
            String vehicleType = parts[2].trim().toUpperCase();
            String slotId = parts[3].trim().toUpperCase();
            LocalDateTime entryTime = DateTimeHelper.parse(parts[4]);
            LocalDateTime exitTime = DateTimeHelper.parse(parts[5]);
            long billedHours = Long.parseLong(parts[6].trim());
            double feePaid = Double.parseDouble(parts[7].trim());
            if (exitTime.isBefore(entryTime) || billedHours <= 0 || feePaid < 0) {
                warnings.add("parking_history.txt line " + lineNum + ": invalid duration or fee, skipped.");
                return null;
            }
            return new ParkingHistoryRecord(
                    ticketId, vehicleNumber, vehicleType, slotId, entryTime, exitTime, billedHours, feePaid);
        } catch (NumberFormatException | InvalidTimeException e) {
            warnings.add("parking_history.txt line " + lineNum + ": " + e.getMessage());
            return null;
        }
    }

    private Vehicle findVehicleByNumber(ArrayList<Vehicle> vehicles, String vehicleNumber) {
        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle v = vehicles.get(i);
            if (v.getVehicleNumber().equals(vehicleNumber)) {
                return v;
            }
        }
        return null;
    }

    private String sanitizeField(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(",", " ").trim();
    }

    private void closeReader(BufferedReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ignored) {
                // Closing reader safely in finally block
            }
        }
    }

    private void closeWriter(BufferedWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ignored) {
                // Closing writer safely in finally block
            }
        }
    }
}
