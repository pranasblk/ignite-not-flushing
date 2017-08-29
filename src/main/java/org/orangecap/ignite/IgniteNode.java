/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.orangecap.ignite;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.Ignition;
import org.orangecap.ignite.model.Key;
import org.orangecap.ignite.model.OHLC;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.UniformReservoir;

/**
 * Start Ignite node.
 */
public class IgniteNode {
  private static final Random RAND = new Random(123);
  private static final int SAMPLE_SIZE = 100_000_000;
  private static final int BLOCK_SIZE = 5_000;
  private static final String CACHE_NAME = "OHLCV";

  public static void main(String args[]) throws InterruptedException {
    final Ignite ignite = Ignition.start("ignite-config.xml");

    System.out.println(" >>> Apache Ignite node is up and running.");

    System.out.println(" >>> Simulator - Real code would process journal events ... TODO.");

    long date = 0L;
    Key key = new Key();
    OHLC value = new OHLC();
    try (IgniteDataStreamer<Key, OHLC> streamer = ignite.dataStreamer(CACHE_NAME)) {
      streamer.allowOverwrite(true);
      streamer.perNodeBufferSize(50);
      streamer.autoFlushFrequency(TimeUnit.SECONDS.toMillis(45));
      streamer.skipStore(false);
      streamer.keepBinary(true);

      int secId = 0;
      final int maxBlocks = SAMPLE_SIZE / BLOCK_SIZE;
      for (int blockId = 0; blockId < maxBlocks; blockId++) {

        final long[] time = new long[BLOCK_SIZE];
        final double[] open = new double[BLOCK_SIZE];
        final double[] close = new double[BLOCK_SIZE];
        final double[] high = new double[BLOCK_SIZE];
        final double[] low = new double[BLOCK_SIZE];
        final double[] marketVWAP = new double[BLOCK_SIZE];

        int i = 0;
        for (; i < BLOCK_SIZE; i++) {
          // Fake data
          time[i] = System.nanoTime();
          open[i] = Math.abs(RAND.nextGaussian());
          close[i] = Math.abs(RAND.nextGaussian());
          high[i] = Math.max(open[i], Math.abs(RAND.nextGaussian()));
          low[i] = Math.min(open[i], Math.abs(RAND.nextGaussian()));
          marketVWAP[i] = Math.abs(RAND.nextGaussian());
        }

        // Add to the cache
        key.setSecurityId(secId);
        key.setDate(date);

        value.setSize(time.length);
        value.setSecurityId(secId);
        value.setDate(date);
        value.setOpen(open);
        value.setHigh(high);
        value.setLow(low);
        value.setClose(close);
        value.setMarketVWAP(marketVWAP);

        streamer.addData(key, value);
        secId++;

        if (blockId % 50 == 0)
          System.out.println("Processed " + (blockId * BLOCK_SIZE) + " events so far " + new Date());
      }
      streamer.flush();

      final IgniteCache<Object, Object> cache = ignite.getOrCreateCache(CACHE_NAME);
      System.out.println(" >>> Simulator - Inserted " + cache.size() * BLOCK_SIZE + " " + new Date());
      if (!cache.containsKey(key)) {
        System.err.println("Last key not found");
      } else {
        System.out.println(" >>> Simulator - Checking all entries ");
        System.out.println("Found the last entry " + cache.get(key));
        for (secId = 0; secId < maxBlocks; secId++) {
          key.setSecurityId(secId);
          if (!cache.containsKey(key))
            System.err.println("Key not found" + key);
        }

        final Histogram hist = new Histogram(new UniformReservoir(1_000));
        for (int i = 0; i < 10_000; i++) {
          key.setSecurityId(RAND.nextInt(maxBlocks));
          final long from = System.nanoTime();
          cache.get(i);
          final long to = System.nanoTime();
          hist.update((to - from) / 1_000);
        }
        System.out.println("Histogram of RTT latencies in Percentile: <value> micro seconds.");
        final Snapshot snapshot = hist.getSnapshot();
        System.out.println("0: " + snapshot.getMin());
        System.out.println("50: " + snapshot.getMedian());
        System.out.println("59: " + snapshot.get75thPercentile());
        System.out.println("98: " + snapshot.get98thPercentile());
        System.out.println("99: " + snapshot.get99thPercentile());
        System.out.println("999: " + snapshot.get999thPercentile());
      }
      System.out.println(" >>> Simulator - finished checks " + new Date());

    }
  }
}

