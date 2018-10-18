/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum;

import com.erhannis.theallforum.data.Handle;
import com.erhannis.theallforum.data.events.Event;
import com.erhannis.theallforum.data.events.post.PostCreated;
import com.erhannis.theallforum.data.events.post.PostEvent;
import com.erhannis.theallforum.data.events.post.PostTagsAdded;
import com.erhannis.theallforum.data.events.post.PostTagsRemoved;
import com.erhannis.theallforum.data.events.post.PostTextUpdated;
import com.erhannis.theallforum.data.events.tag.TagEvent;
import com.erhannis.theallforum.data.events.user.UserCreated;
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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
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

    EntityManagerFactory factory = Persistence.createEntityManagerFactory("default");
    {
      EntityManager em = factory.createEntityManager();
      em.getTransaction().begin();

      System.err.println("//TODO Generate server user");
      
      PostCreated pc = new PostCreated();
      pc.handle = Handle.gen();
      pc.parents = new HashSet<Handle>();
      pc.previous = new HashSet<Handle>();
      pc.tags = new HashSet<Handle>();
      pc.text = "This is a test.";
      pc.userTimestamp = System.currentTimeMillis();
      pc.serverTimestamp = System.currentTimeMillis();
      pc.server;
      pc.serverSignature;
      pc.user;
      pc.userSignature;
      //TODO There are other fields
      em.persist(pc);

      em.getTransaction().commit();
      em.close();
    }

    staticFileLocation("/public");
    startApi(factory, gson);
    get("*", (req, res) -> {
      res.status(404);
      return "404'd!";
    });
//    Spark.awaitStop();
//    factory.close();
//    System.exit(0);
  }

  public static void startApi(EntityManagerFactory factory, Gson gson) {
    String prefix = "/api";
    post(prefix + "/event", (req, res) -> {
      Event event = gson.fromJson(req.body(), Event.class);
      if (event instanceof PostEvent) {
        if (event instanceof PostCreated) {
          PostCreated pc = (PostCreated)event;
          /*
          Ok, so.
          
          handle
          parents[]
          previous[]
          tags[]
          text
          user
          signature
          timestamp
          
          All but the last two can be set by the client.
          The signature, the client may not be able to unless they've taken
          ownership of their private key, in which case they MUST provide
          the signature and the server will authenticate it.
          The timestamp...dang.  It's tricky.  We want the timestamps to be
          "accurate".  However, we also want the client to be allowed to sign
          their own messages.  We could:
          1. Not include the timestamp in the signature.  This leadeth into shenanigans.
          2. Allow the client to set and sign their own timestamp.  This leadeth into shenanigans.
          3. Allow the client to send a partial msg to the server to be timestamped, then the user signs it and sends it back.
          This allows the user to wait before posting a message into the past (shenanigans), OR prevents long lag times (e.g. interplanetary internet).
          4. What if a user timestamped and signed a message, then sent it to the server to be timestamped and signed by the server, too?
          It's more complicated, but I kinda like it.  It furthermore allows different servers' influences to be marked.  I don;t know what THAT gains us exactly, but I kinda like it.
          
          */
          asdf();
        } else if (event instanceof PostTextUpdated) {
          asdf();
        } else if (event instanceof PostTagsAdded) {
          asdf();
        } else if (event instanceof PostTagsRemoved) {
          asdf();
        } else {
          res.status(400);
          return "unknown event type";
        }
      } else if (event instanceof TagEvent) {
        asdf();
      } else if (event instanceof UserEvent) {
        asdf();
      } else {
        res.status(400);
        return "unknown event type";
      }
      return "";
    });
    get(prefix + "/event", (req, res) -> {
      //TODO JDBC
      //TODO Remove or authenticate
      EntityManager em = factory.createEntityManager();
      List<PostCreated> pcs = em.createQuery("select pc from PostCreated pc", PostCreated.class).getResultList();
      em.close();
      String result = gson.toJson(pcs);
      res.type("application/json");
      return result;
    });
    get(prefix + "/event/:id", (req, res) -> {
      return "";
    });
  }

  private static Handle getThisServer(Gson gson) throws IOException {
    File idFile = new File("./id.txt");
    if (idFile.exists()) {
      asdf();
    } else {
      if (idFile.createNewFile()) {
        FileWriter fw = new FileWriter(idFile);
        
        UserCreated uc = new UserCreated();
        uc.handle = Handle.gen();
        uc.username = null;
        uc.avatarUrl = null;
        uc.description = "AUTOGENERATED SERVER USER";
        uc.email = null;
        uc.parents = new HashSet<Handle>();
        uc.privateKeyEncrypted;
        uc.publicKey;
        uc.user = uc.handle;
        uc.userTimestamp = System.currentTimeMillis();
        uc.userSignature;
        uc.server = uc.user;
        uc.serverTimestamp = uc.userTimestamp;
        uc.serverSignature;
        
        fw.append(gson.toJson());
        fw.flush();
        fw.close();
      } else {
        throw new IOException("Can't create id file");
      }
    }
  }
  
  //public static void asdf() {throw new RuntimeException();}
}
