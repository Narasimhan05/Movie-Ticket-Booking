import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class MovieBookingSystem extends JFrame {
    static Map<String, boolean[][]> seatMap = new HashMap<>();
    static Map<String, java.util.List<String>> userBookings = new HashMap<>();
    static String[] shows = {"Avengers 5 - 6PM", "Batman 7 - 9PM", "Spiderman 9 - 11PM"};
    static final int ROWS = 5, COLS = 5;
    static String currentUser = null;

    public static void main(String[] args) {
        loadBookings();
        SwingUtilities.invokeLater(() -> new LoginWindow());
    }

    static class LoginWindow extends JFrame {
        LoginWindow() {
            setTitle("Login");
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new BorderLayout());

            JPanel loginPanel = new JPanel(new GridLayout(3, 2));
            JTextField userField = new JTextField();
            JPasswordField passField = new JPasswordField();
            JButton loginBtn = new JButton("Login");

            loginPanel.add(new JLabel("Username: "));
            loginPanel.add(userField);
            loginPanel.add(new JLabel("Password: "));
            loginPanel.add(passField);
            loginPanel.add(new JLabel(""));
            loginPanel.add(loginBtn);

            loginBtn.addActionListener(e -> {
                currentUser = userField.getText().trim();
                if (!currentUser.isEmpty()) {
                    dispose();
                    new MovieSelectionWindow();
                }
            });

            add(loginPanel, BorderLayout.CENTER);
            setSize(300, 150);
            setLocationRelativeTo(null);
            setVisible(true);
        }
    }

    static class MovieSelectionWindow extends JFrame {
        MovieSelectionWindow() {
            setTitle("Select a Movie Show");
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLayout(new GridLayout(shows.length + 1, 1));

            for (String show : shows) {
                JButton btn = new JButton(show);
                btn.addActionListener(e -> new BookingWindow(show));
                add(btn);
            }

            JButton myBookings = new JButton("View My Bookings");
            myBookings.addActionListener(e -> showUserBookings());
            add(myBookings);

            setSize(300, 250);
            setLocationRelativeTo(null);
            setVisible(true);
        }

        void showUserBookings() {
            java.util.List<String> bookings = userBookings.getOrDefault(currentUser, new ArrayList<>());
            JOptionPane.showMessageDialog(this, bookings.isEmpty() ? "No bookings." : String.join("\n", bookings));
        }
    }

    static class BookingWindow extends JFrame {
        String showName;
        boolean[][] seats;
        JPanel seatPanel;
        Set<Point> selectedSeats = new HashSet<>();

        BookingWindow(String showName) {
            this.showName = showName;
            this.seats = seatMap.getOrDefault(showName, new boolean[ROWS][COLS]);
            seatMap.put(showName, seats);

            setTitle("Book Seats: " + showName);
            setLayout(new BorderLayout());

            seatPanel = new JPanel(new GridLayout(ROWS, COLS));
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    JButton btn = new JButton(" ");
                    btn.setBackground(seats[r][c] ? Color.RED : Color.GREEN);
                    int row = r, col = c;
                    btn.addActionListener(e -> toggleSeat(row, col, btn));
                    seatPanel.add(btn);
                }
            }

            JPanel bottom = new JPanel();
            JButton payBtn = new JButton("Proceed to Payment");
            JButton cancelBtn = new JButton("Cancel");
            bottom.add(payBtn);
            bottom.add(cancelBtn);

            payBtn.addActionListener(e -> simulatePayment());
            cancelBtn.addActionListener(e -> dispose());

            add(new JLabel("Green = Available | Red = Booked", SwingConstants.CENTER), BorderLayout.NORTH);
            add(seatPanel, BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);

            setSize(400, 400);
            setLocationRelativeTo(null);
            setVisible(true);
        }

        void toggleSeat(int r, int c, JButton btn) {
            if (seats[r][c]) {
                JOptionPane.showMessageDialog(this, "Seat already booked!");
                return;
            }
            Point p = new Point(r, c);
            if (selectedSeats.contains(p)) {
                selectedSeats.remove(p);
                btn.setBackground(Color.GREEN);
            } else {
                selectedSeats.add(p);
                btn.setBackground(Color.YELLOW);
            }
        }

        void simulatePayment() {
            if (selectedSeats.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Select at least one seat.");
                return;
            }

            int total = selectedSeats.size() * 120; // ₹120 per seat
            int confirm = JOptionPane.showConfirmDialog(this, "Total: ₹" + total + ". Proceed to Pay?", "Payment", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                for (Point p : selectedSeats) {
                    seats[p.x][p.y] = true;
                }
                seatMap.put(showName, seats);
                saveBooking();
                saveToFile();
                JOptionPane.showMessageDialog(this, "Booking confirmed!");
                dispose();
            }
        }

        void saveBooking() {
            java.util.List<String> bookings = userBookings.getOrDefault(currentUser, new ArrayList<>());
            for (Point p : selectedSeats) {
                bookings.add(showName + " - Seat[" + (p.x + 1) + "," + (p.y + 1) + "]");
            }
            userBookings.put(currentUser, bookings);
        }
    }

    static void saveToFile() {
        try (PrintWriter pw = new PrintWriter("bookings.txt")) {
            for (String show : shows) {
                boolean[][] seats = seatMap.get(show);
                for (int i = 0; i < ROWS; i++) {
                    for (int j = 0; j < COLS; j++) {
                        if (seats[i][j]) {
                            pw.println(show + "," + i + "," + j);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void loadBookings() {
        File file = new File("bookings.txt");
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String show = parts[0];
                int r = Integer.parseInt(parts[1]);
                int c = Integer.parseInt(parts[2]);
                boolean[][] seats = seatMap.getOrDefault(show, new boolean[ROWS][COLS]);
                seats[r][c] = true;
                seatMap.put(show, seats);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
