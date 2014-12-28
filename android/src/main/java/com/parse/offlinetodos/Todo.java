package com.parse.offlinetodos;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;
import java.util.UUID;

@ParseClassName("Todo")
public class Todo extends ParseObject {

    public static String AUTHOR_KEY = "author";
    public static String IS_DRAFT_KEY = "isDraft";
    public static String UUID_KEY = "uuid";
    public static String TITLE_KEY = "title";
    public static String LIST_NAME_KEY = "todoListName";
	
	public String getTitle() {
		return getString("title");
	}
	
	public void setTitle(String title) {
        String titleN = new String(title);
        Log.d("setTitle", "Old title : " + getTitle() + " new title : " + title + " UUID : "+ getUuidString());
		put("title", titleN);
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

    public String getTodoListName() {
        return getString("todoListName");
    }

    public void setTodoListName(String todoListName) {
        put("todoListName", todoListName);
    }

}
