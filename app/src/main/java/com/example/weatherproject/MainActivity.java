package com.example.weatherproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private EditText cityInput;
    private EditText countryInput;
    private TextView output;
    private Button weatherButton;
    private ProgressBar pb;
    private SharedPreferences sharedPreferences;
    private boolean isWeatherDataStored;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityInput = findViewById(R.id.inputbox);
        countryInput = findViewById(R.id.countyinput);
        output = findViewById(R.id.output);
        weatherButton = findViewById(R.id.weather);
        pb = findViewById(R.id.progressBar);

        sharedPreferences = getSharedPreferences("WeatherData", MODE_PRIVATE);
        isWeatherDataStored = sharedPreferences.getBoolean("isWeatherDataStored", false);

        if (isWeatherDataStored) {
            String savedWeatherData = sharedPreferences.getString("weatherData", "");
            output.setText(savedWeatherData);
            pb.setVisibility(View.GONE);
        }

        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWeatherData();
            }
        });
    }

    private void getWeatherData() {
        String city = cityInput.getText().toString().trim();
        String country = countryInput.getText().toString().trim();

        if (!city.isEmpty()) {
            WeatherTask task = new WeatherTask();
            task.execute(city, country);
        } else {
            Toast.makeText(this, "Please enter a city", Toast.LENGTH_SHORT).show();
        }
    }

    private class WeatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String city = params[0];
            String country = params[1];
            String apiKey = "f2cf806827bd4e0b990100457232905";
            String apiUrl = "https://api.weatherapi.com/v1/current.json?key=" + apiKey + "&q=" + city + "," + country;

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                inputStream.close();
                connection.disconnect();

                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            pb.setVisibility(View.GONE);

            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject currentObject = jsonObject.getJSONObject("current");

                    // Extract weather information
                    double temperature = currentObject.getDouble("temp_c");
                    int humidity = currentObject.getInt("humidity");
                    double windSpeed = currentObject.getDouble("wind_kph");
                    String weatherInfo = currentObject.getJSONObject("condition").getString("text");

                    // Display weather information
                    String weatherData = "Temperature: " + temperature + "°C\n"
                            + "Humidity: " + humidity + "%\n"
                            + "Wind Speed: " + windSpeed + " km/h\n"
                            + "Weather: " + weatherInfo;

                    // Save weather data to SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("weatherData", weatherData);
                    editor.putBoolean("isWeatherDataStored", true);
                    editor.apply();

                    output.setText(weatherData);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Error retrieving weather data", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
