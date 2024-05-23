package com.empresa.hito_programacion_daniel_jimenez;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.bson.Document;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private TableView<Registro> tableView;

    @FXML
    private TableColumn<Registro, String> col1;

    @FXML
    private TableColumn<Registro, String> col2;

    @FXML
    private TableColumn<Registro, String> col3;

    @FXML
    private TableColumn<Registro, Boolean> col4;

    @FXML
    private TextField searchField;

    private final MongoDBHandler mongoDBHandler = new MongoDBHandler();
    private final Pattern emailPattern = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private List<Registro> allRegistros; // Variable para almacenar todos los registros

    @FXML
    protected void onHelloButtonClick() {
        System.out.println("Cargando registros...");
        tableView.getItems().clear();
        List<Document> registros = mongoDBHandler.getRegistros();
        allRegistros = registros.stream().map(doc -> {
            String nombre = doc.getString("nombre");
            String correo = doc.getString("correo");
            String factura = doc.getString("factura");
            Boolean pagado = doc.getBoolean("pagado");

            // Manejar valores nulos
            if (nombre == null) nombre = "";
            if (correo == null) correo = "";
            if (factura == null) factura = "";
            if (pagado == null) pagado = false;

            return new Registro(nombre, correo, factura, pagado);
        }).collect(Collectors.toList());

        tableView.getItems().addAll(allRegistros);
    }

    @FXML
    protected void onSearchFieldKeyReleased() {
        String filter = searchField.getText().toLowerCase();
        tableView.getItems().clear();
        List<Registro> filteredRegistros = allRegistros.stream()
                .filter(registro -> registro.getNombre().toLowerCase().contains(filter))
                .collect(Collectors.toList());
        tableView.getItems().addAll(filteredRegistros);
    }

    @FXML
    protected void onAddButtonClick() {
        Dialog<Registro> dialog = new Dialog<>();
        dialog.setTitle("Añadir Nuevo Registro");
        dialog.setHeaderText("Ingrese la información del nuevo registro");

        ButtonType addButtonType = new ButtonType("Añadir", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        VBox vbox = new VBox();
        vbox.setSpacing(10);

        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre");
        TextField correoField = new TextField();
        correoField.setPromptText("Correo");
        TextField facturaField = new TextField();
        facturaField.setPromptText("Factura");
        CheckBox pagadoCheckBox = new CheckBox("Pagado");

        vbox.getChildren().addAll(new Label("Nombre:"), nombreField, new Label("Correo:"), correoField, new Label("Factura:"), facturaField, pagadoCheckBox);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String correo = correoField.getText();
                if (!emailPattern.matcher(correo).matches()) {
                    showAlert("Correo inválido", "El correo debe contener un '@' y un '.'.");
                    return null;
                }
                return new Registro(nombreField.getText(), correo, facturaField.getText(), pagadoCheckBox.isSelected());
            }
            return null;
        });

        Optional<Registro> result = dialog.showAndWait();
        result.ifPresent(registro -> {
            mongoDBHandler.addRegistro(registro);
            // Refrescar la tabla después de añadir el registro
            onHelloButtonClick();
        });
    }

    @FXML
    public void initialize() {
        col1.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        col2.setCellValueFactory(new PropertyValueFactory<>("correo"));
        col3.setCellValueFactory(new PropertyValueFactory<>("factura"));
        col4.setCellValueFactory(new PropertyValueFactory<>("pagado"));

        // Establecer la fábrica de celdas personalizada para la columna "Pagado"
        col4.setCellFactory(column -> new TableCell<Registro, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item ? "Sí" : "No");
                }
            }
        });

        // Establecer la fábrica de filas para colorear las filas según el estado de pago
        tableView.setRowFactory(new Callback<TableView<Registro>, TableRow<Registro>>() {
            @Override
            public TableRow<Registro> call(TableView<Registro> tableView) {
                return new TableRow<Registro>() {
                    @Override
                    protected void updateItem(Registro registro, boolean empty) {
                        super.updateItem(registro, empty);
                        if (registro == null || empty) {
                            setStyle("");
                        } else {
                            if (registro.isPagado()) {
                                setStyle("-fx-background-color: lightgreen;");
                            } else {
                                setStyle("-fx-background-color: lightcoral;");
                            }
                        }
                    }
                };
            }
        });

        tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                Registro selectedRegistro = tableView.getSelectionModel().getSelectedItem();
                if (selectedRegistro != null) {
                    ContextMenu contextMenu = new ContextMenu();

                    MenuItem deleteItem = new MenuItem("Eliminar");
                    deleteItem.setOnAction(e -> eliminarRegistro(selectedRegistro));

                    MenuItem editItem = new MenuItem("Modificar");
                    editItem.setOnAction(e -> modificarRegistro(selectedRegistro));

                    contextMenu.getItems().addAll(deleteItem, editItem);
                    contextMenu.show(tableView, event.getScreenX(), event.getScreenY());
                }
            }
        });
    }

    private void eliminarRegistro(Registro registro) {
        mongoDBHandler.deleteRegistro(registro);
        // Refrescar la tabla después de eliminar el registro
        onHelloButtonClick();
    }

    private void modificarRegistro(Registro registro) {
        Dialog<Registro> dialog = new Dialog<>();
        dialog.setTitle("Modificar Registro");
        dialog.setHeaderText("Modifique la información del registro");

        ButtonType modifyButtonType = new ButtonType("Modificar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(modifyButtonType, ButtonType.CANCEL);

        VBox vbox = new VBox();
        vbox.setSpacing(10);

        TextField nombreField = new TextField(registro.getNombre());
        TextField correoField = new TextField(registro.getCorreo());
        TextField facturaField = new TextField(registro.getFactura());
        CheckBox pagadoCheckBox = new CheckBox("Pagado");
        pagadoCheckBox.setSelected(registro.isPagado());

        vbox.getChildren().addAll(new Label("Nombre:"), nombreField, new Label("Correo:"), correoField, new Label("Factura:"), facturaField, pagadoCheckBox);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == modifyButtonType) {
                String correo = correoField.getText();
                if (!emailPattern.matcher(correo).matches()) {
                    showAlert("Correo inválido", "El correo debe contener un '@' y un '.'.");
                    return null;
                }
                return new Registro(nombreField.getText(), correo, facturaField.getText(), pagadoCheckBox.isSelected());
            }
            return null;
        });

        Optional<Registro> result = dialog.showAndWait();
        result.ifPresent(newRegistro -> {
            mongoDBHandler.updateRegistro(registro, newRegistro);
            // Refrescar la tabla después de modificar el registro
            onHelloButtonClick();
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Registro {
        private final String nombre;
        private final String correo;
        private final String factura;
        private final boolean pagado;

        public Registro(String nombre, String correo, String factura, boolean pagado) {
            this.nombre = nombre;
            this.correo = correo;
            this.factura = factura;
            this.pagado = pagado;
        }

        public String getNombre() {
            return nombre;
        }

        public String getCorreo() {
            return correo;
        }

        public String getFactura() {
            return factura;
        }

        public boolean isPagado() {
            return pagado;
        }
    }
}
