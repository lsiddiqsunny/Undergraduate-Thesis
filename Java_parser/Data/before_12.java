public class Dummy{
public void addUserInfo(User userInfo) {
    Statement preStat = null;
    String sqlQuery = "Insert into User(id,firstName,lastName,userName,email,password,mobileNo) values("+userInfo.getId()+","+userInfo.getFirstName()+","+userInfo.getLastName()+","+userInfo.getUserName()+","+userInfo.getEmail()+","+userInfo.getPassword()+","+userInfo.getMobileNo()+")";
    try {
        preStat = connection.createStatement();
        preStat.executeUpdate(sqlQuery);
        System.out.println("your data is sucessfully added....!!!!!");
    } catch (SQLException e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
    }
}}

