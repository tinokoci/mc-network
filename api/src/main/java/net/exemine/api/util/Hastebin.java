package net.exemine.api.util;

import net.exemine.api.controller.ApiController;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.Objects;

public class Hastebin {

    private static final String API_URL = ApiController.getInstance().getHastebinUrl();
    private static final MediaType MEDIA_TYPE_PLAINTEXT = MediaType.get("text/plain; charset=utf-8");

    public static String paste(String text, boolean raw) {
        Objects.requireNonNull(API_URL, "Hastebin URL is not defined");
        if (text.isEmpty()) return "Empty";

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(text, MEDIA_TYPE_PLAINTEXT);
        Request request = new Request.Builder()
                .url("http://" + API_URL + "/documents")
                .post(requestBody)
                .addHeader("User-Agent", "Hastebin Java Api")
                .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if (responseBody == null) return "Response body is null";

            String url = Objects.requireNonNull(response.body()).string();
            String postURL = raw ? "https://" + API_URL + "/raw/" : "http://" + API_URL + "/";

            if (url.contains("\"key\"")) {
                url = url.substring(url.indexOf(":") + 2, url.length() - 2);
                url = postURL + url;
            }
            return url;
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed";
        }
    }
}