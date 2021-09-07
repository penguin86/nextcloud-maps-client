/*
 * Nextcloud Maps Geofavorites for Android
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
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.model.Geofavorite;

public class GeofavoriteAdapter extends RecyclerView.Adapter<GeofavoriteAdapter.GeofavoriteViewHolder> implements Filterable {

    public static final int SORT_BY_TITLE = 0;
    public static final int SORT_BY_CREATED = 1;

    private Context context;
    private ItemClickListener itemClickListener;

    private List<Geofavorite> geofavoriteList = new ArrayList<>();
    private List<Geofavorite> geofavoriteListFiltered = new ArrayList<>();
    private int sortRule = SORT_BY_CREATED;

    public GeofavoriteAdapter(Context context, ItemClickListener itemClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;
    }

    public void setGeofavoriteList(@NonNull List<Geofavorite> geofavoriteList) {
        this.geofavoriteList = geofavoriteList;
        this.geofavoriteListFiltered = new ArrayList<>(geofavoriteList);

        performSort();
        notifyDataSetChanged();
    }

    public Geofavorite get(int position) {
        return geofavoriteListFiltered.get(position);
    }

    public int getSortRule() {
        return sortRule;
    }

    public void setSortRule(int sortRule) {
        this.sortRule = sortRule;

        performSort();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GeofavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_geofav, parent, false);
        return new GeofavoriteViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull GeofavoriteViewHolder holder, int position) {
        Geofavorite geofavorite = geofavoriteListFiltered.get(position);

        holder.tv_title.setText(Html.fromHtml(geofavorite.getName()));
        holder.tv_content.setText(geofavorite.getComment());
    }

    @Override
    public int getItemCount() {
        return geofavoriteListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        // Run on Background thread.
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults filterResults = new FilterResults();
            List <Geofavorite> filteredGeofavorites = new ArrayList<>();

            if (charSequence.toString().isEmpty()) {
                filteredGeofavorites.addAll(geofavoriteList);
            } else {
                for (Geofavorite geofavorite : geofavoriteList) {
                    String query = charSequence.toString().toLowerCase();
                    if (geofavorite.getName() != null && geofavorite.getName().toLowerCase().contains(query)) {
                        filteredGeofavorites.add(geofavorite);
                    } else if (geofavorite.getComment() != null && geofavorite.getComment().toLowerCase().contains(query)) {
                        filteredGeofavorites.add(geofavorite);
                    }
                }
            }

            filterResults.values = filteredGeofavorites;
            return filterResults;
        }
        //Run on ui thread
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            geofavoriteListFiltered.clear();
            geofavoriteListFiltered.addAll((Collection<? extends Geofavorite>) filterResults.values);

            performSort();
            notifyDataSetChanged();
        }
    };

    class GeofavoriteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tv_title, tv_content;
        ImageView bt_context_menu;
        ImageView bt_share;

        ItemClickListener itemClickListener;


        GeofavoriteViewHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
            super(itemView);

            tv_title = itemView.findViewById(R.id.title);
            tv_content = itemView.findViewById(R.id.content);
            bt_context_menu = itemView.findViewById(R.id.geofav_context_menu_bt);
            bt_share = itemView.findViewById(R.id.geofav_share_bt);

            this.itemClickListener = itemClickListener;
            itemView.setOnClickListener(this);

            bt_context_menu.setOnClickListener(this);
            bt_share.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.geofav_context_menu_bt:
                    openContextMenu(view);
                    break;
                case R.id.geofav_share_bt:
                    if (itemClickListener != null)
                        itemClickListener.onItemShareClick(get(getAdapterPosition()));
                    break;
                default:
                    itemClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    private void performSort() {
        if (sortRule == SORT_BY_TITLE) {
            Collections.sort(geofavoriteListFiltered, Geofavorite.ByTitleAZ);
        } else if (sortRule == SORT_BY_CREATED) {
            Collections.sort(geofavoriteListFiltered, Geofavorite.ByLastCreated);
        }
    }

    private void openContextMenu(View v) {
        //.showContextMenuForChild(v);
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
        void onItemShareClick(Geofavorite item);
    }

    public interface ContextMenuClickListener {
        void onContextMenuClick();
    }
}