package com.example.weatherappui;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    TextView cityName;
    TextView temperature;
    TextView weatherName;
    EditText cityEdit;
    ImageView weatherIcon;
    Button button;
    String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityName = findViewById(R.id.cityName);
        temperature = findViewById(R.id.temperature);
        weatherName = findViewById(R.id.weatherName);
        cityEdit = findViewById(R.id.cityEdit);
        weatherIcon = findViewById(R.id.weatherIcon);
        button = findViewById(R.id.btn_click);

        content = "https://openweathermap.org/data/2.5/weather?q=Seoul&appid=439d4b804bc8187953eb36d2a8c26a02";
        callWeatherData(content);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityEdit.getText().toString().trim();
                if (city.length() > 0) {
                    content = "https://openweathermap.org/data/2.5/weather?q=" + city + "&appid=439d4b804bc8187953eb36d2a8c26a02";
                    callWeatherData(content);
                } else {
                    Toast.makeText(getApplicationContext(), "Wrong input!", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    static class Weather extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream streamIn = connection.getInputStream();
                InputStreamReader streamInReader = new InputStreamReader(streamIn);

                int data = streamInReader.read();
                StringBuilder weatherContent = new StringBuilder();

                while (data != -1) {
                    char ch = (char) data;
                    weatherContent.append(ch);
                    data = streamInReader.read();
                }
                return weatherContent.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void callWeatherData(String content) {
        Weather weather = new Weather();

        try {
            String dataReceived = weather.execute(content).get();
            JSONObject jsonObject = new JSONObject(dataReceived);

            String cityInfo = jsonObject.getString("name");

            String weatherInfo = jsonObject.getString("weather");

            JSONArray arrayInfo = new JSONArray(weatherInfo);
            String iconInfo = "";
            String weather_name = "";
            for (int i = 0; i < arrayInfo.length(); i++) {
                JSONObject dataFromArray = arrayInfo.getJSONObject(i);
                iconInfo = dataFromArray.getString("icon");
                weather_name = dataFromArray.getString("main");
            }

            JSONObject mainInfo = jsonObject.getJSONObject("main");
            String tempMin = mainInfo.getString("temp_min");
            String tempMax = mainInfo.getString("temp_max");

            setMainInfo(cityInfo, tempMin, tempMax, weather_name);
            setIconInfo(iconInfo);
            setTrivial(mainInfo);

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "There is no such city!", Toast.LENGTH_SHORT).show();
        }

    }

    private void setMainInfo(String city, String min, String max, String weather) {
        cityName.setText(city);
        min += '\u2103';
        max += '\u2103';
        temperature.setText(min + " ~ " + max);
        weatherName.setText(weather);
    }

    private void setIconInfo(String iconData) {
        String targetIcon = "http://openweathermap.org/img/wn/" + iconData + "@2x.png";
        Uri uri = Uri.parse(targetIcon);
        Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(weatherIcon);
    }

    private void setTrivial(JSONObject mainObj) throws JSONException {
        String tempMax = mainObj.getString("temp_max");
        String humidity = mainObj.getString("humidity");
        StringBuilder trivial = new StringBuilder();
        trivial.append(tempMax).append('/').append(humidity).append("%");
    }
}