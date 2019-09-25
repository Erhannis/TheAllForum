/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.server;

import com.erhannis.theallforum.BaseMain;
import com.erhannis.theallforum.Constants;
import com.erhannis.theallforum.Context;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.config.TinkConfig;
import com.google.crypto.tink.subtle.AesGcmJce;
import com.google.gson.Gson;
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
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.AEADBadTagException;
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

    Context ctx = getBaseContext();
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
      Event event = ctx.om.readValue(req.body(), Event.class);
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
      String result = ctx.om.writeValueAsString(new ArrayList<Event>(es){});
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
      return ctx.om.writeValueAsString(createUserTest(ctx, username, password));
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
    //TODO Should probably hash the password before sending it.  Maybe one of those nifty challenge response things?
    Spark.get(prefix + "/login/:username/:password", (req, res) -> {
      String username = req.params("username");
      String password = req.params("password");
      if (username == null || password == null) {
        res.status(400);
        return "Error; username or password missing";
      }
      EntityManager em = ctx.factory.createEntityManager();
      //TODO Deal with username changes, etc.
      //TODO In fact, deal with the general concept of searching the most up-to-date version of stuff
      List<UserCreated> users = em.createQuery("SELECT uc from UserCreated uc where uc.username = :username", UserCreated.class)
              .setParameter("username", username)
              .getResultList();
      em.close();
      for (UserCreated user : users) {
        if (doesDecryptKey(user.privateKeyEncrypted, password)) {
          return ctx.om.writeValueAsString(user.handle); //TODO Return Handle, or (Handle).value, or UserEvent?
        }
      }
      res.status(401);
      return ctx.om.writeValueAsString("Invalid login");
    });
    
    setUpTestEndpoints(ctx);
  }
  
  private static void setUpTestEndpoints(Context ctx) {
    String prefix = "/api/test";
    get(prefix + "/gson", (req, res) -> {
      res.type("application/json");
      return "[{\"description\":\"AUTOGENERATED SERVER USER\",\"publicKey\":{\"n\":27489270369487691404789322813019840188581131678243816819476252556341188232597971693132409850420273121652316470283580958199475325354985996208582522146461761230388834162617803020842342164172332552483032174637704395101880269869423117272912881030149768328590454942277545906598622344785801838279072581812985869064748035484689710862000577074620427556173308827627523328306843145266579208482699314073077445017916181180806844234001236719580111566438948930089466007079606989402731462495797694463761691440785379165415119206769873485245132033732573774697686221470316370099329914875852439081256590606596271255570705905352392015007,\"e\":65537,\"algid\":{\"algid\":{\"encoding\":[42,-122,72,-122,-9,13,1,1,1],\"componentLen\":-1},\"constructedFromDer\":true},\"key\":[48,-126,1,10,2,-126,1,1,0,-39,-63,-56,-111,-87,108,-8,43,103,90,46,21,-97,10,-46,-49,56,68,-92,3,14,113,83,75,90,-104,100,27,37,-20,79,122,62,-74,82,-42,112,-30,1,78,111,22,30,22,-63,-4,5,30,-94,6,58,-108,11,-123,-78,-88,96,47,65,-25,6,46,10,71,50,67,-120,24,-119,75,-114,58,-27,-95,29,104,-74,-111,58,-59,50,48,-12,-80,72,-81,-21,0,-103,-38,-75,104,-73,-78,-80,54,105,-77,20,90,-92,7,-24,-67,-86,102,-29,7,64,-70,97,-41,32,-69,-95,-17,43,88,-69,23,53,-109,122,66,67,-56,85,33,-124,113,37,75,89,72,-34,18,77,-101,50,102,-40,27,90,-68,-40,94,53,15,-60,52,-55,-61,-126,21,80,21,-108,-41,64,92,89,-112,-62,1,55,90,-49,-18,-32,-50,-34,20,-96,8,-115,91,-7,56,-33,92,111,67,80,1,-117,-51,-38,125,83,-10,27,113,80,75,125,72,30,-10,-39,-91,84,41,96,21,83,-47,-11,118,47,-128,113,47,-115,117,54,71,-86,-77,-47,-124,30,-51,-22,114,-14,88,16,70,50,-56,-122,90,110,14,50,-89,5,-124,-73,13,37,46,-55,-24,-14,-86,22,-22,-4,20,46,-78,19,103,124,-97,2,3,1,0,1],\"unusedBits\":0,\"bitStringKey\":{\"repn\":[48,-126,1,10,2,-126,1,1,0,-39,-63,-56,-111,-87,108,-8,43,103,90,46,21,-97,10,-46,-49,56,68,-92,3,14,113,83,75,90,-104,100,27,37,-20,79,122,62,-74,82,-42,112,-30,1,78,111,22,30,22,-63,-4,5,30,-94,6,58,-108,11,-123,-78,-88,96,47,65,-25,6,46,10,71,50,67,-120,24,-119,75,-114,58,-27,-95,29,104,-74,-111,58,-59,50,48,-12,-80,72,-81,-21,0,-103,-38,-75,104,-73,-78,-80,54,105,-77,20,90,-92,7,-24,-67,-86,102,-29,7,64,-70,97,-41,32,-69,-95,-17,43,88,-69,23,53,-109,122,66,67,-56,85,33,-124,113,37,75,89,72,-34,18,77,-101,50,102,-40,27,90,-68,-40,94,53,15,-60,52,-55,-61,-126,21,80,21,-108,-41,64,92,89,-112,-62,1,55,90,-49,-18,-32,-50,-34,20,-96,8,-115,91,-7,56,-33,92,111,67,80,1,-117,-51,-38,125,83,-10,27,113,80,75,125,72,30,-10,-39,-91,84,41,96,21,83,-47,-11,118,47,-128,113,47,-115,117,54,71,-86,-77,-47,-124,30,-51,-22,114,-14,88,16,70,50,-56,-122,90,110,14,50,-89,5,-124,-73,13,37,46,-55,-24,-14,-86,22,-22,-4,20,46,-78,19,103,124,-97,2,3,1,0,1],\"length\":2160},\"encodedKey\":[48,-126,1,34,48,13,6,9,42,-122,72,-122,-9,13,1,1,1,5,0,3,-126,1,15,0,48,-126,1,10,2,-126,1,1,0,-39,-63,-56,-111,-87,108,-8,43,103,90,46,21,-97,10,-46,-49,56,68,-92,3,14,113,83,75,90,-104,100,27,37,-20,79,122,62,-74,82,-42,112,-30,1,78,111,22,30,22,-63,-4,5,30,-94,6,58,-108,11,-123,-78,-88,96,47,65,-25,6,46,10,71,50,67,-120,24,-119,75,-114,58,-27,-95,29,104,-74,-111,58,-59,50,48,-12,-80,72,-81,-21,0,-103,-38,-75,104,-73,-78,-80,54,105,-77,20,90,-92,7,-24,-67,-86,102,-29,7,64,-70,97,-41,32,-69,-95,-17,43,88,-69,23,53,-109,122,66,67,-56,85,33,-124,113,37,75,89,72,-34,18,77,-101,50,102,-40,27,90,-68,-40,94,53,15,-60,52,-55,-61,-126,21,80,21,-108,-41,64,92,89,-112,-62,1,55,90,-49,-18,-32,-50,-34,20,-96,8,-115,91,-7,56,-33,92,111,67,80,1,-117,-51,-38,125,83,-10,27,113,80,75,125,72,30,-10,-39,-91,84,41,96,21,83,-47,-11,118,47,-128,113,47,-115,117,54,71,-86,-77,-47,-124,30,-51,-22,114,-14,88,16,70,50,-56,-122,90,110,14,50,-89,5,-124,-73,13,37,46,-55,-24,-14,-86,22,-22,-4,20,46,-78,19,103,124,-97,2,3,1,0,1]},\"handle\":{\"value\":\"12ce1c87-a34c-3aa4-a5e4-6837fcad0910_897886fd-55a2-3173-be27-053aac3929f3\"},\"parents\":[],\"userTimestamp\":1569123637443,\"serverTimestamp\":1569123637443,\"user\":{\"value\":\"12ce1c87-a34c-3aa4-a5e4-6837fcad0910_897886fd-55a2-3173-be27-053aac3929f3\"},\"server\":{\"value\":\"12ce1c87-a34c-3aa4-a5e4-6837fcad0910_897886fd-55a2-3173-be27-053aac3929f3\"},\"userSignature\":{\"value\":[92,28,-64,-13,-128,103,-116,-64,120,123,68,-106,-87,45,38,74,-34,-110,99,-98,-117,62,85,-31,7,16,-9,113,34,11,-33,5,-79,83,13,-122,44,-43,-92,77,-16,-81,46,-67,68,60,18,23,-71,26,-63,69,39,-51,104,97,-117,20,59,66,14,-36,-109,-61,126,61,-110,-25,-55,-52,-44,8,-36,-75,10,32,-24,-87,88,-10,111,-24,20,-65,36,-56,-54,-91,66,49,97,68,-29,-95,96,15,-60,-4,66,116,-95,57,-62,109,111,104,-99,62,-89,118,85,-23,-94,20,53,67,78,8,117,82,-86,45,59,61,-92,70,-111,47,-73,-39,57,36,-115,1,93,-68,-107,-18,-64,47,1,-36,24,-55,7,-125,7,122,72,5,48,-2,-115,65,123,54,-40,38,1,61,11,-64,-103,43,-5,-78,2,40,27,83,127,-21,119,41,112,30,94,-108,41,80,-114,-112,-102,102,-91,83,84,8,84,14,114,69,22,106,35,-80,-73,72,86,123,-12,71,64,93,-103,-65,-76,25,24,-42,50,101,101,-114,-22,82,110,-81,-112,-36,64,58,-13,-15,-27,42,-18,100,120,35,90,-20,71,68,-58,47,84,100,-104,-76,-119,62,68,-77,-22,99,3,61,-70,86,-19,101,35,6,97,-67]},\"serverSignature\":{\"value\":[79,-114,-13,-99,122,-99,-48,-114,62,-70,37,31,6,-4,-61,-32,58,57,11,72,-91,-25,109,-76,-40,-10,107,35,90,72,41,17,-70,-110,92,-63,-1,12,-40,-117,-92,96,-17,-25,-91,7,61,51,-58,11,-84,8,-51,-116,-78,-124,16,-123,-77,-96,38,51,99,-101,35,94,8,32,49,73,-85,51,7,-42,25,90,-57,-42,24,-69,37,36,30,44,-51,-27,-64,51,63,-97,-61,-114,-45,-79,-50,-93,98,-13,-40,-15,48,50,24,10,-61,43,-70,-33,43,-108,-113,-23,-117,-78,-15,-2,12,-61,121,-114,78,-22,113,93,1,-117,-39,100,-63,-65,87,-66,-78,-120,-45,-71,-62,37,65,-119,-107,-23,25,104,71,-63,-114,-53,49,-40,-65,18,-74,32,-109,-86,-113,-58,98,99,68,-128,-91,2,30,126,78,-5,-38,-97,-109,107,20,102,124,-55,-128,-46,-70,-34,-39,-70,101,68,-11,-110,94,-24,-78,110,-103,-82,77,-13,103,76,-94,51,6,-73,108,-22,-32,115,21,-49,-13,-118,-128,-80,-84,-3,114,58,-24,116,-105,123,89,-33,102,-59,46,53,116,30,-20,-50,89,36,26,100,-112,40,-27,-121,6,81,57,-4,-88,93,73,-126,13,-100,51,-74,112,-1,60,-66,88,121,2,-75]}},{\"username\":\"erhannis\",\"email\":\"email@internet.com\",\"description\":\"You\\u0027re a kitty!\",\"publicKey\":{\"n\":23473564557800745591339816633603591120915794928719694563105325830594901896186506580173677535891868286487292903325085595329920573732291295140373213640999189234344247198712405161142504363404637531641868345572420429558010551328243576435580357155785878714231703472796383892717747813203833050116979799890471093537973275593775297585622861677036502968987099591837038968999470425625512301178733876085204542848129428445520556278345039428927125930611423618531235505287316827320929932081106157992129610542488042874917037077967314084965493380151928279587376480805628495232518177176323194499313326743156734150501494761817052161299,\"e\":65537,\"algid\":{\"algid\":{\"encoding\":[42,-122,72,-122,-9,13,1,1,1],\"componentLen\":-1},\"constructedFromDer\":true},\"key\":[48,-126,1,10,2,-126,1,1,0,-71,-14,74,126,44,-91,123,-92,-96,46,93,-55,50,-54,-38,-110,65,-92,56,-88,-72,-108,126,-10,28,39,116,114,-91,1,-53,74,69,4,121,12,-117,32,119,52,110,79,123,-22,25,-127,-67,-71,103,74,-11,-25,55,97,-94,45,-55,-25,113,65,95,-5,-98,36,29,-127,30,82,3,123,-86,105,118,78,53,115,51,41,109,7,-41,116,22,12,-4,23,19,-46,-96,-7,1,28,84,-20,-19,72,-74,-125,82,57,-103,52,7,-123,109,115,-67,-30,27,104,70,110,-37,92,-78,74,101,31,43,1,115,49,8,28,89,-60,-69,-59,-22,93,22,95,48,-6,-76,95,-83,-70,122,53,53,38,122,-42,-36,-33,88,-68,69,-113,-32,102,2,73,18,67,-110,0,18,-123,127,-49,94,-120,-126,-75,33,51,51,-61,-73,57,57,58,-35,121,-92,18,69,-26,106,-39,-26,106,94,-96,-13,-114,-6,56,41,-78,-36,15,-38,59,-98,-71,67,86,112,50,-93,-49,-28,-33,63,5,-79,-60,-4,-7,-111,41,-72,41,93,-51,-21,-62,-40,60,-90,-83,-17,-94,-21,75,-74,19,-67,82,106,-117,-73,-112,-21,-69,37,46,18,78,27,-2,10,40,73,-17,-122,-55,4,-28,24,70,37,19,2,3,1,0,1],\"unusedBits\":0,\"bitStringKey\":{\"repn\":[48,-126,1,10,2,-126,1,1,0,-71,-14,74,126,44,-91,123,-92,-96,46,93,-55,50,-54,-38,-110,65,-92,56,-88,-72,-108,126,-10,28,39,116,114,-91,1,-53,74,69,4,121,12,-117,32,119,52,110,79,123,-22,25,-127,-67,-71,103,74,-11,-25,55,97,-94,45,-55,-25,113,65,95,-5,-98,36,29,-127,30,82,3,123,-86,105,118,78,53,115,51,41,109,7,-41,116,22,12,-4,23,19,-46,-96,-7,1,28,84,-20,-19,72,-74,-125,82,57,-103,52,7,-123,109,115,-67,-30,27,104,70,110,-37,92,-78,74,101,31,43,1,115,49,8,28,89,-60,-69,-59,-22,93,22,95,48,-6,-76,95,-83,-70,122,53,53,38,122,-42,-36,-33,88,-68,69,-113,-32,102,2,73,18,67,-110,0,18,-123,127,-49,94,-120,-126,-75,33,51,51,-61,-73,57,57,58,-35,121,-92,18,69,-26,106,-39,-26,106,94,-96,-13,-114,-6,56,41,-78,-36,15,-38,59,-98,-71,67,86,112,50,-93,-49,-28,-33,63,5,-79,-60,-4,-7,-111,41,-72,41,93,-51,-21,-62,-40,60,-90,-83,-17,-94,-21,75,-74,19,-67,82,106,-117,-73,-112,-21,-69,37,46,18,78,27,-2,10,40,73,-17,-122,-55,4,-28,24,70,37,19,2,3,1,0,1],\"length\":2160},\"encodedKey\":[48,-126,1,34,48,13,6,9,42,-122,72,-122,-9,13,1,1,1,5,0,3,-126,1,15,0,48,-126,1,10,2,-126,1,1,0,-71,-14,74,126,44,-91,123,-92,-96,46,93,-55,50,-54,-38,-110,65,-92,56,-88,-72,-108,126,-10,28,39,116,114,-91,1,-53,74,69,4,121,12,-117,32,119,52,110,79,123,-22,25,-127,-67,-71,103,74,-11,-25,55,97,-94,45,-55,-25,113,65,95,-5,-98,36,29,-127,30,82,3,123,-86,105,118,78,53,115,51,41,109,7,-41,116,22,12,-4,23,19,-46,-96,-7,1,28,84,-20,-19,72,-74,-125,82,57,-103,52,7,-123,109,115,-67,-30,27,104,70,110,-37,92,-78,74,101,31,43,1,115,49,8,28,89,-60,-69,-59,-22,93,22,95,48,-6,-76,95,-83,-70,122,53,53,38,122,-42,-36,-33,88,-68,69,-113,-32,102,2,73,18,67,-110,0,18,-123,127,-49,94,-120,-126,-75,33,51,51,-61,-73,57,57,58,-35,121,-92,18,69,-26,106,-39,-26,106,94,-96,-13,-114,-6,56,41,-78,-36,15,-38,59,-98,-71,67,86,112,50,-93,-49,-28,-33,63,5,-79,-60,-4,-7,-111,41,-72,41,93,-51,-21,-62,-40,60,-90,-83,-17,-94,-21,75,-74,19,-67,82,106,-117,-73,-112,-21,-69,37,46,18,78,27,-2,10,40,73,-17,-122,-55,4,-28,24,70,37,19,2,3,1,0,1]},\"privateKeyEncrypted\":[-71,85,30,-59,-50,-81,-22,-123,69,89,-127,113,104,-34,6,5,-49,115,24,69,94,-54,31,-94,-51,-13,-17,-82,-27,-55,81,15,12,107,41,-18,43,80,80,119,-92,111,-21,97,-19,-112,96,59,2,-51,-55,110,87,-9,-10,100,-77,59,-21,30,58,85,-56,46,97,-37,-41,1,-15,95,-29,76,58,-88,95,-66,40,-37,-54,121,-64,-6,126,50,-119,-14,28,-10,99,16,-32,-57,118,18,-63,41,57,39,21,-124,121,35,-112,113,-34,86,42,-2,-15,127,-95,-113,-39,28,-38,-104,116,-35,18,117,30,-28,-17,2,-94,80,-68,-123,81,-91,41,-10,115,119,-39,-117,85,-37,79,42,-72,111,-43,-64,70,14,19,67,-80,115,62,-84,123,56,82,-1,38,20,-48,80,-41,93,-74,2,29,-30,-109,-11,-125,-46,106,-82,-109,-51,54,-67,-18,-79,-108,-5,118,10,52,-110,65,70,124,-80,97,32,82,-43,106,-83,8,62,1,-88,-109,-38,-118,122,110,-5,-49,57,108,39,-76,-45,74,95,-63,98,-90,72,-86,53,-3,93,29,107,15,-124,-106,-64,-103,-24,113,28,43,43,33,-5,-86,84,56,83,119,102,-54,111,98,-9,51,-25,50,55,19,46,-124,108,9,-45,-95,-65,84,-8,-72,45,-91,62,24,78,-72,63,57,-111,-88,-107,43,-97,110,31,-16,51,109,107,15,-1,98,104,26,89,51,-53,-115,58,-33,37,63,-32,17,-15,94,-4,3,-97,18,-105,-98,80,70,-90,5,97,-31,40,107,50,74,49,-22,1,-79,-13,14,24,-14,111,-43,44,38,-105,18,-128,-50,-101,73,-22,-87,-3,45,54,-6,-39,15,-87,22,86,-109,67,-35,39,-1,24,-90,91,-63,78,35,-16,-91,-123,-20,-29,97,126,105,112,-8,-35,71,5,-37,-24,30,-71,-96,124,-65,97,32,60,-91,122,-56,42,63,-58,71,85,5,-92,87,109,-31,-118,69,-103,66,-106,-122,68,18,64,109,109,6,-120,1,-60,-14,105,-69,59,-35,-79,98,-109,30,26,-2,84,-91,-113,60,-87,-80,103,-60,124,-108,-119,-74,-23,-39,106,120,-21,29,59,-30,24,-12,0,-20,-33,-66,-117,-15,31,-49,-120,85,-89,69,25,11,125,106,31,-128,76,-53,-90,6,28,-113,94,-85,-120,8,-53,9,-78,-112,-87,41,33,40,-13,72,-78,-52,-25,68,114,60,57,101,64,-92,62,51,-108,11,29,-54,121,-107,-29,-30,-60,73,-106,-12,-24,115,-4,-28,-32,125,-57,33,121,-50,67,94,85,87,-7,27,-126,88,4,40,-102,4,-14,-97,-17,-103,-17,-124,-98,68,-45,83,48,107,45,2,-128,-20,9,-88,70,80,125,78,75,-98,-86,8,3,63,7,-121,80,82,-92,-36,-16,73,-32,51,-49,-126,-63,-73,-46,98,124,-43,14,106,107,-85,-33,-32,-109,2,108,-110,-37,-122,-91,-24,-60,-7,122,121,66,95,87,-30,17,-116,-117,-81,-107,-6,101,116,41,-53,-118,101,-89,102,-45,57,-42,-43,-7,39,-2,88,2,-65,44,-36,56,105,-9,91,-70,23,-29,-10,55,-108,-6,116,-44,35,6,-106,1,-56,-4,114,35,-103,-91,52,74,80,22,36,110,-49,-52,-119,-12,-43,-9,19,8,-43,-3,-114,-62,-32,122,13,118,72,83,-3,-128,-14,-12,115,101,-101,-18,-104,42,95,-53,-50,10,-33,83,106,-59,-84,-61,5,-108,-41,105,3,68,124,25,-13,-54,84,-98,-103,126,-86,69,-107,91,-107,98,-44,109,-56,-34,-3,13,-55,105,-110,108,-120,-62,66,-95,-69,107,-23,-81,38,-102,108,127,-61,33,-108,-89,6,105,-18,-113,47,93,78,-127,108,-117,11,-85,112,91,-87,-126,31,-15,126,2,-40,107,-57,-106,-101,84,-28,-31,115,-90,97,12,115,76,-37,44,-86,126,13,28,111,-53,6,-50,-106,-15,12,67,-70,-19,-107,55,-83,64,36,-14,-125,-85,-9,29,-53,-76,122,-128,-42,-28,89,-119,24,45,73,-19,13,71,12,63,-22,-1,84,-75,123,-28,77,-92,90,-88,-119,57,36,76,-106,-52,52,-98,104,30,21,-9,86,-105,111,-18,-33,-33,-48,-36,-86,50,66,99,-107,12,-29,41,7,115,-42,-3,-74,1,-106,-56,33,-25,-128,-45,118,-36,51,44,4,37,21,-54,26,-13,-55,-52,46,120,15,-128,36,25,35,-123,-92,-63,15,-122,65,100,-8,-124,26,-68,44,-83,31,36,117,55,-26,-71,-39,-78,66,-37,37,49,97,22,116,-10,-47,62,123,-115,-89,35,-17,74,-96,-42,-23,85,-46,-55,-84,-105,67,62,81,-22,64,-115,-118,-32,88,20,48,-12,127,-95,-90,76,-48,61,-86,6,2,121,78,-1,36,-26,-85,-113,-95,75,-63,-104,-101,82,74,17,-21,-45,-120,-90,64,-3,11,7,111,-29,-11,-18,8,90,58,46,-25,2,48,76,-28,-109,123,66,-128,88,80,-104,92,-20,89,-98,89,-29,84,93,75,-111,-24,71,-31,-32,33,-106,119,-106,-128,95,-10,22,36,8,87,-49,76,-106,117,58,-62,-98,-41,-111,72,14,21,-59,78,34,-91,53,23,42,-8,85,70,25,-112,53,90,-7,-70,124,-31,36,-23,-73,-111,-113,116,-31,-58,-51,-108,23,-89,-76,-40,-62,103,-62,107,18,-87,-69,-11,-100,68,83,-8,44,-125,114,58,118,-84,-23,17,-15,74,-12,-87,120,-112,93,96,123,-63,58,-118,-45,42,99,-125,37,57,-83,65,-64,87,-89,22,-54,-122,104,116,25,55,-93,-73,28,-44,103,-38,113,59,80,-113,-49,-107,112,-53,-34,-53,-16,-13,-27,-106,23,-84,-27,-59,19,7,33,18,63,123,-128,61,70,-91,51,-52,-75,-51,107,127,57,110,-93,-99,113,-79,58,94,-125,43,-120,124,-107,94,98,74,-86,13,-43,19,65,13,117,-89,59,6,-41,72,-76,109,-46,-102,-89,120,84,72,53,-124,-65,-8,-94,89,0,118,29,-76,96,103,89,-45,11,117,-58,-117,119,39,47,-79,117,115,91,-101,11,18,23,119,118,-5,-123,60,-77,89,100,49,63,-31,-49,125,30,-119,97,-14,-43,-58,65,-94,60,58,-96,56,111,72,-88,-81,87,-112,-63,121,-81,-101,-61,72,-83,105,66,83,24,-28,83,-95,-116,-88,92,83,12,-29,-75,-27,-37,-39,-120,-104,-105,110,101,-79,54,-3,-108,-3,81,-111,96,64,120,122,74,-59,32,-24,-41,114,11,66,114,26,63,17,-77,-83,-29,27,123,-32,90,118,71,63,-83,-49,2,-54,-31,124,-106,-95,-83,124,-20,-6,93,6,-50,24,75,-87,16,83,106,-76,84,-22,74,-67,100,-124,-98,-33,-101,117,-75,82,0,41,77,-5,-73,-116,50,124,-111,35,70,-2,-36,13,-94,16,-91,-113,100,28,-73,70,99,-110,-127,47,4,20,103,10,-64,-116,-17,-1,43,-88,126,-80,62,3,56,65,86,40,-83,96,61,30,-90,-63,-41,-48,59,86,71,-22,2,103,-7,110,34,-37,63,-122,-67,-99,24,119,61,120,-17,35,-4,-97,20,67,-69,36,-13,-127,28,-28,-59,45,20,119,38,118,62,3,-120,-13,69,-21,-22,-28,14,-109,99,24,-87,-109,-122,-69,12,74,126,81,-58,110,-91,31,-48,-17,-4,124,84,-56,31,-113,-31,-13,-77,69,-122,29,-93,-8,22,50,-110,-41,-124,-16,46,68,-127,-79,-101,42,32,56,-61,-92,-10,-71,-100,-122,-97,50,-16,89,49,-106,7,-55,-77,-93,-14,39,-74,21,-88,-78,26,97,119,35,-85,-20,81,-45,92,-15],\"handle\":{\"value\":\"399694ae-68f4-3545-87df-3300b00f4144_00158bdb-a57e-3e95-94e3-1162a98450fc\"},\"parents\":[],\"userTimestamp\":1569123659474,\"serverTimestamp\":1569123659474,\"user\":{\"value\":\"399694ae-68f4-3545-87df-3300b00f4144_00158bdb-a57e-3e95-94e3-1162a98450fc\"},\"server\":{\"value\":\"12ce1c87-a34c-3aa4-a5e4-6837fcad0910_897886fd-55a2-3173-be27-053aac3929f3\"},\"userSignature\":{\"value\":[13,63,-11,-56,111,-124,-80,-77,-102,-117,49,49,-42,83,-87,76,62,-13,103,96,-23,-15,105,-110,-94,123,-11,-88,14,-98,-120,87,-67,-107,-55,37,122,-22,-46,36,-59,125,-115,59,3,20,81,-93,2,-51,125,-80,7,-29,-48,102,-109,59,-111,-20,69,10,50,-15,-38,-68,72,73,-6,-84,-21,-38,-108,-108,95,-69,119,80,-62,-71,69,-105,47,14,48,-44,86,-104,-24,-74,99,-127,82,-66,-56,113,-109,-62,109,-21,-107,-57,-126,-80,-88,109,70,-9,68,33,-39,75,-127,27,-52,-11,74,51,7,41,-120,56,69,52,90,-116,43,19,-27,-48,-84,58,22,99,83,-98,18,-122,94,-111,4,-128,-68,119,-35,-72,-22,-114,-61,53,7,-103,-20,55,28,90,9,-32,17,38,-3,88,92,-70,42,108,8,101,66,62,-82,-119,119,-106,-117,-112,-48,-101,-28,-25,-81,-40,-87,68,-16,6,-59,-88,-4,-10,80,30,20,47,38,107,-13,13,84,-20,57,116,-8,-6,-19,-45,37,89,-38,-87,22,-13,117,81,-127,-122,-20,32,-60,103,-96,93,-95,-50,-104,31,92,-91,104,-40,-85,-80,57,-27,-117,-83,-97,-56,-85,-34,-36,-79,48,75,-55,-64,-107,-18,56,-52,-39,-104,53,25,-10,-60]},\"serverSignature\":{\"value\":[-119,-120,-50,-119,79,-18,22,-4,89,40,20,46,111,121,127,67,-86,45,126,17,-18,-109,3,-32,115,18,58,103,-53,65,-81,19,27,-107,-19,109,90,11,119,-27,111,-19,75,105,-65,42,-71,-24,33,-24,-48,84,64,126,-56,-25,2,-40,-3,79,-84,-22,81,-87,86,-117,25,3,-6,-111,-37,-84,50,48,11,83,100,56,55,-95,103,-75,43,-25,-103,10,19,30,-23,-46,20,-20,38,-31,19,53,-62,-31,26,111,114,75,-95,98,116,55,25,-19,89,90,-119,51,-47,114,64,105,-1,-88,-92,-107,-9,-18,89,-41,117,19,80,-2,-46,102,-8,39,-103,-32,-47,69,25,-83,-53,79,-66,85,-56,88,-105,-20,27,-111,0,-58,-56,93,14,127,79,99,80,11,80,27,-16,-106,-46,-10,-32,6,74,-28,62,67,18,92,105,124,-35,33,25,105,-18,36,9,77,-40,4,59,32,-20,74,121,-123,96,36,30,58,-17,37,-70,-56,-95,-68,50,-1,-52,57,-98,13,8,69,5,40,37,48,115,-124,-104,70,34,51,-60,16,16,-36,-91,38,-55,27,-9,23,-91,-78,54,19,106,-7,-54,-40,95,98,92,31,13,103,97,-86,21,81,98,87,-98,98,28,90,33,-54,-7,-117]}}]";
    });
    get(prefix + "/jackson", (req, res) -> {
      res.type("application/json");
      return "[{\"@type\":\"UserCreated\",\"handle\":{\"value\":\"12ce1c87-a34c-3aa4-a5e4-6837fcad0910_897886fd-55a2-3173-be27-053aac3929f3\"},\"parents\":[],\"userTimestamp\":1569123637443,\"serverTimestamp\":1569123637443,\"user\":{\"value\":\"12ce1c87-a34c-3aa4-a5e4-6837fcad0910_897886fd-55a2-3173-be27-053aac3929f3\"},\"server\":{\"value\":\"12ce1c87-a34c-3aa4-a5e4-6837fcad0910_897886fd-55a2-3173-be27-053aac3929f3\"},\"userSignature\":{\"value\":\"XBzA84BnjMB4e0SWqS0mSt6SY56LPlXhBxD3cSIL3wWxUw2GLNWkTfCvLr1EPBIXuRrBRSfNaGGLFDtCDtyTw349kufJzNQI3LUKIOipWPZv6BS/JMjKpUIxYUTjoWAPxPxCdKE5wm1vaJ0+p3ZV6aIUNUNOCHVSqi07PaRGkS+32TkkjQFdvJXuwC8B3BjJB4MHekgFMP6NQXs22CYBPQvAmSv7sgIoG1N/63cpcB5elClQjpCaZqVTVAhUDnJFFmojsLdIVnv0R0Bdmb+0GRjWMmVljupSbq+Q3EA68/HlKu5keCNa7EdExi9UZJi0iT5Es+pjAz26Vu1lIwZhvQ==\"},\"serverSignature\":{\"value\":\"T47znXqd0I4+uiUfBvzD4Do5C0il52202PZrI1pIKRG6klzB/wzYi6Rg7+elBz0zxgusCM2MsoQQhbOgJjNjmyNeCCAxSaszB9YZWsfWGLslJB4szeXAMz+fw47Tsc6jYvPY8TAyGArDK7rfK5SP6Yuy8f4Mw3mOTupxXQGL2WTBv1e+sojTucIlQYmV6RloR8GOyzHYvxK2IJOqj8ZiY0SApQIefk772p+TaxRmfMmA0rre2bplRPWSXuiybpmuTfNnTKIzBrds6uBzFc/zioCwrP1yOuh0l3tZ32bFLjV0HuzOWSQaZJAo5YcGUTn8qF1Jgg2cM7Zw/zy+WHkCtQ==\"},\"username\":null,\"email\":null,\"avatarUrl\":null,\"description\":\"AUTOGENERATED SERVER USER\",\"publicKey\":{\"modulus\":27489270369487691404789322813019840188581131678243816819476252556341188232597971693132409850420273121652316470283580958199475325354985996208582522146461761230388834162617803020842342164172332552483032174637704395101880269869423117272912881030149768328590454942277545906598622344785801838279072581812985869064748035484689710862000577074620427556173308827627523328306843145266579208482699314073077445017916181180806844234001236719580111566438948930089466007079606989402731462495797694463761691440785379165415119206769873485245132033732573774697686221470316370099329914875852439081256590606596271255570705905352392015007,\"publicExponent\":65537,\"algorithm\":\"RSA\",\"format\":\"X.509\",\"encoded\":\"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2cHIkals+CtnWi4VnwrSzzhEpAMOcVNLWphkGyXsT3o+tlLWcOIBTm8WHhbB/AUeogY6lAuFsqhgL0HnBi4KRzJDiBiJS4465aEdaLaROsUyMPSwSK/rAJnatWi3srA2abMUWqQH6L2qZuMHQLph1yC7oe8rWLsXNZN6QkPIVSGEcSVLWUjeEk2bMmbYG1q82F41D8Q0ycOCFVAVlNdAXFmQwgE3Ws/u4M7eFKAIjVv5ON9cb0NQAYvN2n1T9htxUEt9SB722aVUKWAVU9H1di+AcS+NdTZHqrPRhB7N6nLyWBBGMsiGWm4OMqcFhLcNJS7J6PKqFur8FC6yE2d8nwIDAQAB\",\"algorithmId\":{\"oid\":{},\"encodedParams\":null,\"name\":\"RSA\",\"parameters\":null},\"encodedInternal\":\"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2cHIkals+CtnWi4VnwrSzzhEpAMOcVNLWphkGyXsT3o+tlLWcOIBTm8WHhbB/AUeogY6lAuFsqhgL0HnBi4KRzJDiBiJS4465aEdaLaROsUyMPSwSK/rAJnatWi3srA2abMUWqQH6L2qZuMHQLph1yC7oe8rWLsXNZN6QkPIVSGEcSVLWUjeEk2bMmbYG1q82F41D8Q0ycOCFVAVlNdAXFmQwgE3Ws/u4M7eFKAIjVv5ON9cb0NQAYvN2n1T9htxUEt9SB722aVUKWAVU9H1di+AcS+NdTZHqrPRhB7N6nLyWBBGMsiGWm4OMqcFhLcNJS7J6PKqFur8FC6yE2d8nwIDAQAB\"},\"privateKeyEncrypted\":null},{\"@type\":\"UserCreated\",\"handle\":{\"value\":\"399694ae-68f4-3545-87df-3300b00f4144_00158bdb-a57e-3e95-94e3-1162a98450fc\"},\"parents\":[],\"userTimestamp\":1569123659474,\"serverTimestamp\":1569123659474,\"user\":{\"value\":\"399694ae-68f4-3545-87df-3300b00f4144_00158bdb-a57e-3e95-94e3-1162a98450fc\"},\"server\":{\"value\":\"12ce1c87-a34c-3aa4-a5e4-6837fcad0910_897886fd-55a2-3173-be27-053aac3929f3\"},\"userSignature\":{\"value\":\"DT/1yG+EsLOaizEx1lOpTD7zZ2Dp8WmSonv1qA6eiFe9lckleurSJMV9jTsDFFGjAs19sAfj0GaTO5HsRQoy8dq8SEn6rOvalJRfu3dQwrlFly8OMNRWmOi2Y4FSvshxk8Jt65XHgrCobUb3RCHZS4EbzPVKMwcpiDhFNFqMKxPl0Kw6FmNTnhKGXpEEgLx33bjqjsM1B5nsNxxaCeARJv1YXLoqbAhlQj6uiXeWi5DQm+Tnr9ipRPAGxaj89lAeFC8ma/MNVOw5dPj67dMlWdqpFvN1UYGG7CDEZ6Bdoc6YH1ylaNirsDnli62fyKve3LEwS8nAle44zNmYNRn2xA==\"},\"serverSignature\":{\"value\":\"iYjOiU/uFvxZKBQub3l/Q6otfhHukwPgcxI6Z8tBrxMble1tWgt35W/tS2m/KrnoIejQVEB+yOcC2P1PrOpRqVaLGQP6kdusMjALU2Q4N6FntSvnmQoTHunSFOwm4RM1wuEab3JLoWJ0NxntWVqJM9FyQGn/qKSV9+5Z13UTUP7SZvgnmeDRRRmty0++VchYl+wbkQDGyF0Of09jUAtQG/CW0vbgBkrkPkMSXGl83SEZae4kCU3YBDsg7Ep5hWAkHjrvJbrIobwy/8w5ng0IRQUoJTBzhJhGIjPEEBDcpSbJG/cXpbI2E2r5ythfYlwfDWdhqhVRYleeYhxaIcr5iw==\"},\"username\":\"erhannis\",\"email\":\"email@internet.com\",\"avatarUrl\":null,\"description\":\"You're a kitty!\",\"publicKey\":{\"modulus\":23473564557800745591339816633603591120915794928719694563105325830594901896186506580173677535891868286487292903325085595329920573732291295140373213640999189234344247198712405161142504363404637531641868345572420429558010551328243576435580357155785878714231703472796383892717747813203833050116979799890471093537973275593775297585622861677036502968987099591837038968999470425625512301178733876085204542848129428445520556278345039428927125930611423618531235505287316827320929932081106157992129610542488042874917037077967314084965493380151928279587376480805628495232518177176323194499313326743156734150501494761817052161299,\"publicExponent\":65537,\"algorithm\":\"RSA\",\"format\":\"X.509\",\"encoded\":\"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAufJKfiyle6SgLl3JMsrakkGkOKi4lH72HCd0cqUBy0pFBHkMiyB3NG5Pe+oZgb25Z0r15zdhoi3J53FBX/ueJB2BHlIDe6ppdk41czMpbQfXdBYM/BcT0qD5ARxU7O1ItoNSOZk0B4Vtc73iG2hGbttcskplHysBczEIHFnEu8XqXRZfMPq0X626ejU1JnrW3N9YvEWP4GYCSRJDkgAShX/PXoiCtSEzM8O3OTk63XmkEkXmatnmal6g8476OCmy3A/aO565Q1ZwMqPP5N8/BbHE/PmRKbgpXc3rwtg8pq3voutLthO9UmqLt5DruyUuEk4b/gooSe+GyQTkGEYlEwIDAQAB\",\"algorithmId\":{\"oid\":{},\"encodedParams\":null,\"name\":\"RSA\",\"parameters\":null},\"encodedInternal\":\"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAufJKfiyle6SgLl3JMsrakkGkOKi4lH72HCd0cqUBy0pFBHkMiyB3NG5Pe+oZgb25Z0r15zdhoi3J53FBX/ueJB2BHlIDe6ppdk41czMpbQfXdBYM/BcT0qD5ARxU7O1ItoNSOZk0B4Vtc73iG2hGbttcskplHysBczEIHFnEu8XqXRZfMPq0X626ejU1JnrW3N9YvEWP4GYCSRJDkgAShX/PXoiCtSEzM8O3OTk63XmkEkXmatnmal6g8476OCmy3A/aO565Q1ZwMqPP5N8/BbHE/PmRKbgpXc3rwtg8pq3voutLthO9UmqLt5DruyUuEk4b/gooSe+GyQTkGEYlEwIDAQAB\"},\"privateKeyEncrypted\":\"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\"}]";
    });
    Spark.get(prefix + "/serialized/:username/:password", (req, res) -> {
      String username = req.params("username");
      String password = req.params("password");
      if (username == null || password == null) {
        res.status(400);
        return "Error; username or password missing";
      }
      EntityManager em = ctx.factory.createEntityManager();
      //TODO Deal with username changes, etc.
      //TODO In fact, deal with the general concept of searching the most up-to-date version of stuff
      List<UserCreated> users = em.createQuery("SELECT uc from UserCreated uc where uc.username = :username", UserCreated.class)
              .setParameter("username", username)
              .getResultList();
      em.close();
      for (UserCreated user : users) {
        if (doesDecryptKey(user.privateKeyEncrypted, password)) {
          byte[] data = "This is a test string".getBytes();
    java.security.Signature sigS = java.security.Signature.getInstance(Constants.SIGNATURE_ALGORITHM);
    sigS.initSign(decryptKey(user.privateKeyEncrypted, password));
    sigS.update(data);
    byte[] signature = sigS.sign();
    
    java.security.Signature sigV = java.security.Signature.getInstance(Constants.SIGNATURE_ALGORITHM);
    sigV.initVerify(user.publicKey);
    sigV.update(data);
    return "Sig checks out: " + sigV.verify(signature);
        }
      }
      res.status(401);
      return ctx.om.writeValueAsString("Invalid login");
    });
    Spark.get(prefix + "/rehydrate", (req, res) -> {
      KeyPairGenerator kpg = KeyPairGenerator.getInstance(Constants.KEY_ALGORITHM);
      kpg.initialize(Constants.KEY_BITS);
      KeyPair keyPair = kpg.genKeyPair();

      byte[] data = "This is a test string".getBytes();
      
      java.security.Signature sigS = java.security.Signature.getInstance(Constants.SIGNATURE_ALGORITHM);
      sigS.initSign(keyPair.getPrivate());
      sigS.update(data);
      byte[] signature = sigS.sign();

      String dehydratedPublicKey = new Gson().toJson(keyPair.getPublic());
      PublicKey rehydratedPublicKey = new Gson().fromJson(dehydratedPublicKey, sun.security.rsa.RSAPublicKeyImpl.class);
      
      java.security.Signature sigV = java.security.Signature.getInstance(Constants.SIGNATURE_ALGORITHM);
      sigV.initVerify(rehydratedPublicKey);
      sigV.update(data);
      return "Sig checks out: " + sigV.verify(signature);
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
    uc.user = uc.handle;
    uc.userTimestamp = System.currentTimeMillis();
    uc.userSignature = Signature.signUser(ctx, uc, keyPair.getPrivate());
    uc.server = ctx.keyFile.serverHandle;
    uc.serverTimestamp = uc.userTimestamp;
    uc.serverSignature = Signature.signServer(ctx, uc, ctx.keyFile.serverPrivateKey);

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

  private static PrivateKey decryptKey(byte[] encryptedKey, String password) throws IOException, GeneralSecurityException, ClassNotFoundException {
    //TODO AesGcmJce is unsupported, and pure SHA256 is prolly a poor key-derivation function
    AesGcmJce agj = new AesGcmJce(DigestUtils.sha256(password));
    byte[] keyBytes = agj.decrypt(encryptedKey, null);
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(keyBytes));
    Object pk = ois.readObject();
    ois.close();
    if (pk instanceof PrivateKey) {
      return (PrivateKey)pk;
    } else {
      throw new GeneralSecurityException("Serialized object was not a PrivateKey");
    }
  }
  
  private static boolean doesDecryptKey(byte[] encryptedKey, String password) throws IOException, GeneralSecurityException {
    //TODO AesGcmJce is unsupported, and pure SHA256 is prolly a poor key-derivation function
    AesGcmJce agj = new AesGcmJce(DigestUtils.sha256(password));
    //TODO Is there a better way that doesn't involve actually decrypting it?  Would that even buy us anything?
    try {
      agj.decrypt(encryptedKey, null);
    } catch (AEADBadTagException e) {
      return false;
    }

    return true;
  }

  public static void asdf() {throw new RuntimeException();}
}
