/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data;

import com.erhannis.theallforum.Constants;
import com.erhannis.theallforum.Context;
import com.erhannis.theallforum.Main;
import com.erhannis.theallforum.data.events.Event;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
    System.err.println("NOT YET IMPLEMENTED: getServerSignature");
    Signature result = new Signature();
    result.value = new byte[0];
    Main.asdf();
    return result;
  }

  /**
   * Serializes an event to bytes, for hashing/signing.<br/>
   * Processes from superclass to subclass.<br/>
   * Iterates through fields by name in alphabetical order.<br/>
   * For each field f, f is serialized to a byte array via
   * com.google.common.io.ByteStreams.newDataOutput()'s `writeX`
   * methods, then its length is written to the output, followed
   * by the serialization of f itself.<br/>
   * Only a particular set of field classes are supported for
   * serialization. Read the code to figure it out.<br/>
   * If `isUserSignature`, skips fields named `serverSignature`, `serverTimestamp`,
   * or `userSignature`, because they won't have happened yet.<br/>
   * If not `isUserSignature`, skips fields named `serverSignature`,
   * because it won't have happened yet.<br/>
   * 
   * @param ctx
   * @param event
   * @param isUserSignature
   * @return
   * @throws IllegalArgumentException
   * @throws IllegalAccessException 
   */
  protected static byte[] toBytes(Context ctx, Event event, boolean isUserSignature) throws IllegalArgumentException, IllegalAccessException {
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
          if ("serverSignature".equals(field.getName())) {
            continue;
          }
        }
        field.setAccessible(true);
        Class<?> type = field.getType();
        boolean failure = false;
        if (type == boolean.class) {
          byte[] val = ToBytes.toBytesBoolean(field.getBoolean(event));
          out.writeInt(val.length);
          out.write(val);
        } else if (type == byte.class) {
          byte[] val = ToBytes.toBytesByte(field.getByte(event));
          out.writeInt(val.length);
          out.write(val);
        } else if (type == char.class) {
          byte[] val = ToBytes.toBytesChar(field.getChar(event));
          out.writeInt(val.length);
          out.write(val);
        } else if (type == double.class) {
          byte[] val = ToBytes.toBytesDouble(field.getDouble(event));
          out.writeInt(val.length);
          out.write(val);
        } else if (type == float.class) {
          byte[] val = ToBytes.toBytesFloat(field.getFloat(event));
          out.writeInt(val.length);
          out.write(val);
        } else if (type == int.class) {
          byte[] val = ToBytes.toBytesInt(field.getInt(event));
          out.writeInt(val.length);
          out.write(val);
        } else if (type == long.class) {
          byte[] val = ToBytes.toBytesLong(field.getLong(event));
          out.writeInt(val.length);
          out.write(val);
        } else if (type == short.class) {
          byte[] val = ToBytes.toBytesShort(field.getShort(event));
          out.writeInt(val.length);
          out.write(val);
        } else {
          // An object of some kind
          Type genericType = field.getGenericType();
          Object o = field.get(event);
          if (o == null) {
            out.writeInt(0);
          } else if (type == Handle.class) {
            if ("handle".equals(field.getName()) && field.getDeclaringClass() == Event.class) {
              byte[] val = ToBytes.toBytesUTF(((Handle) field.get(event)).value);
              out.writeInt(val.length);
              out.write(val);
            } else {
              Handle handle = ((Handle) field.get(event));
              byte[] val0 = ToBytes.toBytesUTF(handle.value);
              out.writeInt(val0.length);
              out.write(val0);
              byte[] val1 = getServerSignature(ctx, handle.value).value;
              out.writeInt(val1.length);
              out.write(val1);
            }
          } else if (type == Signature.class) {
            byte[] val = ((Signature) field.get(event)).value;
            out.writeInt(val.length);
            out.write(val);
          } else if (type == String.class) {
            byte[] val = ToBytes.toBytesUTF((String) field.get(event));
            out.writeInt(val.length);
            out.write(val);
          } else if (type == PublicKey.class) {
            PublicKey publicKey = ((PublicKey) field.get(event));
            byte[] val0 = ToBytes.toBytesUTF(publicKey.getAlgorithm());
            out.writeInt(val0.length);
            out.write(val0);
            byte[] val1 = ToBytes.toBytesUTF(publicKey.getFormat());
            out.writeInt(val1.length);
            out.write(val1);
            byte[] val2 = publicKey.getEncoded();
            out.writeInt(val2.length);
            out.write(val2);
          } else if (type == byte[].class) {
            byte[] val = (byte[]) field.get(event);
            out.writeInt(val.length);
            out.write(val);
          } else if (type == Set.class) { //TODO Note this kinda restricts the use of other Collections
            if (genericType instanceof ParameterizedType) {
              if (((ParameterizedType) genericType).getActualTypeArguments()[0] == Handle.class) {
                Set<Handle> set = (Set<Handle>) field.get(event);
                if (set.contains(null) || set.stream().anyMatch(h -> h.value == null)) {
                  throw new IllegalArgumentException("Field contains nulls: " + field);
                }
                List<Handle> handles = set.stream().sorted((a, b) -> a.value.compareTo(b.value)).collect(Collectors.toList());
                for (Handle handle : handles) {
                  byte[] val0 = ToBytes.toBytesUTF(handle.value);
                  out.writeInt(val0.length);
                  out.write(val0);
                  byte[] val1 = getServerSignature(ctx, handle.value).value;
                  out.writeInt(val1.length);
                  out.write(val1);
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

    return out.toByteArray();
  }
  
  protected static boolean verify(Context ctx, Event event, byte[] signature, PublicKey key, boolean isUserSignature) throws IllegalAccessException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, SignatureException {
    //TODO Need to make sure key algorithm matches
    java.security.Signature sig = java.security.Signature.getInstance(Constants.SIGNATURE_ALGORITHM);
    sig.initVerify(key);
    byte[] data = toBytes(ctx, event, isUserSignature);
    sig.update(data);
    return sig.verify(signature);
  }

  public static boolean verifyUser(Context ctx, Event event, byte[] signature, PublicKey userKey) throws IllegalAccessException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
    return verify(ctx, event, signature, userKey, true);
  }
  
  public static boolean verifyServer(Context ctx, Event event, byte[] signature, PublicKey serverKey) throws IllegalAccessException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
    return verify(ctx, event, signature, serverKey, false);
  }
  
  protected static byte[] sign(Context ctx, Event event, PrivateKey key, boolean isUserSignature) throws IllegalAccessException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, SignatureException {
    //TODO Need to make sure key algorithm matches
    java.security.Signature sig = java.security.Signature.getInstance(Constants.SIGNATURE_ALGORITHM);
    sig.initSign(key);
    byte[] data = toBytes(ctx, event, isUserSignature);
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

  //TODO This all is pretty overkill; way more computation than desirable.
  private static class ToBytes {
    public static byte[] toBytes(int b) {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.write(b);
      return out.toByteArray();
    }

    public static byte[] toBytes(byte[] b) {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.write(b);
      return out.toByteArray();
    }

    public static byte[] toBytesBoolean(boolean v) {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeBoolean(v);
      return out.toByteArray();
    }

    public static byte[] toBytesByte(int v) {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeByte(v);
      return out.toByteArray();
    }

    public static byte[] toBytesShort(int v) {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeShort(v);
      return out.toByteArray();
    }

    public static byte[] toBytesChar(int v) {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeChar(v);
      return out.toByteArray();
    }

    public static byte[] toBytesInt(int v) {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeInt(v);
      return out.toByteArray();
    }

    public static byte[] toBytesLong(long v) {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeLong(v);
      return out.toByteArray();
    }

    public static byte[] toBytesFloat(float v) {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeFloat(v);
      return out.toByteArray();
    }

    public static byte[] toBytesDouble(double v) {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeDouble(v);
      return out.toByteArray();
    }

    public static byte[] toBytesChars(String s) {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeChars(s);
      return out.toByteArray();
    }

    public static byte[] toBytesUTF(String s) {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      out.writeUTF(s);
      return out.toByteArray();
    }
  }
}
