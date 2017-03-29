# Benchmark
An approach on benchmarking the performance of the BufferedImage and the VolatileImage in a 2D-Graphics envoirement.

## Strategy
Seven different methods of drawing to an offscreen buffer were used.
* Draw to a new RGB BufferedImage, then draw the image to the screen.
* Draw to a new ARGB BufferedImage, then draw the image to the screen.
* Draw to a new VolatileImage, then draw the image to the screen.
* Draw to an existing RGB BufferedImage, then draw the image to the screen.
* Draw to an existing ARGB BufferedImage, then draw the image to the screen.
* Draw to an existing VolatileImage, then draw the image to the screen.
* Set up a Window buffer strategy, obtain a graphics context from the strategy, draw to that offscreen buffer, then flip to that buffer using the buffer strategy.

To ensure the results were not as likely to be affected by garbage collection and heap reallocation, the benchmark runs the tests in three batches, and only the last batch of results is displayed.  Displaying the results for earlier batches demonstrates that performance improves with each run early in the JVM run.  Performance seems to settle down around the middle of the second batch - at least on the test system I used.

## Test System
Intel Core i3-3227U<br/>
6 GB RAM<br/>
Intel HD Graphics 4000 

java version "1.8.0_121"<br/>
Java(TM) SE Runtime Environment (build 1.8.0_121-b13)<br/>
Java HotSpot(TM) 64-Bit Server VM (build 25.121-b13, mixed mode)

## Results by buffer count
<table>
<tr><td colspan="2">3 Buffers</td></tr>
<tr><td>Reuse Volatile Average</td><td>256.30</td></tr>
<tr><td>Reuse Buffered Average</td><td>249.34</td></tr>
<tr><td>Buffered Image Average</td><td>230.89</td></tr>
<tr><td>Direct Buffer Average</td><td>199.58</td></tr>
<tr><td>Volatile Image Average</td><td>196.19</td></tr>
<tr><td>Reuse ARGB Image Average</td><td>86.53</td></tr>
<tr><td>ARGB Image Average</td><td>82.65</td></tr>
<tr><td colspan="2">2 Buffers</td></tr>
<tr><td>Reuse Buffered Average</td><td>251.15</td></tr>
<tr><td>Reuse Volatile Average</td><td>249.38</td></tr>
<tr><td>Direct Buffer Average</td><td>247.06</td></tr>
<tr><td>Buffered Image Average</td><td>230.40</td></tr>
<tr><td>Volatile Image Average</td><td>196.54</td></tr>
<tr><td>Reuse ARGB Image Average</td><td>86.49</td></tr>
<tr><td>ARGB Image Average</td><td>83.25</td></tr>
<tr><td colspan="2">1 Buffer</td></tr>
<tr><td>Reuse Volatile Average</td><td>255.94</td></tr>
<tr><td>Reuse Buffered Average</td><td>248.44</td></tr>
<tr><td>Buffered Image Average</td><td>230.90</td></tr>
<tr><td>Direct Buffer Average</td><td>210.96</td></tr>
<tr><td>Volatile Image Average</td><td>195.40</td></tr>
<tr><td>Reuse ARGB Image Average</td><td>85.97</td></tr>
<tr><td>ARGB Image Average</td><td>83.23</td></tr>
</table>

## Results by offscreen buffer method
<table>
<tr><td colspan="2">Direct Buffer</td></tr>
<tr><td>3 Buffers Avg FPS</td><td>199.58</td></tr>
<tr><td>2 Buffers Avg FPS</td><td>247.06</td></tr>
<tr><td>1 Buffer Avg FPS</td><td>210.96</td></tr>

<tr><td colspan="2">Buffered Image</td></tr>
<tr><td>3 Buffers Avg FPS</td><td>230.89</td></tr>
<tr><td>2 Buffers Avg FPS</td><td>230.40</td></tr>
<tr><td>1 Buffer Avg FPS</td><td>230.90</td></tr>

<tr><td colspan="2">Reuse Buffered</td></tr>
<tr><td>3 Buffers Avg FPS</td><td>249.34</td></tr>
<tr><td>2 Buffers Avg FPS</td><td>251.15</td></tr>
<tr><td>1 Buffer Avg FPS</td><td>248.44</td></tr>

<tr><td colspan="2">ARGB Image</td></tr>
<tr><td>3 Buffers Avg FPS</td><td>82.65</td></tr>
<tr><td>2 Buffers Avg FPS</td><td>83.25</td></tr>
<tr><td>1 Buffer Avg FPS</td><td>83.23</td></tr>

<tr><td colspan="2">Reuse ARGB Image</td></tr>
<tr><td>3 Buffers Avg FPS</td><td>86.53</td></tr>
<tr><td>2 Buffers Avg FPS</td><td>86.49</td></tr>
<tr><td>1 Buffer Avg FPS</td><td>85.97</td></tr>

<tr><td colspan="2">Volatile Image</td></tr>
<tr><td>3 Buffers Avg FPS</td><td>196.19</td></tr>
<tr><td>2 Buffers Avg FPS</td><td>196.54</td></tr>
<tr><td>1 Buffer Avg FPS</td><td>195.40</td></tr>

<tr><td colspan="2">Reuse Volatile</td></tr>
<tr><td>3 Buffers Avg FPS</td><td>256.30</td></tr>
<tr><td>2 Buffers Avg FPS</td><td>249.38</td></tr>
<tr><td>1 Buffer Avg FPS</td><td>255.94</td></tr>
</table>

## Analysis
The number of buffers used by the AWT window is clearly only of any use when using the buffer strategy method.  Using an AWT Image of any kind as your offscreen buffer bypasses the buffer strategy entirely.

My earlier iterations of the benchmark did not reuse the buffer, so I was getting consistently better performance with the RGB BufferedImage implementation.  That surprised me, but after some reflection I realized that the image allocation may be the bottleneck in that case.  I then implemented the methods where the buffer images were reused, and found the performance benefit I was expecting from the VolatileImage method.

It's worth pointing out that reusing an RGB BufferedImage gives only slightly worse performance than using a reused VolatileImage.  If you want to be able to draw to an offscreen buffer without worrying about losing the contents due to other operations (like the user minimizing the window) an RGB BufferedImage may be the way to go.

Another note: The ARGB BufferedImage implementation is consistently the slowest method for offscreen buffering.  You should avoid using an ARGB BufferedImage as your offscreen buffer unless you know you really need the alpha channel.
