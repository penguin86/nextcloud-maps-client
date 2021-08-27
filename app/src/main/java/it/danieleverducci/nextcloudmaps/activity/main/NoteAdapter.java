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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import it.danieleverducci.nextcloudmaps.R;
import it.danieleverducci.nextcloudmaps.model.Note;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.RecyclerViewAdapter> implements Filterable {

    public static final int SORT_BY_TITLE = 0;
    public static final int SORT_BY_CREATED = 1;

    private Context context;
    private ItemClickListener itemClickListener;

    private List<Note> noteList = new ArrayList<>();
    private List<Note> noteListFiltered = new ArrayList<>();
    private int sortRule = SORT_BY_CREATED;

    public NoteAdapter(Context context, ItemClickListener itemClickListener) {
        this.context = context;
        this.itemClickListener = itemClickListener;
    }

    public void setNoteList(@NonNull List<Note> noteList) {
        this.noteList = noteList;
        this.noteListFiltered = new ArrayList<>(noteList);

        performSort();
        notifyDataSetChanged();
    }

    public Note get(int position) {
        return noteListFiltered.get(position);
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
    public RecyclerViewAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new RecyclerViewAdapter(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter holder, int position) {
        Note note = noteListFiltered.get(position);

        holder.tv_title.setText(Html.fromHtml(note.getTitle().trim()));
        holder.tv_content.setText(note.getContent().trim());
        holder.card_item.setCardBackgroundColor(context.getResources().getColor(R.color.defaultNoteColor));
    }

    @Override
    public int getItemCount() {
        return noteListFiltered.size();
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
            List <Note> filteredNotes = new ArrayList<>();

            if (charSequence.toString().isEmpty()) {
                filteredNotes.addAll(noteList);
            } else {
                for (Note note: noteList) {
                    String query = charSequence.toString().toLowerCase();
                    if (note.getTitle().toLowerCase().contains(query)) {
                        filteredNotes.add(note);
                    } else if (note.getContent().toLowerCase().contains(query)) {
                        filteredNotes.add(note);
                    }
                }
            }

            filterResults.values = filteredNotes;
            return filterResults;
        }
        //Run on ui thread
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            noteListFiltered.clear();
            noteListFiltered.addAll((Collection<? extends Note>) filterResults.values);

            performSort();
            notifyDataSetChanged();
        }
    };

    class RecyclerViewAdapter extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView card_item;
        TextView tv_title, tv_content;

        ItemClickListener itemClickListener;

        RecyclerViewAdapter(@NonNull View itemView, ItemClickListener itemClickListener) {
            super(itemView);

            card_item = itemView.findViewById(R.id.card_item);
            tv_title = itemView.findViewById(R.id.title);
            tv_content = itemView.findViewById(R.id.content);

            this.itemClickListener = itemClickListener;
            card_item.setOnClickListener(this);

            tv_content.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    private void performSort() {
        if (sortRule == SORT_BY_TITLE) {
            Collections.sort(noteListFiltered, Note.ByTitleAZ);
        } else if (sortRule == SORT_BY_CREATED) {
            Collections.sort(noteListFiltered, Note.ByLastCreated);
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}