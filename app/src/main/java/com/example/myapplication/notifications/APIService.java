package com.example.myapplication.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Conetnt-Type:application/json",
            "Authorization:key=BIQnYlk70Akk-ktx5rABdqgXHko0puLzuhnLTh8rA2UaKgr48WsfHiwtKbLq_cG1H0tEEXl438ZSBsvQo_qbMws"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
