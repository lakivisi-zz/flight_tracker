package com.andia.loice.flighttracker.model.api;

import com.andia.loice.flighttracker.utils.Constants;
import com.andia.loice.flighttracker.view.activity.FlightListActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Inject;

import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * Factory class that provides Retrofit, LoggingInteceptor , OkHttpClient
 * and ApiService instances to be able to fetch data
 */
public class ApiServiceFactory {

    private Gson gson;

    @Inject
    public ApiServiceFactory() {
        gson = new GsonBuilder().create();
    }

    /**
     * Method to Log the body of each request
     *
     * @return
     */
    public HttpLoggingInterceptor provideLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new
                HttpLoggingInterceptor();
        Timber.d(HttpLoggingInterceptor.Level.BODY.toString());
        interceptor.level(HttpLoggingInterceptor.Level.BODY);

        return interceptor;
    }

    /**
     * Intercept request to add query params such as the api key
     *
     * @return
     */
    private Interceptor getQueryParamsInterceptor() {
        String token = FlightListActivity.token;


        Timber.d("token %s", token);

        return chain -> {
            Request original = chain.request();
            HttpUrl url = original.url();
            HttpUrl newUrl = url.newBuilder().build();
            Request.Builder builder = original.newBuilder().url(newUrl);
            Request request = builder.build();
            return chain.proceed(request);
        };
    }


    public GsonConverterFactory provideGsonConverterFactory() {
        return GsonConverterFactory.create();
    }

    public OkHttpClient provideClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(getQueryParamsInterceptor())
                .addInterceptor(provideLoggingInterceptor())
                .build();
    }

    /**
     * Method to provide Retrofit instance
     *
     * @return
     */
    public ApiService providesApiService() {
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(provideClient())
                .baseUrl(Constants.BASE_URL)
                .build();
        return retrofit.create(ApiService.class);

    }
}
