package main;

import exception.DataStorageException;
import exception.InvalidTimeException;
import exception.InvalidVehicleException;
import exception.NoAvailableSlotException;
import exception.TicketNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import manager.ParkingManager;
import model.AvailabilitySnapshot;
import model.Bike;
import model.Car;
import model.Customer;
import model.ParkingReceipt;
import model.ParkingTicket;
import model.Truck;
import model.Vehicle;
import service.ParkingReportService;
import thread.MonitorLog;
import thread.ParkingMonitor;
import util.ConsoleFormatter;
import util.InputValidator;
import util.ParkingConstants;
import util.TicketIdGenerator;

/**
 * Handles interactive console menus, input collection, and user feedback.
 */
public class ConsoleUI {

    private final ParkingManager parkingManager;
    private final ParkingReportService reportService;
    private final ParkingMonitor monitor;
    private final MonitorLog monitorLog;
    private final Scanner scanner;

    public ConsoleUI(
            ParkingManager parkingManager,
            ParkingReportService reportService,
            ParkingMonitor monitor,
            MonitorLog monitorLog,
            Scanner scanner) {
        this.parkingManager = parkingManager;
        this.reportService = reportService;
        this.monitor = monitor;
        this.monitorLog = monitorLog;
        this.scanner = scanner;
    }

    public void runMenuLoop() {
        boolean exitRequested = false;
        while (!exitRequested) {
            printMainMenu();
            int choice = InputValidator.readIntInRange(scanner, "Enter your choice (1-8): ", 1, 8);
            switch (choice) {
                case 1:
                    handleRegisterAndPark();
                    break;
                case 2:
                    handleViewAvailability();
                    break;
                case 3:
                    handleFindActiveTicket();
                    break;
                case 4:
                    handleProcessExit();
                    break;
                case 5:
                    handleViewHistory();
                    break;
                case 6:
                    handleViewDashboard();
                    break;
                case 7:
                    handleSaveRecords();
                    break;
                case 8:
                    exitRequested = handleExitApplication();
                    break;
                default:
                    System.out.println("Invalid selection.");
                    break;
            }
        }
    }

    private void printMainMenu() {
        System.out.println(ConsoleFormatter.header(ParkingConstants.APP_NAME + " - " + ParkingConstants.MALL_NAME));
        System.out.println("  Attendant: " + parkingManager.getActiveAttendant().getName()
                + " | Gate: " + parkingManager.getActiveAttendant().getGateNumber());
        AvailabilitySnapshot latest = monitorLog.getLatestSnapshot();
        if (latest != null) {
            System.out.println("  Monitor  : " + latest.toCompactLine());
        }
        System.out.println(ConsoleFormatter.divider('-'));
        System.out.println("  1. Register and park vehicle");
        System.out.println("  2. View parking availability (3x10 Grid)");
        System.out.println("  3. Find active ticket");
        System.out.println("  4. Process vehicle exit");
        System.out.println("  5. View parking history");
        System.out.println("  6. View dashboard & monitor status");
        System.out.println("  7. Save records");
        System.out.println("  8. Exit");
        System.out.println(ConsoleFormatter.divider('-'));
    }

    private void handleRegisterAndPark() {
        System.out.println(ConsoleFormatter.subHeader("REGISTER & PARK VEHICLE"));
        String vehicleNumber = InputValidator.readVehicleNumber(scanner, "Vehicle Number (e.g. MH12AB1234): ");

        Vehicle vehicle = parkingManager.findRegisteredVehicle(vehicleNumber);
        if (vehicle != null) {
            System.out.println("Found existing registered vehicle: " + vehicle);
            boolean useExisting = InputValidator.readYesNo(scanner, "Use saved vehicle details?");
            if (!useExisting) {
                vehicle = promptForNewVehicle(vehicleNumber);
            }
        } else {
            vehicle = promptForNewVehicle(vehicleNumber);
        }

        try {
            ParkingTicket ticket = parkingManager.registerAndParkVehicle(vehicle);
            System.out.println("\nVehicle parked successfully!");
            System.out.println("  Ticket ID   : " + ticket.getTicketId());
            System.out.println("  Assigned    : " + ticket.getSlot().getSlotId()
                    + " (" + ticket.getSlot().getSlotType() + " slot, Floor "
                    + ticket.getSlot().getFloorNumber() + ")");
            System.out.println("  Rate        : " + ConsoleFormatter.formatCurrency(vehicle.getHourlyRate()) + "/hr");
        } catch (InvalidVehicleException | NoAvailableSlotException e) {
            System.out.println("\nCannot park vehicle: " + e.getMessage());
        }
    }

    private Vehicle promptForNewVehicle(String vehicleNumber) {
        System.out.println("Select Vehicle Type:");
        System.out.println("  1. Bike  (" + ConsoleFormatter.formatCurrency(ParkingConstants.BIKE_HOURLY_RATE) + "/hr)");
        System.out.println("  2. Car   (" + ConsoleFormatter.formatCurrency(ParkingConstants.CAR_HOURLY_RATE) + "/hr)");
        System.out.println("  3. Truck (" + ConsoleFormatter.formatCurrency(ParkingConstants.TRUCK_HOURLY_RATE) + "/hr)");
        int typeChoice = InputValidator.readIntInRange(scanner, "Type (1-3): ", 1, 3);

        String ownerName = InputValidator.readNonEmptyString(scanner, "Owner Name: ");
        String ownerPhone = InputValidator.readPhoneNumber(scanner, "Owner Phone: ");

        parkingManager.addUser(new Customer(TicketIdGenerator.nextUserId(), ownerName, ownerPhone, "Regular"));

        if (typeChoice == 1) {
            boolean helmetStorage = InputValidator.readYesNo(scanner, "Has helmet storage?");
            return new Bike(vehicleNumber, ownerName, ownerPhone, helmetStorage);
        }
        if (typeChoice == 2) {
            String bodyType = InputValidator.readNonEmptyString(scanner, "Car Body Type (e.g. Sedan/SUV/Hatchback): ");
            return new Car(vehicleNumber, ownerName, ownerPhone, bodyType);
        }
        double capacity = InputValidator.readPositiveDouble(scanner, "Truck Load Capacity (tons): ", 50.0);
        return new Truck(vehicleNumber, ownerName, ownerPhone, capacity);
    }

    private void handleViewAvailability() {
        AvailabilitySnapshot snapshot = parkingManager.getAvailabilitySnapshot();
        String report = reportService.buildAvailabilityReport(parkingManager.getLayout(), snapshot);
        System.out.println(report);
    }

    private void handleFindActiveTicket() {
        System.out.println(ConsoleFormatter.subHeader("FIND ACTIVE TICKET"));
        System.out.println("  1. Search by Ticket ID or Vehicle Number");
        System.out.println("  2. View all active tickets");
        int option = InputValidator.readIntInRange(scanner, "Choose (1-2): ", 1, 2);

        if (option == 2) {
            ArrayList<ParkingTicket> active = parkingManager.getActiveTicketsCopy();
            System.out.println(reportService.buildActiveTicketsReport(active));
            return;
        }

        String query = InputValidator.readNonEmptyString(scanner, "Enter Ticket ID (e.g. TKT-1001) or Vehicle Number: ");
        try {
            ParkingTicket ticket = parkingManager.findActiveTicket(query);
            System.out.println("\nActive Ticket Found:");
            System.out.println("  " + ticket);
            System.out.println("  Owner Details : " + ticket.getVehicle().getOwnerName()
                    + " (" + ticket.getVehicle().getOwnerPhone() + ")");
            System.out.println("  Vehicle Info  : " + ticket.getVehicle().getSpecificDetail());
        } catch (TicketNotFoundException e) {
            System.out.println("\n" + e.getMessage());
        }
    }

    private void handleProcessExit() {
        System.out.println(ConsoleFormatter.subHeader("PROCESS VEHICLE EXIT"));
        String query = InputValidator.readNonEmptyString(scanner, "Enter Ticket ID or Vehicle Number to exit: ");
        try {
            ParkingReceipt receipt = parkingManager.exitVehicle(query);
            System.out.println(receipt.generateReport());
            System.out.println("Slot " + receipt.getSlotId() + " is now available.");
        } catch (TicketNotFoundException | InvalidTimeException e) {
            System.out.println("\nExit failed: " + e.getMessage());
        }
    }

    private void handleViewHistory() {
        System.out.println(ConsoleFormatter.subHeader("VIEW PARKING HISTORY"));
        System.out.println("  1. Most recent exit first");
        System.out.println("  2. Highest fee paid first");
        int sortOption = InputValidator.readIntInRange(scanner, "Sort order (1-2): ", 1, 2);
        boolean byFee = (sortOption == 2);
        System.out.println(reportService.buildHistoryReport(parkingManager.getParkingHistoryCopy(), byFee));
    }

    private void handleViewDashboard() {
        AvailabilitySnapshot snapshot = parkingManager.getAvailabilitySnapshot();
        String dashboard = reportService.buildDashboardReport(
                snapshot,
                parkingManager.getActiveTicketsCopy(),
                parkingManager.getParkingHistoryCopy(),
                parkingManager.getRegisteredVehiclesCopy(),
                parkingManager.getUsersCopy(),
                monitor,
                monitorLog);
        System.out.println(dashboard);
    }

    private void handleSaveRecords() {
        try {
            parkingManager.saveAllRecords();
            System.out.println("\nAll vehicles, active tickets, and parking history saved to '"
                    + ParkingConstants.DATA_DIRECTORY + "/'.");
        } catch (DataStorageException e) {
            System.out.println("\nSave failed: " + e.getMessage());
        }
    }

    private boolean handleExitApplication() {
        if (!InputValidator.readYesNo(scanner, "Are you sure you want to exit SmartPark Pro?")) {
            return false;
        }
        try {
            parkingManager.saveAllRecords();
            System.out.println("Records saved cleanly.");
        } catch (DataStorageException e) {
            System.out.println("Warning: could not save records on exit: " + e.getMessage());
        }
        return true;
    }
}
