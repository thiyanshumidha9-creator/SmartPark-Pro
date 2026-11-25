package service;

import java.time.LocalDateTime;
import manager.ParkingManager;
import model.Bike;
import model.Car;
import model.Customer;
import model.ParkingAttendant;
import model.ParkingHistoryRecord;
import model.Truck;
import util.ParkingConstants;
import util.TicketIdGenerator;

/**
 * Seeds realistic initial mall data on first run when no saved files exist.
 */
public class SampleDataLoader {

    public void loadSampleData(ParkingManager manager) {
        // Sample system users
        ParkingAttendant gate1Attendant = new ParkingAttendant(
                TicketIdGenerator.nextUserId(), "Rohan Mehta", "9876501122", "G1", "Morning");
        ParkingAttendant gate2Attendant = new ParkingAttendant(
                TicketIdGenerator.nextUserId(), "Priya Nair", "9876503344", "G2", "Evening");

        manager.addUser(gate1Attendant);
        manager.addUser(gate2Attendant);
        manager.addUser(new Customer(TicketIdGenerator.nextUserId(), "Aarav Sharma", "9811122233", "Mall Gold"));
        manager.addUser(new Customer(TicketIdGenerator.nextUserId(), "Sneha Kapoor", "9822233344", "Regular"));
        manager.addUser(new Customer(TicketIdGenerator.nextUserId(), "Vikram Logistics", "9833344455", "Commercial"));
        manager.setActiveAttendant(gate1Attendant);

        // Sample vehicles currently parked in the mall
        LocalDateTime now = LocalDateTime.now();
        manager.seedActiveParking(
                new Bike("MH12BK1024", "Aarav Sharma", "9811122233", true),
                now.minusMinutes(75));
        manager.seedActiveParking(
                new Car("MH12CR4589", "Sneha Kapoor", "9822233344", "SUV"),
                now.minusMinutes(130));
        manager.seedActiveParking(
                new Truck("MH14TR9901", "Vikram Logistics", "9833344455", 4.5),
                now.minusMinutes(40));

        // Sample registered vehicles not currently parked
        manager.addRegisteredVehicle(new Car("DL04CA7788", "Neha Verma", "9844455566", "Sedan"));
        manager.addRegisteredVehicle(new Bike("KA01XY2211", "Karan Joshi", "9855566677", false));

        // Sample completed parking history records
        manager.addHistoryRecord(new ParkingHistoryRecord(
                TicketIdGenerator.nextTicketId(),
                "MH01AB3040",
                ParkingConstants.VEHICLE_TYPE_CAR,
                "F1-C05",
                now.minusHours(5),
                now.minusHours(3).minusMinutes(20),
                2,
                80.0));
        manager.addHistoryRecord(new ParkingHistoryRecord(
                TicketIdGenerator.nextTicketId(),
                "MH12ZZ0099",
                ParkingConstants.VEHICLE_TYPE_BIKE,
                "F1-B02",
                now.minusHours(4),
                now.minusHours(3).minusMinutes(15),
                1,
                20.0));
        manager.addHistoryRecord(new ParkingHistoryRecord(
                TicketIdGenerator.nextTicketId(),
                "GJ05TR4100",
                ParkingConstants.VEHICLE_TYPE_TRUCK,
                "F1-T08",
                now.minusHours(6),
                now.minusHours(3).minusMinutes(10),
                3,
                180.0));
    }
}
