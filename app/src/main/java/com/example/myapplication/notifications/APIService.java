package com.example.myapplication.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Conetnt-Type:application/json",
            "Authorization:key=AAAAzGSOk5c:APA91bGLhoywuJlK32zOtI_7lgLbTVFOtID6bUjt50n2o2L1ZZjaF0GmcE_hn7Ra2fHn8fEsBF8_pxMRG00JF8gksRs62yxbKQev3QQRdrjmp4XT_fUpt8Y1D-4Eb_KJ8CudOI_91BO7"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
