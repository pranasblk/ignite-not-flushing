package org.orangecap.ignite.model;

import java.util.Arrays;

public class OHLC {
  private long date;
  private int securityId;
  private int size;
  private long[] time;
  private double[] open;
  private double[] high;
  private double[] low;
  private double[] close;
  private double[] marketVWAP;

  public OHLC() {
    // Required for default binary serialization
  }

  public OHLC(int securityId, long date, long[] time, double[] open, double[] high, double[] low, double[] close,
    double[] marketVWAP) {
    this.securityId = securityId;
    this.date = date;
    this.time = time;
    this.open = open;
    this.high = high;
    this.low = low;
    this.close = close;
    this.marketVWAP = marketVWAP;
    this.size = time.length;
  }

  public int getSecurityId() {
    return securityId;
  }

  public void setSecurityId(int securityId) {
    this.securityId = securityId;
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

  public int getSize() {
    return size;
  }

  public long getDate() {
    return date;
  }

  public void setDate(long date) {
    this.date = date;
  }

  public void setSize(int size) {
    this.size = size;
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
      "date=" + date +
      ", securityId=" + securityId +
      ", size=" + size +
      ", time=" + Arrays.toString(time) +
      ", open=" + Arrays.toString(open) +
      ", high=" + Arrays.toString(high) +
      ", low=" + Arrays.toString(low) +
      ", close=" + Arrays.toString(close) +
      ", marketVWAP=" + Arrays.toString(marketVWAP) +
      '}';
  }
}
