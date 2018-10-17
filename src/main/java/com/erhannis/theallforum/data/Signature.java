/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data;

import javax.persistence.Embeddable;

/**
 *
 * @author erhannis
 */
@Embeddable
public class Signature {
  public byte[] value; //TODO Add "type", etc.?
}
