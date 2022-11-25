package com.zord.recipe.api.model;

import java.util.Objects;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class Comment {

	@JsonSerialize(using = ToStringSerializer.class)
	
	private ObjectId id;
	private String comment;
	
	public Comment() {
	}
	
	public Comment(String comment) {
		this.comment = comment;
	}
	
	public ObjectId getId() {
		return id;
	}
	
	public void setId(ObjectId objectId) {
		this.id = objectId;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Comment other = (Comment) obj;
		return Objects.equals(id, other.id);
	}
	
}
