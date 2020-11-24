package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.text.DecimalFormat;

import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.statemanager.PlayerSettings;


public class settingsController implements Controller {
    private PlayerSettings settings;

    @FXML
    private Slider StatusSlider, MapSlider;
    @FXML
    private Label lblStatusFrequency, lblMapFrequency;
    @FXML
    private Button btnMoveUp, btnMoveDown, btnMoveLeft, btnMoveRight;
    @FXML
    private Button btnShootUp, btnShootDown, btnShootLeft, btnShootRight;


    public void initialize() {
        settings = PlayerSettings.getInstance();

        System.out.println("Game Settings Controller done");
    }

    @Override
    public void updateContent() {
        // Update sliders values
        MapSlider.setValue(settings.getMapFreq().doubleValue());
        StatusSlider.setValue(settings.getStatusFreq().doubleValue());
        setSliderLabel(lblMapFrequency, settings.getMapFreq());
        setSliderLabel(lblStatusFrequency, settings.getStatusFreq());

        // Movement bindings
        btnMoveDown.setText(settings.getMoveDown().toString());
        btnMoveUp.setText(settings.getMoveUp().toString());
        btnMoveLeft.setText(settings.getMoveLeft().toString());
        btnMoveRight.setText(settings.getMoveRight().toString());

        // Shooting bindings
        btnShootDown.setText(settings.getShootDown().toString());
        btnShootUp.setText(settings.getShootUp().toString());
        btnShootLeft.setText(settings.getShootLeft().toString());
        btnShootRight.setText(settings.getShootRight().toString());

        // Event handler status slider change
        StatusSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                setSliderLabel(lblStatusFrequency, newValue);

                // Update settings
                settings.setStatusFreq(newValue.intValue());
            }
        });

        // Event handler map slider change
        MapSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                setSliderLabel(lblMapFrequency, newValue);

                // Update settings
                settings.setMapFreq(newValue.intValue());
            }
        });
    }

    @FXML
    private void btnGoBackPressed(ActionEvent event) { Renderer.getInstance().show("mainMenu"); }
    @FXML
    private void btnMoveUpPressed(ActionEvent event) { setKeyButton(btnMoveUp, btnMoveUp.getText()); }
    @FXML
    private void btnMoveDownPressed(ActionEvent event) { setKeyButton(btnMoveDown, btnMoveDown.getText()); }
    @FXML
    private void btnMoveLeftPressed(ActionEvent event) { setKeyButton(btnMoveLeft, btnMoveLeft.getText()); }
    @FXML
    private void btnMoveRightPressed(ActionEvent event) { setKeyButton(btnMoveRight, btnMoveRight.getText()); }
    @FXML
    private void btnShootUpPressed(ActionEvent event) { setKeyButton(btnShootUp, btnShootUp.getText()); }
    @FXML
    private void btnShootDownPressed(ActionEvent event) { setKeyButton(btnShootDown, btnShootDown.getText()); }
    @FXML
    private void btnShootLeftPressed(ActionEvent event) { setKeyButton(btnShootLeft, btnShootLeft.getText()); }
    @FXML
    private void btnShootRightPressed(ActionEvent event) { setKeyButton(btnShootRight, btnShootRight.getText()); }

    private void setKeyButton(Button btn, String oldValue) {
        btn.setText("...");
        btn.setOnKeyReleased(keyEvent -> {
            String newValue = keyEvent.getCode().toString();

            // Check if a key is already set
            if (!newValue.equals(oldValue)) {
                if (!settings.isAlreadySet(newValue.charAt(0))) {
                    btn.setText(newValue);
                    settings.setMoveUp(newValue.charAt(0));
                } else {
                    btn.setText(oldValue);
                    Alert message = new Alert(Alert.AlertType.ERROR);
                    message.setTitle("Already used");
                    message.setContentText("This key is already bound.");
                    message.showAndWait();
                    return;
                }
            }

            btn.setText(oldValue);
        });
    }

    private void setSliderLabel(Label lbl, Number newValue) {
        if (newValue.intValue() < 100) {
            lbl.setText(newValue.intValue() + " ms");
        } else {
            Double SecondsValue = newValue.doubleValue() / 1000;
            lbl.setText(new DecimalFormat("##.#").format(SecondsValue) + " sec");
        }
    }
}
