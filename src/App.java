import java.io.Console;

public class App {
    static int numOfThreads;
    static long inputNumber;
    static int threadNum;
    static long[] workingNumberGroups;
    static long workingNumberDelta;
    static int numberOfThreadsCompleted;
    static Thread[] threads;
    static boolean isPrime;
    static long factor;
    static boolean runTests;
    static int testNumber;
    static long startingTime;

    public static void main(String[] args) throws Exception {
        numOfThreads = 1;
        //Get number of threads in args
        try {
            numOfThreads = Integer.parseInt(args[0]);
            //If more threads then available
            if (numOfThreads > Runtime.getRuntime().availableProcessors()) {
                throw new OutOfMemoryError();
            }
            //If # of threads isn't requested in args
            else if (numOfThreads == 0)
            {
                throw new ArrayIndexOutOfBoundsException();
            }
        } catch (NumberFormatException e) {

            System.out.println("Error: Desired number of threads is not a number.");
            return;
        } catch (OutOfMemoryError e) {
            System.out.println(
                    "Error: Requested number of threads is more threads than the CPU can handle. Setting to maximum amount of CPU threads available. CPU Threads: "
                            + Runtime.getRuntime().availableProcessors());
            numOfThreads = Runtime.getRuntime().availableProcessors();
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(
                    "Warning: There is no requested number of threads. Setting to maximum amount of CPU threads available. CPU Threads: "
                            + Runtime.getRuntime().availableProcessors() + ". Request number of threads by passing an argument to the program.");
            numOfThreads = Runtime.getRuntime().availableProcessors();
        }
        threads = new Thread[numOfThreads];
        if (!run()) {
            return;
        }
        while (true) {
            if (numberOfThreadsCompleted >= (numOfThreads - 1)) {
                System.out.println("100% Completed.");
                if (isPrime) {
                    System.out.println(inputNumber + " is prime.");
                } else {
                    if (factor != 0)
                    {
                        String factorString = "Two factors are " + (inputNumber / factor) + " and " + factor;
                        System.out.println(inputNumber + " is NOT prime. " + factorString);
                    }
                    else System.out.println(inputNumber + " is NOT prime. A program error occurred and it cannot find any factors.");
                }
                long curTime = (System.currentTimeMillis()/1000);
                System.out.println("Seconds elasped: " + (curTime - startingTime));
                for (int i = 0; i < numOfThreads; i++) {
                    threads[i].interrupt();
                }
                main(args);
            }
            else {
                continue;
            }
        }
    }
    public static boolean run() {
        Console console = System.console();
        System.out.println("Input a number to check if prime:");
        String input = console.readLine();
        startingTime = System.currentTimeMillis()/1000;
        System.out.println("Starting time (unix): " + startingTime);
        try {
            inputNumber = Long.parseLong(input);
        } catch (NumberFormatException e) {
            if (input.equals("exit")) {
                return false;
            }
            System.out.println("Error: Input is not a number, or is too long for this program. Please try again.");
            return true;
        }
        if (input.length() >= 10)
        System.out.println("Warning: Number is very long and will take some time to compute.");
        numberOfThreadsCompleted = 0;
        threadNum = 0;
        isPrime = true;
        splitNumber();
        for (int i = 0; i < numOfThreads; i++) {
            threads[i] = new Thread(() -> {
                startThread();
            });
            threads[i].start();
        }
        return true;
    }

    static void startThread() {
        int myThreadNum = threadNum;
        threadNum += 1;
        boolean checker = calc1(myThreadNum);
        if (!checker && isPrime) 
        {
            isPrime = checker;
        }
        if ((float) (numberOfThreadsCompleted + 1) / (float) numOfThreads != 1f) {
            int percentageCompleted = (int)(((float) numberOfThreadsCompleted / (float) numOfThreads) * 100);
            System.out.println(percentageCompleted + "% Completed.");
        }
        numberOfThreadsCompleted += 1;
        return;
    }

    static boolean calc1(int myThreadNum) {
        long workingNumber = workingNumberGroups[myThreadNum];
        boolean prime = true;
        if (inputNumber % 2 == 0) {
            factor = 2;
            return false;
        }
        for (long i = workingNumber - workingNumberDelta; i <= workingNumber; i++) {
            // if (workingNumber == workingNumberDelta)
            // {
            //     continue;
            // }
            double testNum = inputNumber;
            if (i == 1)
            {
                continue;
            }
            if (testNum % i == 0) {
                factor = i;
                prime = false;
            }
        }
        return prime;
    }

    static void splitNumber() {
        workingNumberGroups = new long[numOfThreads];
        workingNumberDelta = (inputNumber / 2) / numOfThreads;
        long lastNumUsed = 0;
        for (int i = 0; i < numOfThreads; i++) {
            workingNumberGroups[i] = lastNumUsed + workingNumberDelta;
            lastNumUsed = workingNumberGroups[i];
        }
    }
}
