package com.parse.offlinetodos;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by administrateur on 24/12/14.
 */
public class TodoTextWatcher implements TextWatcher{
    EditText editText;
    Button buttonOk;
    Todo todo;

    public TodoTextWatcher(EditText editText, Todo todo, Button buttonOk) {
        this.editText = editText;
        this.todo = todo;
        this.buttonOk = buttonOk;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        /*Log.d("afterTextChanged", "editable " + editable + " todo title " + todo.getTitle() + " uuid " + todo.getUuidString());
        if(!todo.getTitle().equals(editable.toString())) {
            buttonOk.setVisibility(View.VISIBLE);
            todo.setDraft(true);
            Utils.setTitleItalicIfDraft(todo, editText);
            todo.setTitle(editable.toString());
        }
        else {
            todo.setDraft(false);
            Utils.setTitleItalicIfDraft(todo, editText);
        }*/
        buttonOk.setVisibility(View.VISIBLE);
    }
}
