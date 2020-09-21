public void removeDatabase(String databaseName) {
    try {
    Statement stmt = conn.createStatement();
    stmt.execute("DROP DATABASE " + databaseName);
    } catch (SQLException ex) {...}
    }