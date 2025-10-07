import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Max2 {
    static final int TOTAL_SEATS = 20; // total seats (can change to 50)
    static final String CUSTOMER_FILE = "customer_tickets.txt"; 
    static final String CHECKER_FILE = "checker_records.txt";  

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try {
            // Ensure files exist
            new File(CUSTOMER_FILE).createNewFile();
            new File(CHECKER_FILE).createNewFile();

            System.out.print("Enter your name: ");
            String name = sc.nextLine();

            System.out.print("Enter movie name: |--LEO--| |--KANTARA--| |--KGF 2--| ");
            String movie = sc.nextLine();

            // Language selection
            System.out.print("Select movie language (Tamil / English / Hindi): ");
            String language = sc.nextLine();

            System.out.print("Enter showtime |--first show :10:30--| |--second show :2:30--| |--third show :6:30--| ");
            String showtime = sc.nextLine();

            System.out.print("first class(1) :150rs | second class(2) :200rs | third class(3) :300rs: ");
            String classType = sc.nextLine();

            // Load already booked seats
            Set<Integer> bookedSeats = getBookedSeats(CHECKER_FILE, movie, showtime, language);

            // Show seat availability
            printSeatLayout(bookedSeats);

            System.out.print("\nEnter number of seats to book: ");
            int seats = sc.nextInt();
            sc.nextLine();

            if (seats <= 0) {
                throw new Exception("You must book at least 1 seat.");
            }

            //  Ask user to choose seat numbers (retry if already booked)
            List<Integer> chosenSeats = new ArrayList<>();
            for (int i = 0; i < seats; i++) {
                int seatNum;
                while (true) {
                    System.out.print("Select seat number: ");
                    seatNum = sc.nextInt();
                    if (seatNum < 1 || seatNum > TOTAL_SEATS) {
                        System.out.println("Invalid seat number! Choose between 1 and " + TOTAL_SEATS);
                        continue;
                    }
                    if (bookedSeats.contains(seatNum) || chosenSeats.contains(seatNum)) {
                        System.out.println("Seat " + seatNum + " is already booked. Please choose another seat.");
                        continue;
                    }
                    break; // valid seat
                }
                chosenSeats.add(seatNum);
            }

            int price = 0;
            if (classType.equals("1")) {
                price = 150 * seats;
            } else if (classType.equals("2")) {
                price = 200 * seats;
            } else if (classType.equals("3")) {
                price = 300 * seats;
            } else {
                throw new Exception("Invalid class type selected.");
            }

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
            String formattedDateTime = now.format(dtf);

            // Unique code for verification
            String uniqueCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // save customer ticket
            FileWriter customerWriter = new FileWriter(CUSTOMER_FILE, true);
            customerWriter.write("---------------- THE BLITZ CINEMAS -------------------\n");
            customerWriter.write("Date & time : " + formattedDateTime + "\n");
            customerWriter.write("Customer Name : " + name + "\n");
            customerWriter.write("Movie         : " + movie + "\n");
            customerWriter.write("Language      : " + language + "\n");
            customerWriter.write("Class type    : " + classType + "\n");
            customerWriter.write("Showtime      : " + showtime + "\n");
            customerWriter.write("Seats Booked  : " + chosenSeats + "\n");
            customerWriter.write("Total price   : " + price + "\n");
            customerWriter.write("Verification Code : " + uniqueCode + "\n"); // ✅ code shown to customer
            customerWriter.write("------------------------------------------------------\n\n");
            customerWriter.close();

            // Save checker file with verification details
            FileWriter checkerWriter = new FileWriter(CHECKER_FILE, true);
            checkerWriter.write("CODE: " + uniqueCode + " | Name: " + name + " | Movie: " + movie
                    + " | Lang: " + language + " | Show: " + showtime + " | Seats: " + chosenSeats + "\n");
            checkerWriter.close();

            System.out.println("\n✅ Ticket booked successfully!");
            System.out.println("🎟 Seats booked: " + chosenSeats);
            System.out.println("📄 Customer ticket saved in: " + CUSTOMER_FILE);
            System.out.println("🔑 Your Verification Code: " + uniqueCode);
            System.out.println("🛂 Checker record updated in: " + CHECKER_FILE);

            //  Show updated layout
            bookedSeats.addAll(chosenSeats);
            System.out.println("\nUpdated Seat Layout:");
            printSeatLayout(bookedSeats);

        } catch (Exception e) {
            System.out.println("Booking Failed: " + e.getMessage());
        } finally {
            sc.close();
        }
    }

    // ✅ Print seat layout (5 seats per row)
    private static void printSeatLayout(Set<Integer> bookedSeats) {
        System.out.println("\nSeat Layout:");
        for (int i = 1; i <= TOTAL_SEATS; i++) {
            if (bookedSeats.contains(i)) {
                System.out.print("[X] "); // booked
            } else {
                System.out.print("[" + i + "] "); // available
            }
            if (i % 5 == 0) {
                System.out.println();
            }
        }
    }

    // ✅ Get already booked seats for a movie + showtime + language
    private static Set<Integer> getBookedSeats(String filename, String movie, String showtime, String language) {
        Set<Integer> booked = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("Movie: " + movie) && line.contains("Lang: " + language) && line.contains("Show: " + showtime)) {
                    int seatIndex = line.indexOf("Seats:");
                    if (seatIndex != -1) {
                        String seatString = line.substring(seatIndex + 6).trim();
                        seatString = seatString.replace("[", "").replace("]", "");
                        if (!seatString.isEmpty()) {
                            String[] seatNums = seatString.split(",");
                            for (String s : seatNums) {
                                booked.add(Integer.parseInt(s.trim()));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore if empty
        }
        return booked;
}
}