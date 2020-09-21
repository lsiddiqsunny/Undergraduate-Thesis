public class Dummy{
private void saveAlteActivitati(AlteActivitati alteActivitati) throws SQLException {
    Connection conn = getConnection();
    Statement p = conn.createStatement();
    p.executeUpdate("Insert into Alte_Activitati_CD values("+alteActivitati.getId_Cadru_Didactic()+","+alteActivitati.getPregAdmitere()+","+alteActivitati.getComisiiAbsolvire()+","+alteActivitati.getConsultatii()+","+alteActivitati.getExamene()+","+alteActivitati.getIndrLucrDisert()+","+alteActivitati.getIndrLucrLic()+","+alteActivitati.getIndrProiect()+","+alteActivitati.getLucrControl()+","+alteActivitati.getSeminariiCerc()+")");
}}

