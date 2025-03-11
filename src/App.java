import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
@SuppressWarnings("unchecked")

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
    static ArrayList<Result> rememberedResults = new ArrayList<Result>();

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

            //If first arg is not a number, exit running
            System.out.println("Error: Desired number of threads is not a number.");
            return;
        } catch (OutOfMemoryError e) {
            //If more threads than available, set to maximum
            System.out.println(
                    "Error: Requested number of threads is more threads than the CPU can handle. Setting to maximum amount of CPU threads available. CPU Threads: "
                            + Runtime.getRuntime().availableProcessors());
            numOfThreads = Runtime.getRuntime().availableProcessors();
        } catch (ArrayIndexOutOfBoundsException e) {
            //If no arg is present, set to max amount available
            System.out.println(
                    "Warning: There is no requested number of threads. Setting to maximum amount of CPU threads available. CPU Threads: "
                            + Runtime.getRuntime().availableProcessors() + ". Request number of threads by passing an argument to the program.");
            numOfThreads = Runtime.getRuntime().availableProcessors();
        }
        //Array of threads
        threads = new Thread[numOfThreads];
        rememberedResults = loadMemory();
        //Starting code - if run() is false, then user asked to exit
        if (!run()) {
            return;
        }
        //While loop for the entirety of the program. Technically is a recursive function so can only run a finite amount of times before a StackOverflow.
        while (true) {
            //If all threads done calculating...
            if (numberOfThreadsCompleted >= (numOfThreads - 1) && numberOfThreadsCompleted != Integer.MAX_VALUE) {
                System.out.println("100% Completed.");
                if (isPrime) {
                    System.out.println(inputNumber + " is prime.");
                } else {
                    //If there is a saved factor, display them. Else show that there was an error.
                    if (factor != 0)
                    {
                        String factorString = "Two factors are " + (inputNumber / factor) + " and " + factor;
                        System.out.println(inputNumber + " is NOT prime. " + factorString);
                    }
                    else System.out.println(inputNumber + " is NOT prime. A program error occurred and it cannot find any factors.");
                }
                memoize(inputNumber,factor,isPrime);
                //Get current system time
                long curTime = (System.currentTimeMillis()/1000);
                //Display how long the calculation took
                System.out.println("Seconds elasped: " + (curTime - startingTime));
                //End all threads
                for (int i = 0; i < numOfThreads; i++) {
                    threads[i].interrupt();
                }
                //Restart the program, recursive, StackOverflow is not handled.
                main(args);
            }
            //Else if user failed to input a integer
            else if (numberOfThreadsCompleted == Integer.MAX_VALUE)
            {
                //Restart the program, recursive, StackOverflow is not handled.
                main(args);
            }
            //If not all threads completed, continue
            else {
                continue;
            }
        }
    }
    public static ArrayList<Result> loadMemory()
    {
        ArrayList<Result> results = new ArrayList<Result>();
        try 
        (
            FileInputStream fis = new FileInputStream(System.getenv("APPDATA") + "\\PrimeCalculator\\memoize.primes");
            ObjectInputStream ois = new ObjectInputStream(fis)
        ) 
        {
            results = (ArrayList<Result>) ois.readObject();
        } 
        catch (IOException | ClassNotFoundException e) 
        {
            e.printStackTrace();
        }
        return results;
    }
    public static void memoize(long input, long factor1, boolean prime)
    {
        try 
        {
            rememberedResults.add(new Result(input,factor1,prime));
            File myObj = new File(System.getenv("APPDATA") + "\\PrimeCalculator\\memoize.primes");
            new File(System.getenv("APPDATA") + "\\PrimeCalculator").mkdirs();
            myObj.delete();
            myObj.createNewFile();
            myObj.setWritable(true);
            FileOutputStream fos = new FileOutputStream(myObj);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(rememberedResults);
            oos.close();
        }
        catch (IOException e) 
        {
            System.out.println("An error occurred while trying to create a file.");
            e.printStackTrace();
        }
    }
    public static Result isRemembered(long number)
    {
        for (Result result : rememberedResults)
        {
            if (result.number == number)
            {
                return result;
            }
        }
        return null;
    }
    public static boolean run() {
        //start a console to read input
        Console console = System.console();
        System.out.println("Input a number to check if prime:");
        String input = console.readLine();
        //Get starting time to find out how long calc takes
        startingTime = System.currentTimeMillis()/1000;
        //Try to parse input as long
        try {
            inputNumber = Long.parseLong(input);
        } catch (NumberFormatException e) {
            if (input.equals("exit")) {
                //If exit, end program
                return false;
            }
            System.out.println("Error: Input is not a number, or is too long for this program. Please try again.");
            //Tell main() to restart program
            numberOfThreadsCompleted = Integer.MAX_VALUE;
            return true;
        }
        Result rememberedResult = isRemembered(inputNumber);
        if (rememberedResult != null)
        {
            if (rememberedResult.isPrime) {
                System.out.println(inputNumber + " is prime.");
            } else {
                //If there is a saved factor, display them. Else show that there was an error.
                if (rememberedResult.factor1 != 0)
                {
                    String factorString = "Two factors are " + rememberedResult.factor2 + " and " + rememberedResult.factor1;
                    System.out.println(inputNumber + " is NOT prime. " + factorString);
                }
                else System.out.println(inputNumber + " is NOT prime. A program error occurred and it cannot find any factors.");
            }
            numberOfThreadsCompleted = Integer.MAX_VALUE;
            return true;
        }
        //if number is big, warn the user of how long it will take.
        if (input.length() >= 10)
        System.out.println("Warning: Number is very long and will take some time to compute.");
        //Prepare all threads
        numberOfThreadsCompleted = 0;
        threadNum = 0;
        //Assume is prime unless found otherwise
        isPrime = true;
        //Split number for each thread to calc
        splitNumber();
        //Start all threads to startup with the function startThread()
        for (int i = 0; i < numOfThreads; i++) {
            threads[i] = new Thread(() -> {
                startThread();
            });
            threads[i].start();
        }
        //Tell program to continue running
        return true;
    }

    static void startThread() {
        //Get index of thread, before another thread steals it
        int myThreadNum = threadNum;
        threadNum += 1;
        //Calc each thread's specific set of numbers
        boolean checker = calc1(myThreadNum);
        //If we found out the number is not prime, and we don't already know that
        if (!checker && isPrime) 
        {
            //Set not prime
            isPrime = checker;
        }
        //If 100% of threads are done, don't report the percentage of completion (main() reports 100% complete.)
        if ((float) ((numberOfThreadsCompleted + 1) / (float) numOfThreads) != 1f) {
            //Else report the percentage of completion, as a whole number
            int percentageCompleted = (int)(((float) numberOfThreadsCompleted / (float) numOfThreads) * 100);
            System.out.println(percentageCompleted + "% Completed.");
        }
        //Announce the thread is done
        numberOfThreadsCompleted += 1;
        return;
    }

    static boolean calc1(int myThreadNum) {
        //Get the maximum number this thread works to.
        long maximumWorkingNumber = workingNumberGroups[myThreadNum];
        //Assume prime, unless disproven
        boolean prime = true;
        //If even number, don't do any calculations
        if (inputNumber % 2 == 0) {
            factor = 2;
            return false;
        }
        //Work through each number we are assigned, starting at our minimum number (maximumWorkingNumber - workingNumberDelta), and work up to our maximum.
        for (long i = maximumWorkingNumber - workingNumberDelta; i <= maximumWorkingNumber; i++) {
            double testNum = inputNumber;
            //If we are testing 1, skip it, every number is divisable by 1
            if (i == 1)
            {
                continue;
            }
            //If the input number / our current test is perfectly divisable, remember the factor, and report number is not prime. End all calculations, no point to continuing.
            if (testNum % i == 0) {
                factor = i;
                return false;
            }
        }
        //Else return true
        return prime;
    }

    static void splitNumber() {
        workingNumberGroups = new long[numOfThreads];
        //Get the amount of numbers each thread will work on 
        workingNumberDelta = (inputNumber / 2) / numOfThreads;
        long lastNumUsed = 0;
        //Create sets of numbers for each thread to work to. (just by setting the maximum number the thread will work to)
        for (int i = 0; i < numOfThreads; i++) {
            workingNumberGroups[i] = lastNumUsed + workingNumberDelta;
            lastNumUsed = workingNumberGroups[i];
        }
    }
}
