package thread;

import java.util.ArrayList;
import java.util.LinkedList;
import model.AvailabilitySnapshot;
import util.ParkingConstants;

/**
 * Thread-safe bounded log populated by ParkingMonitor.
 * Uses synchronized methods and StringBuffer so console input is never interrupted.
 */
public class MonitorLog {

    private final LinkedList<String> entries;
    private final StringBuffer logBuffer;
    private AvailabilitySnapshot latestSnapshot;
    private int totalChecks;

    public MonitorLog() {
        this.entries = new LinkedList<>();
        this.logBuffer = new StringBuffer();
        this.latestSnapshot = null;
        this.totalChecks = 0;
    }

    public synchronized void recordSnapshot(AvailabilitySnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        this.latestSnapshot = snapshot;
        this.totalChecks++;

        String line = snapshot.toCompactLine();
        if (entries.size() >= ParkingConstants.MAX_MONITOR_LOG_ENTRIES) {
            entries.removeFirst();
        }
        entries.addLast(line);
        logBuffer.append(line).append('\n');
    }

    public synchronized AvailabilitySnapshot getLatestSnapshot() {
        return latestSnapshot;
    }

    public synchronized int getTotalChecks() {
        return totalChecks;
    }

    public synchronized ArrayList<String> getRecentEntries() {
        return new ArrayList<>(entries);
    }

    public synchronized String getLogText() {
        return logBuffer.toString();
    }
}
