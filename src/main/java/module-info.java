module com.empresa.hito_programacion_daniel_jimenez {
    requires javafx.controls;
    requires javafx.fxml;
    requires mongo.java.driver;


    opens com.empresa.hito_programacion_daniel_jimenez to javafx.fxml;
    exports com.empresa.hito_programacion_daniel_jimenez;
}