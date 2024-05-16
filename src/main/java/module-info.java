module org.example.antonio_talamantes_assignement4 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.healthmarketscience.jackcess;
    requires com.google.gson;


    opens org.example.antonio_talamantes_assignement4 to javafx.fxml, com.google.gson;
    exports org.example.antonio_talamantes_assignement4;
}