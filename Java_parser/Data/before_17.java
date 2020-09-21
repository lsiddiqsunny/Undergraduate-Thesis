public class Dummy {

  public String loginUser(String username) throws DatabaseException {
    try (Statement LoginUserStmt = databaseConnection.createStatement()) {
      String Login_SQL_QUERY =
        "Select Password from " +
        dbTableName +
        " where Username = '" +
        username +
        "';";
      try (
        ResultSet QueryResult = LoginUserStmt.executeQuery(Login_SQL_QUERY)
      ) {
        String password = null;
        if (QueryResult.isBeforeFirst() && QueryResult.next()) {
          password = QueryResult.getString(1);
        }
        return password;
      } catch (SQLException ex) {
        throw new DatabaseException(
          "SQLException occured in ResultSet of LoginUser method",
          ex
        );
      }
    } catch (SQLException ex) {
      throw new DatabaseException("SQLException in LoginUser Method", ex);
    }
  }
}
