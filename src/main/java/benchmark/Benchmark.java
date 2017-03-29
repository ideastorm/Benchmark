package benchmark;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.concurrent.Callable;
import javax.imageio.ImageIO;

public class Benchmark implements Callable<Long> {
    private static final int LOOP_COUNT = 1000;

    // Window Objects
    static javax.swing.JFrame frame;
    static javax.swing.JPanel panel;

    private final Runnable painter;

    private Benchmark(Runnable painter) {
        this.painter = painter;
    }

    @Override
    public Long call() {
        long start = System.nanoTime();
        for (int i = 0; i < LOOP_COUNT; i++) {
            painter.run();
        }
        long end = System.nanoTime();
        return end - start;
    }

    public static void main(String[] args) {

        // Create JFrame
        frame = new javax.swing.JFrame("Benchmark");
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
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
            Long bufferedImageTime = new Benchmark(Benchmark::drawBufferedImage).call();
            Long volatileImageTime = new Benchmark(Benchmark::drawVolatileImage).call();
            frame.createBufferStrategy(2);
            Long doubleBufferTime = new Benchmark(Benchmark::drawDoubleBuffer).call();
            Long bufferedImage2Time = new Benchmark(Benchmark::drawBufferedImage).call();
            Long volatileImage2Time = new Benchmark(Benchmark::drawVolatileImage).call();
            System.out.printf("Buffered Image 1 %01.2f\n", calcFps(bufferedImageTime));
            System.out.printf("Volatile Image 1 %01.2f\n", calcFps(volatileImageTime));
            System.out.printf("Double Buffered  %01.2f\n", calcFps(doubleBufferTime));
            System.out.printf("Buffered Image 2 %01.2f\n", calcFps(bufferedImage2Time));
            System.out.printf("Volatile Image 2 %01.2f\n", calcFps(volatileImage2Time));

            frame.setVisible(false);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.exit(0); //force shutdown
    }
    
    static double calcFps(Long nanoTime) {
        return LOOP_COUNT * 1_000_000_000.0/nanoTime;
    }

    // Test Image
    static java.awt.Image testimage;
    static java.awt.Image blackimage;

    // Draw Graphics
    static Graphics panelGraphics;

    static void drawBufferedImage() {
        BufferedImage bufferedImage = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_RGB);
        Graphics bufferedImageGraphics = bufferedImage.getGraphics();
        bufferedImageGraphics.drawImage(testimage, 0, 0, 1280, 720, null);
        panelGraphics.drawImage(bufferedImage, 0, 0, 1280, 720, null);
        panelGraphics.drawImage(blackimage, 0, 0, 1280, 720, null);
    }

    static void drawVolatileImage() {
        VolatileImage volatileImage = panel.createVolatileImage(1280, 720);
        Graphics volatileImageGraphics = volatileImage.getGraphics();
        volatileImageGraphics.drawImage(testimage, 0, 0, 1280, 720, null);
        panelGraphics.drawImage(volatileImage, 0, 0, 1280, 720, null);
        panelGraphics.drawImage(blackimage, 0, 0, 1280, 720, null);
    }
    
    static void drawDoubleBuffer() {
            Toolkit.getDefaultToolkit().sync();
            BufferStrategy strategy = frame.getBufferStrategy();
            if (strategy != null) {
                do {
                    do {
                        Graphics2D graphics = (Graphics2D) strategy.getDrawGraphics();
                        graphics.drawImage(testimage,0,0,1280,720,null);
                        graphics.dispose();
                    } while (strategy.contentsRestored());
                    strategy.show();
                } while (strategy.contentsLost());
                do {
                    do {
                        Graphics2D graphics = (Graphics2D) strategy.getDrawGraphics();
                        graphics.drawImage(blackimage,0,0,1280,720,null);
                        graphics.dispose();
                    } while (strategy.contentsRestored());
                    strategy.show();
                } while (strategy.contentsLost());
            }
        
    }
}
