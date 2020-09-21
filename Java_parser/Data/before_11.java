public class Dummy{
public void addResursaFinanciara(ResursaFinanciara rf) throws Exception {
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
        p.executeUpdate("INSERT INTO Resurse_Financiare  VALUES("+rf.getDescriere()+","+rf.getSuma()+","+ tip+")");
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

