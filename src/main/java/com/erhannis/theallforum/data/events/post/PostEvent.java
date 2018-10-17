/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data.events.post;

import com.erhannis.theallforum.data.Handle;
import com.erhannis.theallforum.data.Signature;
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
public abstract class PostEvent extends Event {
  /**
   * The user performing the event.
   */
  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name="value",column=@Column(name="userValue")),
  })
  public Handle user; // Handle //TODO Move to Event?
  
  /**
   * The hash of the event, signed by the responsible user - should depend on
   * the signatures of this event's `previous`, the signatures of `parents`,
   * and every field of `this`.
   */
  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name="value",column=@Column(name="signatureValue")),
  })
  public Signature signature; //TODO Should this be here, or in the subclasses?
}
