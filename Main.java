import java.io.*;
import java.util.*;

class Room {
    int number;
    String type;
    int price;          // price per night
    boolean booked;
    String guest;
    int nights;

    Room(int number, String type, int price) {
        this.number = number;
        this.type = type;
        this.price = price;
        this.booked = false;
        this.guest = "-";
        this.nights = 0;
    }

    String status() {
        return booked ? "Booked" : "Available";
    }

    String display() {
        String s = number + " | " + type + " | " + price + " | " + status();
        if (booked) s += " | Guest: " + guest + " | Nights: " + nights;
        return s;
    }

    String toFileLine() {
        return number + " " + type + " " + price + " " + status() + " " + guest + " " + nights;
    }
}

public class Main {
    static final String FILE_NAME = "Rooms.txt";
    static ArrayList<Room> rooms = new ArrayList<>();
    static HashMap<Integer, Room> roomMap = new HashMap<>();
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        loadRooms();
        while (true) {
            System.out.println("\n===== HOTEL ROOM BOOKING SYSTEM =====");
            System.out.println("1. View all rooms");
            System.out.println("2. Book room");
            System.out.println("3. Search available rooms by type");
            System.out.println("4. Check out");
            System.out.println("5. Sort rooms by price (high to low)");
            System.out.println("6. Save and exit");
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1: viewRooms(); break;
                case 2: bookRoom(); break;
                case 3: searchByType(); break;
                case 4: checkOut(); break;
                case 5: sortByPrice(); break;
                case 6:
                    saveRooms();
                    System.out.println("Data saved to " + FILE_NAME + ". Goodbye!");
                    return;
                default: System.out.println("Invalid choice. Enter 1 to 6.");
            }
        }
    }

    // ---------- File handling ----------
    static void loadRooms() {
        File f = new File(FILE_NAME);
        if (!f.exists()) {
            // first run: create the file with the default rooms
            addRoom(new Room(101, "Single", 1500));
            addRoom(new Room(102, "Double", 2500));
            addRoom(new Room(103, "Suite", 5000));
            saveRooms();
            System.out.println(FILE_NAME + " not found. Created with default rooms.");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\s+");
                try {
                    Room r = new Room(Integer.parseInt(p[0]), p[1], Integer.parseInt(p[2]));
                    if (p.length >= 6 && p[3].equalsIgnoreCase("Booked")) {
                        r.booked = true;
                        r.guest = p[4];
                        r.nights = Integer.parseInt(p[5]);
                    }
                    addRoom(r);
                } catch (Exception e) {
                    System.out.println("Skipping invalid line in file: " + line);
                }
            }
            System.out.println(rooms.size() + " room(s) loaded from " + FILE_NAME + ".");
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    static void saveRooms() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Room r : rooms) pw.println(r.toFileLine());
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    static void addRoom(Room r) {
        rooms.add(r);
        roomMap.put(r.number, r);   // HashMap for O(1) lookup by room number
    }

    // ---------- Features ----------
    static void viewRooms() {
        if (rooms.isEmpty()) {
            System.out.println("No rooms in the hotel.");
            return;
        }
        System.out.println("Room | Type | Price/night | Status");
        for (Room r : rooms) System.out.println(r.display());
        System.out.println("Available rooms: " + countAvailable());
    }

    static void bookRoom() {
        int no = readInt("Room number: ");
        Room r = roomMap.get(no);
        if (r == null) {
            System.out.println("Room " + no + " not found.");
            return;
        }
        if (r.booked) {
            System.out.println("Room " + no + " is already booked by " + r.guest + ".");
            return;
        }
        String guest = readName("Guest name: ");
        int nights = readInt("Nights: ");
        if (nights < 1) {
            System.out.println("Nights must be at least 1.");
            return;
        }
        r.booked = true;
        r.guest = guest;
        r.nights = nights;
        saveRooms();
        System.out.println("Room " + r.number + " booked for " + guest + ". Total: " + (r.price * nights));
        System.out.println("Available rooms: " + countAvailable());
    }

    // Linear search through the ArrayList by type
    static void searchByType() {
        System.out.print("Room type (Single/Double/Suite): ");
        String type = sc.nextLine().trim();
        boolean found = false;
        for (Room r : rooms) {
            if (!r.booked && r.type.equalsIgnoreCase(type)) {
                System.out.println(r.number + " | " + r.type + " | " + r.price + " | Available");
                found = true;
            }
        }
        if (!found) System.out.println("No available rooms of type " + type + ".");
    }

    static void checkOut() {
        int no = readInt("Room number: ");
        Room r = roomMap.get(no);
        if (r == null) {
            System.out.println("Room " + no + " not found.");
            return;
        }
        if (!r.booked) {
            System.out.println("Room " + no + " is not booked.");
            return;
        }
        int bill = r.price * r.nights;
        r.booked = false;
        r.guest = "-";
        r.nights = 0;
        saveRooms();
        System.out.println("Room " + r.number + " checked out. Bill: " + bill + ". Status: " + r.status());
    }

    // Insertion sort on a copy of the list, by price, high to low
    static void sortByPrice() {
        ArrayList<Room> sorted = new ArrayList<>(rooms);
        for (int i = 1; i < sorted.size(); i++) {
            Room key = sorted.get(i);
            int j = i - 1;
            while (j >= 0 && sorted.get(j).price < key.price) {
                sorted.set(j + 1, sorted.get(j));
                j--;
            }
            sorted.set(j + 1, key);
        }
        for (Room r : sorted) System.out.println(r.number + " | " + r.price);
    }

    // ---------- Helper methods ----------
    static int countAvailable() {
        int count = 0;
        for (Room r : rooms) if (!r.booked) count++;
        return count;
    }

    static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!sc.hasNextLine()) { saveRooms(); System.exit(0); }
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    // Guest name is stored as one word in the file, so spaces become underscores
    static String readName(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!sc.hasNextLine()) { saveRooms(); System.exit(0); }
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) return s.replaceAll("\\s+", "_");
            System.out.println("Name cannot be empty.");
        }
    }
}
