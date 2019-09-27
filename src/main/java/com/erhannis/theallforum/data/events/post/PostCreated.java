/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data.events.post;

import com.erhannis.theallforum.data.Handle;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 *
 * @author erhannis
 */
@Entity
public class PostCreated extends PostEvent {
  //TODO Isn't this what `parents` maybe means???
  // Non-editable
  @ElementCollection
  public Set<Handle> previous; //TODO Name's a bit weird
  
  // Editable
  //TODO Use the events instead, maybe?
  public String text; //TODO byte[]?
  
  //TODO Should tags be part of the post?  Or should they be attached to the post, separately?
  //  I guess `PostTagsAdded` IS a separate addition of tags - they're just initialized here for convenience.
  @ElementCollection
  public Set<Handle> tags;
}
