/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum;

import com.erhannis.theallforum.data.Handle;
import java.security.PrivateKey;

/**
 *
 * @author erhannis
 */
public class KeyFile {
  public Handle serverHandle;
  public PrivateKey serverPrivateKey;

  public KeyFile(Handle serverHandle, PrivateKey serverPrivateKey) {
    this.serverHandle = serverHandle;
    this.serverPrivateKey = serverPrivateKey;
  }
}
