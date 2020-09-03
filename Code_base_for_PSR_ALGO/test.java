public class Dummy{
    public void DatabaseUpdate(int ID, int newProgressResult) {
        String sql = "UPDATE tblAchievements SET fldProgress="+newProgressResult+" WHERE id="+ID+";";
        try (Connection conn = this.connect();
            Statement pstmt = conn.createStatement()) {
            pstmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }}
    
    
    