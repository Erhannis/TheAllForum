/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data.events.tag;

import com.erhannis.theallforum.data.Handle;
import com.erhannis.theallforum.data.events.Event;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

/**
 *
 * @author erhannis
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class TagEvent extends Event {
  /**
   * The user performing the event.
   */
  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name="value",column=@Column(name="userValue")),
  })
  public Handle user; //TODO Move to Event?

  //TODO Should this have a hash?
}
