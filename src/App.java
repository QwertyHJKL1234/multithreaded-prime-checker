import java.io.*;
import java.util.*;
//Doesn't check if the file of saved results is actually an ArrayList, could be a security issue.
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
    static int insertionPoint;

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
        //Load all saved numbers we have already calculated
        rememberedResults = loadMemory();
        //Starting code - if run() is false, then user asked to exit
        if (!run()) {
            return;
        }
        //While loop for the entirety of the program.
        while (true) {
            //If all threads done calculating...
            if (numberOfThreadsCompleted >= (numOfThreads - 1) && numberOfThreadsCompleted != Integer.MAX_VALUE) {
                //Tell user that we are done
                System.out.println("100% Completed.");
                if (isPrime) {
                    System.out.println(inputNumber + " is prime.");
                } else {
                    //If there is a saved factor, display the factors.
                    if (factor != 0)
                    {
                        String factorString = "Two factors are " + (inputNumber / factor) + " and " + factor;
                        System.out.println(inputNumber + " is NOT prime. " + factorString);
                    }
                    //Else show there was an error in the remembered factors
                    else System.out.println(inputNumber + " is NOT prime. A program error occurred and it cannot find any factors.");
                }
                //Remember our saved results to use later
                memoize(inputNumber,factor,isPrime);
                //Get current system time
                long curTime = (System.currentTimeMillis()/1000);
                //Display how long the calculation took
                System.out.println("Seconds elasped: " + (curTime - startingTime));
                //End all threads
                for (int i = 0; i < numOfThreads; i++) {
                    threads[i].interrupt();
                }
                //Restart the program. Technically a recursive function, so can only run a finite amount of times before a StackOverflow.
                main(args);
            }
            //Else if user failed to input a integer
            else if (numberOfThreadsCompleted == Integer.MAX_VALUE)
            {
                //Restart the program, recursive, StackOverflow is not handled.
                main(args);
            }
            //If not all threads completed, continue running until they are all finished.
            else {
                continue;
            }
        }
    }
    //Load our saved numbers that we have already calculated.
    public static ArrayList<Result> loadMemory()
    {
        ArrayList<Result> results = new ArrayList<Result>();
        try
        {
            //Try to load the file to the arraylist
            FileInputStream fis = new FileInputStream(System.getenv("APPDATA") + "\\PrimeCalculator\\memoize.primes");
            ObjectInputStream ois = new ObjectInputStream(fis);
            results = (ArrayList<Result>) ois.readObject();
            ois.close();
        }
        catch (FileNotFoundException e) 
        {
            //If file not found, try to create it.
            try 
            {
                File myObj = new File(System.getenv("APPDATA") + "\\PrimeCalculator\\memoize.primes");
                new File(System.getenv("APPDATA") + "\\PrimeCalculator").mkdirs();
                myObj.createNewFile();
                e.printStackTrace();
            } 
            catch (IOException e2) 
            {
                e.printStackTrace();
            }
        }
        catch (EOFException e) 
        {
            //If file is empty, return an empty arraylist
            return new ArrayList<Result>();
        }
        catch (IOException | ClassNotFoundException e) 
        {
            e.printStackTrace();
        }
        //Return the arraylist of saved numbers
        return results;
    }
    public static int binarySearchResultList(ArrayList<Result> al, long number)
    {
        int low = 0;
        int high = al.size() - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (al.get(mid).number < number) {
                low = mid + 1;
            }
            else if (al.get(mid).number > number) {
                high = mid - 1;
            }
            else {
                return mid;
            }
        }
        //Return the insertion point of the number if not found
        return -(low + 1);
    }
    // public static ArrayList<Result> sort(ArrayList<Result> in)
    // {
    //     for (int i = 0; i < in.size(); i++)
    //     {
    //         for (int j = i + 1; j < in.size(); j++)
    //         {
    //             if (in.get(i).number > in.get(j).number)
    //             {
    //                 Result temp = in.get(i);
    //                 in.set(i,in.get(j));
    //                 in.set(j,temp);
    //             }
    //         }
    //     }
    //     return in;
    // }
    //Save our newest result to a file
    public static void memoize(long input, long factor1, boolean prime)
    {
        try 
        {
            if (insertionPoint < 0)
            {
                return;
            }
            //Add our newest result to the arraylist
            rememberedResults.add(insertionPoint, new Result(input,factor1,prime));
            //Clear the file by deleting the file, and then creating empty file
            File myObj = new File(System.getenv("APPDATA") + "\\PrimeCalculator\\memoize.primes");
            new File(System.getenv("APPDATA") + "\\PrimeCalculator").mkdirs();
            myObj.delete();
            myObj.createNewFile();
            myObj.setWritable(true);
            //Write the arraylist to the new, empty file
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
    //Check if number has already been calculated
    public static Result isRemembered(long number)
    {
        //Check every result we have saved
        int found = binarySearchResultList(rememberedResults, number);
        if (found >= 0) 
        {
            insertionPoint = -(found + 1);
            return rememberedResults.get(found);
        }
        insertionPoint = -(found + 1);
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
            if (input.toLowerCase().equals("exit")) {
                //If exit, end program
                System.out.println("Exiting...");
                return false;
            }
            else if (input.toLowerCase().equals("clr") || input.toLowerCase().equals("clear"))
            {
                //Command to clear the memory of saved numbers
                try 
                {
                    File myObj = new File(System.getenv("APPDATA") + "\\PrimeCalculator\\memoize.primes");
                    myObj.delete();
                    myObj.createNewFile();
                } catch (IOException e2) 
                {
                    System.out.println("An error occurred while trying to create a file.");
                    e2.printStackTrace();
                }
                numberOfThreadsCompleted = Integer.MAX_VALUE;
                System.out.println("Memory cleared.");
                return true;
            }
            System.out.println("Error: Input is not a number, or is too long for this program. Please try again.");
            //Tell main() to restart program
            numberOfThreadsCompleted = Integer.MAX_VALUE;
            return true;
        }
        //Check if number has already been calculated
        Result rememberedResult = isRemembered(inputNumber);
        //If number has already been calculated, display the result and restart the program
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
        System.out.println("Warning: Number is very long and may take some time to compute.");
        //Prepare all threads
        numberOfThreadsCompleted = 0;
        threadNum = 0;
        //Check first 100 numbers for prime (faster than starting the threads)
        isPrime = checkFirst100(inputNumber);
        //If not prime in the first 100 possible numbers, display the factors and restart the program
        if (!isPrime) 
        {
            //If there is a saved factor, display them. Else show that there was an error.
            if (factor != 0)
            {
                String factorString = "Two factors are " + (inputNumber / factor) + " and " + factor;
                System.out.println(inputNumber + " is NOT prime. " + factorString);
            }
            else System.out.println(inputNumber + " is NOT prime. A program error occurred and it cannot find any factors.");
            numberOfThreadsCompleted = Integer.MAX_VALUE;
            memoize(inputNumber, factor, isPrime);
            return true;
        }
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
    //Check first 100 numbers for prime, to skip starting all the threads for 100 numbers.
    static boolean checkFirst100(long inputNumber)
    {
        for (int i = 2; i < 100; i++)
        {
            if (inputNumber % i == 0)
            {
                factor = i;
                return false;
            }
        }
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
                for (int j = 0; j < numOfThreads; j++) {
                    threads[j].interrupt();
                }
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
