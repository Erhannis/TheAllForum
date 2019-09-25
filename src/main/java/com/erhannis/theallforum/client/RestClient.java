/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.client;

import com.erhannis.theallforum.Context;
import com.erhannis.theallforum.data.Handle;
import com.erhannis.theallforum.data.events.Event;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 *
 * @author erhannis
 */
public class RestClient {
  private final Context ctx;
  private final HttpUrl api;
  private final OkHttpClient client;

  public RestClient(Context ctx) {
    this.ctx = ctx;
    this.client = new OkHttpClient();
    this.api = new HttpUrl.Builder()
            .scheme("http")
            .host("localhost")
            .port(4567)
            .addPathSegment("api")
            .build();
  }

  public List<Event> events() throws IOException {
    Request request = new Request.Builder()
            .url(api.newBuilder().addPathSegment("event").build())
            .build();

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        throw new IOException("Unexpected code " + response);
      }
      if (response.body() == null) {
        throw new IOException("Null response");
      }

      String str = response.body().string();
      //System.out.println("/events received: " + str);
      return ctx.om.readValue(str, new TypeReference<List<Event>>() {
      });
    }
  }

  public Handle login(String username, String password) throws IOException {
    Request request = new Request.Builder()
            .url(api.newBuilder().addPathSegment("login").build())
            .post(new FormBody.Builder()
                    .add("username", username)
                    .add("password", password)
                    .build())
            .build();

    try (Response response = client.newCall(request).execute()) {
      if (response.code() == 401) {
        return null;
      }
      if (!response.isSuccessful()) {
        throw new IOException("Unexpected code " + response);
      }
      if (response.body() == null) {
        throw new IOException("Null response");
      }

      String str = response.body().string();
      return ctx.om.readValue(str, Handle.class);
    }
  }
}
