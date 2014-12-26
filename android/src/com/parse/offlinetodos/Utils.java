package com.parse.offlinetodos;

import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Created by administrateur on 24/12/14.
 */
public class Utils {

    public static void setTitleItalicIfDraft(Todo todo, TextView todoTitle) {
        if (todo.isDraft()) {
            todoTitle.setTypeface(null, Typeface.ITALIC);
        } else {
            todoTitle.setTypeface(null, Typeface.NORMAL);
        }
    }
}
