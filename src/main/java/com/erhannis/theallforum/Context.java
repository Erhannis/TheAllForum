/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum;

import com.erhannis.theallforum.data.Handle;
import com.erhannis.theallforum.data.events.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Type;
import java.util.List;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author erhannis
 */
public class Context {
  public ObjectMapper om;
  public EntityManagerFactory factory;
  public KeyFile keyFile; //TODO Is this dangerous?
}
