/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data;

import com.google.crypto.tink.subtle.AesGcmJce;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author erhannis
 */
@Embeddable
public class Handle implements Serializable {
  public String value;
  
  public static Handle gen() {
    Handle result = new Handle();
    result.value = UUID.randomUUID() + "_" + UUID.randomUUID(); //TODO This could be overkill
    return result;
  }

  /**
   * Deterministically generates a random handle from a public key.
   * A User is (until further notice) uniquely identified and defined
   * by their key, so their handle (to prevent attempted handle collisions)
   * is derived from their public key.<br/>
   * <br/>
   * (Currently done by: serializing  the key, SHA256 it twice
   * (once with "first" appended, and once with "second" appended), and feeding
   * the results to UUID.nameUUIDFromBytes(byte[]).)<br/>
   * <br/>
   * //TODO I'm concerned that my rampant use of serialization will bite me in the form of implementation-differences.
   * 
   * @param src
   * @return 
   */
  public static Handle gen(PublicKey publicKey) throws NoSuchAlgorithmException, IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(publicKey);
      oos.flush();

      byte[] bytes = baos.toByteArray();
      byte[] first = "first".getBytes("UTF-8");
      byte[] second = "second".getBytes("UTF-8");
      
      byte[] bytesFirst = Arrays.copyOf(bytes, bytes.length + first.length);
      byte[] bytesSecond = Arrays.copyOf(bytes, bytes.length + second.length);
      System.arraycopy(first, 0, bytesFirst, bytes.length, first.length);
      System.arraycopy(second, 0, bytesSecond, bytes.length, second.length);
      
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      
      Handle result = new Handle();
      result.value = UUID.nameUUIDFromBytes(digest.digest(bytesFirst)) + "_" + UUID.nameUUIDFromBytes(digest.digest(bytesSecond)); //TODO This could be overkill
      return result;
    }
  }
}
