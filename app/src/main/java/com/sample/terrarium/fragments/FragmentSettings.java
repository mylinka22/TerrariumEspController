package com.sample.terrarium.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.sample.terrarium.network.NetworkCallback;
import com.sample.terrarium.network.NetworkUtils;
import com.sample.terrarium.R;

import java.io.IOException;

import okhttp3.OkHttpClient;

public class FragmentSettings extends Fragment {

    private String savedIP;
    private Vibrator vibrator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        Button buttonSaveIP = rootView.findViewById(R.id.ButtonSaveIP);
        EditText edittextViewIP = rootView.findViewById(R.id.edittextViewIP);


        Spinner spinner1 = rootView.findViewById(R.id.spinner1);
        EditText editHourin = rootView.findViewById(R.id.editHourin);
        EditText editMinin = rootView.findViewById(R.id.editMinin);
        EditText editHourout = rootView.findViewById(R.id.editHourout);
        EditText editMinout = rootView.findViewById(R.id.editMinout);
        Button btnconfirm1 = rootView.findViewById(R.id.btnconfirm1);

        Spinner spinner2 = rootView.findViewById(R.id.spinner2);
        EditText editEveryhour = rootView.findViewById(R.id.editEveryhour);
        EditText editEveryhourstart = rootView.findViewById(R.id.editEveryhourstart);
        EditText editEveryhourend = rootView.findViewById(R.id.editEveryhourend);
        Button btnconfirm2 = rootView.findViewById(R.id.btnconfirm2);

        Button btncancel1 = rootView.findViewById(R.id.btncancel1);
        Button btncancel2 = rootView.findViewById(R.id.btncancel2);

        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        buttonSaveIP.setOnClickListener(v -> {
            String ip = edittextViewIP.getText().toString();
            saveIPToSharedPreferences(ip);

        });

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        savedIP = sharedPreferences.getString("savedIP", "");




        btnconfirm1.setOnClickListener(v -> {

            String id = "-1";

            switch (spinner1.getSelectedItem().toString()){
                case "Свет и подогрев":
                    id = "2";
                    break;
                case "Туманогенратор":
                    id = "1";
                    break;
                case "Помпа":
                    id = "0";
                    break;
                default:
                    break;
            }

            NetworkUtils networkUtils = new NetworkUtils(new OkHttpClient(), savedIP);
            respons(networkUtils ,"relay?id=" + id + "&hourin=" + editHourin.getText().toString() + "&minin=" + editMinin.getText().toString() + "&hourout=" + editHourout.getText().toString() + "&minout=" + editMinout.getText().toString());

        });


        btncancel1.setOnClickListener(v -> {
            NetworkUtils networkUtils = new NetworkUtils(new OkHttpClient(), savedIP);
            for (int i = 0; i <= 2; i++) {
                respons(networkUtils, "relay?id=" + i + "&hourin=-1&minin=-1&hourout=-1&minout=-1");
            }
        });



        btnconfirm2.setOnClickListener(v -> {

            String id = "-1";

            switch (spinner2.getSelectedItem().toString()){
                case "Свет и подогрев":
                    id = "2";
                    break;
                case "Туманогенратор":
                    id = "1";
                    break;
                case "Помпа":
                    id = "0";
                    break;
                default:
                    break;
            }

            NetworkUtils networkUtils = new NetworkUtils(new OkHttpClient(), savedIP);
            respons(networkUtils ,"everyhour?id="+id+"&status=1&timein="+editEveryhourstart.getText().toString()+"&timeout="+editEveryhourend.getText().toString()+"&time="+editEveryhour.getText().toString());

        });


        btncancel2.setOnClickListener(v -> {
            NetworkUtils networkUtils = new NetworkUtils(new OkHttpClient(), savedIP);
            for (int i = 0; i <= 2; i++) {
                respons(networkUtils, "everyhour?id="+i+"&status=0&timein=-1&timeout=-1&time=-1");
            }
        });




        return rootView;
    }


    private void saveIPToSharedPreferences(String ip) {
        Context context = getActivity();
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("savedIP", ip);
        editor.apply();
    }


    private void respons(NetworkUtils networkUtils, String post){

        networkUtils.post(post, new NetworkCallback() {
            @Override
            public void onResponse(String response) {
                // Обработка успешного ответа
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        System.out.println(response);
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

}
