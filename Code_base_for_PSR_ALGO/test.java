import java.sql.Statement;

public class dummy{
    public void f(){
        Connection conn  = connect();
        Statement stmt  = conn.createStatement();
        stmt.execute("select * from table where id = "+a);
    }
}