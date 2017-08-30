package org.orangecap.ignite.model;

import java.io.Serializable;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class Key implements Serializable {
  @QuerySqlField(orderedGroups = {@QuerySqlField.Group(
    name = "date_sec_idx", order = 0, descending = true)})
  private long date;

  @QuerySqlField(index = true, orderedGroups = {@QuerySqlField.Group(
    name = "date_sec_idx", order = 3)})
  private int securityId;

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

  public void setSecurityId(int securityId) {
    this.securityId = securityId;
  }

  public void setDate(long date) {
    this.date = date;
  }
}
