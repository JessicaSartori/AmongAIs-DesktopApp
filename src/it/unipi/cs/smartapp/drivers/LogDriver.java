package it.unipi.cs.smartapp.drivers;

import it.unipi.cs.smartapp.statemanager.GlobalPlayerStatistics;
import it.unipi.cs.smartapp.statemanager.PlayerGameHistory;
import it.unipi.cs.smartapp.statemanager.StateManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

public class LogDriver {
    private static LogDriver instance = null;
    private StateManager stateManager;

    public final static String LOG_SERVER = "http://sa20dssystem.pythonanywhere.com/";
    private final static String USER_AGENT = "Mozilla/5.0";
    public final static String USER_NOTFOUND = "User not found.";

    public static LogDriver getInstance() {
        if(instance == null) instance = new LogDriver();
        return instance;
    }

    private LogDriver() { stateManager = StateManager.getInstance(); }

    // Perform HTTP GET request without parameters
    private String doGetRequest(String endpoint) {
        StringBuffer response = new StringBuffer();

        try {
            URL obj = new URL(LOG_SERVER + endpoint);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = con.getResponseCode();

            // Check Result
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }
        } catch(IOException err) {
            // User not found - read the body request
            return USER_NOTFOUND;
        }

        return response.toString();
    }

    // Parse ISO Date as simpler String
    private String getDate(String isoDate) {
        // Every date will be in this format: 2020-12-17T11:56:41.866Z
        // Remove last 6 chars and replace with 'Z'
        String correctDate = isoDate.substring(0, isoDate.length() - 5) + "Z";
        Instant instant = Instant.parse(correctDate);
        Date myDate = Date.from(instant);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm - dd/MM/yyyy");
        String date = formatter.format(myDate);

        return date;
    }

    public ArrayList<PlayerGameHistory> getPlayerHistory(String playerName) {
        String endpoint = "history/" + playerName;
        String response = doGetRequest(endpoint);
        ArrayList<PlayerGameHistory> playerHistory = new ArrayList<>();

        // Check if user exists
        if (!response.equals(USER_NOTFOUND)) {
            try {
                JSONParser parse = new JSONParser();
                JSONObject json = (JSONObject)parse.parse(response);
                JSONArray gamesHistory = (JSONArray)json.get("history");

                for(int i = 0; i < gamesHistory.size(); i++) {
                    PlayerGameHistory game = new PlayerGameHistory();
                    JSONObject match = (JSONObject) gamesHistory.get(i);

                    Long matchId = (Long) match.get("match_id");
                    game.matchIdProperty().set(matchId.toString());
                    game.playerAccuracyProperty().set((Double) match.get("player_accuracy"));
                    game.playerMatchNameProperty().set((String) match.get("player_in_match_name"));
                    game.playerRealNameProperty().set((String) match.get("player_real_name"));
                    Long leaderboardPosition = (Long) match.get("player_leaderboard_position");
                    game.playerLeaderboardPositionProperty().set(leaderboardPosition.intValue());
                    Long score = (Long) match.get("player_score");
                    game.playerScoreProperty().set(score.intValue());
                    Long kills = (Long) match.get("player_kills");
                    game.playerKillsProperty().set(kills.intValue());
                    game.playerKilledProperty().set((Boolean) match.get("player_was_killed"));

                    playerHistory.add(game);
                    stateManager.addGameHistory(game);
                }

            } catch(ParseException err) {
                playerHistory = new ArrayList<>();
            }
        }

        return playerHistory;
    }

    public GlobalPlayerStatistics getGlobalPlayerStatistics(String playerName) {
        String endpoint = "global_statistics/" + playerName;
        String response = doGetRequest(endpoint);
        GlobalPlayerStatistics playerStats = new GlobalPlayerStatistics();

        // Check if user exists
        if (!response.equals(USER_NOTFOUND)) {
            try {
                JSONParser parse = new JSONParser();
                JSONObject json = (JSONObject)parse.parse(response);
                JSONObject stats = (JSONObject)json.get("global_statistics");

                Long data = (Long) stats.get("best_score");
                playerStats.bestScore = data.intValue();
                data = (Long) stats.get("played_matches");
                playerStats.playedMatches = data.intValue();
                playerStats.totalAccuracy = (Double) stats.get("total_accuracy");
                data = (Long) stats.get("total_deaths");
                playerStats.totalDeaths = data.intValue();
                playerStats.totalKillDeathRatio = (Double) stats.get("total_kill_death_ratio");
                data = (Long) stats.get("total_kills");
                playerStats.totalKills = data.intValue();
                data = (Long) stats.get("total_score");
                playerStats.totalScore = data.intValue();

            } catch(ParseException err) {
                playerStats = null;
            }
        }

        return playerStats;
    }
}
