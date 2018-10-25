/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author erhannis
 */
@Embeddable
public class Handle implements Serializable {
  public String value;
  
  public static Handle gen() {
    Handle result = new Handle();
    result.value = UUID.randomUUID().toString() + "_" + UUID.randomUUID(); //TODO This could be overkill
    return result;
  }
}
