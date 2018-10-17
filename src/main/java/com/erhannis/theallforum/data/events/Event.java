/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data.events;

import com.erhannis.theallforum.data.Handle;

/**
 * //TODO Make this and subclasses immutable, I think
 *
 * @author erhannis
 */
public abstract class Event {
  public Handle handle;
  public Handle[] previous; //TODO Is this a good idea, actually?  This could be...confusing.  Particularly the multiples.
  public long timestamp;
}
