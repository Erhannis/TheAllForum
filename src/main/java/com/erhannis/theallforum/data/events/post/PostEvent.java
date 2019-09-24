/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.erhannis.theallforum.data.events.post;

import com.erhannis.theallforum.Context;
import com.erhannis.theallforum.data.Handle;
import com.erhannis.theallforum.data.Signature;
import com.erhannis.theallforum.data.events.Event;
import com.erhannis.theallforum.data.events.tag.TagEvent;
import com.erhannis.theallforum.data.events.user.UserEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.security.PrivateKey;
import java.util.List;
import java.util.stream.Collectors;
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
    @JsonSubTypes.Type(value = PostCreated.class, name = "PostCreated"),
    @JsonSubTypes.Type(value = PostTagsAdded.class, name = "PostTagsAdded"),
    @JsonSubTypes.Type(value = PostTagsRemoved.class, name = "PostTagsRemoved"),
    @JsonSubTypes.Type(value = PostTextUpdated.class, name = "PostTextUpdated")
})
public abstract class PostEvent extends Event {
}
