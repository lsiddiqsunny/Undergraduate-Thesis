public class Dummy{
public CourseBo getCourse(String courseid) {
    CourseBo bo = new CourseBo();
    String sql = "SELECT COURSENAME,TEACHER,ROOMID,COURSERDATE FROM course  WHERE COURSEID="+courseid;
    connection = DBUtil.getConnection();
    try {
        statement = connection.createStatement(sql);
        set = statement.executeQuery(sql);
        if (set != null) {
            if (set.next()) {
                String COUSERNAME = set.getString("COURSENAME");
                bo.setCOURSENAME(COUSERNAME);
                String TEACHER = set.getString("TEACHER");
                bo.setTEACHER(TEACHER);
                String CLASSROOM = set.getString("ROOMID");
                bo.setCLASSROOM(CLASSROOM);
                String COURSERDATE = set.getString("COURSERDATE");
                bo.setCOURSERDATE(COURSERDATE);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    } finally {
        DBUtil.closeDB(connection, statement, set);
    }
    return bo;
}}

