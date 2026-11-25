package util;

import java.util.Comparator;
import model.ParkingHistoryRecord;

/**
 * Comparator to sort completed parking history records by highest fee paid first.
 */
public class HistoryFeeComparator implements Comparator<ParkingHistoryRecord> {

    @Override
    public int compare(ParkingHistoryRecord first, ParkingHistoryRecord second) {
        int feeCompare = Double.compare(second.getFeePaid(), first.getFeePaid());
        if (feeCompare != 0) {
            return feeCompare;
        }
        return second.getExitTime().compareTo(first.getExitTime());
    }
}
