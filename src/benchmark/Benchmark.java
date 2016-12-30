package benchmark;

import java.awt.Graphics;
import java.awt.image.*;


public class Benchmark {
	
	
	// Window Objects
	static javax.swing.JFrame frame;
	static javax.swing.JPanel panel;
	
	// Settings
	static int amt;
	static boolean directDraw;
	static boolean withTransparency;
	
	
	public static void main( String[] args ) {
		
		// Create JFrame
		frame = new javax.swing.JFrame( "Benchmark" );
		frame.setDefaultCloseOperation( javax.swing.JFrame.EXIT_ON_CLOSE );
		// Create JPanel
		panel = new javax.swing.JPanel( true );
		panel.setPreferredSize( new java.awt.Dimension( 1280, 720 ) );
		frame.setContentPane( panel );
		
		// Getting Settings (Input)
		java.util.Scanner scanner = new java.util.Scanner( System.in );
		System.out.print( "Number of loop-runs: " );
		amt = scanner.nextInt();
		System.out.print( "Draw image directly? " );
		directDraw = scanner.nextBoolean();
		System.out.print( "Run the test with transparency? " );
		withTransparency = scanner.nextBoolean();
		scanner.close();
		
		// Load Test Image
		try {
			if( !withTransparency )
				testimage = javax.imageio.ImageIO.read( new java.io.File( "src/benchmark/image.png" ) );
			else if( withTransparency )
				testimage = javax.imageio.ImageIO.read( new java.io.File( "src/benchmark/image_transp.png" ) );
		} catch( java.io.IOException e ) {
			e.printStackTrace();
		}
		
		// Set Window Visible and get Panel Graphics
		frame.pack();
		frame.setVisible( true );
		panelGraphics = panel.getGraphics();
		
		// Start Tests and Display Results
		testBufferedImage();
		testVolatileImage();
		
		// Exiting
		System.exit( 0 );
		
	}
	
	
	// Test Image
	static java.awt.Image testimage;
	
	// Draw Graphics
	static Graphics panelGraphics;
	
	// BufferedImage
	static BufferedImage bufferedImage;
	static Graphics bufferedImageGraphics;
	
	// VolatileImage
	static VolatileImage volatileImage;
	static Graphics volatileImageGraphics;
	
	
	static void testBufferedImage() {
		
		double start = System.nanoTime();
		
		bufferedImage = new BufferedImage( 1280, 720, BufferedImage.TYPE_INT_RGB );
		bufferedImageGraphics = bufferedImage.getGraphics();
		
		// Check methodology
		if( directDraw ) { // 1
			for( int counter = 0; counter < amt; counter++ ) {
				// Draw to image
				bufferedImageGraphics.drawImage( testimage, 0, 0, 1280, 720, null );
				// Draw to screen
				panelGraphics.drawImage( bufferedImage, 0, 0, 1280, 720, null );
			}
		} else { // 2
			for( int counter = 0; counter < amt; counter++ ) {
				// Draw to image
				bufferedImageGraphics.drawImage( testimage, 0, 0, 1280, 720, null );
			}
			// Draw to screen
			panelGraphics.drawImage( bufferedImage, 0, 0, 1280, 720, null );
		}
		
		double elapsed = (System.nanoTime() - start) / 1000000000.0;
		
		System.out.printf( "\n BufferedImage results:" + "\t" + "%.3fs", elapsed );
		
	}
	
	static void testVolatileImage() {
		
		double start = System.nanoTime();
		
		volatileImage = panel.createVolatileImage( 1280, 720 );
		volatileImageGraphics = volatileImage.getGraphics();
		
		// Check methodology
		if( directDraw ) { // 1
			for( int counter = 0; counter < amt; counter++ ) {
				// Draw to image
				volatileImageGraphics.drawImage( testimage, 0, 0, 1280, 720, null );
				// Draw to screen
				panelGraphics.drawImage( volatileImage, 0, 0, 1280, 720, null );
			}
		} else { // 2
			for( int counter = 0; counter < amt; counter++ ) {
				// Draw to image
				volatileImageGraphics.drawImage( testimage, 0, 0, 1280, 720, null );
			}
			// Draw to screen
			panelGraphics.drawImage( volatileImage, 0, 0, 1280, 720, null );
		}
		
		double elapsed = (System.nanoTime() - start) / 1000000000.0;
		
		System.out.printf( "\n VolatileImage results:" + "\t" + "%.3fs", elapsed );
		
	}
	
	
}
