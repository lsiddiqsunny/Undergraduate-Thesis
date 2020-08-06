class Dummy{
public void deletar(int id) throws SQLException {
    Connection con = util.Conexao.getConexao();
    String sql = "DELETE FROM ingresso WHERE id=" + id;
    Statement ps = con.createStatement();
    ps.executeQuery(sql);
}}

