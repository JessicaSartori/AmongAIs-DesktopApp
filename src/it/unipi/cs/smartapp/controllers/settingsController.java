package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;

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
    @FXML
    private Button btnToggleChat, btnToggleList;


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
        StatusSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            setSliderLabel(lblStatusFrequency, newValue);

            // Update settings
            settings.setStatusFreq(newValue.intValue());
        });

        // Event handler map slider change
        MapSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            setSliderLabel(lblMapFrequency, newValue);

            // Update settings
            settings.setMapFreq(newValue.intValue());
        });
    }

    @FXML
    private void btnGoBackPressed() { Renderer.getInstance().show("mainMenu"); }
    @FXML
    private void btnMoveUpPressed() { setKeyButton(btnMoveUp, settings.getMoveUp()); }
    @FXML
    private void btnMoveDownPressed() { setKeyButton(btnMoveDown, settings.getMoveDown()); }
    @FXML
    private void btnMoveLeftPressed() { setKeyButton(btnMoveLeft, settings.getMoveLeft()); }
    @FXML
    private void btnMoveRightPressed() { setKeyButton(btnMoveRight, settings.getMoveRight()); }
    @FXML
    private void btnShootUpPressed() { setKeyButton(btnShootUp, settings.getShootUp()); }
    @FXML
    private void btnShootDownPressed() { setKeyButton(btnShootDown, settings.getShootDown()); }
    @FXML
    private void btnShootLeftPressed() { setKeyButton(btnShootLeft, settings.getShootLeft()); }
    @FXML
    private void btnShootRightPressed() { setKeyButton(btnShootRight, settings.getShootRight()); }
    @FXML
    private void btnToggleChatPressed() { setKeyButton(btnToggleChat, settings.getShootRight()); }
    @FXML
    private void btnToggleListPressed() { setKeyButton(btnToggleList, settings.getShootRight()); }

    private void setKeyButton(Button btn, KeyCode oldValue) {
        btn.setText("...");
        btn.setOnKeyReleased(keyEvent -> {
            KeyCode newValue = keyEvent.getCode();

            // Check if a key is already set
            if (!settings.isAlreadySet(newValue)) {
                btn.setText(newValue.toString());

                switch (btn.getId()) {
                    case "btnMoveUp": settings.setMoveUp(newValue); break;
                    case "btnMoveDown": settings.setMoveDown(newValue); break;
                    case "btnMoveLeft": settings.setMoveLeft(newValue); break;
                    case "btnMoveRight": settings.setMoveRight(newValue); break;
                    case "btnShootUp": settings.setShootUp(newValue); break;
                    case "btnShootDown": settings.setShootDown(newValue); break;
                    case "btnShootLeft": settings.setShootLeft(newValue); break;
                    case "btnShootRight": settings.setShootRight(newValue); break;
                    case "btnToggleChat": settings.setFlipRight(newValue); break;
                    case "btnToggleList": settings.setFlipLeft(newValue); break;
                }
            } else {
                btn.setText(oldValue.toString());
                Alert message = new Alert(Alert.AlertType.ERROR);
                message.setTitle("Already used");
                message.setContentText("This key is already bound.");
                message.showAndWait();
            }
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