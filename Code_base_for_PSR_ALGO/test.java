public class Dummy {

  public boolean toCheckEmailUser(String quota,String TBL_NAME ,String FIELD_QUOTA) {
    try {
      open();
      String query = "insert into " + TBL_NAME + " (" + FIELD_QUOTA + ") values("+quota+")";
      Connection conn = this.connect()
      Statement st = conn.createStatement();
      st.executeUpdate(query);
      Modifica modifica = new Modifica();
      ModificaService modificaService = new ModificaService();
      modifica.setTabella(TBL_NAME);
      modifica.setTipo("Inserimento piano");
      modificaService.insert(modifica);
  } catch (SQLException e) {
      System.out.println(e.getMessage());
  } finally {
      close();
  }
   
  }

}
