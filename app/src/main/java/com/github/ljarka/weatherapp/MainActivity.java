package com.github.ljarka.weatherapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.temperature)
    TextView temperature;

    @BindView(R.id.city)
    TextView city;

    @BindView(R.id.sky_text)
    TextView skyText;

    @BindView(R.id.weather_icon)
    ImageView weatherIcon;

    @BindView(R.id.city_name_edit_text)
    TextInputEditText editText;

    private Retrofit retrofit;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        retrofit = new Retrofit.Builder()
                .baseUrl("http://weathers.co/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        search("Warszawa");
    }

    private void search(String searchQuery) {
        WeatherService weatherService = retrofit.create(WeatherService.class);
        weatherService.getWeather(searchQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dataContainer -> {
                    WeatherDetail weatherDetail = dataContainer.getData();
                    city.setText(weatherDetail.getLocation());
                    temperature.setText(weatherDetail.getTemperature()
                            + "\u00b0 C");
                    skyText.setText(weatherDetail.getSkytext());
                    showImageBySkyText(weatherDetail.getSkytext());

                    if (progressDialog != null) {
                        progressDialog.hide();
                        showNotification(searchQuery);
                    }

                });
    }

    private void showNotification(String searchQuery) {
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(this, 11,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sun);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentText("Załadowano informacje pogodowe dla miasta " + searchQuery)
                .setSmallIcon(R.drawable.sun)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap)
                        .setBigContentTitle("BIG CONTENT TITLE").setSummaryText("SUMMARY TEXT"))
                .addAction(R.drawable.humidity, "Search", mainActivityPendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(searchQuery.hashCode(), notification);
    }

    @OnClick(R.id.search_button)
    void onSearchButtonClick() {
        progressDialog = ProgressDialog.show(this, "Ładowanko", "Sie ładuje", true);
        search(editText.getText().toString());
    }

    private void showImageBySkyText(String skytext) {
        if ("Sky is clear".equalsIgnoreCase(skytext)) {
            weatherIcon.setImageResource(R.drawable.sun);
        } else if ("Few clouds".equalsIgnoreCase(skytext)) {
            weatherIcon.setImageResource(R.drawable.humidity);
        } else {
            weatherIcon.setImageResource(R.drawable.rain);
        }
    }
}
