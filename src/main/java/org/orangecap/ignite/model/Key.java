package org.orangecap.ignite.model;

public class Key {
  private int securityId;
  private long date;

  public Key() {
    // Required for default binary serialization
  }

  public Key(int securityId, long date) {
    this.securityId = securityId;
    this.date = date;
  }

  public int getSecurityId() {
    return securityId;
  }

  public long getDate() {
    return date;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Key)) return false;

    Key key = (Key) o;

    return securityId == key.securityId && date == key.date;
  }

  @Override
  public int hashCode() {
    int result = securityId;
    result = 31 * result + (int) (date ^ (date >>> 32));
    return result;
  }
}
