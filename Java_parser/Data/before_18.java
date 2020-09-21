public class Dummy{
public void smodificaResursaFinanciara(ResursaFinanciara rf) throws Exception {
    Connection conn = getConnection();
    Statement p;
    try {
        p = conn.createStatement();
        int tip = 0;
        if (rf.getTip() != null) {
            switch(rf.getTip()) {
                case CHELTUIELA_CU_MOBILITATE:
                    tip = 1;
                    break;
                case CHELTUIALA_CU_MANOPERA:
                    tip = 2;
                    break;
                case CHELTUIALA_DE_LOGISTICA:
                    tip = 3;
                    break;
            }
        }
       
        p.executeUpdate("UPDATE Resurse_Financiare  SET descriere="+rf.getDescriere()+",suma="+rf.getSuma()+", tip="+tip+" where id="+rf.getId());
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

