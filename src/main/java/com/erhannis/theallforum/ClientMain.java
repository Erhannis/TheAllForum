/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum;

import com.erhannis.theallforum.data.events.Event;
import com.google.gson.Gson;
import com.google.gson.typeadapters.RuntimePolytypeAdapterFactory;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Persistence;

/**
 *
 * @author erhannis
 */
public class ClientMain extends BaseMain {
  private static Logger LOGGER = Logger.getLogger(ClientMain.class.getName());
  
  public static void main(String[] args) throws IOException, IllegalAccessException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, ClassNotFoundException {
    LOGGER.info("Client startup");
    LOGGER.info("Commit hash: " + getHash());
    
    Context ctx = new Context();
    RuntimePolytypeAdapterFactory rtaf = RuntimePolytypeAdapterFactory.of(Event.class);
    ctx.gson = new Gson().newBuilder().registerTypeAdapterFactory(rtaf).create();
    ctx.factory = Persistence.createEntityManagerFactory("default");
    //TODO Not sure what to do about local user, and/or their key
    //ctx.keyFile = getKeyFile(ctx, "./private.key");
    
    //TODO Should a User handle be derived from their public key??
    
    
  }
}