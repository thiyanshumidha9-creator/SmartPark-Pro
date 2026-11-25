package util;

import java.util.Scanner;

/**
 * Validates console inputs and domain strings safely without crashing on bad input.
 */
public final class InputValidator {

    private InputValidator() {
    }

    public static int readIntInRange(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(raw);
                if (value < min || value > max) {
                    System.out.println("Please enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter digits only.");
            }
        }
    }

    public static int readPositiveInt(Scanner scanner, String prompt, int max) {
        return readIntInRange(scanner, prompt, 1, max);
    }

    public static double readPositiveDouble(Scanner scanner, String prompt, double max) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(raw);
                if (value <= 0 || value > max) {
                    System.out.println("Please enter a value greater than 0 and at most " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid decimal number. Please try again.");
            }
        }
    }

    public static String readNonEmptyString(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
                continue;
            }
            if (input.contains(",")) {
                System.out.println("Commas are not allowed in text fields. Please re-enter.");
                continue;
            }
            return input;
        }
    }

    public static String readVehicleNumber(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String normalized = normalizeVehicleNumber(scanner.nextLine());
            if (isValidVehicleNumber(normalized)) {
                return normalized;
            }
            System.out.println("Invalid vehicle number. Use 4-15 alphanumeric characters (e.g., MH12AB1234).");
        }
    }

    public static String readPhoneNumber(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (isValidPhoneNumber(input)) {
                return input;
            }
            System.out.println("Invalid phone number. Enter 7 to 15 digits.");
        }
    }

    public static boolean readYesNo(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " (Y/N): ");
            String answer = scanner.nextLine().trim().toUpperCase();
            if ("Y".equals(answer) || "YES".equals(answer)) {
                return true;
            }
            if ("N".equals(answer) || "NO".equals(answer)) {
                return false;
            }
            System.out.println("Please enter Y or N.");
        }
    }

    public static String normalizeVehicleNumber(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().replace(" ", "").replace("-", "").toUpperCase();
    }

    public static boolean isValidVehicleNumber(String vehicleNumber) {
        if (vehicleNumber == null) {
            return false;
        }
        String trimmed = vehicleNumber.trim();
        if (trimmed.length() < 4 || trimmed.length() > 15) {
            return false;
        }
        for (int i = 0; i < trimmed.length(); i++) {
            if (!Character.isLetterOrDigit(trimmed.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidVehicleType(String vehicleType) {
        if (vehicleType == null) {
            return false;
        }
        String upper = vehicleType.trim().toUpperCase();
        return ParkingConstants.VEHICLE_TYPE_BIKE.equals(upper)
                || ParkingConstants.VEHICLE_TYPE_CAR.equals(upper)
                || ParkingConstants.VEHICLE_TYPE_TRUCK.equals(upper);
    }

    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null) {
            return false;
        }
        String trimmed = phone.trim();
        if (trimmed.length() < 7 || trimmed.length() > 15) {
            return false;
        }
        for (int i = 0; i < trimmed.length(); i++) {
            if (!Character.isDigit(trimmed.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
