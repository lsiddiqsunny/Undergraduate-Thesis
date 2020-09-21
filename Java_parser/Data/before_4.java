public class Dummy{
private void saveState(State state) throws SQLException {
   
    Connection conn = getConnection();
    Statement p = conn.createStatement();
    p.executeUpdate("Insert into State_de_Functii values("+state.getIdCadruDidactic()+","+
    state.getIdDisciplina()+","+
    state.getIdSectia()+","+
    state.getAn()+","+
    state.getOreC1()+","+
    state.getOreS1()+","+
    state.getOreL1()+","+
    state.getOreC2()+","+
    state.getOreS2()+","+state.getOreL2()+")");

}}

