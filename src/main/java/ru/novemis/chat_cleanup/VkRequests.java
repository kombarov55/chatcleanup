package ru.novemis.chat_cleanup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class VkRequests {

    private static String token = "445404f3a96413afc0709e6f8ea9de9bbc82f8b71f8d6befdc595496888833543f14e11f869720fefbaf2";
    private static String version = "5.103";

    public static void send(int peerId, String message) throws Throwable {
        String url = buildUrl("messages.send", new LinkedHashMap<String, String>() {{
            put("random_id", "" + new Random().nextInt());
            put("peer_id", "" + peerId);
            put("message", message);
        }});

        HttpConnector.get(url);
    }

    public static List<UserDto> getConversationMembers(int chatId) throws Throwable {
        List<UserDto> result = new ArrayList<>();

        String url = buildUrl("messages.getConversationMembers", new LinkedHashMap<String, String>() {{
            put("peer_id", "" + chatId);
        }});

        JSONObject json = new JSONObject(HttpConnector.get(url));
        JSONArray profiles = json.getJSONObject("response").getJSONArray("profiles");

        for (int i = 0; i < profiles.length(); i++) {
            JSONObject profile = profiles.getJSONObject(i);
            UserDto u = new UserDto();
            u.userId = profile.getInt("id");
            u.firstName = profile.getString("first_name");
            u.lastName = profile.getString("last_name");
            result.add(u);
        }

        return result;
    }

    public static void getConversations() throws Throwable {
        String url = buildUrl("messages.getConversations", new LinkedHashMap<>());
        HttpConnector.get(url);
    }

    public static JSONObject getHistory(int peerId) throws Throwable {
        String url = buildUrl("messages.getHistory", new LinkedHashMap<String, String>() {{
            put("peer_id", "" + peerId);
            put("count", "200");
            put("extended", "1");
        }});

        String rs = HttpConnector.get(url);

        return new JSONObject(rs);
    }

    public static JSONObject getHistory(int peerId, Long startMsgId) throws Throwable {
        String url = buildUrl("messages.getHistory", new LinkedHashMap<String, String>() {{
            put("peer_id", "" + peerId);
            put("count", "200");
            put("start_message_id", "" + startMsgId);
            put("extended", "1");
        }});

        String rs = HttpConnector.get(url);

        return new JSONObject(rs);
    }

    public static void removeChatUser(int chatId, int userId) throws Throwable {
        String url = buildUrl("messages.removeChatUser", new LinkedHashMap<String, String>() {{
            put("user_id", "" + userId);
            put("chat_id", "" + chatId);
        }});

        HttpConnector.get(url);
    }

    private static String buildUrl(String methodName, LinkedHashMap<String, String> params) {
        params.put("v", version);
        params.put("access_token", token);
        String paramsJoined = params.entrySet().stream()
          .map(entry -> URLEncoder.encode(entry.getKey()) + "=" + URLEncoder.encode(entry.getValue()))
          .collect(Collectors.joining("&"));



        String rs = "https://api.vk.com/method/" + methodName + "?" + paramsJoined;

        return rs;
    }

}
