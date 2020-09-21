public class Dummy{
public boolean toCheckEmailUser(ForgotPassword forgotObj) {
    Statement preStat = null;
    boolean isValidUser = false;
    String sqlQuery = "Select * from User where email="+forgotObj.getEmail() +"and userName="+forgotObj.getUserName() ;
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

