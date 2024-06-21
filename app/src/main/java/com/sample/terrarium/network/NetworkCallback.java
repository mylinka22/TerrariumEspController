package com.sample.terrarium.network;

import java.io.IOException;

public interface NetworkCallback {
    void onResponse(String response);
    void onFailure(IOException e);
}
