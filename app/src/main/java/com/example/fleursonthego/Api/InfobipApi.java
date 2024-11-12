package com.example.fleursonthego.Api;

import com.example.fleursonthego.Models.SmsRequest;
import com.example.fleursonthego.Models.SmsResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface InfobipApi {

    @Headers({
            "Authorization: App d8ffa57b8b597347679b5981e3635ebb-a6df7459-0dc7-4a46-b59e-5e8777632552",
            "Content-Type: application/json"
    })
    @POST("/sms/2/text/advanced")
    Call<SmsResponse> sendSms(@Body SmsRequest smsRequest);
}
