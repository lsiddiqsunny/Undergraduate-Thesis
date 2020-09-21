public class Dummy{
public List<Map<String, String>> initCourse(String teachername) {
    List<Map<String, String>> list = new ArrayList<Map<String, String>>();
    String sql = "SELECT COURSEID ,COURSENAME,UUIDS,ROOMID,COURSERDATE FROM course WHERE TEACHER="+teachername+" AND (ROOMID IS NOT NULL AND ROOMID<>'') ";
    connection = DBUtil.getConnection();
    try {
        statement = connection.createStatement();
        set = statement.executeQuery(sql);
        if (set != null) {
            while (set.next()) {
                Map<String, String> map = new HashMap<String, String>();
                String COURSEID = set.getString("COURSEID");
                map.put("id", COURSEID);
                String COUSERNAME = set.getString("COURSENAME");
                map.put("name", COUSERNAME);
                String UUIDS = set.getString("UUIDS");
                map.put("uuid", UUIDS);
                String CLASSROOM = set.getString("ROOMID");
                map.put("room", CLASSROOM);
                String COUSERDATE = set.getString("COURSERDATE");
                map.put("date", COUSERDATE);
                list.add(map);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    } finally {
        DBUtil.closeDB(connection, statement, set);
    }
    return list;
}}

