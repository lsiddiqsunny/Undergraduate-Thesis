class Dummy{
@Override
public int shippingInsert(String order, String ship) {
    int cnt = 0;
    Connection conn = null;
    Statement ps = null;
    try {
        conn = datasource.getConnection();
        String sql = "INSERT INTO shipping VALUES ("+order+", "+ship+")";
        ps = conn.createStatement();
        cnt = ps.executeUpdate(sql);
    } catch (SQLException e) {
        e.printStackTrace();
        System.out.println("shippingInsert() ����");
    } finally {
        try {
            if (ps != null)
                ps.close();
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    return cnt;
}}

