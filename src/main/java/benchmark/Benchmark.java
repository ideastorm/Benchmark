package benchmark;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

public class Benchmark implements Callable<Long> {

    private static final int INNER_LOOP_COUNT = 250;
    private static final int OUTER_LOOP_COUNT = 5;
    private static final int NORMALIZE_LOOP_COUNT = 3;

    // Window Objects
    static javax.swing.JFrame frame;
    static javax.swing.JPanel panel;

    private final Consumer<Consumer<Graphics2D>> painter;

    private Benchmark(Consumer<Consumer<Graphics2D>> painter) {
        this.painter = painter;
    }

    @Override
    public Long call() {
        Font font = panel.getFont().deriveFont(30f);
        long start = System.nanoTime();
        for (int i = 0; i < INNER_LOOP_COUNT; i++) {
            final String frameCount = "" + i;
            painter.accept((Graphics2D g) -> {
                g.setColor(Color.BLACK);
                g.setFont(font);
                g.drawString(frameCount, 30, 100);
            });
        }
        long end = System.nanoTime();
        return end - start;
    }

    public static void main(String[] args) {

        // Create JFrame
        frame = new javax.swing.JFrame("Benchmark");
        frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        // Create JPanel
        panel = new javax.swing.JPanel(false);
        panel.setPreferredSize(new java.awt.Dimension(1280, 720));
        frame.setContentPane(panel);

        ClassLoader classloader = ClassLoader.getSystemClassLoader();

        // Load Test Image
        try {
            testimage = ImageIO.read(classloader.getResource("image.png"));
            blackimage = ImageIO.read(classloader.getResource("black.png"));

            // Set Window Visible and get Panel Graphics
            frame.pack();
            frame.setVisible(true);
            panelGraphics = panel.getGraphics();

            for (int normalizeIndex = 0; normalizeIndex < NORMALIZE_LOOP_COUNT; normalizeIndex++) {

                // Start Tests and Display Results
                for (int bufferCount = 3; bufferCount > 0; bufferCount--) {
                    frame.createBufferStrategy(bufferCount);
                    List<Long> directBufferTimes = new ArrayList<>();
                    List<Long> bufferedImageTimes = new ArrayList<>();
                    List<Long> volatileImageTimes = new ArrayList<>();
                    List<Long> bufferedArgbTimes = new ArrayList<>();
                    List<Long> reuseBufferedTimes = new ArrayList<>();
                    List<Long> reuseVolatileTimes = new ArrayList<>();
                    List<Long> reuseArgbBufrTimes = new ArrayList<>();
                    for (int i = 0; i < OUTER_LOOP_COUNT; i++) {
                        frame.setTitle("Benchmark - Direct Buffer");
                        directBufferTimes.add(new Benchmark(Benchmark::drawDoubleBuffer).call());
                        frame.setTitle("Benchmark - Buffered Image");
                        bufferedImageTimes.add(new Benchmark(Benchmark::drawBufferedImage).call());
                        frame.setTitle("Benchmark - Reuse Buffered Image");
                        reuseBufferedTimes.add(new Benchmark(Benchmark::reuseBufferedImage).call());
                        frame.setTitle("Benchmark - ARGB Buffered Image");
                        bufferedArgbTimes.add(new Benchmark(Benchmark::drawArgbBufferedImage).call());
                        frame.setTitle("Benchmark - Reuse ARGB Buffered Image");
                        reuseArgbBufrTimes.add(new Benchmark(Benchmark::reuseArgbBufferedImage).call());
                        frame.setTitle("Benchmark - Volatile Image");
                        volatileImageTimes.add(new Benchmark(Benchmark::drawVolatileImage).call());
                        frame.setTitle("Benchmark - Reuse Volatile Image");
                        reuseVolatileTimes.add(new Benchmark(Benchmark::reuseVolatileImage).call());
                    }
                    /*
                     * Java benchmarks usually have to "warm up".  By running it multiple times and ignoring the first runs, we 
                     * allow the JVM to normalize its heap allocation, so garbage collection doesn't affect the results as much.
                     */
                    if (normalizeIndex == NORMALIZE_LOOP_COUNT - 1) {
//                        bufferedImageTimes.forEach(t -> System.out.printf("Buffered Image %01.2f\n", calcFps(t)));
//                        volatileImageTimes.forEach(t -> System.out.printf("Volatile Image %01.2f\n", calcFps(t)));
//                        directBufferTimes.forEach(t -> System.out.printf("Direct Buffer  %01.2f\n", calcFps(t)));
                        System.out.printf("%d Buffers\n", bufferCount);
                        System.out.printf("   Direct Buffer Average %01.2f\n", directBufferTimes.stream().mapToDouble(Benchmark::calcFps).average().getAsDouble());
                        System.out.printf("  Buffered Image Average %01.2f\n", bufferedImageTimes.stream().mapToDouble(Benchmark::calcFps).average().getAsDouble());
                        System.out.printf("  Reuse Buffered Average %01.2f\n", reuseBufferedTimes.stream().mapToDouble(Benchmark::calcFps).average().getAsDouble());
                        System.out.printf("      ARGB Image Average %01.2f\n", bufferedArgbTimes.stream().mapToDouble(Benchmark::calcFps).average().getAsDouble());
                        System.out.printf("Reuse ARGB Image Average %01.2f\n", reuseArgbBufrTimes.stream().mapToDouble(Benchmark::calcFps).average().getAsDouble());
                        System.out.printf("  Volatile Image Average %01.2f\n", volatileImageTimes.stream().mapToDouble(Benchmark::calcFps).average().getAsDouble());
                        System.out.printf("  Reuse Volatile Average %01.2f\n", reuseVolatileTimes.stream().mapToDouble(Benchmark::calcFps).average().getAsDouble());
                    }
                }
            }

            frame.setVisible(false);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.exit(0); //force shutdown
    }

    static double calcFps(Long nanoTime) {
        return INNER_LOOP_COUNT * 1_000_000_000.0 / nanoTime;
    }

    // Test Image
    static java.awt.Image testimage;
    static java.awt.Image blackimage;
    private static BufferedImage staticBuffered;
    private static BufferedImage staticArgbBuffered;
    private static VolatileImage staticVolatile;

    // Draw Graphics
    static Graphics panelGraphics;

    static void drawUsingImageBuffer(Consumer<Graphics2D> frameMarker, Image buffer) {
        Graphics2D imageGraphics = (Graphics2D) buffer.getGraphics();
        imageGraphics.drawImage(testimage, 0, 0, 1280, 720, null);
        frameMarker.accept(imageGraphics);
        panelGraphics.drawImage(buffer, 0, 0, 1280, 720, null);
    }

    static void drawBufferedImage(Consumer<Graphics2D> frameMarker) {
        BufferedImage buffer = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_RGB);
        drawUsingImageBuffer(frameMarker, buffer);
    }

    static void reuseBufferedImage(Consumer<Graphics2D> frameMarker) {
        if (staticBuffered == null) {
            staticBuffered = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_RGB);
        }
        drawUsingImageBuffer(frameMarker, staticBuffered);
    }

    static void drawArgbBufferedImage(Consumer<Graphics2D> frameMarker) {
        BufferedImage buffer = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
        drawUsingImageBuffer(frameMarker, buffer);
    }

    static void reuseArgbBufferedImage(Consumer<Graphics2D> frameMarker) {
        if (staticArgbBuffered == null) {
            staticArgbBuffered = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
        }
        drawUsingImageBuffer(frameMarker, staticArgbBuffered);
    }

    static void drawVolatileImage(Consumer<Graphics2D> frameMarker) {
        VolatileImage volatileImage = panel.createVolatileImage(1280, 720);
        drawUsingImageBuffer(frameMarker, volatileImage);
    }

    static void reuseVolatileImage(Consumer<Graphics2D> frameMarker) {
        if (staticVolatile == null || staticVolatile.contentsLost()) {
            staticVolatile = panel.createVolatileImage(1280, 720);
        }
        drawUsingImageBuffer(frameMarker, staticVolatile);
    }

    static void drawDoubleBuffer(Consumer<Graphics2D> frameMarker) {
        Insets insets = frame.getInsets();
        Toolkit.getDefaultToolkit().sync();
        BufferStrategy strategy = frame.getBufferStrategy();
        if (strategy != null) {
            do {
                do {
                    Graphics2D graphics = (Graphics2D) strategy.getDrawGraphics();
                    graphics.translate(insets.left, insets.top);
                    graphics.drawImage(testimage, 0, 0, 1280, 720, null);
                    frameMarker.accept(graphics);
                    graphics.dispose();
                } while (strategy.contentsRestored());
                strategy.show();
            } while (strategy.contentsLost());
        } else {
            Graphics2D graphics = (Graphics2D) panelGraphics;
            graphics.drawImage(testimage, insets.left, insets.top, 1280, 720, null);
            frameMarker.accept(graphics);
        }

    }
}
