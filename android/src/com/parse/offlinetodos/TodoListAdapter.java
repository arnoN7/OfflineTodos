package com.parse.offlinetodos;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by administrateur on 23/12/14.
 */
public class TodoListAdapter extends BaseAdapter {

    List<Todo> todoList;
    Context context;
    Button buttonOk;


    public TodoListAdapter(Context context, int resource, List<Todo> objects, Button buttonOk) {
        this.context = context;
        todoList = new ArrayList<Todo>();
        todoList.addAll(objects);
        this.buttonOk = buttonOk;
    }

    public void reloadNewImages(List<Todo> newTodoList) {
        todoList = newTodoList;
    }

    public List<Todo> getTodoList() {
        return todoList;
    }

    public void reloadNewImages() {
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return todoList.size();
    }

    @Override
    public Object getItem(int i) {
        return todoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void addItem (Todo newTodo) {
        todoList.add(newTodo);
    }
    
    public void printTodoList() {
        for (int i = 0; i < todoList.size(); i++) {
            Log.d("TODOLIST", "["+i+"] : " + todoList.get(i).getTitle() + " : " + todoList.get(i).getUuidString());
        }
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        final Holder holder;
        Todo todo = todoList.get(position);
        String todoTitle = todo.getTitle();
        EditText todoTextView;
        Button deleteButton;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_todo, null);
            todoTextView = (EditText) view.findViewById(R.id.todo_title);
            deleteButton = (Button) view.findViewById(R.id.buttonDeleteTODO);
            todoTextView.setTag(position);
            todoTextView.setText(todoTitle);
            holder = new Holder(todoTextView, todo, null, deleteButton);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
            todoTextView = holder.todoTextView;
            holder.todo = todo;
            todoTextView.setTag(position);
            todoTextView.removeTextChangedListener(holder.watcher);
            if (!todoTextView.getText().toString().equals(todoTitle)) {
                todoTextView.setText(todo.getTitle());
            }
        }
        int tag_position = (Integer) holder.todoTextView.getTag();
        holder.todoTextView.setId(tag_position);
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                todoList.get(position).deleteInBackground();
                todoList.remove(position);
                reloadNewImages();
            }
        });

        holder.watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                final int position2 = holder.todoTextView.getId();
                final EditText Caption = (EditText) holder.todoTextView;
                if (Caption.getText().toString().length() > 0) {
                    todoList.get(position2).setTitle(Caption.getText().toString());
                    todoList.get(position2).setDraft(true);
                    buttonOk.setVisibility(View.VISIBLE);
                    setItalicIfDraft(todoList.get(position2), Caption);
                } else {
                    buttonOk.setVisibility(View.INVISIBLE);
                    Toast.makeText(context, "Please enter some value", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };

        holder.todoTextView.addTextChangedListener(holder.watcher);
        return view;
    }

    public void setItalicIfDraft(Todo todo, EditText editText) {
        if (todo.isDraft()) {
            editText.setTypeface(null, Typeface.ITALIC);
        } else {
            editText.setTypeface(null, Typeface.NORMAL);
        }
    }

    public class Holder {
        EditText todoTextView;
        Todo todo;
        TextWatcher watcher;
        Button delete;

        public Holder(EditText todoTextView, Todo todo, TextWatcher watcher, Button delete) {
            this.todo = todo;
            this.todoTextView = todoTextView;
            this.watcher = watcher;
            this.delete = delete;
        }
    }
}
