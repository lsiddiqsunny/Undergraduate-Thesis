public class Dummy{
public void updatePassword(ForgotPassword forgotObj) {
    Statement preStat = null;
    String sqlQuery = "UPDATE User SET password="+forgotObj.getPassword()+" where userName="+forgotObj.getUserName()+" and email="+forgotObj.getEmail();
    try {
        preStat = connection.createStatement();
        preStat.executeUpdate(sqlQuery);
        System.out.println("your data is updated sucessfully....!!!!!");
    } catch (SQLException e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
    }
}}

