package com.example.myapplication;

import static com.google.android.material.internal.ViewUtils.hideKeyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fetchWeatherData("Bhopal");
        searchCity();
    }

    private void searchCity() {
        SearchView searchView = binding.searchView;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null) {
                    fetchWeatherData(query);
                    hideKeyboard();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && binding.searchView.getWindowToken() != null) {
            imm.hideSoftInputFromWindow(binding.searchView.getWindowToken(), 0);
        }
    }

    private void fetchWeatherData(String cityName) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<WeatherApp> response = apiInterface.getWeatherData(cityName, "ed31e859eff41ac837576e88f0eb7eff", "metric");

        response.enqueue(new Callback<WeatherApp>() {
            @Override
            public void onResponse(Call<WeatherApp> call, Response<WeatherApp> response) {
                WeatherApp responseBody = response.body();
                if (response.isSuccessful() && responseBody != null) {
                    double temperature = responseBody.getMain().getTemp();
                  //  double maxTemp = responseBody.getMain().getTempMax();
                  //  double minTemp = responseBody.getMain().getTempMin();
                    int humidity = (int) responseBody.getMain().getHumidity();
                    double windSpeed = responseBody.getWind().getSpeed();
                    long sunRise = responseBody.getSys().getSunrise();
                    long sunSet = responseBody.getSys().getSunset();
                    int seaLevel = (int) responseBody.getMain().getPressure();
                    String condition = responseBody.getWeather().get(0).getMain();

                    binding.temp.setText(String.format(Locale.getDefault(), "%.2f °C", temperature));
                    binding.weather.setText(condition);
                   // binding.maxTemp.setText(String.format(Locale.getDefault(), "Max: %.2f °C", maxTemp));
                   // binding.minTemp.setText(String.format(Locale.getDefault(), "Min: %.2f °C", minTemp));
                    binding.humidity.setText(String.format(Locale.getDefault(), "%d%%", humidity));
                    binding.windSpeed.setText(String.format(Locale.getDefault(), "%.2f m/s", windSpeed));
                    binding.sunrise.setText(time(sunRise));
                    binding.sunset.setText(time(sunSet));
                    binding.sea.setText(String.format(Locale.getDefault(), "%d hPa", seaLevel));
                    binding.condtion.setText(condition);
                    binding.day.setText(dayName(System.currentTimeMillis()));
                    binding.date.setText(date());
                    binding.cityName.setText(cityName);

                    changeImageAccordingToWeather(condition);
                }
            }

            @Override
            public void onFailure(Call<WeatherApp> call, Throwable t) {
                Log.e("WeatherDebug", "API call failed: " + t.getMessage());
            }
        });
    }

    private void changeImageAccordingToWeather(String conditions) {
        if (conditions == null) return;
        switch (conditions) {
            case "Clear Sky":
            case "Sunny":
            case "Clear":
                binding.getRoot().setBackgroundResource(R.drawable.sunday);
                binding.lottieAnimationView.setAnimation(R.raw.achha_sun);
                break;
            case "Partly Cloudy":
            case "Clouds":
            case "Overcast":
            case "Mist":
            case "Foggy":
            case "Fog":
                binding.getRoot().setBackgroundResource(R.drawable.colud_background);
                binding.lottieAnimationView.setAnimation(R.raw.cloud);
                break;
            case "Light Rain":
            case "Rain":
            case "Thunderstorm":
            case "Drizzle":
            case "Moderate Rain":
            case "Showers":
            case "Heavy Rain":
                binding.getRoot().setBackgroundResource(R.drawable.rain_background);
                binding.lottieAnimationView.setAnimation(R.raw.ok);
                break;
            case "Light Snow":
            case "Snow":
            case "Moderate Snow":
            case "Heavy Snow":
            case "Blizzard":
                binding.getRoot().setBackgroundResource(R.drawable.blursnow);
                binding.lottieAnimationView.setAnimation(R.raw.snow);
                break;
            default:
                binding.getRoot().setBackgroundResource(R.drawable.sunday);
                binding.lottieAnimationView.setAnimation(R.raw.achha_sun);
                break;
        }
        binding.lottieAnimationView.playAnimation();
    }

    private String date() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String time(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp * 1000));
    }

    private String dayName(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
