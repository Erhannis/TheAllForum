/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data.events.user;

import javax.persistence.Entity;

/**
 *
 * @author erhannis
 */
@Entity
public class UserEmailUpdated extends UserEvent {
  public String email;  
}
