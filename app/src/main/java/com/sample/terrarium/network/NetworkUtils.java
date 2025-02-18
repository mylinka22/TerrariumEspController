package com.sample.terrarium.network;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkUtils {

    private OkHttpClient client;
    private String ip;

    public NetworkUtils(OkHttpClient client, String ip) {
        this.client = client;
        this.ip = ip;
    }

    public void post(final String post, final NetworkCallback callback) {
        Request request = new Request.Builder().url("http://" + ip + "/" + post).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onResponse(response.body().string());
                } else {
                    callback.onFailure(new IOException("Unexpected code " + response));
                }
            }
        });
    }

}
