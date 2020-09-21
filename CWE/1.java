public BankAccount getUserBankAccount(String username, String accountNumber) {
BankAccount userAccount = null;
String query = null;
try {
if (isAuthorizedUser(username)) {
query = "SELECT * FROM accounts WHERE owner = "
+ username + " AND accountID = " + accountNumber;
DatabaseManager dbManager = new DatabaseManager();
Connection conn = dbManager.getConnection();
Statement stmt = conn.createStatement();
ResultSet queryResult = stmt.executeQuery(query);
userAccount = (BankAccount)queryResult.getObject(accountNumber);
}
} catch (SQLException ex) {
String logMessage = "Unable to retrieve account information from database,\nquery: " + query;
Logger.getLogger(BankManager.class.getName()).log(Level.SEVERE, logMessage, ex);
}
return userAccount;
}