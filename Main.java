import java.util.*;

public class Main {
    private static int maxPosition;
    private static int currentPosition = 0;
    private static Map<String, List<Order>> orders = new HashMap<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Main maximum-position=<maximum-position>");
            System.exit(1);
        }

        try {
            maxPosition = Integer.parseInt(args[0].split("=")[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Invalid maximum position provided.");
            System.exit(1);
        }

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            if (input.equals("FINISH")) {
                printTrades();
                break;
            }
            processInput(input);
        }
    }

    private static void processInput(String input) {
        String[] parts = input.split("\t");
        if (parts[0].equals("DF")) {
            processDFOrder(parts);
        } else if (parts[0].equals("VE")) {
            processVenueOrder(parts);
        }
    }

    private static void processDFOrder(String[] parts) {
        String messageID = parts[1];
        String side = parts[2];
        int size = Integer.parseInt(parts[3]);
        double price = Double.parseDouble(parts[4]);
        String productID = parts[5];

        if (side.equals("SELL")) {
            size *= -1; // Convert to negative to represent a sell order
        }

        Order order = new Order(messageID, size, price, productID);
        orders.computeIfAbsent(productID, k -> new ArrayList<>()).add(order);
    }

    private static void processVenueOrder(String[] parts) {
        String messageID = parts[1];
        String side = parts[2];
        int size = Integer.parseInt(parts[3]);
        double price = Double.parseDouble(parts[4]);
        String productID = parts[5];

        if (currentPosition >= maxPosition && side.equals("BUY")) {
            return; // Reject buy order if maximum position reached
        }

        List<Order> dfOrders = orders.getOrDefault(productID, new ArrayList<>());
        for (Order order : dfOrders) {
            if ((order.getSize() < 0 && side.equals("SELL") && order.getPrice() <= price) ||
                    (order.getSize() > 0 && side.equals("BUY") && order.getPrice() >= price)) {
                int quantity = Math.min(Math.abs(order.getSize()), size);
                order.setSize(order.getSize() - quantity); // Reduce DF order size
                size -= quantity; // Reduce venue order size

                currentPosition += (side.equals("BUY")) ? quantity : -quantity; // Update position

                System.out.println((side.equals("BUY") ? "SELL" : "BUY") + "\t" + quantity + "\t" + price + "\t" + productID);
                if (size == 0) {
                    return; // Venue order fully filled
                }
            }
        }
    }

    private static void printTrades() {
        for (List<Order> orderList : orders.values()) {
            for (Order order : orderList) {
                if (order.getSize() != 0) {
                    System.out.println((order.getSize() > 0 ? "BUY" : "SELL") + "\t" + Math.abs(order.getSize()) + "\t" + order.getPrice() + "\t" + order.getProductID());
                }
            }
        }
    }

    static class Order {
        private String messageID;
        private int size;
        private double price;
        private String productID;

        public Order(String messageID, int size, double price, String productID) {
            this.messageID = messageID;
            this.size = size;
            this.price = price;
            this.productID = productID;
        }

        public String getMessageID() {
            return messageID;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public double getPrice() {
            return price;
        }

        public String getProductID() {
            return productID;
        }
    }
}