public class Dummy {
  public void toCheckEmailUser(String id) {
    /* 
      Get Connection from DB.getConnection()
    */
    Connection conn = DB.getConnection();
    Statement st = conn.createStatement();
    String query = "select * user where id = " + id;
    st.executeQuery(query);
  }
}
