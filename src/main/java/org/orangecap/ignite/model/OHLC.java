package org.orangecap.ignite.model;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class OHLC implements Serializable {

  @QuerySqlField() //Apache Ignite 2.1 BUG
  private long[] time;

  @QuerySqlField()
  private double[] open;

  @QuerySqlField()
  private double[] high;

  @QuerySqlField()
  private double[] low;

  @QuerySqlField()
  private double[] close;

  @QuerySqlField()
  private double[] marketVWAP;

  public OHLC() {
    // Required for default binary serialization
  }

  public OHLC(int securityId, long date, long[] time, double[] open, double[] high, double[] low, double[] close,
    double[] marketVWAP) {
    this.time = time;
    this.open = open;
    this.high = high;
    this.low = low;
    this.close = close;
    this.marketVWAP = marketVWAP;
  }

  public double[] getOpen() {
    return open;
  }

  public double[] getHigh() {
    return high;
  }

  public double[] getLow() {
    return low;
  }

  public double[] getClose() {
    return close;
  }

  public double[] getMarketVWAP() {
    return marketVWAP;
  }

  public long[] getTime() {
    return time;
  }

  public void setTime(long[] time) {
    this.time = time;
  }

  public void setOpen(double[] open) {
    this.open = open;
  }

  public void setHigh(double[] high) {
    this.high = high;
  }

  public void setLow(double[] low) {
    this.low = low;
  }

  public void setClose(double[] close) {
    this.close = close;
  }

  public void setMarketVWAP(double[] marketVWAP) {
    this.marketVWAP = marketVWAP;
  }

  @Override
  public String toString() {
    return "OHLC{" +
      ", time=" + Arrays.toString(time) +
      ", open=" + Arrays.toString(open) +
      ", high=" + Arrays.toString(high) +
      ", low=" + Arrays.toString(low) +
      ", close=" + Arrays.toString(close) +
      ", marketVWAP=" + Arrays.toString(marketVWAP) +
      '}';
  }
}
