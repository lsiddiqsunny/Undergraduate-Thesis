public class Dummy{
private ResultSet fetchTable(String tableName, String stockIndex) throws SQLException {
    Statement statement = null;
    statement = connection.createStatement();
    ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName + " WHERE stock = \"" + stockIndex + "\";");
    return resultSet;
}}

