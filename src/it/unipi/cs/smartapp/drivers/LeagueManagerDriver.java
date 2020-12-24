package it.unipi.cs.smartapp.drivers;

import it.unipi.cs.smartapp.statemanager.Tournament;
import it.unipi.cs.smartapp.statemanager.TournamentLeaderboard;
import it.unipi.cs.smartapp.statemanager.TournamentRound;
import it.unipi.cs.smartapp.statemanager.TournamentStatus;
import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

public class LeagueManagerDriver {
    private static LeagueManagerDriver instance = null;

    public final static String LM_SERVER = "http://api.dbarasti.com:8080/";
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String ALREADY_JOINED = "You already joined this tournament.";
    private static final String ALREADY_WITHDRAWN = "The given player is not registered to the given tournament!";
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
            } else if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
                return ALREADY_JOINED;
            }
        } catch(IOException err) {
            System.err.println("Error with LM server.");
        }

        return response.toString();
    }

    // Perform HTTP DELETE request with one parameter
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
            } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                return ALREADY_WITHDRAWN;
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
                // Ignore min/maxParticipants since always null (LM rules)
                // Long max = (Long)tournament.get("max_participants");
                // t.maxParticipantsProperty().set(max.intValue());
                // Long min = (Long)tournament.get("min_participants");
                // t.minParticipantsProperty().set(min.intValue());
                String StartSubs = (String)tournament.get("start_subscriptions_date");
                String EndSubs = (String)tournament.get("end_subscriptions_date");
                String StartTourn = (String)tournament.get("start_matches_date");
                String EndTourn = (String)tournament.get("end_matches_date");

                t.startSubscriptionsProperty().set(getDate(StartSubs));
                t.endSubscriptionsProperty().set(getDate(EndSubs));
                t.startTournamentProperty().set(getDate(StartTourn));
                t.endTournamentProperty().set(getDate(EndTourn));

                // Update Tournaments State
                tournamentStatus.addTournament(t);
            }

        } catch(ParseException err) {
            System.err.println("Error parsing the received JSON");
        }
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

    // Join a specific tournament
    public String joinTournament(String TournamentName, String PlayerId) {
        String parameters = "{ \"player_id\": \"" + PlayerId + "\", \"tournament_id\": \"" + TournamentName + "\" }";
        String response = doPostRequest("registration", parameters);

        if (!response.equals(ALREADY_JOINED)) {
            try {
                JSONParser parse = new JSONParser();
                JSONObject json = (JSONObject)parse.parse(response);
                String message = (String)json.get("message");

                return message;
            } catch (ParseException err) {
                System.err.println("Error parsing the JSON.");
            }
        }

        return response;
    }

    // Withdraw from a specific tournament
    public String withdrawTournament(String TournamentName, String PlayerId) {
        String parameters = "tournament_id=" + TournamentName + "&player_id=" + PlayerId;
        return doDeleteRequest("registration", parameters);
    }

    // Get all participants of a specific tournament
    public ArrayList<String> getTournamentParticipants(String TournamentName) {
        String response = doGetRequest("registration", new Pair<>("tournament_id", TournamentName));

        ArrayList<String> listParticipants = new ArrayList<>();

        try {
            JSONParser parse = new JSONParser();
            JSONObject json = (JSONObject)parse.parse(response);
            JSONArray playerAttributes = (JSONArray)json.get("data");

            for(int i = 0; i < playerAttributes.size(); i++)  {
                JSONObject player = (JSONObject)playerAttributes.get(i);

                String playerInfo = (String)player.get("player_id") + " - " + getDate((String)player.get("datetime"));
                listParticipants.add(playerInfo);
            }

        } catch(ParseException err) {
            System.err.println("Error parsing the received JSON");
        }

        return listParticipants;
    }

    // Get schedule of a specific tournament
    public ArrayList<TournamentRound> getTournamentSchedule(String TournamentName) {
        String response = doGetRequest("schedule", new Pair<>("tournament_id", TournamentName));

        ArrayList<TournamentRound> tournamentRounds = new ArrayList<>();

        try {
            JSONParser parse = new JSONParser();
            JSONObject json = (JSONObject)parse.parse(response);
            JSONArray tournamentAttributes = (JSONArray)json.get("rounds");
            TournamentRound r = new TournamentRound();

            // For all rounds
            for (int i = 0; i < tournamentAttributes.size(); i++) {
                JSONObject match = (JSONObject)tournamentAttributes.get(i);
                JSONArray matchesAttributes = (JSONArray)match.get("matches");
                ArrayList<TournamentRound.Match> roundMatches = new ArrayList<>();

                // For all matches
                for (int j = 0; j < matchesAttributes.size(); j++) {
                    JSONObject participants = (JSONObject)matchesAttributes.get(j);
                    JSONArray participantsArray = (JSONArray)participants.get("participants");
                    TournamentRound.Match m = new TournamentRound.Match();
                    ArrayList<String> p = new ArrayList<>();

                    m.startDate = getDate((String)participants.get("start_date"));

                    // For all participants
                    for (int k = 0; k < participantsArray.size(); k++) {
                        p.add((String)participantsArray.get(k));
                    }

                    m.participants = p;
                    roundMatches.add(m);
                }

                r.rounds.addAll(roundMatches);
                tournamentRounds.add(r);
            }

        } catch(ParseException err) {
            System.err.println("Error parsing the received JSON");
        }

        return tournamentRounds;
    }

    // Get tournament leaderboard
    public ArrayList<TournamentLeaderboard> getTournamentLeaderboard(String TournamentName) {
        String response = doGetRequest("leaderboard", new Pair<>("tournament_id", TournamentName));
        ArrayList<TournamentLeaderboard> leaderboard = new ArrayList<>();

        try {
            JSONParser parse = new JSONParser();
            JSONObject json = (JSONObject)parse.parse(response);
            JSONArray tournLeaderboard = (JSONArray)json.get("leaderboard");

            for(int i = 0; i < tournLeaderboard.size(); i++)  {
                TournamentLeaderboard tl = new TournamentLeaderboard();
                JSONObject playerLeaderboard = (JSONObject)tournLeaderboard.get(i);

                tl.playerName = (String)playerLeaderboard.get("player_id");
                Long playerRankLong = (Long)playerLeaderboard.get("player_rank");
                tl.playerRank = playerRankLong.intValue();
                Long playerScoreLong = (Long)playerLeaderboard.get("player_score");
                tl.playerScore = playerScoreLong.intValue();
                leaderboard.add(tl);
            }

        } catch(ParseException err) {
            System.err.println("Error parsing the received JSON");
        }

        return leaderboard;
    }
}