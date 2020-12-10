package it.unipi.cs.smartapp.controllers;

import javafx.fxml.FXML;

import it.unipi.cs.smartapp.screens.Renderer;


public class howToPlayController implements Controller {

    public void initialize() {
        System.out.println("How to Play Controller done");
    }

    @Override
    public void updateContent() { }

    @FXML
    private void btnGoBackPressed() { Renderer.getInstance().show("mainMenu"); }
}
