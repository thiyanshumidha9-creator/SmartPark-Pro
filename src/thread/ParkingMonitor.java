package thread;

import manager.ParkingManager;
import model.AvailabilitySnapshot;
import util.ParkingConstants;

/**
 * Read-only background monitor thread that periodically captures parking availability
 * snapshots via synchronized access and stores them in MonitorLog.
 * It does not modify parking records or write over interactive console prompts.
 */
public class ParkingMonitor implements Runnable {

    private final ParkingManager parkingManager;
    private final MonitorLog monitorLog;
    private volatile boolean running;
    private Thread monitorThread;

    public ParkingMonitor(ParkingManager parkingManager, MonitorLog monitorLog) {
        this.parkingManager = parkingManager;
        this.monitorLog = monitorLog;
        this.running = false;
    }

    public synchronized void startMonitor() {
        if (running) {
            return;
        }
        running = true;
        monitorThread = new Thread(this, "SmartPark-Monitor");
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            AvailabilitySnapshot snapshot = parkingManager.getAvailabilitySnapshot();
            monitorLog.recordSnapshot(snapshot);

            try {
                Thread.sleep(ParkingConstants.MONITOR_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        running = false;
    }

    public void stopMonitor() {
        running = false;
        if (monitorThread != null) {
            monitorThread.interrupt();
            try {
                monitorThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean isRunning() {
        return running && monitorThread != null && monitorThread.isAlive();
    }

    public String getThreadStateName() {
        if (monitorThread == null) {
            return "NOT_STARTED";
        }
        return monitorThread.getState().name();
    }
}
