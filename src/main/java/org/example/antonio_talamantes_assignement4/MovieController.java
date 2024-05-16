package org.example.antonio_talamantes_assignement4;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.sql.*;

public class MovieController {

    @FXML
    private TableView<Movie> moviesTableView;
    @FXML
    private TableColumn<Movie, Double> column_Sales;

    @FXML
    private TableColumn<Movie, String> column_Title;

    @FXML
    private TableColumn<Movie, Integer> column_Year;

    @FXML
    private MenuItem menu_Item_About(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Movie Database");
        alert.setHeaderText("Name and Integrity Statement");
        alert.setContentText("Antonio Talamantes\n" +
                "I certify that this submission is my original work.");
        alert.show();
        return null;
    }

    @FXML
    private MenuItem menu_Item_Close(ActionEvent event){
        Platform.exit();
        return null;
    }


    @FXML
    private MenuItem menu_Item_Export_JSON;

    @FXML
    private TextField textField_Sales;

    @FXML
    private TextField textField_Title;

    @FXML
    private TextField textField_Year;
    @FXML
    private TextField textField_Status;

    @FXML
    private void handleCreateTable(ActionEvent event) {
        String databaseURL = DB_Connection.databaseURL;
        try (
            Connection conn = DriverManager.getConnection(databaseURL);
            Statement stmt = conn.createStatement()){
                 // Use one connection for both drop and add table
            String dropSQL = "DROP TABLE movies";
            try {// Drop table
                stmt.execute(dropSQL); // Execute the SQL statement
                System.out.println("Movies table dropped");
                conn.commit(); // Most JDBC connections are in auto-commit mode by default so don't really need this.
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }//end of drop table if exists
            // Create table
            String createSQL = "CREATE TABLE movies (title nvarchar(255), year INTEGER, sales DOUBLE)";
            stmt.execute(createSQL);
            System.out.println("New movies table created");
            conn.commit();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        handleListRecords(new ActionEvent());
        textField_Status.setText("Table created");

    }

    @FXML
    protected void handleImportJSON(ActionEvent event){
        FileChooser fileChooser = fileChooserMethod();
        File file = fileChooser.showOpenDialog(moviesTableView.getScene().getWindow());

        loadTableFromJson(file);
        textField_Status.setText("Import data from " + file.getPath());

    }

    @FXML
    private void handleExportJSON(ActionEvent event){
        FileChooser fileChooser = fileChooserMethod();
        File file = fileChooser.showSaveDialog(moviesTableView.getScene().getWindow());
        saveTableToJson(file);
        textField_Status.setText("Table exported to JSON file");
    }

    @FXML
    protected void handleListRecords(ActionEvent event) {
        ObservableList<Movie> movies = readFromDB(".//Movies.accdb"); // Make sure to use <Movie>
        moviesTableView.getItems().clear();
        moviesTableView.getItems().addAll(movies);
        textField_Status.setText("Movie table displayed");

    }

    @FXML
    private void handleAddRecord(ActionEvent event) throws SQLException {
        String title = textField_Title.getText().trim();
        String year = textField_Year.getText().trim();
        String sales = textField_Sales.getText().trim();
        // Get text from textFields --> Use Validation class to validate

        // Debugging
        System.out.println("Title: " + title + ", Year: " + year + ", Sales: " + sales);

        String titleError = Validation.validateTitle(title);
        String yearError = Validation.validateYear(year);
        String salesError = Validation.validateSales(sales);

        // Debugging
        System.out.println("Title Error: " + titleError + ", Year Error: " + yearError + ", Sales Error: " + salesError);

        if (!titleError.isEmpty() || !yearError.isEmpty() || !salesError.isEmpty()) {
            // Build the Alert Error message
            StringBuilder errorMessage = new StringBuilder();
            if (!titleError.isEmpty()) {
                errorMessage.append(titleError).append("\n");
            }
            if (!yearError.isEmpty()) {
                errorMessage.append(yearError).append("\n");
            }
            if (!salesError.isEmpty()) {
                errorMessage.append(salesError);
            }

            // Show alert with the collected error messages
            Alert alert = new Alert(Alert.AlertType.WARNING);//The type of alert is determined by the AlertType WARNING/ERROR/INFORMATION/CONFIRMATION
            alert.setTitle("Warning");
            alert.setHeaderText("Invalid input");
            alert.setContentText(errorMessage.toString());
            alert.show();
        } else {
            // Add the movie to the database since there are no validation errors
            addMovieToDB(title, Integer.parseInt(year), Double.parseDouble(sales));
            textField_Title.clear();
            textField_Year.clear();
            textField_Sales.clear();
            handleListRecords(new ActionEvent());
            textField_Status.setText("A movie has been inserted: " + title);
        }
    }

    @FXML
    private void handleDeleteRecord(ActionEvent event) {
        Movie movie = moviesTableView.getSelectionModel().getSelectedItem();
        try {
            moviesTableView.getItems().remove(movie);
            deleteMovieFromDB(movie);
            handleListRecords(new ActionEvent());
            textField_Status.setText("A movie has been deleted: " + movie.getTitle());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void initialize() {
        // Bind the table columns
        column_Title.setCellValueFactory(new PropertyValueFactory<Movie, String>("title"));
        column_Year.setCellValueFactory(new PropertyValueFactory<Movie, Integer>("year"));
        column_Sales.setCellValueFactory(new PropertyValueFactory<Movie, Double>("sales"));

        System.out.println("Initialize called");
        // Create DB file if it doesn't exist

        File dbFile = new File(".//MoviesDB.accdb");
        if (!dbFile.exists()) {
            try (Database db =
                         DatabaseBuilder.create(Database.FileFormat.V2010, dbFile)) {
                System.out.println("The database file has been created.");
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
            }
        } // end of if dbFile.exists
    }     // end initialize


    private FileChooser fileChooserMethod() {
        FileChooser fileChooser = new FileChooser(); // Create a FileChooser object
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir"))); // Set initial directory
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json")); // Set JSON filter
        return fileChooser;
    }

    private void loadTableFromJson(File file) {

        Gson gson = new Gson(); // Create a Gson object

        try (FileReader fr = new FileReader(file)) { // Create FileReader object
            Movie[] moviesArray = gson.fromJson(fr, Movie[].class); // JSON file into array

            try( Connection conn = DB_Connection.getConnection();
                Statement clearTableStmt = conn.createStatement()) {

                String clearTableSQL = "DELETE FROM movies";

                String insertSQL = "INSERT INTO movies (title, year, sales) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {
                    for (Movie movie : moviesArray) {
                        insertStmt.setString(1, movie.getTitle());
                        insertStmt.setInt(2, movie.getYear());
                        insertStmt.setDouble(3, movie.getSales());
                        insertStmt.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            handleListRecords(new ActionEvent());
        } catch (IOException e) {
        }
    }

    private void saveTableToJson(File file) {
        ObservableList<Movie> moviesList = moviesTableView.getItems(); // Get TableView items
        Gson gson = new GsonBuilder().setPrettyPrinting().create(); // Create a Gson object
        String jsonString = gson.toJson(moviesList); // Convert ObservableList to JSON string

        try (FileWriter fw = new FileWriter(file)) { // Create FileWriter object
            fw.write(jsonString); // Write JSON string to file
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private ObservableList<Movie> readFromDB(String dbFilePath) {
        ObservableList<Movie> movies = FXCollections.observableArrayList();
        String sql = "Select * from movies";


        try {
            Connection conn = DB_Connection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(sql);

            while (result.next()) {
                String title = result.getString("title");
                int year = result.getInt("year");
                double sales = result.getDouble("sales");
                movies.add(new Movie(title, year, sales)); // !!! Remember need to use .add to insert into ObservableList !!!
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return movies;
    }

    public static void addMovieToDB(String dbTitle, int dbYear, double dbSales) throws SQLException {
        String sql = "Insert into movies(title, year, sales) Values (?, ?, ?)";

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

                preparedStatement.setString(1, dbTitle);
                preparedStatement.setInt(2, dbYear);
                preparedStatement.setDouble(3, dbSales);
                preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteMovieFromDB(Movie movie) {
        String sql = "DELETE FROM movies WHERE title = ? AND year = ? AND sales = ?";

        try (Connection conn = DB_Connection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

                preparedStatement.setString(1, movie.getTitle());
                preparedStatement.setInt(2, movie.getYear());
                preparedStatement.setDouble(3, movie.getSales());
                preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        readFromDB(".//Movies.accdb").forEach(moviesTableView.getItems()::add);
    }

}