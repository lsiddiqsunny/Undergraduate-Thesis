public class Dummy{
public boolean isUser(Login login) {
    Statement preStat = null;
    boolean isValidUser = false;
    String sqlQuery = "Select * from User where userName="+login.getUserName()+" and password="+login.getPassword();
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

