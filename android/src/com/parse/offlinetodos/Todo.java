package com.parse.offlinetodos;

import java.util.List;
import java.util.UUID;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("Todo")
public class Todo extends ParseObject {
	
	public String getTitle() {
		return getString("title");
	}
	
	public void setTitle(String title) {
		put("title", title);
	}
	
	public ParseUser getAuthor() {
		return getParseUser("author");
	}
	
	public void setAuthor(ParseUser currentUser) {
		put("author", currentUser);
	}
	
	public boolean isDraft() {
		return getBoolean("isDraft");
	}
	
	public void setDraft(boolean isDraft) {
		put("isDraft", isDraft);
	}
	
	public void setUuidString() {
	    UUID uuid = UUID.randomUUID();
	    put("uuid", uuid.toString());
	}
	
	public String getUuidString() {
		return getString("uuid");
	}
	
	public static ParseQuery<Todo> getQuery() {
		return ParseQuery.getQuery(Todo.class);
	}
    public static boolean isEmptyTodoQuery() {
        boolean result;
        ParseQuery<Todo> query = ParseQuery.getQuery(Todo.class);
        query.fromLocalDatastore();
        query.whereEqualTo("title", "");
        try {
            List<Todo> emptyTODO;
            emptyTODO = query.find();
            if (emptyTODO.size()>0) {
                result = false;
            } else {
                result = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            result = true;
        }
        return result;
    }

}
