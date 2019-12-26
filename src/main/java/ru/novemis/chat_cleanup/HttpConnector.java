package ru.novemis.chat_cleanup;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class HttpConnector {

    private static Map<String, String> DEFAULT_HEADERS = new HashMap<String, String>() {{

    }};

    public static String get(String url) throws Throwable {
        return makeRequest("GET", url, DEFAULT_HEADERS, "");
    }

    public static String post(String url, String body) throws Throwable {
        return makeRequest("POST", url, DEFAULT_HEADERS, body);
    }

    private static String makeRequest(String method, String url, Map<String, String> headers, String body) throws Throwable {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        headers.forEach((k, v) -> connection.setRequestProperty(k, v));

        logRequest(method, url, headers, body);

        connection.connect();

        if (body != null && !body.isEmpty()) {
            byte[] bytes = body.getBytes("UTF-8");
            connection.getOutputStream().write(bytes);
            connection.getOutputStream().flush();
        }

        String responseCode = "" + connection.getResponseCode();

        InputStream in = responseCode.startsWith("5") || responseCode.startsWith("4") ?
          connection.getErrorStream() :
          connection.getInputStream();

        String rs = readAll(in);

        logResponse(method, url, connection.getHeaderFields(), rs);

        return rs;

    }

    private static void logRequest(String method, String url, Map<String, String> headers, String body) {
        String str = "\r\n";
        str += method + " " + url + "\r\n";
        if (!headers.isEmpty()) {
            str += "Headers: \r\n";
            for (Map.Entry<String, String> pair : headers.entrySet()) {
                String k = pair.getKey();
                String v = pair.getValue();

                str += "  " + k + " -> " + v + "\r\n";
            }
        }

        if (body != null && !body.isEmpty()) {
            str += body + "\r\n";
        }

        System.out.println(str);
    }

    private static void logResponse(String method, String url, Map<String, List<String>> headers, String body) {
        String str = "\r\n";
        str += "Response for " + method + " " + url + "\r\n";
        if (!headers.isEmpty()) {
            str += "Headers: \r\n";
            if (!headers.isEmpty()) {
                for (Map.Entry<String, List<String>> pair : headers.entrySet()) {
                    String k = pair.getKey();
                    String v = String.join(", ", pair.getValue());
                    str += "  " + k + " -> " + v + "\r\n";
                }
            }
        }
        if (body != null && !body.isEmpty()) {
            str += body + "\r\n";
        }

        System.out.println(str);
    }


    private static String readAll(InputStream inputStream) {
        if (inputStream == null) {
            return "";
        }

        try (Scanner scanner = new Scanner(inputStream, "UTF-8")) {
            if (scanner.hasNext()) {
                return scanner.useDelimiter("\\A").next();
            } else {
                return "";
            }
        }
    }

}
