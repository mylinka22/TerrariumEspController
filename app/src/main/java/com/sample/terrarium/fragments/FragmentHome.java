package com.sample.terrarium.fragments;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.os.VibrationEffect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.OkHttpClient;


import android.os.Vibrator;
import android.os.Build;

import com.sample.terrarium.network.NetworkCallback;
import com.sample.terrarium.network.NetworkUtils;
import com.sample.terrarium.R;

public class FragmentHome extends Fragment {

    private String savedIP;
    private TextView textC;
    private TextView textH;
    private Vibrator vibrator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);


        TextView textViewIP = rootView.findViewById(R.id.textViewIP);
        Button buttonR1 = rootView.findViewById(R.id.buttonR1);
        Button buttonR2 = rootView.findViewById(R.id.buttonR2);
        Button buttonR3 = rootView.findViewById(R.id.buttonR3);
        textC = rootView.findViewById(R.id.textC);
        textH = rootView.findViewById(R.id.textH);
        SwipeRefreshLayout swiperefresh = rootView.findViewById(R.id.swiperefresh);
        refreshApp(swiperefresh);

        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);



        setupButtonClickListener(buttonR1, "led3");
        setupButtonClickListener(buttonR2, "led2");
        setupButtonClickListener(buttonR3, "led1");


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        savedIP = sharedPreferences.getString("savedIP", "");
        textViewIP.setText(savedIP);

        NetworkUtils networkUtils = new NetworkUtils(new OkHttpClient(), savedIP);
        respons(networkUtils ,"temperature");


        return rootView;
    }


    private void refreshApp(SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            vibrate(100);
            NetworkUtils networkUtils = new NetworkUtils(new OkHttpClient(), savedIP);
            respons(networkUtils ,"temperature");
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void setupButtonClickListener(Button button, String endpoint) {
        button.setOnClickListener(v -> {
            vibrate(100);
            NetworkUtils networkUtils = new NetworkUtils(new OkHttpClient(), savedIP);
            respons(networkUtils, endpoint);
        });
    }


    private void respons(NetworkUtils networkUtils, String post){

        networkUtils.post(post, new NetworkCallback() {
            @Override
            public void onResponse(String response) {
                // Обработка успешного ответа
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        System.out.println(response);
                        if (post.equals("temperature")) {
                            setdatchik(response);
                        }
                    });
                }
            }

            @Override
            public void onFailure(IOException e) {
                // Обработка ошибки
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Обновите UI здесь, если нужно
                        Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });

    }

    private void setdatchik(String response){

        String[] lines = response.split("\r\n");
        int datchiktValue = -1;
        int datchikhValue = -1;


        for (String line : lines) {
            if (line.startsWith("Datchikt:")) {
                String datchiktStr = line.substring("Datchikt: ".length()).trim();
                datchiktValue = Integer.parseInt(datchiktStr);
            }
            else if (line.contains("Datchikh:")) {
                String datchikhStr = line.substring(line.lastIndexOf(":") + 1).trim();
                datchikhValue = Integer.parseInt(datchikhStr);
            }
        }

        textC.setText(datchiktValue + "°С");
        textH.setText(datchikhValue + "%");


    }

    private void vibrate(int milliseconds) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            // Для версий ниже API 26
            vibrator.vibrate(milliseconds);
        }
    }


}