public class Dummy{
public boolean registerUser(String username, String password) throws DatabaseException {
    try (Statement RegisterUserStmt = databaseConnection.createStatement()) {
        boolean isRegistered = false;
        String Register_SQL_QUERY = "Insert into " + dbTableName + " (Username,Password) values('" + username + "','" + password + "');";
        if (RegisterUserStmt.executeUpdate(Register_SQL_QUERY) == 1) {
            isRegistered = true;
        }
        return isRegistered;
    } catch (SQLException ex) {
        if (ex.getErrorCode() == 1062 || ex.getErrorCode() == 23505) {
            throw new DatabaseException("Sorry, The Username " + username + " already exists! ", ex);
        } else {
            throw new DatabaseException("SQLException occured while inserting user record in registerUser Method", ex);
        }
    }
}}

