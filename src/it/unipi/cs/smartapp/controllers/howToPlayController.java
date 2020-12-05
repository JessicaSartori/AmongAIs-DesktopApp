package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.screens.Renderer;
import javafx.fxml.FXML;

public class howToPlayController implements Controller {

    public void initialize() {
        System.out.println("How to Play Controller done");
    }

    @Override
    public void updateContent() { }

    @FXML
    private void btnGoBackPressed() { Renderer.getInstance().show("mainMenu"); }
}
