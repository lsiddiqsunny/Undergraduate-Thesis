public class Dummy{
public boolean checkUserName(String userName) {
    Statement preStat = null;
    boolean isValidUser = false;
    String sqlQuery = "Select * from User where userName= "+userName;
    try {
        preStat = connection.createStatement();
        ResultSet resultSet = preStat.executeQuery(sqlQuery);
        if (resultSet.next()) {
            return isValidUser = true;
        } else
            return isValidUser;
    } catch (SQLException e) {
        return isValidUser;
    }
}}

