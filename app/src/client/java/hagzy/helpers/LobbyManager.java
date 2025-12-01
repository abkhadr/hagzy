package hagzy.helpers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class LobbyManager {
    private static final String TAG = "LobbyManager";
    private static final String WS_URL = "wss://hagzy-match-lobby.hagzy-pro.workers.dev";

    private WebSocket webSocket;
    private OkHttpClient client;
    private Handler handler;
    private Handler pingHandler;
    private Runnable pingRunnable;

    private boolean isConnected = false;
    private long pingStartTime = 0;
    private long currentPing = 0;

    private LobbyListener listener;

    public interface LobbyListener {
        void onConnected();
        void onDisconnected();
        void onJoinedLobby(LobbyData lobby, boolean isLeader);
        void onPlayerJoined(LobbyData lobby);
        void onPlayerLeft(LobbyData lobby);
        void onMatchStarted(String matchId, LobbyData lobby);
        void onPingUpdate(long ping);
        void onError(String error);
    }

    public LobbyManager(LobbyListener listener) {
        this.listener = listener;
        this.client = new OkHttpClient();
        this.handler = new Handler(Looper.getMainLooper());
        this.pingHandler = new Handler(Looper.getMainLooper());
    }

    public void connect(String lobbyId, String userId, String userName, String matchType) {
        Log.d(TAG, "==========================================");
        Log.d(TAG, "→ Connecting to lobby: " + lobbyId);
        Log.d(TAG, "  User: " + userName + " (" + userId + ")");
        Log.d(TAG, "  Match Type: " + matchType);

        if (isConnected) {
            Log.w(TAG, "Already connected, disconnecting first...");
            disconnect();
        }

        Request request = new Request.Builder().url(WS_URL).build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                Log.d(TAG, "✓✓✓ WebSocket OPENED ✓✓✓");
                isConnected = true;

                handler.post(() -> {
                    if (listener != null) {
                        listener.onConnected();
                    }
                });

                // Start ping and send join message
                startPingMeasurement();
                sendJoinMessage(lobbyId, userId, userName, matchType);
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                Log.d(TAG, "==========================================");
                Log.d(TAG, "📨 MESSAGE RECEIVED: " + text);
                handleMessage(text);
            }

            @Override
            public void onClosing(WebSocket ws, int code, String reason) {
                Log.d(TAG, "⚠️ WebSocket CLOSING: " + code + " - " + reason);
                isConnected = false;
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                Log.d(TAG, "✗✗✗ WebSocket CLOSED: " + code + " - " + reason);
                isConnected = false;

                handler.post(() -> {
                    if (listener != null) {
                        listener.onDisconnected();
                    }
                });
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Log.e(TAG, "✗✗✗ WebSocket FAILED ✗✗✗");
                Log.e(TAG, "Error: " + t.getMessage());
                if (t.getCause() != null) {
                    Log.e(TAG, "Cause: " + t.getCause().getMessage());
                }

                isConnected = false;

                handler.post(() -> {
                    if (listener != null) {
                        listener.onDisconnected();
                    }
                });
            }
        });
    }

    private void sendJoinMessage(String lobbyId, String userId, String userName, String matchType) {
        try {


            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String userIdClient = user != null ? user.getUid() : null;
            String image = !Objects.equals(userId, userIdClient) ? "https://i.pravatar.cc/150?img=" + (Math.random() * 70) : String.valueOf(user.getPhotoUrl());

            JSONObject message = new JSONObject();
            message.put("type", "join");

            JSONObject data = new JSONObject();
            data.put("lobbyId", lobbyId);
            data.put("userId", userId);
            data.put("userName", userName);
            data.put("userLevel", 1);
            data.put("userAvatar", image);
            data.put("matchType", matchType);

            message.put("data", data);

            String messageStr = message.toString();
            Log.d(TAG, "→ Sending JOIN message: " + messageStr);

            if (webSocket != null && isConnected) {
                boolean sent = webSocket.send(messageStr);
                Log.d(TAG, sent ? "✓ JOIN message sent successfully" : "✗ Failed to send JOIN message");
            } else {
                Log.e(TAG, "✗ Cannot send JOIN: WebSocket not ready");
            }
        } catch (Exception e) {
            Log.e(TAG, "✗ Error sending join: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void disconnect() {
        Log.d(TAG, "→ Disconnecting...");
        stopPingMeasurement();

        if (webSocket != null && isConnected) {
            try {
                webSocket.close(1000, "User leaving");
                Log.d(TAG, "✓ Disconnect initiated");
            } catch (Exception e) {
                Log.e(TAG, "✗ Error closing: " + e.getMessage());
            }
            webSocket = null;
            isConnected = false;
        }
    }

    public void startMatch(String lobbyId, String userId) {
        Log.d(TAG, "==========================================");
        Log.d(TAG, "→ Starting match for lobby: " + lobbyId);

        if (!isConnected || webSocket == null) {
            Log.e(TAG, "✗ Cannot start match: Not connected");
            if (listener != null) {
                listener.onError("غير متصل بالساحة");
            }
            return;
        }

        try {
            JSONObject message = new JSONObject();
            message.put("type", "start_match");

            JSONObject data = new JSONObject();
            data.put("lobbyId", lobbyId);
            data.put("userId", userId);
            message.put("data", data);

            String messageStr = message.toString();
            Log.d(TAG, "→ Sending START_MATCH: " + messageStr);

            boolean sent = webSocket.send(messageStr);
            Log.d(TAG, sent ? "✓ START_MATCH sent" : "✗ Failed to send START_MATCH");
        } catch (Exception e) {
            Log.e(TAG, "✗ Error starting match: " + e.getMessage());
            if (listener != null) {
                listener.onError("فشل بدء المباراة");
            }
        }
    }

    private void handleMessage(String text) {
        try {
            JSONObject message = new JSONObject(text);
            String type = message.getString("type");

            Log.d(TAG, "📋 Processing message type: " + type);

            handler.post(() -> {
                try {
                    switch (type) {
                        case "joined":
                            Log.d(TAG, "✓ Handling: JOINED");
                            handleJoined(message);
                            break;

                        case "player_joined":
                            Log.d(TAG, "✓ Handling: PLAYER_JOINED");
                            handlePlayerJoined(message);
                            break;

                        case "player_left":
                            Log.d(TAG, "✓ Handling: PLAYER_LEFT");
                            handlePlayerLeft(message);
                            break;

                        case "match_started":
                            Log.d(TAG, "✓ Handling: MATCH_STARTED");
                            handleMatchStarted(message);
                            break;

                        case "pong":
                            handlePong(message);
                            break;

                        case "error":
                            Log.e(TAG, "✗ Server ERROR");
                            String error = message.optString("error", "حدث خطأ");
                            Log.e(TAG, "Error message: " + error);
                            if (listener != null) {
                                listener.onError(error);
                            }
                            break;

                        default:
                            Log.w(TAG, "⚠️ Unknown message type: " + type);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "✗ Error handling message: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "✗ Error parsing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleJoined(JSONObject message) throws Exception {
        JSONObject lobbyObj = message.getJSONObject("lobby");
        boolean isLeader = message.optBoolean("isLeader", false);
        LobbyData lobby = parseLobby(lobbyObj);

        Log.d(TAG, "  → Lobby: " + lobby.lobbyId);
        Log.d(TAG, "  → Players: " + lobby.players.size() + "/" + lobby.maxPlayers);
        Log.d(TAG, "  → Leader: " + (isLeader ? "YES" : "NO"));
        Log.d(TAG, "  → Status: " + lobby.status);

        if (listener != null) {
            listener.onJoinedLobby(lobby, isLeader);
        }
    }

    private void handlePlayerJoined(JSONObject message) throws Exception {
        JSONObject lobbyObj = message.getJSONObject("lobby");
        LobbyData lobby = parseLobby(lobbyObj);

        Log.d(TAG, "  → New player count: " + lobby.players.size() + "/" + lobby.maxPlayers);
        Log.d(TAG, "  → Lobby status: " + lobby.status);

        // Log all players
        for (int i = 0; i < lobby.players.size(); i++) {
            PlayerData p = lobby.players.get(i);
            Log.d(TAG, "    Player " + (i+1) + ": " + p.name + " (" + p.userId + ")");
        }

        if (listener != null) {
            listener.onPlayerJoined(lobby);
        }
    }

    private void handlePlayerLeft(JSONObject message) throws Exception {
        JSONObject lobbyObj = message.getJSONObject("lobby");
        LobbyData lobby = parseLobby(lobbyObj);

        Log.d(TAG, "  → Remaining players: " + lobby.players.size() + "/" + lobby.maxPlayers);

        if (listener != null) {
            listener.onPlayerLeft(lobby);
        }
    }

    private void handleMatchStarted(JSONObject message) throws Exception {
        String matchId = message.getString("matchId");
        LobbyData lobby = null;

        if (message.has("lobby")) {
            JSONObject lobbyObj = message.getJSONObject("lobby");
            lobby = parseLobby(lobbyObj);
        }

        Log.d(TAG, "  → Match ID: " + matchId);
        Log.d(TAG, "  → Players: " + (lobby != null ? lobby.players.size() : "N/A"));

        if (listener != null) {
            listener.onMatchStarted(matchId, lobby);
        }
    }

    private void handlePong(JSONObject message) {
        if (pingStartTime > 0) {
            currentPing = System.currentTimeMillis() - pingStartTime;
            pingStartTime = 0;

            if (listener != null) {
                listener.onPingUpdate(currentPing);
            }
        }
    }

    private void startPingMeasurement() {
        pingRunnable = new Runnable() {
            @Override
            public void run() {
                if (webSocket != null && isConnected) {
                    try {
                        pingStartTime = System.currentTimeMillis();
                        JSONObject ping = new JSONObject();
                        ping.put("type", "ping");
                        webSocket.send(ping.toString());
                    } catch (Exception e) {
                        Log.e(TAG, "Ping error: " + e.getMessage());
                    }
                }

                if (pingHandler != null && pingRunnable != null) {
                    pingHandler.postDelayed(pingRunnable, 3000); // Every 3 seconds
                }
            }
        };

        pingHandler.postDelayed(pingRunnable, 1000); // Start after 1 second
    }

    private void stopPingMeasurement() {
        if (pingHandler != null && pingRunnable != null) {
            pingHandler.removeCallbacks(pingRunnable);
            pingRunnable = null;
        }
    }

    private LobbyData parseLobby(JSONObject obj) throws Exception {
        LobbyData lobby = new LobbyData();
        lobby.lobbyId = obj.optString("lobbyId");
        lobby.matchType = obj.getString("matchType");
        lobby.status = obj.optString("status", "waiting");
        lobby.maxPlayers = obj.optInt("maxPlayers", getMaxPlayers(lobby.matchType));
        lobby.players = new ArrayList<>();

        JSONArray playersArray = obj.getJSONArray("players");
        for (int i = 0; i < playersArray.length(); i++) {
            JSONObject p = playersArray.getJSONObject(i);
            PlayerData player = new PlayerData();
            player.userId = p.getString("userId");
            player.name = p.getString("userName");
            player.level = p.optInt("level", 1);
            player.avatar = p.optString("avatar", null);
            lobby.players.add(player);
        }

        return lobby;
    }

    private int getMaxPlayers(String type) {
        if (type == null) return 4;
        else if (type.equals("2v2")) return 2;
        else if (type.equals("3v3")) return 3;
        else if (type.equals("4v4")) return 4;
        else if (type.equals("5v5")) return 5;
        else if (type.equals("6v6")) return 6;
        return 4;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public long getCurrentPing() {
        return currentPing;
    }

    public static class LobbyData {
        public String lobbyId;
        public String matchType;
        public String status;
        public int maxPlayers;
        public List<PlayerData> players;

        public boolean isFull() {
            return players != null && players.size() >= maxPlayers;
        }
    }

    public static class PlayerData {
        public String userId;
        public String name;
        public int level;
        public String avatar;
    }
}