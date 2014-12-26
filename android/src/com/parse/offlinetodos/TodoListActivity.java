package com.parse.offlinetodos;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ui.ParseLoginBuilder;

public class TodoListActivity extends Activity {

    private static final int LOGIN_ACTIVITY_CODE = 100;
    private static final int EDIT_ACTIVITY_CODE = 200;

    // Adapter for the Todos Parse Query
    private TodoListAdapter todoListAdapter;

    // For showing empty and non-empty todo views
    private ListView todoListView;
    private LinearLayout noTodosView;
    private Button buttonOk;
    private ParseEventTaskQueue taskQueue;

    private TextView loggedInInfoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_todo_list);
        taskQueue = new ParseEventTaskQueue();

        // If User is not Logged In Start Activity Login
        if (ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
            ParseLoginBuilder builder = new ParseLoginBuilder(this);
            startActivityForResult(builder.build(), LOGIN_ACTIVITY_CODE);
        }

        // Set up the views
        todoListView = (ListView) findViewById(R.id.todo_list_view);
        noTodosView = (LinearLayout) findViewById(R.id.no_todos_view);
        todoListView.setEmptyView(noTodosView);
        loggedInInfoView = (TextView) findViewById(R.id.loggedin_info);
        buttonOk = (Button) findViewById(R.id.buttonSetTODO);
        buttonOk.setVisibility(View.INVISIBLE);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                synchroniseTodos();
            }
        });

        // Set up the Parse query to use in the adapter
        ParseQueryAdapter.QueryFactory<Todo> factory = new ParseQueryAdapter.QueryFactory<Todo>() {
            public ParseQuery<Todo> create() {
                ParseQuery<Todo> query = Todo.getQuery();
                query.orderByDescending("createdAt");
                return query;
            }
        };

        ParseQuery<Todo> query = Todo.getQuery();
        List<Todo> todoList = null;
        try {
            todoList = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //Add empty Todo at the end of the list
        Todo emptyTodo = new Todo();
        initEmptyTodo(emptyTodo);
        todoListAdapter = new TodoListAdapter(this, R.layout.list_item_todo, todoList, buttonOk);
        todoListAdapter.addItem(emptyTodo);
        // Attach the query adapter to the view
        ListView todoListView = (ListView) findViewById(R.id.todo_list_view);
        todoListView.setAdapter(todoListAdapter);
    }

    private void synchroniseTodos() {
        todoListAdapter.printTodoList();
        boolean needToAddEmtyTodo = true;
        List<Todo> todoList = todoListAdapter.getTodoList();

        for (int i = 0; i < todoList.size(); i++) {
            Todo todo = todoList.get(i);
            if (todo.getTitle().equals("")) {
                needToAddEmtyTodo = false;
            }
            if (todo.isDraft() && !todo.getTitle().equals("")) {
                todo.setDraft(false);
                taskQueue.add(todo);
            }
        }
        if(needToAddEmtyTodo) {
            Todo newTodo = new Todo();
            initEmptyTodo(newTodo);
            todoListAdapter.addItem(newTodo);
        }
        todoListAdapter.reloadNewImages();
    }

    private void initEmptyTodo(Todo newTodo) {
        newTodo.setDraft(true);
        newTodo.setTitle("");
        newTodo.setAuthor(ParseUser.getCurrentUser());
        newTodo.setUuidString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if we have a real user
        if (!ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
            // Sync data to Parse
            //syncTodosToParse();
            // Update the logged in label info
            updateLoggedInInfo();
        }
    }

    private void updateLoggedInInfo() {
        if (!ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
            ParseUser currentUser = ParseUser.getCurrentUser();
            loggedInInfoView.setText(getString(R.string.logged_in,
                    currentUser.getString("name")));
        } else {
            loggedInInfoView.setText(getString(R.string.not_logged_in));
        }
    }

    private void openEditView(Todo todo) {
        Intent i = new Intent(this, NewTodoActivity.class);
        i.putExtra("ID", todo.getUuidString());
        startActivityForResult(i, EDIT_ACTIVITY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.todo_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_new) {
            // Make sure there's a valid user, anonymous
            // or regular
            if (ParseUser.getCurrentUser() != null) {
                startActivityForResult(new Intent(this, NewTodoActivity.class),
                        EDIT_ACTIVITY_CODE);
            }
        }

        if (item.getItemId() == R.id.action_sync) {
            //syncTodosToParse();
        }

        if (item.getItemId() == R.id.action_logout) {
            // Log out the current user
            ParseUser.logOut();
            // Update the logged in label info
            updateLoggedInInfo();
            // Clear the view
            //todoList.clear();
            // Unpin all the current objects
            ParseObject
                    .unpinAllInBackground(TodoListApplication.TODO_GROUP_NAME);
        }

        if (item.getItemId() == R.id.action_login) {
            ParseLoginBuilder builder = new ParseLoginBuilder(this);
            startActivityForResult(builder.build(), LOGIN_ACTIVITY_CODE);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean realUser = !ParseAnonymousUtils.isLinked(ParseUser
                .getCurrentUser());
        menu.findItem(R.id.action_login).setVisible(!realUser);
        menu.findItem(R.id.action_logout).setVisible(realUser);
        return true;
    }
}
