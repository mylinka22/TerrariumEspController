package com.sample.terrarium.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sample.terrarium.R;
import com.sample.terrarium.network.NetworkCallback;
import com.sample.terrarium.network.NetworkUtils;

import java.io.IOException;

import okhttp3.OkHttpClient;

public class FragmentTasks extends Fragment {

    private String savedIP;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tasks, container, false);


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        savedIP = sharedPreferences.getString("savedIP", "");

        NetworkUtils networkUtils = new NetworkUtils(new OkHttpClient(), savedIP);



        networkUtils.post("task", new NetworkCallback() {
            @Override
            public void onResponse(String response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        System.out.println(response);

                        parse(response);





                    });
                }
            }

            @Override
            public void onFailure(IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });




        return rootView;

    }


    private void parse(String response) {
        Map<String, String> parsedData = new HashMap<>();

        String[] lines = response.split("\n");
        for (String line : lines) {
            String[] parts = line.split(": ");
            if (parts.length == 2) {
                parsedData.put(parts[0], parts[1]);
            }
        }

        TextView task1TextView = getActivity().findViewById(R.id.time1);
        TextView task2TextView = getActivity().findViewById(R.id.time2);
        TextView task3TextView = getActivity().findViewById(R.id.time3);
        TextView hourTextView = getActivity().findViewById(R.id.time4);

        task1TextView.setText(parseTask(parsedData.get("task3")));
        task2TextView.setText(parseTask(parsedData.get("task2")));
        task3TextView.setText(parseTask(parsedData.get("task1")));
        hourTextView.setText(parseTask(parsedData.get("hour")));
    }


    private String parseTask(String task) {
        if (task != null && !task.isEmpty()) {
//            String[] times = task.split("/");
//            if (times[0].equals("-1:-1") || times[0].equals("-1-1")) {
//                return "Null";
//            }
//            if (times.length == 2) {
//                return times[0] + " / " + times[1];
//            }

            if (task.startsWith("-1")) {
                return "Null";
            }

        }
        return task;
    }




}
