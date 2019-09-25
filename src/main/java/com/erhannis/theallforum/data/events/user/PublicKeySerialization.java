/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data.events.user;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.google.gson.Gson;
import java.io.IOException;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author erhannis
 */
public class PublicKeySerialization {
  private static final Gson GSON = new Gson();

  public static class Serializer extends JsonSerializer<PublicKey> {
    @Override
    public void serialize(PublicKey publicKey, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
      String dehydratedPublicKey = GSON.toJson(publicKey);
      PublicKey rehydratedPublicKey = new Gson().fromJson(dehydratedPublicKey, sun.security.rsa.RSAPublicKeyImpl.class);
      jsonGenerator.writeStartArray();
      jsonGenerator.writeString(publicKey.getClass().getCanonicalName());
      jsonGenerator.writeString(dehydratedPublicKey);
      jsonGenerator.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<PublicKey> {
    @Override
    public PublicKey deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      JsonNode node = p.getCodec().readTree(p);
      String classname = node.get(0).textValue();
      String dehydratedPublicKey = node.get(1).textValue();
      PublicKey rehydratedPublicKey;
      try {
        //TODO Is this a security problem?
        rehydratedPublicKey = new Gson().fromJson(dehydratedPublicKey, (Class<? extends PublicKey>)this.getClass().getClassLoader().loadClass(classname));
      } catch (ClassNotFoundException ex) {
        Logger.getLogger(PublicKeySerialization.class.getName()).log(Level.SEVERE, null, ex);
        throw new IOException("Problem with claimed class " + classname);
      }
      return rehydratedPublicKey;
    }
  }
}
