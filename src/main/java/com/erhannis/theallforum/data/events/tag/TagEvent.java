/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data.events.tag;

import com.erhannis.theallforum.data.Handle;
import com.erhannis.theallforum.data.events.Event;

/**
 *
 * @author erhannis
 */
public class TagEvent extends Event {
  /**
   * The user performing the event.
   */
  public Handle user; //TODO String? //TODO Move to Event?

  //TODO Should this have a hash?
}
