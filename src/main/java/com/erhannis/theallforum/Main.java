/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum;

import com.erhannis.theallforum.data.Handle;
import com.erhannis.theallforum.data.Signature;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.HashSet;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
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
  private static Logger LOGGER = Logger.getLogger(Main.class.getName());
  
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws IOException, IllegalAccessException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, ClassNotFoundException {
    LOGGER.info("Server startup");
    LOGGER.info("Commit hash: " + getHash());

    Context ctx = new Context();
    RuntimePolytypeAdapterFactory rtaf = RuntimePolytypeAdapterFactory.of(Event.class);
    ctx.gson = new Gson().newBuilder().registerTypeAdapterFactory(rtaf).create();
    ctx.factory = Persistence.createEntityManagerFactory("default");

    KeyFile kf = getKeyFile(ctx, "./private.key");

    staticFileLocation("/public");
    startApi(ctx);
    get("*", (req, res) -> {
      res.status(404);
      return "404'd!";
    });
//    Spark.awaitStop();
//    factory.close();
//    System.exit(0);
  }

  public static void startApi(Context ctx) {
    String prefix = "/api";
    post(prefix + "/event", (req, res) -> {
      Event event = ctx.gson.fromJson(req.body(), Event.class);
      if (event instanceof PostEvent) {
        if (event instanceof PostCreated) {
          PostCreated pc = (PostCreated) event;
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
      EntityManager em = ctx.factory.createEntityManager();
      List<PostCreated> pcs = em.createQuery("select pc from PostCreated pc", PostCreated.class).getResultList();
      em.close();
      String result = ctx.gson.toJson(pcs);
      res.type("application/json");
      return result;
    });
    get(prefix + "/event/:id", (req, res) -> {
      return "";
    });
  }

  private static KeyFile getKeyFile(Context ctx, String path) throws IOException, IllegalAccessException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, ClassNotFoundException {
    File idFile = new File(path);
    if (idFile.exists()) {
      String currentHash = getHash();
      String hash = "UNKNOWN";
      try (FileInputStream fis = new FileInputStream(idFile); ObjectInputStream ois = new ObjectInputStream(fis)) {
        hash = ois.readUTF();
        return (KeyFile) ois.readObject();
      } catch (Exception e) {
        throw new RuntimeException("Error reading key file.  Server commit hash: " + currentHash + "  File commit hash: " + hash, e);
      }
    } else {
      if (idFile.createNewFile()) {
        try (FileOutputStream fos = new FileOutputStream(idFile); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
          KeyPairGenerator kpg = KeyPairGenerator.getInstance(Constants.KEY_ALGORITHM);
          kpg.initialize(Constants.KEY_BITS);
          KeyPair keyPair = kpg.genKeyPair();

          UserCreated uc = new UserCreated();
          uc.handle = Handle.gen();
          uc.username = null;
          uc.avatarUrl = null;
          uc.description = "AUTOGENERATED SERVER USER";
          uc.email = null;
          uc.parents = new HashSet<Handle>();
          uc.privateKeyEncrypted = null;
          uc.publicKey = keyPair.getPublic();
          uc.user = uc.handle;
          uc.userTimestamp = System.currentTimeMillis();
          uc.userSignature = Signature.signUser(ctx, uc, keyPair.getPrivate());
          uc.server = uc.user;
          uc.serverTimestamp = uc.userTimestamp;
          uc.serverSignature = Signature.signServer(ctx, uc, keyPair.getPrivate());

          EntityManager em = ctx.factory.createEntityManager();
          em.getTransaction().begin();
          em.persist(uc);
          em.getTransaction().commit();

          KeyFile kf = new KeyFile(uc.handle, keyPair.getPrivate());
          oos.writeUTF(getHash());
          oos.writeObject(kf);
          oos.flush();
          return kf;
        }
      } else {
        throw new IOException("Can't create id file");
      }
    }
  }

  public static String getHash() {
    try {
      String className = Main.class.getSimpleName() + ".class";
      String classPath = Main.class.getResource(className).toString();
      if (!classPath.startsWith("jar")) {
        return "unknown";
      }
      String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
      Manifest manifest = new Manifest(new URL(manifestPath).openStream());
      Attributes attr = manifest.getMainAttributes();
      return attr.getValue("git-hash");
    } catch (IOException e) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
      return "unknown";
    }
  }

  public static void asdf() {throw new RuntimeException();}
}
