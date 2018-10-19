/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data;

import com.erhannis.theallforum.Constants;
import com.erhannis.theallforum.Context;
import com.erhannis.theallforum.data.events.Event;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Embeddable;

/**
 * Ok, listen up. Here's how signatures work, until further notice. Assuming
 * signing algorithm where you can append data to the text to be signed, the
 * text is constructed as follows:
 *
 * From super-most class to sub-most class: Iterate through each field in
 * alphabetical order (as by String.compareTo): If the field is multiple
 * Handles, order them by alphabetical, then add each Handle's serverSignature,
 * else if the field is a Handle (that is not the Handle of the current Event),
 * add the Handle's serverSignature, otherwise add the field itself. ASIDE from
 * the following exceptions:
 *
 * Skip the signature you're currently computing. If you are computing the
 * userSignature, skip the serverTimestamp and serverSignature.
 *
 * Sign the resulting blob with the appropriate key.
 *
 * It is recommended that classes structure their methods as follows: Event
 * defines signUser(_) and signServer(_), but these will be implemented only on
 * the leaf classes. Starting with (Event).signUser0(_) (omitting "server" from
 * here on), each subclass defines ...
 *
 * @author erhannis
 */
@Embeddable
public class Signature {
  public byte[] value; //TODO Add "type", etc.?

  private static Signature getServerSignature(Context ctx, String handleValue) {
    asdf;
  }
  
  protected static byte[] sign(Context ctx, Event event, PrivateKey key, boolean isUserSignature) throws IllegalAccessException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, SignatureException {
    //TODO Might be able to use SignedObject instead of all this?  Don't think so, though
    LinkedList<Class<?>> clazzes = new LinkedList<>();
    Class<?> curClazz = event.getClass();
    clazzes.push(curClazz);
    while (curClazz != Event.class) {
      curClazz = curClazz.getSuperclass();
      clazzes.push(curClazz);
    }
    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    while (!clazzes.isEmpty()) {
      curClazz = clazzes.pop();
      List<Field> fields = Arrays.asList(curClazz.getDeclaredFields()).stream().sorted((a, b) -> a.getName().compareTo(b.getName())).collect(Collectors.toList());
      for (Field field : fields) {
        if (isUserSignature) {
          if ("serverSignature".equals(field.getName())) {
            continue;
          } else if ("serverTimestamp".equals(field.getName())) {
            continue;
          } else if ("userSignature".equals(field.getName())) {
            continue;
          }
        } else {
          if ("userSignature".equals(field.getName())) {
            continue;
          }
        }
        field.setAccessible(true);
        Class<?> type = field.getType();
        boolean failure = false;
        if (type == boolean.class) {
          out.writeBoolean(field.getBoolean(event));
        } else if (type == byte.class) {
          out.writeByte(field.getByte(event));
        } else if (type == char.class) {
          out.writeChar(field.getChar(event));
        } else if (type == double.class) {
          out.writeDouble(field.getDouble(event));
        } else if (type == float.class) {
          out.writeFloat(field.getFloat(event));
        } else if (type == int.class) {
          out.writeInt(field.getInt(event));
        } else if (type == long.class) {
          out.writeLong(field.getLong(event));
        } else if (type == short.class) {
          out.writeShort(field.getShort(event));
        } else {
          // An object of some kind
          Type genericType = field.getGenericType();
          if (type == Handle.class) {
            if ("handle".equals(field.getName()) && field.getDeclaringClass() == Event.class) {
              out.writeUTF(((Handle)field.get(event)).value);
            } else {
              out.write(getServerSignature(ctx, ((Handle)field.get(event)).value).value);
            }
          } else if (type == Signature.class) {
            out.write(((Signature)field.get(event)).value);
          } else if (type == String.class) {
            out.writeUTF((String)field.get(event));
          } else if (type == PublicKey.class) {
            PublicKey publicKey = ((PublicKey)field.get(event));
            out.writeUTF(publicKey.getAlgorithm());
            out.writeUTF(publicKey.getFormat());
            out.write(publicKey.getEncoded());
          } else if (type == byte[].class) {
            out.write((byte[])field.get(event));
          } else if (type == HashSet.class) { //TODO Note this kinda restricts the use of other Collections
            if (genericType instanceof ParameterizedType) {
              if (((ParameterizedType)genericType).getActualTypeArguments()[0] == Handle.class) {
                HashSet<Handle> set = (HashSet<Handle>)field.get(event);
                if (set.contains(null) || set.stream().anyMatch(h -> h.value == null)) {
                  throw new IllegalArgumentException("Field contains nulls: " + field);
                }
                List<Handle> handles = set.stream().sorted((a, b) -> a.value.compareTo(b.value)).collect(Collectors.toList());
                for (Handle handle : handles) {
                  out.write(getServerSignature(ctx, handle.value).value);
                }
              } else {
                failure = true;
              }
            } else {
              failure = true;
            }
          } else {
            failure = true;
          }
        }
        if (failure) {
          throw new IllegalArgumentException("Unhandled class field: " + field);
        }
      }
    }

    byte[] data = out.toByteArray();
    
    //TODO Need to make sure key algorithm matches
    java.security.Signature sig = java.security.Signature.getInstance(Constants.SIGNATURE_ALGORITHM);
    sig.initSign(key);
    sig.update(data);
    byte[] signatureBytes = sig.sign();

    return signatureBytes;
  }

  public static Signature signUser(Context ctx, Event event, PrivateKey userKey) throws IllegalAccessException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
    Signature result = new Signature();
    result.value = sign(ctx, event, userKey, true);
    return result;
  }

  public static Signature signServer(Context ctx, Event event, PrivateKey serverKey) throws IllegalAccessException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
    Signature result = new Signature();
    result.value = sign(ctx, event, serverKey, false);
    return result;
  }
}
