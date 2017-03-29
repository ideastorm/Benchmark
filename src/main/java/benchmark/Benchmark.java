package benchmark;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
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

    private static final int INNER_LOOP_COUNT = 200;
    private static final int OUTER_LOOP_COUNT = 10;
    

    // Window Objects
    static javax.swing.JFrame frame;
    static javax.swing.JPanel panel;

    private final Consumer<Consumer<Graphics2D>> painter;

    private Benchmark(Consumer<Consumer<Graphics2D>> painter) {
        this.painter = painter;
    }

    @Override
    public Long call() {
        long start = System.nanoTime();
        for (int i = 0; i < INNER_LOOP_COUNT; i++) {
            final String frameCount = "" + i;
            painter.accept((Graphics2D g) -> {
                g.setColor(Color.BLACK);
                g.drawString(frameCount, 10, 100);
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

            // Start Tests and Display Results
            frame.createBufferStrategy(2);
            List<Long> doubleBufferTimes = new ArrayList<>();
            List<Long> bufferedImageTimes = new ArrayList<>();
            List<Long> volatileImageTimes = new ArrayList<>();
            for (int i = 0; i < OUTER_LOOP_COUNT; i++) {
                frame.setTitle("Benchmark - Direct Double Buffer");
                doubleBufferTimes.add(new Benchmark(Benchmark::drawDoubleBuffer).call());
                frame.setTitle("Benchmark - Buffered Image");
                bufferedImageTimes.add(new Benchmark(Benchmark::drawBufferedImage).call());
                frame.setTitle("Benchmark - Volatile Image");
                volatileImageTimes.add(new Benchmark(Benchmark::drawVolatileImage).call());
            }
            bufferedImageTimes.forEach(t -> System.out.printf("Buffered Image %01.2f\n", calcFps(t)));
            volatileImageTimes.forEach(t -> System.out.printf("Volatile Image %01.2f\n", calcFps(t)));
            doubleBufferTimes.forEach(t -> System.out.printf("Double Buffer  %01.2f\n", calcFps(t)));
            System.out.printf("Buffered Image Average %01.2f\n", bufferedImageTimes.stream().mapToDouble(Benchmark::calcFps).average().getAsDouble());
            System.out.printf("Volatile Image Average %01.2f\n", volatileImageTimes.stream().mapToDouble(Benchmark::calcFps).average().getAsDouble());
            System.out.printf("Double Buffer Average  %01.2f\n", doubleBufferTimes.stream().mapToDouble(Benchmark::calcFps).average().getAsDouble());
            

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

    // Draw Graphics
    static Graphics panelGraphics;

    static void drawBufferedImage(Consumer<Graphics2D> frameMarker) {
        BufferedImage bufferedImage = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_RGB);
        Graphics2D bufferedImageGraphics = (Graphics2D) bufferedImage.getGraphics();
        bufferedImageGraphics.drawImage(testimage, 0, 0, 1280, 720, null);
        frameMarker.accept(bufferedImageGraphics);
        panelGraphics.drawImage(bufferedImage, 0, 0, 1280, 720, null);
    }

    static void drawVolatileImage(Consumer<Graphics2D> frameMarker) {
        VolatileImage volatileImage = panel.createVolatileImage(1280, 720);
        Graphics2D volatileImageGraphics = (Graphics2D) volatileImage.getGraphics();
        volatileImageGraphics.drawImage(testimage, 0, 0, 1280, 720, null);
        frameMarker.accept(volatileImageGraphics);
        panelGraphics.drawImage(volatileImage, 0, 0, 1280, 720, null);
    }

    static void drawDoubleBuffer(Consumer<Graphics2D> frameMarker) {
        Insets insets = frame.getInsets();
        Toolkit.getDefaultToolkit().sync();
        BufferStrategy strategy = frame.getBufferStrategy();
        if (strategy != null) {
            do {
                do {
                    Graphics2D graphics = (Graphics2D) strategy.getDrawGraphics();
                    graphics.drawImage(testimage, insets.left, insets.top, 1280, 720, null);
                    frameMarker.accept(graphics);
                    graphics.dispose();
                } while (strategy.contentsRestored());
                strategy.show();
            } while (strategy.contentsLost());
        }

    }
}
