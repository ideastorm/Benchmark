# Benchmark
An approach on benchmarking the performance of the BufferedImage and the VolatileImage in a 2D-Graphics envoirement.

## Strategy
Three different methods of drawing to an offscreen buffer were used.
Method 1: Draw to a BufferedImage, then draw the image to the screen.
Method 2: Draw to a VolatileImage, then draw the image to the screen.
Method 3: Set up a Window buffer strategy, obtain a graphics context from the strategy, draw to that offscreen buffer, then flip to that buffer using the buffer strategy.

## Test System
Intel Core i3-3227U
6 GB RAM
Intel HD Graphics 4000 

## Results
3 Buffers
Buffered Image Average FPS 218.88
Volatile Image Average FPS 187.57
 Direct Buffer Average FPS 190.25

2 Buffers
Buffered Image Average FPS 221.64
Volatile Image Average FPS 189.68
 Direct Buffer Average FPS 240.82

1 Buffers
Buffered Image Average FPS 219.36
Volatile Image Average FPS 190.13
 Direct Buffer Average FPS 214.22

## Analysis
The number of buffers used by the AWT window is clearly only of any use when using the buffer strategy method.  Drawing to either a Buffered or Volatile image as your offscreen buffer bypasses the buffer strategy.

The BufferedImage method gives consistently good performance, and surprisingly, better performance than the volatile image.  I was thinking it might be due to color space differences, but those would also show up when drawing the buffered image to the screen.  At this point, I don't know why BufferedImage is actually faster.

Since the best direct buffer drawing performance was with a double buffer, I'm going to ignore the single buffer and triple buffer results.  That means that we effectively have three double buffer methods.  The most performant obtains a graphics drawing context from the graphics buffer, which is what I thought VolatileImage was supposed to be doing as well.  The buffer strategy approach is more complicated than simply using BufferedImage, but if you need maximum performance (or you want to do your best to avoid redrawing flicker) using the buffer strategy is the way to go.
