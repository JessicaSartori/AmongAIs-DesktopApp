package it.unipi.cs.smartapp.controllers;

import it.unipi.cs.smartapp.drivers.ChatSystemDriver;
import it.unipi.cs.smartapp.drivers.GameServerDriver;
import it.unipi.cs.smartapp.drivers.GameServerResponse;
import it.unipi.cs.smartapp.drivers.ResponseCode;
import it.unipi.cs.smartapp.screens.Renderer;
import it.unipi.cs.smartapp.statemanager.StateManager;


public class Controllers {

    static void updateStatus(boolean spectating) {
        GameServerDriver gameServer = GameServerDriver.getInstance();
        StateManager stateMgr = StateManager.getInstance();

        GameServerResponse res = gameServer.sendSTATUS(stateMgr.getGameName());

        if (res.code != ResponseCode.OK) {
            System.err.println(res.freeText);
            return;
        }
        System.out.println(res.freeText);
        String[] data = (String[]) res.data;

        // Update game status
        String GA = data[0].substring(4); // Remove "GA: "
        stateMgr.updateGameState(GA);

        if(!spectating) {
            // Update player status
            String ME = data[1].substring(4); // Remove "ME: "
            stateMgr.updatePlayerStatus(ME);
        }

        // Update list of players
        for (int i = (spectating) ? 1 : 2; i < data.length; i++) {
            String PL = data[i].substring(4); // Remove "PL: "
            stateMgr.updatePlayerStatus(PL);
        }
    }

    static void updateMap() {
        GameServerDriver gameServer = GameServerDriver.getInstance();
        StateManager stateMgr = StateManager.getInstance();

        GameServerResponse response = gameServer.sendLOOK(stateMgr.getGameName());

        if (response.code != ResponseCode.OK) {
            System.err.println(response.freeText);
            return;
        }
        System.out.println(response.freeText);

        stateMgr.map.setGameMap((String[]) response.data);
    }

    static void quit() {
        GameServerDriver gameServer = GameServerDriver.getInstance();
        StateManager stateMgr = StateManager.getInstance();
        ChatSystemDriver chatSystem = ChatSystemDriver.getInstance();

        // Close connection with game server
        GameServerResponse response = gameServer.sendLEAVE(stateMgr.getGameName(), "Done playing");
        if (response.code != ResponseCode.OK) { System.err.println(response.freeText); }
        else { System.out.println(response.freeText); }
        gameServer.closeConnection();

        // Unsubscribe from all chat channels and close connection
        chatSystem.sendLEAVE(stateMgr.getGameName());
        chatSystem.closeConnection();

        Renderer.getInstance().show("mainMenu");
    }
}
