public class Dummy{
public void modificaCadruDidactic(CadruDidactic cd) throws SQLException {
    Connection conn = getConnection();
    Statement p;
    p = conn.createStatement();
    p.executeUpdate("UPDATE Cadre_Didactice  SET " +"pozitia = "+cd.getPozitia()+", den_post = "+cd.getDenumirePost() +", nume = "+cd.getNume() +", functia= "+cd.getFunctia()+ " , tit_vac = "+cd.getTitVac() +"  WHERE Id_Cadru_Didactic= "+cd.getId());
}}

