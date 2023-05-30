package es.ricardo.pruebafx2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import es.ricardo.sgbd.SGBD;
import javafx.animation.FadeTransition;
import javafx.scene.control.ChoiceBox;
import javafx.util.Duration;
import static es.ricardo.pruebafx2.App.*;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class Controlador {

    @FXML
    private Button botonActualizar;

    @FXML
    private Button botonBorrar;

    @FXML
    private Button botonEliminar;

    @FXML
    private Button botonInsertar;

    @FXML
    private ChoiceBox<String> boxIdioma;

    @FXML
    private Label etiquetaTitulo;

    @FXML
    private ImageView imagenMysql;

    @FXML
    private Label infoLabel;

    @FXML
    private Label labelApellidos;

    @FXML
    private Label labelDni;

    @FXML
    private Label labelMail;

    @FXML
    private Label labelNombre;

    @FXML
    private Label labelNotas;

    @FXML
    private Label labelPassword;

    @FXML
    private Label labelVbox;

    @FXML
    private PasswordField passField;

    @FXML
    private TextField textApellidos;

    @FXML
    private TextField textDni;

    @FXML
    private TextField textMail;

    @FXML
    private TextField textNombre;

    @FXML
    private TextArea textNotas;

    static private final FadeTransition fadeOut = new FadeTransition(Duration.seconds(5));
    static String nombreTabla;

    @FXML
    private void initialize() {
        nombreTabla = "usuario";
        textDni.focusedProperty().addListener(new DNIChangeListener());
        boxIdioma.getItems().addAll("Castellano", "Inglés");
        boxIdioma.getSelectionModel().select(App.idioma);
        boxIdioma.valueProperty().addListener((o, p1, p2) -> {
            App.idioma = p2;
            App.textos = ResourceBundle.getBundle("es.ricardo.pruebafx2/traducciones_" + (p2.equals("Castellano") ? "es_ES" : "en_UK"));
            try {
                App.setRoot("principal");
                mensaje(false, "Nueva Selección: " + p2);
            } catch (IOException ex) {
                Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        fadeOut.setNode(infoLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
    }

    void mensaje(boolean error, String mensaje) {

        if (error) {
            infoLabel.setTextFill(Color.RED);
        } else {
            infoLabel.setTextFill(Color.GREEN);
        }
        infoLabel.setText(mensaje);
        fadeOut.playFromStart();
    }

    private void reiniciaBotonesBD() {
        botonActualizar.setDisable(true);
        botonInsertar.setDisable(true);
        botonEliminar.setDisable(true);
        textDni.setEditable(true);
    }

    @FXML
    void borrarDatos(ActionEvent event) {
        vaciarCampos();
        reiniciaBotonesBD();
        infoLabel.setText("");
    }

    private void vaciarCampos() {
        textDni.setText("");
        textNombre.setText("");
        textApellidos.setText("");
        textMail.setText("");
        passField.setText("");
        textNotas.clear();
    }

    @FXML
    void insertar(ActionEvent event) {

        boolean exito = App.bd.insertarRegistros("usuario", new String[][]{
            {"dni", "'" + textDni.getText() + "'"},
            {"nombre", "'" + textNombre.getText() + "'"},
            {"apellido", "'" + textApellidos.getText() + "'"},
            {"email", "'" + textMail.getText() + "'"},
            {"password", "'" + passField.getText() + "'"},
            {"notas", "'" + textNotas.getText() + "'"},});
        infoLabel.setText(
                exito ? "Registro " + textDni.getText() + " insertado"
                        : "Error en la insercción del registro " + textDni.getText());
        vaciarCampos();
        reiniciaBotonesBD();
        textDni.setDisable(false);
    }

    @FXML
    void actualizar(ActionEvent event) {
        boolean exito = App.bd.actualizarRegistros("usuario", new String[][]{
            {"nombre", "'" + textNombre.getText() + "'"},
            {"apellido", "'" + textApellidos.getText() + "'"},
            {"email", "'" + textMail.getText() + "'"},
            {"password", "'" + passField.getText() + "'"},
            {"notas", "'" + textNotas.getText() + "'"}
        }, "dni  = '" + textDni.getText() + "'");
        infoLabel.setText(
                exito ? "Registro " + textDni.getText() + " actulizado"
                        : "Error en la actualizacicón del registro " + textDni.getText());
        vaciarCampos();
        reiniciaBotonesBD();
        textDni.setDisable(false);
    }

    @FXML
    void eliminar(ActionEvent event) {
        boolean exito = App.bd.eliminarRegistros("usuario", "dni  = '" + textDni.getText() + "'");
        infoLabel.setText(
                exito ? "Registro " + textDni.getText() + " borrado"
                        : "Error en la eliminación del registro " + textDni.getText());
        vaciarCampos();
        reiniciaBotonesBD();
        
    }

    private class DNIChangeListener implements ChangeListener<Boolean> {

        @Override
        public void changed(ObservableValue<? extends Boolean> ov,
                Boolean oldPV, Boolean newPV) {
            if (!newPV) {
                reiniciaBotonesBD();
                if (textDni.getText().isBlank()) {
                    infoLabel.setText("DNI en blanco, para continuar inserte DNI");
                    textDni.requestFocus();
                } else {
                    if (recupera()) {
                        botonActualizar.setDisable(false);
                        botonEliminar.setDisable(false);
                        textDni.setEditable(false);
                    } else {
                        botonInsertar.setDisable(false);
                        textDni.setEditable(true);
                        //infoLabel.setText("Dni con datos");
                    }
                    infoLabel.setText("Dni con datos");
                }
            }
        }
    }

    private boolean recupera() {
        try {
            ResultSet rs = App.bd.recuperarTodo("usuario",
                    "dni = '" + textDni.getText() + "'");
            if (rs.next()) {
                textNombre.setText(rs.getString(2));
                textApellidos.setText(rs.getString(3));
                textMail.setText(rs.getString(4));
                passField.setText(rs.getString(5));
                textNotas.setText(rs.getString((6)));
                infoLabel.setText("Registro " + textDni.getText() + " recuperado");
                return true;
            } else {
                return false;
            }

        } catch (SQLException ex) {
            SGBD.LOG.log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
