/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum;

import com.erhannis.theallforum.data.Handle;
import com.erhannis.theallforum.data.events.Event;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author erhannis
 */
public class Context {
  public static class GsonWrapper {
    public final Gson gson;

    public GsonWrapper(Gson gson) {
      this.gson = gson;
    }

    public String toJson(Event src) {
      //TODO This is STUPID and I'm blaming Gson
      return gson.toJson(src, Event.class);
    }

    public String toJson(Handle src) {
      //TODO Just the dumbest
      return gson.toJson(src, Handle.class);
    }

    public String toJson(String src) {
      //TODO Ugggghhhh
      return gson.toJson(src, String.class);
    }

    public String toJson(List<Event> src) {
      //TODO Is this even going to work???
      return gson.toJson(src, new TypeToken<List<Event>>(){}.getType());
    }
    
    public <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
      return gson.fromJson(json, classOfT);
    }
    
    public <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
      return gson.fromJson(json, typeOfT);
    }
  }

  public GsonWrapper gson;
  public EntityManagerFactory factory;
  public KeyFile keyFile; //TODO Is this dangerous?
}
