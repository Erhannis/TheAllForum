/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data.events.post;

import com.erhannis.theallforum.data.Handle;
import java.util.HashSet;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 *
 * @author erhannis
 */
@Entity
public class PostTagsAdded extends PostEvent {
  @ElementCollection
  public HashSet<Handle> tags;
}
