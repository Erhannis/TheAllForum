/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum;

import com.erhannis.theallforum.data.events.Event;
import com.erhannis.theallforum.data.events.post.PostCreated;
import com.erhannis.theallforum.data.events.post.PostEvent;
import com.erhannis.theallforum.data.events.post.PostTagsAdded;
import com.erhannis.theallforum.data.events.post.PostTagsRemoved;
import com.erhannis.theallforum.data.events.post.PostTextUpdated;
import com.erhannis.theallforum.data.events.tag.TagEvent;
import com.erhannis.theallforum.data.events.user.UserEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.typeadapters.RuntimePolytypeAdapterFactory;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import java.io.IOException;
import java.lang.reflect.Type;
import spark.Spark;
import static spark.Spark.*;

/**
 *
 * @author erhannis
 */
public class Main {
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
//    RuntimeTypeAdapterFactory<PostEvent> shapeAdapterFactory
//            = RuntimeTypeAdapterFactory.of(PostEvent.class, "type");
//
//    shapeAdapterFactory.registerSubtype(PostCreated.class, "PostCreated");
//    shapeAdapterFactory.registerSubtype(PostTagsAdded.class, "PostTagsAdded");
//    shapeAdapterFactory.registerSubtype(PostTagsRemoved.class, "PostTagsRemoved");
//    shapeAdapterFactory.registerSubtype(PostTextUpdated.class, "PostTextUpdated");
//
//    Gson gson = new GsonBuilder()
//            .registerTypeAdapterFactory(shapeAdapterFactory)
//            .create();

    //RuntimeTypeAdapterFactory rtaf = RuntimeTypeAdapterFactory.of(Event.class, "type");
    //rtaf.registerSubtype(PostCreated.class);
    RuntimePolytypeAdapterFactory rtaf = RuntimePolytypeAdapterFactory.of(Event.class);
    Gson gson = new Gson().newBuilder().registerTypeAdapterFactory(rtaf).create();
    //System.out.println(gson.toJson(PostCreated.class));
    System.out.println(gson.toJson(new PostCreated(), Event.class));
    Event test = gson.fromJson(gson.toJson(new PostCreated(), Event.class), Event.class);
    
    post("/event", (req, res) -> {
      Event event = gson.fromJson(req.body(), Event.class);
      if (event instanceof PostEvent) {
        
      } else if (event instanceof TagEvent) {
        
      } else if (event instanceof UserEvent) {
        
      } else {
        res.status(400);
      }
      return "";
    });
    get("/event", (req, res) -> {
      return "";
    });
    get("/event/:id", (req, res) -> {
      return "";
    });
    Spark.awaitStop();
    System.exit(0);
  }
}
