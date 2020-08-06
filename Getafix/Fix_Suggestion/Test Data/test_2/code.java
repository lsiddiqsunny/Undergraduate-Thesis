class Dummy{
public Mappa findByID(Integer search_id) {
    ResultSet rs = null;
    Statement st;
    Mappa mappa = new Mappa();
    try {
        open();
        String query = "select * from " + TBL_NAME + " where " + FIELD_ID + "=" + search_id;
        st = conn.createStatement();
        rs = st.executeQuery(query);
        while (rs.next()) {
            mappa.setNome(rs.getString(FIELD_NOME));
            mappa.setImmagine(rs.getString(FIELD_IMG));
            mappa.setID_mappa(rs.getInt(FIELD_ID));
            mappa.setID_piano(rs.getInt(FIELD_ID_PIANO));
        }
    } catch (SQLException e) {
        System.out.println(e.getMessage());
    } finally {
        close();
    }
    return mappa;
}}

