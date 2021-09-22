package it.danieleverducci.nextcloudmaps.activity.detail;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.HashSet;

public class CategoriesSpinnerAdapter extends ArrayAdapter<String> {

    public CategoriesSpinnerAdapter(@NonNull Context context) {
        super(context, android.R.layout.simple_dropdown_item_1line);
    }

    // TODO: implement colors

    public void setCategoriesList(HashSet<String> categories) {
        clear();
        addAll(categories);
        notifyDataSetChanged();
    }

}
