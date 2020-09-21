public class Dummy{
public int saveCadruDidactic(CadruDidactic cd) throws SQLException {
    Connection conn = getConnection();
    PreparedStatement p;
    p = conn.createStatement();
    p.executeUpdate("INSERT INTO Cadre_Didactice  VALUES("+cd.getPozitia()+","+cd.getDenumirePost()+","+cd.getNume()+","+cd.getFunctia()+","+cd.getTitVac()+")");
    p = conn.createStatement();
    ResultSet rs = p.executeQuery("Select Id_Cadru_Didactic from Cadre_Didactice where pozitia="+cd.getPozitia()+" and den_post="+cd.getDenumirePost()+" and nume="+cd.getN+" and functia="+cd.getFunctia()+" and tit_vac="+cd.getTitVac());
    rs.next();
    return rs.getInt(1);
}}

