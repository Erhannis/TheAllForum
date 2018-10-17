/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data.events;

import com.erhannis.theallforum.data.Handle;
import java.util.HashSet;
import java.util.UUID;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

/**
 * //TODO Make this and subclasses immutable, I think
 *
 * @author erhannis
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Event {
  @EmbeddedId
  @AttributeOverrides({ //TODO UGGGH, this is the worst.
    @AttributeOverride(name="value",column=@Column(name="handleValue")),
  })
  public Handle handle;

  @ElementCollection
  public HashSet<Handle> parents; //TODO Is this a good idea, actually?  This could be...confusing.  Particularly the multiples.

  public long timestamp;
}
