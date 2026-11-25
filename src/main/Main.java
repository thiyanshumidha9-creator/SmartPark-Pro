package main;

import exception.DataStorageException;
import java.util.ArrayList;
import java.util.Scanner;
import manager.ParkingManager;
import service.ParkingReportService;
import thread.MonitorLog;
import thread.ParkingMonitor;
import util.ParkingConstants;

/**
 * Application entry point for SmartPark Pro.
 */
public class Main {

    public static void main(String[] args) {
        ParkingManager parkingManager = new ParkingManager();
        ParkingReportService reportService = new ParkingReportService();
        MonitorLog monitorLog = new MonitorLog();
        ParkingMonitor monitor = new ParkingMonitor(parkingManager, monitorLog);
        Scanner scanner = new Scanner(System.in);

        try {
            ArrayList<String> warnings = parkingManager.initializeData();
            if (!warnings.isEmpty()) {
                System.out.println("Data load completed with " + warnings.size() + " warning(s):");
                for (int i = 0; i < warnings.size(); i++) {
                    System.out.println("  - " + warnings.get(i));
                }
            }
        } catch (DataStorageException e) {
            System.out.println("Warning: could not load saved files (" + e.getMessage() + "). Starting fresh.");
        }

        monitor.startMonitor();
        System.out.println(ParkingConstants.APP_NAME + " started. Background monitor active.");

        try {
            ConsoleUI ui = new ConsoleUI(parkingManager, reportService, monitor, monitorLog, scanner);
            ui.runMenuLoop();
        } finally {
            monitor.stopMonitor();
            scanner.close();
            System.out.println("Background monitor stopped. Goodbye!");
        }
    }
}
