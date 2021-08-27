/*
 * Nextcloud Notes Tutorial for Android
 *
 * @copyright Copyright (c) 2020 John Doe <john@doe.com>
 * @author John Doe <john@doe.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.danieleverducci.nextcloudmaps.activity.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.danieleverducci.nextcloudmaps.R;

public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.ViewHolder> {

    @NonNull
    private final Context context;

    public static class NavigationItem {
        @NonNull
        public String id;
        @NonNull
        public String label;
        @DrawableRes
        public int icon;

        public NavigationItem(@NonNull String id, @NonNull String label, @DrawableRes int icon) {
            this.id = id;
            this.label = label;
            this.icon = icon;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final View view;

        @NonNull
        private final TextView name;
        @NonNull
        private final ImageView icon;

        private NavigationItem currentItem;

        ViewHolder(@NonNull View itemView, @NonNull final ClickListener clickListener) {
            super(itemView);
            view = itemView;

            this.name = itemView.findViewById(R.id.navigationItemLabel);
            this.icon = itemView.findViewById(R.id.navigationItemIcon);

            icon.setOnClickListener(view -> clickListener.onItemClick(currentItem));
            itemView.setOnClickListener(view -> clickListener.onItemClick(currentItem));
        }

        private void bind(@NonNull NavigationItem item) {
            int color = view.getResources().getColor(R.color.accent);

            currentItem = item;
            name.setText(item.label);

            name.setTextColor(color);

            icon.setImageDrawable(DrawableCompat.wrap(icon.getResources().getDrawable(item.icon)));
            icon.setColorFilter(color);
            icon.setVisibility(View.VISIBLE);
        }

    }

    public interface ClickListener {
        void onItemClick(NavigationItem item);
    }

    @NonNull
    private List<NavigationItem> items = new ArrayList<>();
    private String selectedItem = null;
    @NonNull
    private final ClickListener clickListener;

    public NavigationAdapter(@NonNull Context context, @NonNull ClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_navigation, parent, false);
        return new ViewHolder(v, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(@NonNull List<NavigationItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

}
