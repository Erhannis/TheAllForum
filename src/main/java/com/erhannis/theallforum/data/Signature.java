/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data;

import javax.persistence.Embeddable;

/**
 * Ok, listen up.  Here's how signatures work, until further notice.
 * Assuming signing algorithm where you can append data to the text to be
 * signed, the text is constructed as follows:
 * 
 * From super-most class to sub-most class:
 * Iterate through each field in alphabetical order (as by String.compareTo):
 * If the field is multiple Handles, order them by alphabetical, then add each Handle's serverSignature,
 * else if the field is a Handle (that is not the Handle of the current Event), add the Handle's serverSignature,
 * otherwise add the field itself.  ASIDE from the following exceptions:
 * 
 * Skip the signature you're currently computing.
 * If you are computing the userSignature, skip the serverTimestamp and serverSignature.
 * 
 * Sign the resulting blob with the appropriate key.
 * 
 * It is recommended that classes structure their methods as follows:
 * Event defines signUser(_) and signServer(_), but these will be implemented only
 * on the leaf classes.  Starting with (Event).signUser0(_) (omitting "server"
 * from here on), each subclass defines ...
 * 
 * @author erhannis
 */
@Embeddable
public class Signature {
  public byte[] value; //TODO Add "type", etc.?
}
