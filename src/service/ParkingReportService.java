package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import manager.ParkingLayout;
import model.AvailabilitySnapshot;
import model.ParkingHistoryRecord;
import model.ParkingSlot;
import model.ParkingTicket;
import model.User;
import model.Vehicle;
import thread.MonitorLog;
import thread.ParkingMonitor;
import util.ConsoleFormatter;
import util.DateTimeHelper;
import util.HistoryFeeComparator;
import util.ParkingConstants;

/**
 * Formats parking grids, active ticket lists, sorted history, and dashboard reports.
 */
public class ParkingReportService {

    public String buildAvailabilityReport(ParkingLayout layout, AvailabilitySnapshot snapshot) {
        StringBuilder sb = new StringBuilder();
        sb.append(ConsoleFormatter.header("PARKING AVAILABILITY & 3x10 SLOT GRID")).append('\n');
        sb.append(snapshot.generateReport());
        sb.append(ConsoleFormatter.divider('-')).append('\n');
        sb.append("  Legend: [ID TYPE: STATUS]  (B=Bike, C=Car, T=Truck, G=General)\n");

        for (int f = 0; f < layout.getFloorCount(); f++) {
            sb.append("\n  Floor ").append(f + 1).append(":\n");
            for (int s = 0; s < layout.getSlotsPerFloor(); s++) {
                ParkingSlot slot = layout.getSlot(f, s);
                String statusText = slot.isOccupied()
                        ? ("OCC:" + slot.getCurrentVehicleNumber())
                        : "FREE";
                String cell = String.format("[%s %-7s: %-14s]",
                        slot.getSlotId(),
                        slot.getSlotType(),
                        statusText);
                sb.append("  ").append(cell);
                if ((s + 1) % 2 == 0) {
                    sb.append('\n');
                }
            }
        }
        sb.append(ConsoleFormatter.divider('='));
        return sb.toString();
    }

    public String buildActiveTicketsReport(ArrayList<ParkingTicket> activeTickets) {
        StringBuilder sb = new StringBuilder();
        sb.append(ConsoleFormatter.header("ACTIVE PARKING TICKETS (" + activeTickets.size() + ")")).append('\n');
        if (activeTickets.isEmpty()) {
            sb.append("  No vehicles are currently parked.\n");
            sb.append(ConsoleFormatter.divider('='));
            return sb.toString();
        }

        Collections.sort(activeTickets);
        Iterator<ParkingTicket> iterator = activeTickets.iterator();
        while (iterator.hasNext()) {
            ParkingTicket ticket = iterator.next();
            sb.append("  ").append(ticket.toString()).append('\n');
        }
        sb.append(ConsoleFormatter.divider('='));
        return sb.toString();
    }

    public String buildHistoryReport(ArrayList<ParkingHistoryRecord> history, boolean sortByHighestFee) {
        StringBuilder sb = new StringBuilder();
        String mode = sortByHighestFee ? "Sorted by Highest Fee" : "Sorted by Most Recent Exit";
        sb.append(ConsoleFormatter.header("PARKING HISTORY (" + history.size() + ") - " + mode)).append('\n');
        if (history.isEmpty()) {
            sb.append("  No completed parking history records yet.\n");
            sb.append(ConsoleFormatter.divider('='));
            return sb.toString();
        }

        if (sortByHighestFee) {
            Collections.sort(history, new HistoryFeeComparator());
        } else {
            Collections.sort(history);
        }

        double totalRevenue = 0.0;
        Iterator<ParkingHistoryRecord> iterator = history.iterator();
        while (iterator.hasNext()) {
            ParkingHistoryRecord record = iterator.next();
            totalRevenue += record.getFeePaid();
            sb.append("  ").append(record.toString()).append('\n');
        }
        sb.append(ConsoleFormatter.divider('-')).append('\n');
        sb.append("  Total Historical Revenue: ").append(ConsoleFormatter.formatCurrency(totalRevenue)).append('\n');
        sb.append(ConsoleFormatter.divider('='));
        return sb.toString();
    }

    public String buildDashboardReport(
            AvailabilitySnapshot snapshot,
            ArrayList<ParkingTicket> activeTickets,
            ArrayList<ParkingHistoryRecord> history,
            ArrayList<Vehicle> registeredVehicles,
            ArrayList<User> users,
            ParkingMonitor monitor,
            MonitorLog monitorLog) {
        StringBuilder sb = new StringBuilder();
        sb.append(ConsoleFormatter.header(ParkingConstants.MALL_NAME + " - DASHBOARD")).append('\n');

        double occupancyPercent = snapshot.getTotalSlots() == 0
                ? 0.0
                : (snapshot.getOccupiedSlots() * 100.0) / snapshot.getTotalSlots();

        double totalRevenue = 0.0;
        for (int i = 0; i < history.size(); i++) {
            totalRevenue += history.get(i).getFeePaid();
        }

        int parkedBikes = 0;
        int parkedCars = 0;
        int parkedTrucks = 0;
        for (int i = 0; i < activeTickets.size(); i++) {
            String type = activeTickets.get(i).getVehicle().getVehicleType();
            if (ParkingConstants.VEHICLE_TYPE_BIKE.equals(type)) {
                parkedBikes++;
            } else if (ParkingConstants.VEHICLE_TYPE_CAR.equals(type)) {
                parkedCars++;
            } else if (ParkingConstants.VEHICLE_TYPE_TRUCK.equals(type)) {
                parkedTrucks++;
            }
        }

        sb.append(String.format("  Occupancy         : %d / %d slots (%.1f%%)%n",
                snapshot.getOccupiedSlots(), snapshot.getTotalSlots(), occupancyPercent));
        sb.append("  Currently Parked  : Bikes=").append(parkedBikes)
                .append(", Cars=").append(parkedCars)
                .append(", Trucks=").append(parkedTrucks).append('\n');
        sb.append("  Registered        : ").append(registeredVehicles.size())
                .append(" vehicle(s), ").append(users.size()).append(" user(s)\n");
        sb.append("  Completed Trips   : ").append(history.size())
                .append(" | Revenue: ").append(ConsoleFormatter.formatCurrency(totalRevenue)).append('\n');

        sb.append(ConsoleFormatter.subHeader("BACKGROUND MONITOR STATUS (READ-ONLY)")).append('\n');
        sb.append("  Monitor Running   : ").append(monitor.isRunning() ? "YES" : "NO")
                .append(" (Thread State: ").append(monitor.getThreadStateName()).append(")\n");
        sb.append("  Checks Completed  : ").append(monitorLog.getTotalChecks()).append('\n');

        AvailabilitySnapshot lastMonitored = monitorLog.getLatestSnapshot();
        if (lastMonitored != null) {
            sb.append("  Last Monitored At : ").append(DateTimeHelper.format(lastMonitored.getCapturedAt())).append('\n');
        }

        ArrayList<String> recentEntries = monitorLog.getRecentEntries();
        sb.append("  Recent Snapshots  :\n");
        if (recentEntries.isEmpty()) {
            sb.append("    (Waiting for first monitor snapshot...)\n");
        } else {
            int start = Math.max(0, recentEntries.size() - 5);
            for (int i = start; i < recentEntries.size(); i++) {
                sb.append("    ").append(recentEntries.get(i)).append('\n');
            }
        }
        sb.append(ConsoleFormatter.divider('='));
        return sb.toString();
    }
}
