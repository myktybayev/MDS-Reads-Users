package kz.incubator.sdcl.club1.book_list_menu;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import android.widget.SearchView;

import java.util.jar.Attributes;

public class CustomSearchView extends SearchView {

    private AutoCompleteTextView mSearchAutoComplete;

    public CustomSearchView(Context context) {
        super(context);
        initialize();
    }

    public CustomSearchView(Context context, Attributes attrs) {
        super(context, (AttributeSet) attrs);
        initialize();
    }

    public CustomSearchView(Context context, Attributes attrs, int defStyleAttr) {
        super(context, (AttributeSet) attrs, defStyleAttr);
        initialize();
    }

    public void initialize() {
        mSearchAutoComplete = (AutoCompleteTextView) findViewById(android.support.v7.appcompat.R.id.search_src_text);

        if (mSearchAutoComplete == null) {
            Log.wtf("TEST", "Some Changes in AppCompat????");
            return;
        }
        mSearchAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mSearchAutoComplete.removeTextChangedListener(this);
                setSpans(editable, Color.RED);
                mSearchAutoComplete.addTextChangedListener(this);
            }
        });
    }

    private void setSpans(Editable s, @ColorInt int backgroundColor) {
        BackgroundColorSpan[] spans = s.getSpans(0, s.length(), BackgroundColorSpan.class);

        String[] words;
        if (s.toString().endsWith(" ")) {
            words = (s.toString() + "X").split("\\s");
        } else {
            words = s.toString().split("\\s");
        }
        int completedWordsCount = words.length - 1;
        if (spans.length != completedWordsCount) {
            for (BackgroundColorSpan span : spans) {
                s.removeSpan(span);
            }

            int currentIndex = 0;
            for (int i = 0; i < words.length - 1; i++) {
                s.setSpan(new BackgroundColorSpan(backgroundColor), currentIndex, currentIndex + words[i].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                currentIndex += words[i].length() + 1;
            }
        }
    }
}