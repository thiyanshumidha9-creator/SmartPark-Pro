package util;

/**
 * Central configuration constants for SmartPark Pro.
 * Keeps parking layout dimensions, rates, file paths, and monitor intervals in one place.
 */
public final class ParkingConstants {

    public static final String APP_NAME = "SmartPark Pro";
    public static final String MALL_NAME = "Grand Central Mall Parking";

    // Parking layout dimensions (3 floors x 10 slots = 30 total slots)
    public static final int TOTAL_FLOORS = 3;
    public static final int SLOTS_PER_FLOOR = 10;

    // Vehicle types
    public static final String VEHICLE_TYPE_BIKE = "BIKE";
    public static final String VEHICLE_TYPE_CAR = "CAR";
    public static final String VEHICLE_TYPE_TRUCK = "TRUCK";

    // Slot categories
    public static final String SLOT_TYPE_BIKE = "BIKE";
    public static final String SLOT_TYPE_CAR = "CAR";
    public static final String SLOT_TYPE_TRUCK = "TRUCK";
    public static final String SLOT_TYPE_GENERAL = "GENERAL";

    // Hourly parking rates (INR)
    public static final double BIKE_HOURLY_RATE = 20.0;
    public static final double CAR_HOURLY_RATE = 40.0;
    public static final double TRUCK_HOURLY_RATE = 60.0;
    public static final String CURRENCY_SYMBOL = "Rs. ";

    // Date-time display & storage format
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    // Plain-text CSV storage paths
    public static final String DATA_DIRECTORY = "data";
    public static final String VEHICLES_FILE = "data/vehicles.txt";
    public static final String ACTIVE_TICKETS_FILE = "data/active_tickets.txt";
    public static final String HISTORY_FILE = "data/parking_history.txt";

    // Background monitor configuration
    public static final long MONITOR_INTERVAL_MS = 15000L;
    public static final int MAX_MONITOR_LOG_ENTRIES = 20;

    private ParkingConstants() {
        // Prevent instantiation of constants utility class
    }
}
