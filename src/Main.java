import java.util.Random;

class Point 
{
    private double x, y;

    public Point (double x, double y) 
    {
        this.x = x; this.y = y; 
    }

    public double getX () 
    {
        return x; 
    }
    public double getY () 
    { 
        return y; 
    }

    public String toString () 
    { 
        return x + "/" + y; 
    }
}

interface PointGenerator 
{
    public Point next ();
}

class UDGenerator implements PointGenerator 
{ // Unified distribution
    private Random r = new Random();
    private double xMin, xMax, yMin, yMax;

    public UDGenerator (double xMin, double xMax, double yMin, double yMax) 
    {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    public Point next () 
    {
        return new Point(r.nextDouble() * (xMax - xMin) + xMin,
                r.nextDouble() * (yMax - yMin) + yMin);
    }

    public String toString () 
    {
        return "UD " + xMin + " " + xMax + " " + yMin + " " + yMax;
    }
}

public class Main 
{
    private static StopWatch log = new StopWatch();

    private static int getOpt (String arg, String[] args, int value) 
    {
        for (int i=0; i < args.length; i++)
            if (args[i].charAt(0) == '-') // Option found
            {
                if (args[i].indexOf(arg, 1) == 1) 
                { // arg found
                    value = Integer.parseInt(args[i+1]);
                    break;
                }
            }
        return value;
    }

    private static boolean getFlag (String arg, String[] args) 
    {
        for (int i=0; i < args.length; i++)
            if (args[i].charAt(0) == '-') // Option found
            {
                if (args[i].indexOf(arg, 1) == 1)
                {
                    return true;
                }
            }
        return false;
    }

    final static double cloudWidth = 1;  // Width of point cloud
    final static double cloudHeight = 1; // Hight of point cloud

    public static void main (String[] args) 
    {
    
        double start = System.currentTimeMillis();
        PointGenerator randomGen =  new UDGenerator(0, 1000, 0, 1000);
        for (int i = 6400; i <= 103000; i*=2) 
        { // Repeat experiment
            Point[] points = makePoints(i, randomGen);
            Point[] cpp = findCpp(points); // Result (Point pair)

            double end = System.currentTimeMillis();
            System.out.println("Naive " + i + " points: " + (end - start));
        }

        start = System.currentTimeMillis();
        for (int i = 6400; i <= 103000; i*=2)
        {
            Point[] points = makePoints(i, randomGen);
            int lo = 0;
            int hi = 4;
            closestPair(points, lo, hi);
            findCppDC(points);
            double end = System.currentTimeMillis();
            System.out.println("D&C " + i + " points: " + (end - start));
        }
    }

    private static double dist (Point p1, Point p2) 
    {
        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        return  Math.sqrt(dx*dx + dy*dy);
    }

    private static Point[] findCpp (Point[] points) 
    {
        log.enter();
        Point[] cpp = new Point[2]; // Result: Closest pair of points
        cpp[0] = points[0];
        cpp[1] = points[1];

        double minDist = dist(points[0], points[1]);
        for (int i1=0; i1<points.length-1; i1++)
        {
            for (int i2=i1+1; i2<points.length; i2++) 
            {
                log.count();
                double d = dist(points[i1], points[i2]);
                if (d < minDist) 
                {
                    minDist = d;
                    cpp[0] = points[i1];
                    cpp[1] = points[i2];
                }
            }
        }
        log.exit();
        return cpp;
    }

    private static Point[] makePoints (int n, PointGenerator r) 
    {
        Point[] points = new Point[n];
        for (int i=0; i<n; i++) 
        {
            points[i] = r.next();
        }
        return points;
    }

    private static void plot (Point[] points, Point[] pair) 
    {
        final int WIDTH = 800, HEIGHT = 800; // Window size (pixels)
        final double RAD = 0.005; // Radius of point
        StdDraw.setCanvasSize(WIDTH, HEIGHT);
        StdDraw.setTitle("Cloud of " + points.length + " points");
        StdDraw.setXscale(-cloudWidth/2, cloudWidth/2);
        StdDraw.setYscale(-cloudHeight/2, cloudHeight/2);
        StdDraw.setPenColor(0, 0, 255);
        for (Point p : points)
        {
            StdDraw.filledCircle(p.getX(), p.getY(), RAD);
        StdDraw.setPenColor(255, 0, 0);
        }
        for (Point p : pair)
        {
            StdDraw.filledCircle(p.getX(), p.getY(), RAD);
        }
    }

    public static Point[] findCppDC (Point[] points) 
    {
        Point[] cpp = new Point[2]; // Result: Closest pair of points
        cpp[0] = points[0];
        cpp[1] = points[1];

        java.util.Arrays.sort(points, 0, points.length, java.util.Comparator.comparingDouble(Point::getX));
        cpp = closestPair(points, 0, points.length - 1);
        return cpp;
    }

    public static Point[] closestPair(Point[] points, int lo, int hi) 
    {
    int length = hi - lo + 1;
    if (length == 2) 
    {
        return new Point[] {points[lo], points[hi]};
    }
    if (length == 3) 
    {
        double dist01 = dist(points[lo], points[lo + 1]);
        double dist02 = dist(points[lo], points[hi]);
        double dist12 = dist(points[lo + 1], points[hi]);

        if (dist01 < dist02 && dist01 < dist12)
        {  
            return new Point[] {points[lo], points[lo + 1]};
        }
        else if (dist02 < dist01 && dist02 < dist12)
        {
            return new Point[] {points[lo], points[hi]};
        }
        else
        {
            return new Point[] {points[lo + 1], points[hi]};
        }
    }


    int m = lo + length / 2;
    Point[] leftPair = closestPair(points, lo, m-1);
    Point[] rightPair = closestPair(points, m, hi);

    Point[] minPair = dist(leftPair[0], leftPair[1]) < dist(rightPair[0], rightPair[1]) ? leftPair : rightPair;
    double minDist = dist(minPair[0], minPair[1]);

    Point mid = points[m-1];
    Point[] strip = new Point[length];
    int stripSize = 0;
    for (int i = lo; i <= hi; i++) 
    {
        if (Math.abs(points[i].getX() - mid.getX()) < minDist) 
        {
            strip[stripSize++] = points[i];
        }
    }

    java.util.Arrays.sort(strip, 0, stripSize, java.util.Comparator.comparingDouble(Point::getY));

    for (int i = 0; i < stripSize - 1; i++) 
    {
        for (int j = i + 1; j < stripSize && j < i + 7; j++) 
        {
            double tempDist = dist(strip[i], strip[j]);
            if (tempDist < minDist) 
            {
                minDist = tempDist;
                minPair = new Point[]{strip[i], strip[j]};
            }
        }
    }
    return minPair;
    }
}