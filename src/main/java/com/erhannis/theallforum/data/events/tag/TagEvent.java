/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data.events.tag;

import com.erhannis.theallforum.data.Handle;
import com.erhannis.theallforum.data.events.Event;
import com.erhannis.theallforum.data.events.post.PostCreated;
import com.erhannis.theallforum.data.events.post.PostTagsAdded;
import com.erhannis.theallforum.data.events.post.PostTagsRemoved;
import com.erhannis.theallforum.data.events.post.PostTextUpdated;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TagCreated.class, name = "TagCreated")
})
public abstract class TagEvent extends Event {
}
