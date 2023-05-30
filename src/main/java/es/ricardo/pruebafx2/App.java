package es.ricardo.pruebafx2;

import es.ricardo.sgbd.SGBD;
import static es.ricardo.sgbd.SGBD.LOG;
import java.util.logging.Level;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.ResultSet;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import static javafx.application.Application.launch;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    static SGBD bd;
    static ResourceBundle textos;
    public static String idioma = "Castellano";

    private void mostrarAlerta(String titulo, String texto) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setTitle(titulo);
        alert.setContentText(texto);
        alert.showAndWait();
    }

    @Override
    public void start(Stage stage) throws IOException {
        textos = java.util.ResourceBundle.getBundle("es.ricardo.pruebafx2/traducciones_es_ES");
        String tabla = "usuario";
        bd = new SGBD("root", "root", "jdbc:mysql://192.168.1.29/java");
        bd.redirigeSalidaError("logBdFX");
        if (bd.conectar()) {
            mostrarAlerta("ExitoDB", textos.getString("CONEXION_ESTABLECIDA"));
        } else {
            mostrarAlerta("ErrorBD", textos.getString("ERROR_ERROR_CONEXION"));
            Platform.exit();
            System.exit(0);
        }
        if (bd.crearTabla(tabla, new String[]{
            "dni VARCHAR(9) PRIMARY KEY",
            "nombre VARCHAR(15) NOT NULL",
            "apellido VARCHAR(30) NOT NULL",
            "email VARCHAR(30) NOT NULL",
            "password VARCHAR(15) NOT NULL",
            "notas VARCHAR(500)"
        })) {
            LOG.log(Level.SEVERE, textos.getString("TABLA_CREADA"));
        } else {
            LOG.log(Level.SEVERE, textos.getString("ERROR_TABLA"));
        }

        scene = new Scene(loadFXML("principal"), 640, 480);
        stage.setScene(scene);
        stage.setTitle("Probando JavaFX y MariaDB");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("mysql.png")));
        stage.show();
        stage.setResizable(false);
    }

    static void setRoot(String fxml) throws IOException {

        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {

        launch();
        if (bd.desconectar()) {
            LOG.log(Level.SEVERE, textos.getString("DESCONECTADO_CORRECTAMENTE"));
        } else {
            LOG.log(Level.SEVERE, textos.getString("ERROR_DESCONEXION"));
        }
    }

}
