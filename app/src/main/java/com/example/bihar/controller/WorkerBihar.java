package com.example.bihar.controller;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import com.example.bihar.controller.GeneradorConexionesSeguras;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.net.ssl.HttpsURLConnection;

public class WorkerBihar extends Worker {

    public WorkerBihar(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        String jsonString = getInputData().getString("datos");

        HttpsURLConnection urlConnection = GeneradorConexionesSeguras.getInstance()
                .crearConexionSegura(getApplicationContext(), "https://134.209.235.115/uluque001/WEB/BIHAR/Principal.php");

        try {
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(jsonString);
            out.close();

            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) {
                BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line;
                String result="";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                inputStream.close();

                if(!result.isEmpty()){
                    Data.Builder data = new Data.Builder();

                    data.putString("result",result);
                    Log.i("BDD","CORRECTO: " + result);

                    return Result.success(data.build());
                }else{
                    Log.i("BDD","Vacio");
                }
            }else{
                Log.i("BDD","FAIL");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.failure();
    }
}
