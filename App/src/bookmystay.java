import java.util.*;

class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }
}

class BookingRequestQueue {

    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) {
        queue.offer(r);
    }

    public Reservation getNextRequest() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}

class RoomInventory {

    private Map<String, Integer> roomAvailability;

    public RoomInventory() {
        roomAvailability = new HashMap<>();
        roomAvailability.put("Single", 3);
        roomAvailability.put("Double", 2);
        roomAvailability.put("Suite", 1);
    }

    public Map<String, Integer> getRoomAvailability() {
        return roomAvailability;
    }

    public void updateAvailability(String type, int count) {
        roomAvailability.put(type, count);
    }
}

class RoomAllocationService {

    private Map<String, Integer> countMap = new HashMap<>();

    public void allocateRoom(Reservation r, RoomInventory inventory) {

        String type = r.getRoomType();
        int available = inventory.getRoomAvailability().get(type);

        if (available <= 0) {
            return;
        }

        int count = countMap.getOrDefault(type, 0) + 1;
        countMap.put(type, count);

        String roomId = type + "-" + count;

        inventory.updateAvailability(type, available - 1);

        System.out.println("Booking confirmed for Guest: "
                + r.getGuestName()
                + ", Room ID: " + roomId);
    }
}

class ConcurrentBookingProcessor implements Runnable {

    private BookingRequestQueue bookingQueue;
    private RoomInventory inventory;
    private RoomAllocationService allocationService;

    public ConcurrentBookingProcessor(
            BookingRequestQueue bookingQueue,
            RoomInventory inventory,
            RoomAllocationService allocationService) {

        this.bookingQueue = bookingQueue;
        this.inventory = inventory;
        this.allocationService = allocationService;
    }

    @Override
    public void run() {

        while (true) {

            Reservation reservation;

            synchronized (bookingQueue) {
                if (bookingQueue.isEmpty())
                    break;

                reservation = bookingQueue.getNextRequest();
            }

            synchronized (inventory) {
                allocationService.allocateRoom(reservation, inventory);
            }
        }
    }
}

public class mystay {

    public static void main(String[] args) {

        System.out.println("Concurrent Booking Simulation\n");

        BookingRequestQueue queue = new BookingRequestQueue();

        queue.addRequest(new Reservation("Abhi", "Single"));
        queue.addRequest(new Reservation("Subha", "Single"));
        queue.addRequest(new Reservation("Vannathi", "Double"));
        queue.addRequest(new Reservation("Kural", "Suite"));

        RoomInventory inventory = new RoomInventory();
        RoomAllocationService allocationService = new RoomAllocationService();

        Thread t1 = new Thread(
                new ConcurrentBookingProcessor(queue, inventory, allocationService)
        );

        Thread t2 = new Thread(
                new ConcurrentBookingProcessor(queue, inventory, allocationService)
        );

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            System.out.println("Thread execution interrupted.");
        }

        System.out.println("\nRemaining Inventory:");
        System.out.println("Single: " + inventory.getRoomAvailability().get("Single"));
        System.out.println("Double: " + inventory.getRoomAvailability().get("Double"));
        System.out.println("Suite: " + inventory.getRoomAvailability().get("Suite"));
    }
}