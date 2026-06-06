package com.github.rebi0395.instantsnap.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class ExitDialogController {

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

    private boolean result = false;

    @FXML
    public void initialize() {
        okButton.setOnAction(e -> {
            result = true;
            close();
        });

        cancelButton.setOnAction(e -> {
            result = false;
            close();
        });
    }

    public boolean getResult() {
        return result;
    }

    private void close() {
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }
}
