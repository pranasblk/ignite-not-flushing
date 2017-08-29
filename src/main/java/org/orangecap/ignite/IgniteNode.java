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

import org.orangecap.ignite.model.Key;
import org.orangecap.ignite.model.OHLC;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.Ignition;

/**
 * Start Ignite node.
 */
public class IgniteNode {
  private static final Random RAND = new Random(123);
  private static final int SAMPLE_SIZE = 100_000_000;
  private static final int BLOCK_SIZE = 4_500;
  private static final String CACHE_NAME = "OHLCV";

  public static void main(String args[]) throws InterruptedException {
    final Ignite ignite = Ignition.start("ignite-config.xml");

    System.out.println(" >>> Apache Ignite node is up and running.");

    System.out.println(" >>> Simulator - Real code would process journal events ... TODO.");

    long date = 0L;
    try (IgniteDataStreamer<Key, OHLC> streamer = ignite.dataStreamer(CACHE_NAME)) {
      streamer.allowOverwrite(true);
      streamer.perNodeBufferSize(20);
      streamer.autoFlushFrequency(TimeUnit.SECONDS.toMillis(30));
      streamer.skipStore(false);
      streamer.keepBinary(true);

      for (int blockId = 0; blockId < (SAMPLE_SIZE / BLOCK_SIZE); blockId++) {

        final long[] time = new long[BLOCK_SIZE];
        final double[] open = new double[BLOCK_SIZE];
        final double[] close = new double[BLOCK_SIZE];
        final double[] high = new double[BLOCK_SIZE];
        final double[] low = new double[BLOCK_SIZE];
        final double[] marketVWAP = new double[BLOCK_SIZE];

        int secId = 0;
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
        streamer.addData(new Key(secId, date), new OHLC(secId, date, time, open, high, low, close, marketVWAP));
        secId++;
        if (blockId % 20 == 0) {
          streamer.flush();
          System.out.println("Processed " + (blockId * BLOCK_SIZE) + " events so far " + new Date());
        }
      }

      final IgniteCache<Object, Object> cache = ignite.getOrCreateCache(CACHE_NAME);
      System.out.println(" >>> Simulator - Inserted " + cache.size() * BLOCK_SIZE + " " + new Date());
      Thread.sleep(TimeUnit.SECONDS.toMillis(40));
      System.out.println(" >>> Simulator - Inserted " + cache.size() * BLOCK_SIZE + " " + new Date());
      Thread.sleep(TimeUnit.SECONDS.toMillis(40));
      System.out.println(" >>> Simulator - Inserted " + cache.size() * BLOCK_SIZE + " " + new Date());
    }
  }
}
