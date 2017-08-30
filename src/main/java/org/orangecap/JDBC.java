package org.orangecap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class JDBC {
  public static void main(String[] args) throws ClassNotFoundException, SQLException {
    // Register JDBC driver.
    Class.forName("org.apache.ignite.IgniteJdbcThinDriver");

    // Open the JDBC connection.
    Connection conn = DriverManager.getConnection("jdbc:ignite:thin://localhost");

    final PreparedStatement st = conn.prepareStatement("SELECT * FROM OHLC");
    final ResultSet rs = st.executeQuery();
    while (rs.next()) {
      final Object arr = rs.getObject("TIME"); // Hello where are you?
      long[] open = (long[]) arr;
      System.out.println(Arrays.toString(open));
    }
  }
}
