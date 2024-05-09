import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class StopWatch {
    private int calls;                 // Number of calls of a routine
    private long counter;              // Explicit counter (see count())
    private long startTime, totalTime; // Timing
    private String id = "";

    private static long getCpuTime( ) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        // return bean.getCurrentThreadCpuTime();
        return bean.getCurrentThreadUserTime();
        // return System.nanoTime(); // Walltime

        // return System.currentTimeMillis();
    }

    public StopWatch ()          { start(); }
    public StopWatch (String id) { this(); this.id = id; }

    public void start () {
        calls = 0;
        counter = 0;
        totalTime = 0;
    }

    public long elapsedTime () { // ms
        return totalTime ;
    }

    public long getCalls() { return calls; }
    public long getCounter () { return counter; }
    public String getId() { return id; }

    public void count () { counter++; }

    public void enter () { // Entering a routine
        startTime = getCpuTime();
        calls++;
    }

    public void exit () { // Exiting a routine
        totalTime += getCpuTime() - startTime;
    }

    public static String header () { return "Calls Counter Time"; }

    public String toString () { return getId() + ": " + getCalls() + " " + getCounter() + " " + elapsedTime(); }
}