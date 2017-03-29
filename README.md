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

To ensure the results were not as likely to be affected by garbage collection and heap reallocation, the benchmark runs the tests in three batches, and only the last batch of results is displayed.  Displaying the results for earlier batches demonstrates that performance improves with each run early in the JVM run.  Performance seems to settle down around the middle of the second batch - at least on the test systems I used.

## TL;DR
Optimal performance will come from drawing to an existing RGB BufferedImage, an existing VolatileImage, or by using a BufferStrategy with 2 buffers.  If you anticipate multitasking, using an RGB BufferedImage is likely your most performant option.  If you're doing a full screen app with no multitasking expected, use VolatileImage or a BufferStrategy.

Avoid using ARGB BufferedImages unless you really need the embedded alpha channel.

## Test Systems
<table>
<tr><td>Test System 1</td><td>Test System 2</td></tr>
<tr><td>Intel Core i3-3227U</td><td>Intel Core i7-4770</td></tr>
<tr><td>6 GB RAM</td><td>16 GB RAM</td></tr>
<tr><td>Intel HD Graphics 4000</td><td>Intel HD Graphics 4600</td></tr>
<tr><td>Java(TM) SE Runtime Environment (build 1.8.0_121-b13)</td>
<td>Java(TM) SE Runtime Environment (build 1.8.0_92-b14)</td></tr>
</table>

## Results by buffer count
<table>
<tr><th>3 Buffers</th><th>System 1</th><th>System 2</th></tr>
<tr><td>Reuse Volatile Average</td><td>256.30</td><td>536.17</td></tr>
<tr><td>Reuse Buffered Average</td><td>249.34</td><td>530.91</td></tr>
<tr><td>Buffered Image Average</td><td>230.89</td><td>397.37</td></tr>
<tr><td>Direct Buffer Average</td><td>199.58</td><td>359.32</td></tr>
<tr><td>Volatile Image Average</td><td>196.19</td><td>359.03</td></tr>
<tr><td>Reuse ARGB Image Average</td><td>86.53</td><td>157.83</td></tr>
<tr><td>ARGB Image Average</td><td>82.65</td><td>150.76</td></tr>

<tr><th>2 Buffers</th><th>System 1</th><th>System 2</th></tr>
<tr><td>Reuse Buffered Average</td><td>251.15</td><td>547.52</td></tr>
<tr><td>Reuse Volatile Average</td><td>249.38</td><td>531.89</td></tr>
<tr><td>Direct Buffer Average</td><td>247.06</td><td>529.11</td></tr>
<tr><td>Buffered Image Average</td><td>230.40</td><td>418.86</td></tr>
<tr><td>Volatile Image Average</td><td>196.54</td><td>354.33</td></tr>
<tr><td>Reuse ARGB Image Average</td><td>86.49</td><td>156.96</td></tr>
<tr><td>ARGB Image Average</td><td>83.25</td><td>151.10</td></tr>

<tr><th>1 Buffer</th><th>System 1</th><th>System 2</th></tr>
<tr><td>Reuse Volatile Average</td><td>255.94</td><td>537.83</td></tr>
<tr><td>Reuse Buffered Average</td><td>248.44</td><td>531.45</td></tr>
<tr><td>Buffered Image Average</td><td>230.90</td><td>426.35</td></tr>
<tr><td>Direct Buffer Average</td><td>210.96</td><td>405.22</td></tr>
<tr><td>Volatile Image Average</td><td>195.40</td><td>349.76</td></tr>
<tr><td>Reuse ARGB Image Average</td><td>85.97</td><td>153.26</td></tr>
<tr><td>ARGB Image Average</td><td>83.23</td><td>148.01</td></tr>
</table>

## Results by offscreen buffer method
<table>
<tr><th>Direct Buffer</th><th>System 1</th><th>System 2</th></tr>
<tr><td>3 Buffers Avg FPS</td><td>199.58</td><td>359.32</td></tr>
<tr><td>2 Buffers Avg FPS</td><td>247.06</td><td>529.11</td></tr>
<tr><td>1 Buffer Avg FPS</td><td>210.96</td><td>426.35</td></tr>

<tr><th>Buffered Image</th><th>System 1</th><th>System 2</th></tr>
<tr><td>3 Buffers Avg FPS</td><td>230.89</td><td>397.37</td></tr>
<tr><td>2 Buffers Avg FPS</td><td>230.40</td><td>418.86</td></tr>
<tr><td>1 Buffer Avg FPS</td><td>230.90</td><td>405.22</td></tr>

<tr><th>Reuse Buffered</th><th>System 1</th><th>System 2</th></tr>
<tr><td>3 Buffers Avg FPS</td><td>249.34</td><td>530.91</td></tr>
<tr><td>2 Buffers Avg FPS</td><td>251.15</td><td>547.52</td></tr>
<tr><td>1 Buffer Avg FPS</td><td>248.44</td><td>531.45</td></tr>

<tr><th>ARGB Image</th><th>System 1</th><th>System 2</th></tr>
<tr><td>3 Buffers Avg FPS</td><td>82.65</td><td>150.76</td></tr>
<tr><td>2 Buffers Avg FPS</td><td>83.25</td><td>151.10</td></tr>
<tr><td>1 Buffer Avg FPS</td><td>83.23</td><td>148.01</td></tr>

<tr><th>Reuse ARGB Image</th><th>System 1</th><th>System 2</th></tr>
<tr><td>3 Buffers Avg FPS</td><td>86.53</td><td>157.83</td></tr>
<tr><td>2 Buffers Avg FPS</td><td>86.49</td><td>156.96</td></tr>
<tr><td>1 Buffer Avg FPS</td><td>85.97</td><td>153.26</td></tr>

<tr><th>Volatile Image</th><th>System 1</th><th>System 2</th></tr>
<tr><td>3 Buffers Avg FPS</td><td>196.19</td><td>359.03</td></tr>
<tr><td>2 Buffers Avg FPS</td><td>196.54</td><td>354.33</td></tr>
<tr><td>1 Buffer Avg FPS</td><td>195.40</td><td>349.76</td></tr>

<tr><th>Reuse Volatile</th><th>System 1</th><th>System 2</th></tr>
<tr><td>3 Buffers Avg FPS</td><td>256.30</td><td>536.17</td></tr>
<tr><td>2 Buffers Avg FPS</td><td>249.38</td><td>531.89</td></tr>
<tr><td>1 Buffer Avg FPS</td><td>255.94</td><td>537.83</td></tr>
</table>

## Analysis
The number of buffers used by the AWT window is clearly only of any use when using the buffer strategy method.  Using an AWT Image of any kind as your offscreen buffer bypasses the buffer strategy entirely.

My earlier iterations of the benchmark did not reuse the buffer, so I was getting consistently better performance with the RGB BufferedImage implementation.  That surprised me, but after some reflection I realized that the volatile image allocation may be the bottleneck in that case.  I then implemented the methods where the buffer images were reused, and found the performance benefit I was expecting from the VolatileImage method.

It's worth pointing out that reusing an RGB BufferedImage gives only slightly worse performance than using a reused VolatileImage - and in some cases, it was consistently faster.  If you want to be able to draw to an offscreen buffer without worrying about losing the contents due to other operations (like the user minimizing or otherwise obscuring the window) an RGB BufferedImage may be the way to go.

Another note: The ARGB BufferedImage implementations are consistently the slowest methods for offscreen buffering.  You should avoid using an ARGB BufferedImage as your offscreen buffer unless you know you really need the alpha channel.
