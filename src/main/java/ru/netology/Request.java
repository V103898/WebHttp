package ru.netology;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> queryParams;

    public Request(String method, String path, Map<String, String> queryParams) {
        this.method = method;
        this.path = path;
        this.queryParams = Collections.unmodifiableMap(new HashMap<>(queryParams));
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public static Request fromRequestLine(String requestLine) {
        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid request line");
        }

        String method = parts[0];
        String uri = parts[1];

        // Split path and query parameters
        int queryStart = uri.indexOf('?');
        String path;
        Map<String, String> params = new HashMap<>();

        if (queryStart != -1) {
            path = uri.substring(0, queryStart);
            String queryString = uri.substring(queryStart + 1);

            for (String param : queryString.split("&")) {
                String[] keyValue = param.split("=", 2);
                if (keyValue.length > 0) {
                    String value = keyValue.length > 1 ? keyValue[1] : "";
                    params.put(keyValue[0], value);
                }
            }
        } else {
            path = uri;
        }

        return new Request(method, path, params);
    }
}
