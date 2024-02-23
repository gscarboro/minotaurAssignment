import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

class Vase extends Thread {
    private static final AtomicBoolean sign = new AtomicBoolean(true);
    private MinotaurVase main;
    private int guestNumber;

    public Vase(int guestNumber, MinotaurVase main) {
        this.guestNumber = guestNumber;
        this.main = main;
    }

    @Override
    public void run() {
        while (!main.isInterrupted()) {
            // Only enter if the sign says it's open
            if (sign.compareAndSet(true, false)) {
                try {
                    //System.out.println("Guest " + guestNumber + " views.");
                } finally {
                    // Current guest resets sign upon exiting
                    sign.set(true);
                }
                break;
            }
        }
    }
}

public class MinotaurVase extends Thread {
    private static ArrayList<Vase> threads = new ArrayList<>();
    private int numGuests;

    public MinotaurVase(int numGuests) {
        this.numGuests = numGuests;
    }

    public void startViewing() throws InterruptedException {
        for (int i = 1; i <= this.numGuests; i++) {
            threads.add(new Vase(i, this));
        }

        for (Vase thread : threads) {
            thread.start();
        }

        for (Vase thread : threads) {
            thread.join();
        }

        System.out.println("All guests have entered.");
    }

    public static void main(String[] args) throws InterruptedException {
        Scanner scan = new Scanner(System.in);
        System.out.print("Number of guests: ");
        int numGuests = scan.nextInt();
        scan.close();

        final long start = System.currentTimeMillis();

        MinotaurVase main = new MinotaurVase(numGuests);
        main.startViewing();

        final long end = System.currentTimeMillis();
        System.out.println("Time: " + (end - start) + " ms");
    }
}
