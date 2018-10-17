/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data.events.post;

import javax.persistence.Entity;

/**
 *
 * @author erhannis
 */
@Entity
public class PostTextUpdated extends PostEvent {
  public String text; //TODO byte[]?
}
