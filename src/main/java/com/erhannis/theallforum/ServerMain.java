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
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.config.TinkConfig;
import com.google.crypto.tink.subtle.AesGcmJce;
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
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.commons.codec.digest.DigestUtils;
import spark.Spark;
import static spark.Spark.*;

/**
 *
 * @author erhannis
 */
public class ServerMain extends BaseMain {
  private static Logger LOGGER = Logger.getLogger(ServerMain.class.getName());
  
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws IOException, IllegalAccessException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, ClassNotFoundException, GeneralSecurityException {
    LOGGER.info("Server startup");
    LOGGER.info("Commit hash: " + getHash());

    Context ctx = new Context();
    RuntimePolytypeAdapterFactory rtaf = RuntimePolytypeAdapterFactory.of(Event.class);
    ctx.gson = new Gson().newBuilder().registerTypeAdapterFactory(rtaf).create();
    ctx.factory = Persistence.createEntityManagerFactory("default");
    ctx.keyFile = getKeyFile(ctx, "./private.key");

    AeadConfig.register();
    
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
      List<Event> es = em.createQuery("select e from Event e", Event.class).getResultList();
      em.close();
      //TODO Trying to get polymorphism to work
      String result = ctx.gson.toJson(es);
      res.type("application/json");
      return result;
    });
    get(prefix + "/event/:id", (req, res) -> {
      return "";
    });
    get(prefix + "/test_create_user/:username/:password", (req, res) -> {
      String username = req.params("username");
      String password = req.params("password");
      if (username == null || password == null) {
        res.status(400);
        return "Error; username or password missing";
      }
      return ctx.gson.toJson(createUserTest(ctx, username, password));
    });
    Spark.delete(prefix + "/event/:id", (req, res) -> {
      //TODO Authentication
      //TODO Also, deletion is bad in this system
      String handle = req.params("id");
      if (handle == null) {
        res.status(400);
        return "Error: id missing";
      }
      EntityManager em = ctx.factory.createEntityManager();
      em.getTransaction().begin();
      int deleted = em.createQuery("delete from Event evt where evt.handle.value = :id")
              .setParameter("id", handle)
              .executeUpdate();
      em.getTransaction().commit();
      System.out.println("deleted " + deleted);
      res.type("application/json");
      return "" + deleted; //TODO Should return deleted event?
    });
  }

  private static UserCreated createUserTest(Context ctx, String username, String password) throws NoSuchAlgorithmException, IllegalAccessException, InvalidKeyException, SignatureException, IOException, GeneralSecurityException {
    //TODO Allow users to provide their own key
    KeyPairGenerator kpg = KeyPairGenerator.getInstance(Constants.KEY_ALGORITHM);
    kpg.initialize(Constants.KEY_BITS);
    KeyPair keyPair = kpg.genKeyPair();
    
    UserCreated uc = new UserCreated();
    uc.handle = Handle.gen(keyPair.getPublic());
    //TODO Check username availability
    System.err.println("Make sure to check username available before creating");
    uc.username = username;
    uc.avatarUrl = null;
    uc.description = "You're a kitty!";
    uc.email = "email@internet.com";
    uc.parents = new HashSet<Handle>();
    uc.privateKeyEncrypted = encryptKey(keyPair.getPrivate(), password);
    uc.publicKey = keyPair.getPublic();
    uc.user = null;
    uc.userTimestamp = 0;
    uc.userSignature = null;
    uc.server = ctx.keyFile.serverHandle;
    uc.serverTimestamp = System.currentTimeMillis();
    uc.serverSignature = sign(ctx, uc);

    EntityManager em = ctx.factory.createEntityManager();
    em.getTransaction().begin();
    em.persist(uc);
    em.getTransaction().commit();
    em.close();
    
    return uc;
  }
  
  private static byte[] encryptKey(PrivateKey key, String password) throws IOException, GeneralSecurityException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(key);
      oos.flush();

      //TODO AesGcmJce is unsupported, and pure SHA256 is prolly a poor key-derivation function
      AesGcmJce agj = new AesGcmJce(DigestUtils.sha256(password));
      byte[] ciphertext = agj.encrypt(baos.toByteArray(), null);

      return ciphertext;
    }
  }
  
  private static Signature sign(Context ctx, Event evt) throws IllegalAccessException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
    return Signature.signServer(ctx, evt, ctx.keyFile.serverPrivateKey);
  }
  
  public static void asdf() {throw new RuntimeException();}
}