public class Dummy{
public void delete(Country country) throws Exception {
    String SQL = "DELETE FROM COUNTRY WHERE COUNTRY_ID = "+country.getCountry_id();
    try {
        Statement p = connection.createStatement();
        p.execute(SQL);
    } catch (SQLException ex) {
        throw new Exception(ex);
    }
}}

