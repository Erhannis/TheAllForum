/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data.events.user;

import com.erhannis.theallforum.data.Signature;
import java.nio.ByteBuffer;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.persistence.Entity;

/**
 *
 * @author erhannis
 */
@Entity
public class UserCreated extends UserEvent {
  /*
        UserCreated uc = new UserCreated();
        uc.handle = Handle.gen();
        uc.username;
        uc.avatarUrl;
        uc.description;
        uc.email;
        uc.parents;
        uc.privateKeyEncrypted;
        uc.publicKey;
        uc.user;
        uc.userTimestamp;
        uc.userSignature;
        uc.server;
        uc.serverTimestamp;
        uc.serverSignature;
   */

  //TODO Should any of these things be moved to a Profile class or something?
  public String username;
  public String email;
  public String avatarUrl; //TODO Store the image itself?
  public String description;

  // Non-editable
  /**
   * The user's key should never change. It's expected to remain inviolate.
   * However, should the worst happen and the private key is somehow leaked,
   * you'll probably have to close this user and create a new account. It'd be
   * prohibitively difficult to re-sign all the user's posts, because then you'd
   * have to re-sign all child events....hang on
   *
   * If this event is sent to the server with publicKey not null, it is supposed
   * that the client is managing the user's public key. (//TODO Allowed?)
   */
  public PublicKey publicKey;

  /**
   * The user's PrivateKey, encrypted with the user's password.
   *
   * I think this is only given out when in distributed mode. It's still
   * dangerous because of offline brute force attacks, but I don't think that
   * can be fixed in distributed mode. Choose very good passwords. Actually,
   * wait, in distributed mode, the clients control their own private
   * keys...could the functional difference between distributed and non be just
   * whether the users choose to own their private keys?
   *
   * Perhaps a user can sign in either by giving their password, or proving
   * possession of the private key? Do they even need to sign in, if they can
   * sign all their messages? Only for websitey things, or rate-limited
   * functions, I guess - i.e., non-event actions.
   *
   * //TODO Would it be possible to permit the server to sign a single specific
   * message with the user's private key, without giving the server direct
   * access to the private key?
   */
  public byte[] privateKeyEncrypted; //TODO Verify correct decryption?

  @Override
  public Signature signUser(PrivateKey userKey) {
    java.security.Signature.getInstance("")
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Signature signServer(PrivateKey serverKey) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
