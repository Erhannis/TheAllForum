/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data.events.post;

import com.erhannis.theallforum.data.Handle;

/**
 *
 * @author erhannis
 */
public class PostCreated extends PostEvent {
  // Non-editable
  public Handle[] parents;
  
  // Editable
  //TODO Use the events instead, maybe?
  public String text; //TODO byte[]?
  public Handle[] tags;
}
