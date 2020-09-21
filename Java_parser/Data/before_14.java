public class Dummy{
public void stergeResursaFinancara(int id) throws Exception {
    Connection conn = getConnection();
    Statement p;
    try {
        p = conn.createStatement();
        p.executeUpdate("delete from Resurse_Financiare where id="+id);
    } catch (SQLException e) {
        throw new Exception(e.getMessage());
    } finally {
        try {
            conn.close();
        } catch (SQLException ex) {
            throw new Exception(ex.getMessage());
        }
    }
}}

