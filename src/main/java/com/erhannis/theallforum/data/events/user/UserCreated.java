/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data.events.user;

import java.security.KeyPairGenerator;
import java.security.PublicKey;

/**
 *
 * @author erhannis
 */
public class UserCreated extends UserEvent {
  //TODO Should any of these things be moved to a Profile class or something?
  public String username;
  public String email;
  public String avatarUrl; //TODO Store the image itself?
  public String description;
  
  // Non-editable
  /**
   * The user's key should never change.  It's expected to remain inviolate.
   * However, should the worst happen and the private key is somehow leaked,
   * you'll probably have to close this user and create a new account.  It'd be
   * prohibitively difficult to re-sign all the user's posts, because then you'd
   * have to re-sign all child events....hang on
   */
  
  public PublicKey publicKey;
  
  /**
   * The user's PrivateKey, encrypted with the user's password.
   * 
   * //TODO Would it be possible to permit the server to sign a single specific 
   * message with the user's private key, without giving the server direct 
   * access to the private key?
   */
  public byte[] privateKeyEncrypted; //TODO Verify correct decryption?
}
