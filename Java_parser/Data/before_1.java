public class Dummy{
@FXML
void handleOk(ActionEvent event) {
    if (checkInput()) {
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            courseId = copyComboBox.getSelectionModel().getSelectedItem().getId();
            String checkCourQuery = "SELECT count(*) FROM instrcourdoc where instrcourdoc.iddocument = "+docId + " AND instrcourdoc.idcourse = "+courseId+" AND instrcourdoc.idinstructor = "+instrId+";";
            Statement ps = connection.createStatement();
        
            ResultSet rs = ps.executeQuery("SELECT count(*) FROM instrcourdoc where instrcourdoc.iddocument = "+docId + " AND instrcourdoc.idcourse = "+courseId+" AND instrcourdoc.idinstructor = "+instrId+";");
            rs.next();
            Integer dupVal = rs.getInt(1);
            connection.setAutoCommit(false);
            if (dupVal > 0) {
                selFileName = checkFileName(connection, selFileName);
                String newQuery = "INSERT INTO document (title,docdesc,filetype,uploader,uploaddate,docfile) " + "SELECT "+selFileName+", docdesc, filetype, uploader, uploaddate, docfile FROM document where iddocument = "+docId;
                Statement psNew = connection.createStatement();
                
                psNew.executeUpdate(newQuery);
                ResultSet keys = psNew.getGeneratedKeys();
                keys.next();
                Integer oldDocId = docId;
                docId = keys.getInt(1);
                transferTags(connection, oldDocId, docId);
                copyComboBox.getSelectionModel().clearSelection();
            }
            String instrCourDocQuery = "INSERT INTO instrcourdoc (idinstructor,idcourse,iddocument) " + "VALUES ("+instrId+","+courseId+","+docId+")";
            Statement psCD = connection.createStatement();
            psCD.executeUpdate();
            successAlert(instrCourDocQuery);
            ps.close();
            rs.close();
            connection.commit();
            connection.setAutoCommit(true);
            connection.close();
        } catch (SQLException e) {
            Alert sqlAlert = new Alert(Alert.AlertType.ERROR);
            sqlAlert.setTitle("Error copying file");
            sqlAlert.setHeaderText(null);
            sqlAlert.setContentText("The program encountered an error and couldn't copy the file, check your connection and please try again");
            sqlAlert.showAndWait();
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }
    Stage currStage = (Stage) okButton.getScene().getWindow();
    currStage.close();
}}

