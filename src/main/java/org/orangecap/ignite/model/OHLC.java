package org.orangecap.ignite.model;

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

  public int getSecurityId() {
    return securityId;
  }

  public long[] getTime() {
    return time;
  }

  public int getSize() {
    return size;
  }
}
