/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.client;

import com.erhannis.theallforum.Context;
import com.erhannis.theallforum.data.events.Event;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 * @author erhannis
 */
public class RestClient {
  private final Context ctx;
  private final HttpUrl.Builder api;
  private final OkHttpClient client;
  
  public RestClient(Context ctx) {
    this.ctx = ctx;
    this.client = new OkHttpClient();
    this.api = new HttpUrl.Builder()
            .scheme("http")
            .host("localhost")
            .port(4567)
            .addPathSegment("api");
  }

  public List<Event> events() throws IOException {
    Request request = new Request.Builder()
            .url(api.addPathSegment("event").build())
            .build();

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new IOException("Unexpected code " + response);
      }
      if (response.body() == null) {
        throw new IOException("Null response");
      }

      Headers responseHeaders = response.headers();
      for (int i = 0; i < responseHeaders.size(); i++) {
        System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
      }

      return ctx.gson.fromJson(response.body().string(), new TypeToken<List<Event>>(){}.getType());
    }
  }
}
