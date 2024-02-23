import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

class Labyrinth extends Thread {
    static AtomicBoolean stopThreads = new AtomicBoolean(false);

    // Initial state of the cupcake is true (it starts there)
    static AtomicBoolean cupcake = new AtomicBoolean(true);

    MinotaurCupcake main;
    boolean isCounter = false, hasEaten = false;
    int guestNumber, visits;

    Labyrinth(int guestNumber, MinotaurCupcake main, boolean isCounter) {
        this.guestNumber = guestNumber;
        this.isCounter = isCounter;
        this.main = main;
    }

    @Override
    public void run() {
        // Counter countes their own visit
        if (isCounter) {
            this.visits = 1;
        }

        while (true) {
            // Avoid unnecessary lock acquisition.
            if (stopThreads.get()) {
                break;
            }

            // Acquire lock
            while (!main.lock.compareAndSet(false, true)) {
                // Check stopThreads after acquiring the lock to ensure the thread exits if needed
                if (stopThreads.get()) {
                    main.lock.set(false);
                    break;
                }
            }

            // Cupcake eating and counter counting logic
            try {
                if (cupcake.get() && !isCounter && !(this.hasEaten)) {
                    //System.out.println("Guest " + guestNumber + " eats the cupcake.");
                    cupcake.set(false);
                    this.hasEaten = true;
                } else if (isCounter) {
                    //System.out.println("The Counter (guest " + guestNumber + ") visits.");
                    if (!cupcake.get()) {
                        cupcake.set(true);
                        this.visits++;
                        //System.out.println("Counter counts " + this.visits + " visits.");
                    }
                } else {
                    if (!stopThreads.get()) {
                        //System.out.println("Guest " + guestNumber + " visits.");
                    }
                }
            } finally {
                main.lock.set(false);
            }

            if (this.visits == main.numThreads) {
                System.out.println("Counter tells minotaur all guests have entered.");
                stopThreads.set(true);
                break;
            }
        }
    }
}

public class MinotaurCupcake extends Thread {
    public static ArrayList<Labyrinth> threads = new ArrayList<>();
    AtomicBoolean lock = new AtomicBoolean(false);
    int numThreads;

    void start(MinotaurCupcake main) throws InterruptedException {
        // Guests choose random person as the counter
        int counter = (int) (Math.random() * main.numThreads + 1);

        for (int i = 1; i <= main.numThreads; i++) {
            threads.add(new Labyrinth(i, main, i == counter));
        }

        for (Labyrinth thread : threads) {
            thread.start();
        }

        for (Labyrinth thread : threads) {
            thread.join();
        }
    }

    Labyrinth getThread(int num) {
        return threads.get(num);
    }

    MinotaurCupcake(int numGuests) {
        this.numThreads = numGuests;
    }

    public static void main(String args[]) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Number of guests: ");
        int numGuests = sc.nextInt();
        sc.close();

        final long start = System.currentTimeMillis();

        MinotaurCupcake main = new MinotaurCupcake(numGuests);
        main.start(main);

        final long end = System.currentTimeMillis();
        System.out.println("Time: " + (end - start) + " ms");
    }
}
