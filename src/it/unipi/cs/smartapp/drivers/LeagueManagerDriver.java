package it.unipi.cs.smartapp.drivers;

import it.unipi.cs.smartapp.statemanager.Tournament;
import it.unipi.cs.smartapp.statemanager.TournamentStatus;
import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

public class LeagueManagerDriver {
    private static LeagueManagerDriver instance = null;

    public final static String LM_SERVER = "http://api.dbarasti.com/";
    private static final String USER_AGENT = "Mozilla/5.0";
    private TournamentStatus tournamentStatus;

    public static LeagueManagerDriver getInstance() {
        if(instance == null) instance = new LeagueManagerDriver();
        return instance;
    }

    public LeagueManagerDriver() { tournamentStatus = TournamentStatus.getInstance(); }

    // Perform HTTP GET request without parameters
    private String doGetRequest(String endpoint) {
        StringBuffer response = new StringBuffer();

        try {
            URL obj = new URL(LM_SERVER + endpoint);
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
            } else {
                return "Error with League Manager.";
            }
        } catch(IOException err) {
            System.err.println("HTTP ERROR: " + LM_SERVER + endpoint);
        }

        return response.toString();
    }

    // Perform HTTP GET request with only one parameters
    private String doGetRequest(String endpoint, Pair<String, String> param) {
        StringBuffer response = new StringBuffer();

        try {
            String query = "?" + String.format(param.getKey() + "=%s", URLEncoder.encode(param.getValue(), "UTF-8"));
            URL obj = new URL(LM_SERVER + endpoint + query);
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
            } else {
                return "Error with League Manager.";
            }
        } catch(IOException err) {
            System.err.println("HTTP ERROR: " + LM_SERVER + endpoint);
        }

        return response.toString();
    }

    // Perform HTTP POST request with json body
    private String doPostRequest(String endpoint, String parameters) {
        StringBuffer response = new StringBuffer();

        try {
            URL obj = new URL(LM_SERVER + endpoint);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Content-Type", "application/json");

            con.setDoOutput(true);
            con.setDoInput(true);
            OutputStream os = con.getOutputStream();
            os.write(parameters.getBytes());
            os.flush();
            os.close();

            int responseCode = con.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            } else {
                return "Error with League Manager.";
            }
        } catch(IOException err) {
            System.err.println("HTTP ERROR: " + LM_SERVER + endpoint);
        }

        return response.toString();
    }

    private String doDeleteRequest(String endpoint, String parameters) {
        StringBuffer response = new StringBuffer();

        try {
            URL obj = new URL(LM_SERVER + endpoint + "?" + parameters);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("DELETE");
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
            } else {
                return "Error with League Manager.";
            }
        } catch(IOException err) {
            System.err.println("HTTP ERROR: " + LM_SERVER + endpoint);
        }

        return response.toString();
    }

    // Get all available tournaments
    public void getTournaments() {
        try {
            String response = doGetRequest("tournament");
            JSONParser parse = new JSONParser();
            JSONObject json = (JSONObject)parse.parse(response);
            JSONArray tournamentAttributes = (JSONArray)json.get("data");

            for(int i = 0; i < tournamentAttributes.size(); i++)  {
                JSONObject tournament = (JSONObject)tournamentAttributes.get(i);
                Tournament t = new Tournament();

                String tournamentName = (String)tournament.get("id");
                t.tournamentNameProperty().set(tournamentName);
                t.gameTypeProperty().set((String)tournament.get("game_type"));
                t.maxParticipantsProperty().set((String)tournament.get("max_participants"));
                t.minParticipantsProperty().set((String)tournament.get("min_participants"));
                String StartSubs = (String)tournament.get("start_subscriptions_date");
                String EndSubs = (String)tournament.get("end_subscriptions_date");
                String StartTourn = (String)tournament.get("start_matches_date");
                String EndTourn = (String)tournament.get("end_matches_date");

                t.startSubscriptionsProperty().set(getDate(StartSubs));
                t.endSubscriptionsProperty().set(getDate(EndSubs));
                t.startTournamentProperty().set(getDate(StartTourn));
                t.endTournamentProperty().set(getDate(EndTourn));

                tournamentStatus.tournamentsList.put(tournamentName, t);
                tournamentStatus.tournamentTableList.add(t);
            }

        } catch(ParseException err) {
            System.err.println("Error parsing the received JSON");
        }
    }

    // Parse ISO Date as simpler String
    private String getDate(String isoDate) {
        Instant instant = Instant.parse(isoDate);
        Date myDate = Date.from(instant);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm - dd/MM/yyyy");
        String date = formatter.format(myDate);

        return date;
    }

    // Join a specific tournament
    public void joinTournament(String TournamentName, String PlayerId) {
        String parameters = "{ \"player_id\": \"" + PlayerId + "\", \"tournament_id\": \"" + TournamentName + "\" }";
        doPostRequest("registration", parameters);
    }

    // Withdraw from a specific tournament
    public void withdrawTournament(String TournamentName, String PlayerId) {
        String parameters = "tournamentID=" + TournamentName + "&playerID=" + PlayerId;
        doDeleteRequest("registration", parameters);
    }

    // Get all participants of a specific tournament
    public ArrayList<String> getTournamentParticipants(String TournamentName) {
        String response = doGetRequest("registration", new Pair<>("tournament_id", TournamentName));
        ArrayList<String> listParticipants = new ArrayList<String>();

        try {
            JSONParser parse = new JSONParser();
            JSONObject json = (JSONObject)parse.parse(response);
            JSONArray playerAttributes = (JSONArray)json.get("data");

            for(int i = 0; i < playerAttributes.size(); i++)  {
                JSONObject player = (JSONObject)playerAttributes.get(i);

                String playerInfo = (String)player.get("player_id"); // + " - " + getDate((String)player.get("datetime"));
                listParticipants.add(playerInfo);
            }

        } catch(ParseException err) {
            System.err.println("Error parsing the received JSON");
        }

        return listParticipants;
    }
}