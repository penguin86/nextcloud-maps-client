package it.danieleverducci.nextcloudmaps.activity.detail;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.ArrayList;
import java.util.HashSet;

import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.model.Geofavorite;

public class CategoriesAdapter extends ArrayAdapter<String> {

    public CategoriesAdapter(@NonNull Context context) {
        super(context, R.layout.category_listitem, R.id.category_name, new ArrayList<>());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        TextView categoryName = v.findViewById(R.id.category_name);
        Drawable backgroundDrawable = categoryName.getBackground();
        DrawableCompat.setTint(
                backgroundDrawable,
                Geofavorite.categoryColorFromName(categoryName.getText().toString()) == 0
                        ? v.getContext().getColor(R.color.defaultBrand)
                        : Geofavorite.categoryColorFromName(categoryName.getText().toString())
        );
        return v;
    }

    public void setCategoriesList(HashSet<String> categories) {
        clear();
        addAll(categories);
        notifyDataSetChanged();
    }

}
