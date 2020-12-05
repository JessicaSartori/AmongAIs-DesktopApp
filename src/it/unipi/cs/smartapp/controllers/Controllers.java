package it.unipi.cs.smartapp.controllers;

import javafx.scene.layout.Pane;

import it.unipi.cs.smartapp.drivers.GameServerDriver;
import it.unipi.cs.smartapp.drivers.GameServerResponse;
import it.unipi.cs.smartapp.drivers.ResponseCode;
import it.unipi.cs.smartapp.statemanager.StateManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;


public class Controllers {

    // Send s STATUS request, parse the response ad update the state
    public static void updateStatus(boolean spectating) {
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

    // Send a LOOK request and update the state
    public static void updateMap() {
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

    // Flip visibility state of a pane and put it to front
    public static void flipVisiblePane(Pane panel){
        panel.setVisible(!panel.isVisible());
        panel.toFront();
    }

    // Return a pool executor with 2 daemon threads to be used for delayed tasks (automatic actions)
    public static ScheduledThreadPoolExecutor setupPoolExecutor() {
        ScheduledThreadPoolExecutor automaticActions = new ScheduledThreadPoolExecutor(2,r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        });
        automaticActions.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        return automaticActions;
    }

    public static void closeGameServerConnection() {
        GameServerDriver gameServer = GameServerDriver.getInstance();

        GameServerResponse response = gameServer.sendLEAVE(StateManager.getInstance().getGameName(), "Bye");
        if (response.code != ResponseCode.OK) { System.err.println(response.freeText); }
        gameServer.closeConnection();
    }
}
