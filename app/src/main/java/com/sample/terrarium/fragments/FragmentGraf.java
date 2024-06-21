package com.sample.terrarium.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.sample.terrarium.R;
import com.sample.terrarium.network.NetworkCallback;
import com.sample.terrarium.network.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.OkHttpClient;

public class FragmentGraf extends Fragment {

    private String savedIP;
    private ArrayList<Entry> temperatureEntries;
    private ArrayList<Entry> humidityEntries;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_graf, container, false);



        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        savedIP = sharedPreferences.getString("savedIP", "");

        NetworkUtils networkUtils = new NetworkUtils(new OkHttpClient(), savedIP);

        temperatureEntries = new ArrayList<>();
        humidityEntries = new ArrayList<>();

        networkUtils.post("graf", new NetworkCallback() {
            @Override
            public void onResponse(String response) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        System.out.println(response);

                        parseAndAddEntries(response);


                        setGraf(rootView);



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



    private void setGraf(View rootView){

        LineChart chart = rootView.findViewById(R.id.chart1);
        LineDataSet dataSet = new LineDataSet(temperatureEntries, "Label");
        dataSet.setColor(Color.parseColor("#40E0D0"));
        dataSet.setCircleColor(Color.parseColor("#40E0D0"));
        dataSet.setDrawCircleHole(false);
        dataSet.setCircleRadius(5f);
        dataSet.setLineWidth(2.5f);
        dataSet.setDrawValues(false);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);
        LineData lineData = new LineData(dataSets);
        chart.setData(lineData);
        chart.setExtraTopOffset(10f);
        chart.getXAxis().setTextColor(Color.WHITE);
        chart.getXAxis().setTextSize(15);
        chart.getAxisLeft().setTextSize(15);
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisRight().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setScaleEnabled(false);
        chart.getXAxis().setGranularity(1f);
        chart.getAxisLeft().setGranularity(1f);
        chart.invalidate();


        LineChart chart2 = rootView.findViewById(R.id.chart2);
        LineDataSet dataSet2 = new LineDataSet(humidityEntries, "Label");
        dataSet2.setColor(Color.parseColor("#40E0D0"));
        dataSet2.setCircleColor(Color.parseColor("#40E0D0"));
        dataSet2.setDrawCircleHole(false);
        dataSet2.setCircleRadius(5f);
        dataSet2.setLineWidth(2.5f);
        dataSet2.setDrawValues(false);
        ArrayList<ILineDataSet> dataSets2 = new ArrayList<>();
        dataSets2.add(dataSet2);
        LineData lineData2 = new LineData(dataSets2);
        chart2.setData(lineData2);
        chart2.setExtraTopOffset(10f);
        chart2.getXAxis().setTextColor(Color.WHITE);
        chart2.getXAxis().setTextSize(15);
        chart2.getAxisLeft().setTextSize(15);
        chart2.getAxisLeft().setTextColor(Color.WHITE);
        chart2.getAxisRight().setEnabled(false);
        chart2.getDescription().setEnabled(false);
        chart2.getLegend().setEnabled(false);
        chart2.setScaleEnabled(false);
        chart2.getXAxis().setGranularity(1f);
        chart2.getAxisLeft().setGranularity(1f);
        chart2.invalidate();

    }

    private void parseAndAddEntries(String response) {
        String[] parts = response.split("\r\n");

        String temperaturePart = parts[0].replace("TemperatureGraf: ", "").trim();;
        int[] temperatureValues = parseValues(temperaturePart);
        addEntriesWithTime(temperatureValues, temperatureEntries);

        String humidityPart = parts[1].replace("HumidityGraf: ", "").trim();;
        int[] humidityValues = parseValues(humidityPart);
        addEntriesWithTime(humidityValues, humidityEntries);
    }

    private int[] parseValues(String values) {
        String[] stringValues = values.split(",");
        int[] intValues = new int[stringValues.length];
        for (int i = 0; i < stringValues.length; i++) {
            try {
                intValues[i] = Integer.parseInt(stringValues[i].trim());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                intValues[i] = 0;
            }
        }
        return intValues;
    }

    private void addEntriesWithTime(int[] values, List<Entry> entries) {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        for (int i = 0; i < values.length; i++) {
            int hour = currentHour-(values.length-i-1);
            if (hour < 0)
                hour = 24 - (hour*(-1));
            if (hour == 0)
                hour = 24;
            if (i!=0) {
                int currentHourOsn = currentHour;

                if (currentHourOsn == 0)
                    currentHourOsn = 24;

                if (currentHourOsn >= 9) { //todo >= / >
                    entries.add(new Entry(hour, values[i]));
                } else {
                    if (i > (24-(24+currentHour-values.length+2)+1)) {
                        entries.add(new Entry(hour, values[i]));
                    }
                    System.out.println();
                }
            }
        }



//        for (int i = 1; i < values.length; i++) {
//            entries.add(new Entry(i, values[i]));
//
//        }
    }


}
