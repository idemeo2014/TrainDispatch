import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * <a href="http://introcs.cs.princeton.edu/java/stdlib/StdDraw.java">original version</a>
 * @author Robert Sedgewick
 * @author Kevin Wayne
 * 
 * Modifed by:
 * @author cPolaris
 */

@SuppressWarnings({"FieldCanBeLocal", "unused"})
final class CustomDraw implements Runnable {

    // default colors
    private final Color DEFAULT_PEN_COLOR   = Color.black;
    private final Color DEFAULT_CLEAR_COLOR = Color.white;

    // current pen color
    private Color penColor;

    // default canvas size is DEFAULT_SIZE-by-DEFAULT_SIZE
    private final int DEFAULT_SIZE = 512;
    private int width  = DEFAULT_SIZE;
    private int height = DEFAULT_SIZE;

    // default pen radius
    private final double DEFAULT_PEN_RADIUS = 0.002;

    // current pen radius
    private double penRadius;

    // we draw immediately or wait until next run?
    private boolean defer = false;

    // boundary of drawing canvas, 5 %
     private final double BORDER = 0.05;
    private final double DEFAULT_XMIN = 0.0;
    private final double DEFAULT_XMAX = 1.0;
    private final double DEFAULT_YMIN = 0.0;
    private final double DEFAULT_YMAX = 1.0;
    private double xmin, ymin, xmax, ymax;

    // default font
    private final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 14);

    // current font
    private Font font;

    // double buffered graphics
    private BufferedImage offscreenImage, onscreenImage;
    private Graphics2D offscreen, onscreen;

    // the animationFrame for drawing to the screen
    private JFrame animationFrame;
    
    // Graph related
    private final int FPS = 30;
    private final int FPT = 30;
    private Collection<Rail>[] adj;
    private List<RoutingRecord> record;
    private List<TrainSprite> sprites;
    private Map<Integer, Station> stations;
    private Scheduler runningSche;

    // info about the run
    private JLabel statusLabel;
    private int duration;
    private double totalCost;
    private double minCost;
    private String strategy;


    CustomDraw(Collection<Rail>[] a, Map<Integer, Station> ss, List<RoutingRecord> re, int t, double actcost, double mcost, String strat) {
        duration = t;
        totalCost = actcost;
        minCost = mcost;
        strategy = strat;
        adj = a;
        stations = ss;
        record = re;
        sprites = new LinkedList<>();

        init();

        double maxX = -1;
        double maxY = -1;
        for (Station st : stations.values()) {
            double currX = st.location.x;
            double currY = st.location.y;
            if (currX > maxX) maxX = currX;
            if (currY > maxY) maxY = currY;
        }

        setCanvasSize((int) (500.0 / maxY * maxX) , 500);
        setXscale(0, maxX);
        setYscale(0, maxY);
    }


    // Entry point
    @Override
    public void run() {
        record.sort(RoutingRecord.comparator());
        animate();
    }


    private void init() {
        // animationFrame
        if (animationFrame != null) animationFrame.setVisible(false);
        animationFrame = new JFrame(strategy);
        offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        onscreenImage  = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        offscreen = offscreenImage.createGraphics();
        onscreen  = onscreenImage.createGraphics();
        setXscale();
        setYscale();
        offscreen.setColor(DEFAULT_CLEAR_COLOR);
        offscreen.fillRect(0, 0, width, height);
        setPenColor();
        setPenRadius();
        setFont();
        clear();

        // add antialiasing
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        offscreen.addRenderingHints(hints);

        // Interface layout
        ImageIcon icon = new ImageIcon(onscreenImage);
        JLabel draw = new JLabel(icon);
        statusLabel = new JLabel();
        JPanel drawingPanel = new JPanel();
        drawingPanel.setLayout(new BoxLayout(drawingPanel, BoxLayout.Y_AXIS));
        drawingPanel.add(draw);
        drawingPanel.add(statusLabel);

        animationFrame.setContentPane(drawingPanel);
        animationFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        animationFrame.setMinimumSize(new Dimension(width, height + 40));
        animationFrame.pack();
    }


    private void animate() {
        LinkedList<RoutingRecord> recordRemoveList = new LinkedList<>();
        LinkedList<TrainSprite> spriteRemoveList = new LinkedList<>();

        animationFrame.setVisible(true);
        show(0);

        for (int frameCount = 1, now = 0; now <= duration; frameCount++) {
            drawStatusBar(now, sprites.size());
            clear();
            setPenColor(Color.BLACK);
            drawStations();
            drawEdges();

            setPenColor(Color.RED);

            // add new sprites
            for (RoutingRecord r : record) {
                if (r.timeStart == now) {
                    sprites.add(new TrainSprite(r.trainName,
                            stations.get(r.from).location,
                            stations.get(r.to).location,
                            r.timeStart,
                            r.timeEnd));
                    recordRemoveList.add(r);
                }
            }

            // process existing sprites
            for (TrainSprite s : sprites) {
                if (s.timeEnd == now) {
                    spriteRemoveList.add(s);
                } else {
                    s.draw(now);
                }
            }

            show(10);
            now++;

            // clean up
            for (RoutingRecord rec : recordRemoveList) {
                record.remove(rec);
            }

            for (TrainSprite sp : spriteRemoveList) {
                sprites.remove(sp);
            }
        }

    }

    private void drawStatusBar(int t, int numT) {
        statusLabel.setText(String.format("Cost lower bound: %.2f    Actual cost: %.2f    Time: %d / %d    Moving trains: %d",
                minCost, totalCost, t, duration, numT));
    }

    private void drawStations() {
        for (Station station : stations.values()) {
            text(station.location.x, station.location.y, station.name);
        }
    }

    private void drawEdges() {
        setPenRadius(0.003);
        for (Collection<Rail> rails : adj) {
            for (Rail rail : rails) {
                Location kore = stations.get(rail.kore).location;
                Location sore = stations.get(rail.sore).location;
                line(kore.x, kore.y, sore.x, sore.y);
            }
        }
    }

    /**
     * Set the window size to the default size 512-by-512 pixels.
     * This method must be called before any other commands.
     */
    private void setCanvasSize() {
        setCanvasSize(DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /**
     * Set the window size to w-by-h pixels.
     * This method must be called before any other commands.
     *
     * @param w the width as a number of pixels
     * @param h the height as a number of pixels
     * @throws IllegalArgumentException if the width or height is 0 or negative
     */
    private void setCanvasSize(int w, int h) {
        if (w < 1 || h < 1) throw new IllegalArgumentException("width and height must be positive");
        width = w;
        height = h;
        init();
    }


   /*************************************************************************
    *  User and screen coordinate systems
    *************************************************************************/

    /**
     * Set the x-scale to be the default (between 0.0 and 1.0).
     */
    private void setXscale() { setXscale(DEFAULT_XMIN, DEFAULT_XMAX); }

    /**
     * Set the y-scale to be the default (between 0.0 and 1.0).
     */
    private void setYscale() { setYscale(DEFAULT_YMIN, DEFAULT_YMAX); }

    /**
     * Set the x-scale
     * @param min the minimum value of the x-scale
     * @param max the maximum value of the x-scale
     */
    private void setXscale(double min, double max) {
        double size = max - min;
        xmin = min - BORDER * size;
        xmax = max + BORDER * size;
    }

    /**
     * Set the y-scale
     * @param min the minimum value of the y-scale
     * @param max the maximum value of the y-scale
     */
    private void setYscale(double min, double max) {
        double size = max - min;
        ymin = min - BORDER * size;
        ymax = max + BORDER * size;
    }

    /**
     * Set the x-scale and y-scale
     * @param min the minimum value of the x- and y-scales
     * @param max the maximum value of the x- and y-scales
     */
    private void setScale(double min, double max) {
        double size = max - min;
        xmin = min - BORDER * size;
        xmax = max + BORDER * size;
        ymin = min - BORDER * size;
        ymax = max + BORDER * size;
    }

    // helper functions that scale from user coordinates to screen coordinates and back
    private double  scaleX(double x) { return width  * (x - xmin) / (xmax - xmin); }
    private double  scaleY(double y) { return height * (ymax - y) / (ymax - ymin); }
    private double factorX(double w) { return w * width  / Math.abs(xmax - xmin);  }
    private double factorY(double h) { return h * height / Math.abs(ymax - ymin);  }
    private double   userX(double x) { return xmin + x * (xmax - xmin) / width;    }
    private double   userY(double y) { return ymax - y * (ymax - ymin) / height;   }


    /**
     * Clear the screen to the default color (white).
     */
    private void clear() { clear(DEFAULT_CLEAR_COLOR); }
    /**
     * Clear the screen to the given color.
     * @param color the Color to make the background
     */
    private void clear(Color color) {
        offscreen.setColor(color);
        offscreen.fillRect(0, 0, width, height);
        offscreen.setColor(penColor);
        draw();
    }

    /**
     * Set the pen size to the default (.002).
     */
    private void setPenRadius() { setPenRadius(DEFAULT_PEN_RADIUS); }
    /**
     * Set the radius of the pen to the given size.
     * @param r the radius of the pen
     * @throws IllegalArgumentException if r is negative
     */
    private void setPenRadius(double r) {
        if (r < 0) throw new IllegalArgumentException("pen radius must be nonnegative");
        penRadius = r;
        float scaledPenRadius = (float) (r * DEFAULT_SIZE);
        BasicStroke stroke = new BasicStroke(scaledPenRadius, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        // BasicStroke stroke = new BasicStroke(scaledPenRadius);
        offscreen.setStroke(stroke);
    }

    /**
     * Set the pen color to the default color (black).
     */
    private void setPenColor() { setPenColor(DEFAULT_PEN_COLOR); }

    /**
     * Set the pen color to the given color. The available pen colors are
     * BLACK, BLUE, CYAN, DARK_GRAY, GRAY, GREEN, LIGHT_GRAY, MAGENTA,
     * ORANGE, PINK, RED, WHITE, and YELLOW.
     * @param color the Color to make the pen
     */
    private void setPenColor(Color color) {
        penColor = color;
        offscreen.setColor(penColor);
    }

    /**
     * Set the pen color to the given RGB color.
     * @param red the amount of red (between 0 and 255)
     * @param green the amount of green (between 0 and 255)
     * @param blue the amount of blue (between 0 and 255)
     * @throws IllegalArgumentException if the amount of red, green, or blue are outside prescribed range
     */
    private void setPenColor(int red, int green, int blue) {
        if (red   < 0 || red   >= 256) throw new IllegalArgumentException("amount of red must be between 0 and 255");
        if (green < 0 || green >= 256) throw new IllegalArgumentException("amount of green must be between 0 and 255");
        if (blue  < 0 || blue  >= 256) throw new IllegalArgumentException("amount of blue must be between 0 and 255");
        setPenColor(new Color(red, green, blue));
    }

    /**
     * Get the current font.
     */
    private Font getFont() { return font; }

    /**
     * Set the font to the default font (sans serif, 16 point).
     */
    private void setFont() { setFont(DEFAULT_FONT); }

    /**
     * Set the font to the given value.
     * @param f the font to make text
     */
    private void setFont(Font f) { font = f; }


   /*************************************************************************
    *  Drawing geometric shapes.
    *************************************************************************/

    /**
     * Draw a line from (x0, y0) to (x1, y1).
     * @param x0 the x-coordinate of the starting point
     * @param y0 the y-coordinate of the starting point
     * @param x1 the x-coordinate of the destination point
     * @param y1 the y-coordinate of the destination point
     */
    private void line(double x0, double y0, double x1, double y1) {
        offscreen.draw(new Line2D.Double(scaleX(x0), scaleY(y0), scaleX(x1), scaleY(y1)));
        draw();
    }

    /**
     * Draw one pixel at (x, y).
     * @param x the x-coordinate of the pixel
     * @param y the y-coordinate of the pixel
     */
    private void pixel(double x, double y) {
        offscreen.fillRect((int) Math.round(scaleX(x)), (int) Math.round(scaleY(y)), 1, 1);
    }

    /**
     * Draw a point at (x, y).
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     */
    private void point(double x, double y) {
        double xs = scaleX(x);
        double ys = scaleY(y);
        double r = penRadius;
        float scaledPenRadius = (float) (r * DEFAULT_SIZE);

        // double ws = factorX(2*r);
        // double hs = factorY(2*r);
        // if (ws <= 1 && hs <= 1) pixel(x, y);
        if (scaledPenRadius <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - scaledPenRadius/2, ys - scaledPenRadius/2,
                                                 scaledPenRadius, scaledPenRadius));
        draw();
    }

    /**
     * Draw a circle of radius r, centered on (x, y).
     * @param x the x-coordinate of the center of the circle
     * @param y the y-coordinate of the center of the circle
     * @param r the radius of the circle
     * @throws IllegalArgumentException if the radius of the circle is negative
     */
    private void circle(double x, double y, double r) {
        if (r < 0) throw new IllegalArgumentException("circle radius must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }

    /**
     * Draw filled circle of radius r, centered on (x, y).
     * @param x the x-coordinate of the center of the circle
     * @param y the y-coordinate of the center of the circle
     * @param r the radius of the circle
     * @throws IllegalArgumentException if the radius of the circle is negative
     */
    private void filledCircle(double x, double y, double r) {
        if (r < 0) throw new IllegalArgumentException("circle radius must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }


    /**
     * Draw an ellipse with given semimajor and semiminor axes, centered on (x, y).
     * @param x the x-coordinate of the center of the ellipse
     * @param y the y-coordinate of the center of the ellipse
     * @param semiMajorAxis is the semimajor axis of the ellipse
     * @param semiMinorAxis is the semiminor axis of the ellipse
     * @throws IllegalArgumentException if either of the axes are negative
     */
    private void ellipse(double x, double y, double semiMajorAxis, double semiMinorAxis) {
        if (semiMajorAxis < 0) throw new IllegalArgumentException("ellipse semimajor axis must be nonnegative");
        if (semiMinorAxis < 0) throw new IllegalArgumentException("ellipse semiminor axis must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*semiMajorAxis);
        double hs = factorY(2*semiMinorAxis);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }

    /**
     * Draw an ellipse with given semimajor and semiminor axes, centered on (x, y).
     * @param x the x-coordinate of the center of the ellipse
     * @param y the y-coordinate of the center of the ellipse
     * @param semiMajorAxis is the semimajor axis of the ellipse
     * @param semiMinorAxis is the semiminor axis of the ellipse
     * @throws IllegalArgumentException if either of the axes are negative
     */
    private void filledEllipse(double x, double y, double semiMajorAxis, double semiMinorAxis) {
        if (semiMajorAxis < 0) throw new IllegalArgumentException("ellipse semimajor axis must be nonnegative");
        if (semiMinorAxis < 0) throw new IllegalArgumentException("ellipse semiminor axis must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*semiMajorAxis);
        double hs = factorY(2*semiMinorAxis);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }


    /**
     * Draw an arc of radius r, centered on (x, y), from angle1 to angle2 (in degrees).
     * @param x the x-coordinate of the center of the circle
     * @param y the y-coordinate of the center of the circle
     * @param r the radius of the circle
     * @param angle1 the starting angle. 0 would mean an arc beginning at 3 o'clock.
     * @param angle2 the angle at the end of the arc. For example, if
     *        you want a 90 degree arc, then angle2 should be angle1 + 90.
     * @throws IllegalArgumentException if the radius of the circle is negative
     */
    private void arc(double x, double y, double r, double angle1, double angle2) {
        if (r < 0) throw new IllegalArgumentException("arc radius must be nonnegative");
        while (angle2 < angle1) angle2 += 360;
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Arc2D.Double(xs - ws/2, ys - hs/2, ws, hs, angle1, angle2 - angle1, Arc2D.OPEN));
        draw();
    }

    /**
     * Draw a square of side length 2r, centered on (x, y).
     * @param x the x-coordinate of the center of the square
     * @param y the y-coordinate of the center of the square
     * @param r radius is half the length of any side of the square
     * @throws IllegalArgumentException if r is negative
     */
    private void square(double x, double y, double r) {
        if (r < 0) throw new IllegalArgumentException("square side length must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }

    /**
     * Draw a filled square of side length 2r, centered on (x, y).
     * @param x the x-coordinate of the center of the square
     * @param y the y-coordinate of the center of the square
     * @param r radius is half the length of any side of the square
     * @throws IllegalArgumentException if r is negative
     */
    private void filledSquare(double x, double y, double r) {
        if (r < 0) throw new IllegalArgumentException("square side length must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }


    /**
     * Draw a rectangle of given half width and half height, centered on (x, y).
     * @param x the x-coordinate of the center of the rectangle
     * @param y the y-coordinate of the center of the rectangle
     * @param halfWidth is half the width of the rectangle
     * @param halfHeight is half the height of the rectangle
     * @throws IllegalArgumentException if halfWidth or halfHeight is negative
     */
    private void rectangle(double x, double y, double halfWidth, double halfHeight) {
        if (halfWidth  < 0) throw new IllegalArgumentException("half width must be nonnegative");
        if (halfHeight < 0) throw new IllegalArgumentException("half height must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*halfWidth);
        double hs = factorY(2*halfHeight);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }

    /**
     * Draw a filled rectangle of given half width and half height, centered on (x, y).
     * @param x the x-coordinate of the center of the rectangle
     * @param y the y-coordinate of the center of the rectangle
     * @param halfWidth is half the width of the rectangle
     * @param halfHeight is half the height of the rectangle
     * @throws IllegalArgumentException if halfWidth or halfHeight is negative
     */
    private void filledRectangle(double x, double y, double halfWidth, double halfHeight) {
        if (halfWidth  < 0) throw new IllegalArgumentException("half width must be nonnegative");
        if (halfHeight < 0) throw new IllegalArgumentException("half height must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*halfWidth);
        double hs = factorY(2*halfHeight);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.fill(new Rectangle2D.Double(xs - ws/2, ys - hs/2, ws, hs));
        draw();
    }


    /**
     * Draw a polygon with the given (x[i], y[i]) coordinates.
     * @param x an array of all the x-coordindates of the polygon
     * @param y an array of all the y-coordindates of the polygon
     */
    private void polygon(double[] x, double[] y) {
        int N = x.length;
        GeneralPath path = new GeneralPath();
        path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));
        for (int i = 0; i < N; i++)
            path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
        path.closePath();
        offscreen.draw(path);
        draw();
    }

    /**
     * Draw a filled polygon with the given (x[i], y[i]) coordinates.
     * @param x an array of all the x-coordindates of the polygon
     * @param y an array of all the y-coordindates of the polygon
     */
    private void filledPolygon(double[] x, double[] y) {
        int N = x.length;
        GeneralPath path = new GeneralPath();
        path.moveTo((float) scaleX(x[0]), (float) scaleY(y[0]));
        for (int i = 0; i < N; i++)
            path.lineTo((float) scaleX(x[i]), (float) scaleY(y[i]));
        path.closePath();
        offscreen.fill(path);
        draw();
    }


   /*************************************************************************
    *  Drawing text.
    *************************************************************************/

    /**
     * Write the given text string in the current font, centered on (x, y).
     * @param x the center x-coordinate of the text
     * @param y the center y-coordinate of the text
     * @param s the text
     */
    private void text(double x, double y, String s) {
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = metrics.stringWidth(s);
        int hs = metrics.getDescent();
        offscreen.drawString(s, (float) (xs - ws/2.0), (float) (ys + hs));
        draw();
    }

    /**
     * Write the given text string in the current font, centered on (x, y) and
     * rotated by the specified number of degrees  
     * @param x the center x-coordinate of the text
     * @param y the center y-coordinate of the text
     * @param s the text
     * @param degrees is the number of degrees to rotate counterclockwise
     */
    private void text(double x, double y, String s, double degrees) {
        double xs = scaleX(x);
        double ys = scaleY(y);
        offscreen.rotate(Math.toRadians(-degrees), xs, ys);
        text(x, y, s);
        offscreen.rotate(Math.toRadians(+degrees), xs, ys);
    }


    /**
     * Write the given text string in the current font, left-aligned at (x, y).
     * @param x the x-coordinate of the text
     * @param y the y-coordinate of the text
     * @param s the text
     */
    private void textLeft(double x, double y, String s) {
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int hs = metrics.getDescent();
        offscreen.drawString(s, (float) (xs), (float) (ys + hs));
        draw();
    }

    /**
     * Write the given text string in the current font, right-aligned at (x, y).
     * @param x the x-coordinate of the text
     * @param y the y-coordinate of the text
     * @param s the text
     */
    private void textRight(double x, double y, String s) {
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        double xs = scaleX(x);
        double ys = scaleY(y);
        int ws = metrics.stringWidth(s);
        int hs = metrics.getDescent();
        offscreen.drawString(s, (float) (xs - ws), (float) (ys + hs));
        draw();
    }



    /**
     * Display on screen, pause for t milliseconds, and turn on
     * <em>animation mode</em>: subsequent calls to
     * drawing methods such as <tt>line()</tt>, <tt>circle()</tt>, and <tt>square()</tt>
     * will not be displayed on screen until the next call to <tt>ikuzo()</tt>.
     * This is useful for producing animations (clear the screen, draw a bunch of shapes,
     * display on screen for a fixed amount of timeStart, and repeat). It also speeds up
     * drawing a huge number of shapes (call <tt>ikuzo(0)</tt> to defer drawing
     * on screen, draw the shapes, and call <tt>ikuzo(0)</tt> to display them all
     * on screen at once).
     * @param t number of milliseconds
     */
    private void show(int t) {
        defer = false;
        draw();
        try { Thread.sleep(t); }
        catch (InterruptedException e) { System.out.println("Error sleeping"); }
        defer = true;
    }

    /**
     * Display on-screen and turn off animation mode:
     * subsequent calls to
     * drawing methods such as <tt>line()</tt>, <tt>circle()</tt>, and <tt>square()</tt>
     * will be displayed on screen when called. This is the default.
     */
    private void show() {
        defer = false;
        draw();
    }

    // draw onscreen if defer is false
    private void draw() {
        if (defer) return;
        onscreen.drawImage(offscreenImage, 0, 0, null);
        animationFrame.repaint();
    }


    private void test() {
        animationFrame.setVisible(true);

        square(.2, .8, .1);
        filledSquare(.8, .8, .2);
        circle(.8, .2, .2);

        setPenColor(Color.red);
        setPenRadius(.02);
        arc(.8, .2, .1, 200, 45);

        // draw a blue diamond
        setPenRadius();
        setPenColor(Color.blue);
        double[] x = { .1, .2, .3, .2 };
        double[] y = { .2, .3, .2, .1 };
        filledPolygon(x, y);

        // text
        setPenColor(Color.black);
        text(0.2, 0.5, "black text");
        setPenColor(Color.white);
        text(0.8, 0.8, "white text");
    }

    private class TrainSprite {
        String name;
        Location from;
        double currX;
        double currY;
        int timeStart;
        int timeEnd;
        double rangeX;
        double rangeY;

        TrainSprite(String nm, Location f, Location t, int tS, int tE) {
            name = nm;
            from = f;
            timeStart = tS;
            timeEnd = tE;
            currX = f.x;
            currY = f.y;
            rangeX = t.x - f.x;
            rangeY = t.y - f.y;
        }

        void draw(int now) {
            double ratio = ((double) now - timeStart) / (timeEnd - timeStart);
            currX = from.x + ratio * rangeX;
            currY = from.y + ratio * rangeY;
            text(currX, currY, name);
        }
    }


}  // end CustomDraw
