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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.Ignition;
import org.apache.ignite.spark.JavaIgniteContext;
import org.apache.ignite.spark.JavaIgniteRDD;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.jetbrains.annotations.NotNull;
import org.orangecap.ignite.model.Key;
import org.orangecap.ignite.model.OHLC;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.UniformReservoir;

import scala.Tuple2;

/**
 * Start Ignite node.
 */
public class IgniteNode {
  private static final Random RAND = new Random(123);
  private static final int SAMPLE_SIZE = 1_000_000; // 00
  private static final int BLOCK_SIZE = 5_000;
  private static final String CACHE_NAME = "OHLC";
  private static final String IGNITE_CONFIG_XML = "ignite-config.xml";

  public static void main(String args[]) throws InterruptedException, ClassNotFoundException, SQLException {
    setLoggingLevels();

    final Ignite ignite = Ignition.start(IGNITE_CONFIG_XML);

    System.out.println(" >>> Apache Ignite node is up and running.");

    long date = 0L;
    final Key key = new Key();
    final OHLC value = new OHLC();
    final IgniteCache<Object, Object> cache = ignite.getOrCreateCache(CACHE_NAME);

    // Populate cache
    populateIgnite(ignite, date, key, value);

    // Verify and profile cache
    diagnosticsIgnite(key, cache);

    // Spark API
    final JavaIgniteRDD<Key, OHLC> sharedRDD = getSparkRDD();
    // createOrReplaceTempView("people");

    // Custom in memory processing, alternative processing via Spark using spart-ts lib
    // https://github.com/sryza/spark-timeseries
    inMemoryProcessing(sharedRDD);

    // JDBC Thin driver
    testJDBC();


    // End
    System.out.println(" >>> Simulator - finished checks " + new Date());
  }

  private static void testJDBC() throws ClassNotFoundException, SQLException {
    // Register JDBC driver.
    Class.forName("org.apache.ignite.IgniteJdbcThinDriver");

    // Open the JDBC connection.
    try (Connection conn = DriverManager.getConnection("jdbc:ignite:thin://localhost")) {
      try (PreparedStatement st = conn.prepareStatement("SELECT * FROM " + CACHE_NAME)) {
        st.execute();
      }

      System.out.println("JDBC connection test. Is connection closed: " + conn.isClosed());
    }
  }

  private static void inMemoryProcessing(JavaIgniteRDD<Key, OHLC> sharedRDD) {
    final Tuple2<Key, OHLC> entry = sharedRDD.first();
    final double[] dff = subtract(entry._2.getHigh(), entry._2.getLow());
    System.out.println("Sample calculated time series for the first security: " + Arrays.toString(dff));
  }

  @NotNull
  private static JavaIgniteRDD<Key, OHLC> getSparkRDD() {

    // Spark Conf
    final SparkConf sparkConf = new SparkConf()
      .setAppName("CACHE_NAME")
      .setMaster("local")
      .set("spark.executor.instances", "2");

    // Spark context.
    final JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);

    // Creates Ignite context with specific configuration and runs Ignite in the embedded mode.
    final JavaIgniteContext<Key, OHLC> igniteContext = new JavaIgniteContext<>(
      sparkContext, IGNITE_CONFIG_XML, true);


    // Create a Java Ignite RDD of Type (Key,OHLC) Pair.
    final JavaIgniteRDD<Key, OHLC> sharedRDD = igniteContext.<Key, OHLC>fromCache(CACHE_NAME);
    System.out.println("Count of Spark Rows: " + sharedRDD.count());
    return sharedRDD;
  }

  private static void diagnosticsIgnite(Key key, IgniteCache<Object, Object> cache) {
    System.out.println(" >>> Simulator - Inserted " + cache.size() * BLOCK_SIZE + " " + new Date());
    if (!cache.containsKey(key)) {
      System.err.println("Last key not found");
    } else {
      System.out.println(" >>> Simulator - Checking all entries ");
      System.out.println("Found the last entry " + cache.get(key));
      for (int secId = 0; secId < 100; secId++) {
        key.setSecurityId(secId);
        if (!cache.containsKey(key))
          System.err.println("Key not found" + key);
      }

      final Histogram hist = new Histogram(new UniformReservoir(1_000));
      for (int i = 0; i < 10_000; i++) {
        key.setSecurityId(RAND.nextInt(100));
        final long from = System.nanoTime();
        cache.get(i);
        final long to = System.nanoTime();
        hist.update((to - from) / 1_000);
      }
      System.out.println("Histogram of latencies fpr lookup in Percentile: <value> micro seconds.");
      final Snapshot snapshot = hist.getSnapshot();
      System.out.println("Min: " + snapshot.getMin());
      System.out.println("50: " + snapshot.getMedian());
      System.out.println("59: " + snapshot.get75thPercentile());
      System.out.println("98: " + snapshot.get98thPercentile());
      System.out.println("99: " + snapshot.get99thPercentile());
      System.out.println("99.9: " + snapshot.get999thPercentile());
      System.out.println("Max: " + snapshot.getMax());
    }
  }

  private static void populateIgnite(Ignite ignite, long date, Key key, OHLC value) {
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

        secId++; // for unique values
        //secId = RAND.nextInt(1000);

        if (blockId % 100 == 0)
          System.out.println("Processed " + (blockId * BLOCK_SIZE) + " events so far " + new Date());
      }
      streamer.flush();
    }
  }

  /**
   * Subtract values from one time series from another.
   *
   * @param a the time series to subtract from.
   * @param b the time series to subtract.
   * @return values as {@code double[]}.
   */
  private static double[] subtract(double[] a, double[] b) {
    final int length = a.length;
    double[] rez = new double[length];
    for (int i = 0; i < length; i++)
      rez[i] = a[i] - b[i];
    return rez;
  }

  private static void setLoggingLevels() {
    // Adjust the logger to exclude the logs of no interest.
    Logger.getRootLogger().setLevel(Level.ERROR);
    Logger.getLogger("org.apache.ignite").setLevel(Level.INFO);
  }
}

