/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data.events;

import com.erhannis.theallforum.Context;
import com.erhannis.theallforum.data.Handle;
import com.erhannis.theallforum.data.Signature;
import com.erhannis.theallforum.data.events.post.PostEvent;
import com.erhannis.theallforum.data.events.tag.TagEvent;
import com.erhannis.theallforum.data.events.user.UserEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PostEvent.class, name = "PostEvent"),
    @JsonSubTypes.Type(value = TagEvent.class, name = "TagEvent"),
    @JsonSubTypes.Type(value = UserEvent.class, name = "UserEvent")
})
public abstract class Event {
  /*
    this.handle;
    this.parents;
    this.server;
    this.serverSignature;
    this.serverTimestamp;
    this.user;
    this.userSignature;
    this.userTimestamp;
   */

  @EmbeddedId
  @AttributeOverrides({ //TODO UGGGH, this is the worst.
    @AttributeOverride(name = "value", column = @Column(name = "eventHandle")),})
  public Handle handle;

  //TODO Uh...what IS this, actually?
  @ElementCollection
  public Set<Handle> parents; //TODO Is this a good idea, actually?  This could be...confusing.  Particularly the multiples.

  /**
   * The timestamp at which the user is 
   */
  public long userTimestamp;
  
  /**
   * The timestamp at which the server is signing the event.
   * 
   * Set by the server.
   */
  public long serverTimestamp;

  /**
   * The user performing the event.
   */
  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "value", column = @Column(name = "userHandle")),})
  public Handle user;

  /**
   * The server processing the event.
   * Set by the server.
   *
   * Note that a "server" is a user account.
   */
  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "value", column = @Column(name = "serverHandle")),})
  public Handle server;

  /**
   * The hash of the event, signed by the responsible user - should depend on
   * the signatures of this event's `previous`, the signatures of every other
   * linked event (such as `parents`), and every field of `this` EXCEPT for
   * serverTimestamp (and the two signatures).
   */
  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "value", column = @Column(name = "userSignature")),})
  public Signature userSignature; //TODO Should this be here, or in the subclasses?

  /**
   * The hash of the event, signed by the processing server - should depend on
   * the signatures of this event's `previous`, the signatures of every other
   * linked event (such as `parents`), and every field of `this` EXCEPT for
   * `serverSignature` (including, notably, serverTimestamp and userSignature).
   * 
   * Set by the server.
   *
   * Note that a "server" is a user account.
   */
  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "value", column = @Column(name = "serverSignature")),})
  public Signature serverSignature; //TODO Should this be here, or in the subclasses?
}
