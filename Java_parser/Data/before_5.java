public class Dummy{
public void stergeCadruDidactic(int id_Cadru_Didactic) throws SQLException {
    Connection conn = getConnection();
    Statement p;
    p = conn.createStatement();
    
    p.executeUpdate("Delete from Cadre_Didactice where id_Cadru_Didactic = "+id_Cadru_Didactic);
}}

