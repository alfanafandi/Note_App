package com.example.note;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> implements Filterable {

    private List<Note> noteList;
    private List<Note> noteListFull; //
    private Context context;
    private OnItemClickListener listener;

    public NoteAdapter(List<Note> noteList, Context context) {
        this.noteList = new ArrayList<>(noteList);
        this.noteListFull = new ArrayList<>(noteList);
        this.context = context;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvContent.setText(note.getContent());


        String displayDate;
        if (note.getCreatedAt() != null && note.getCreatedAt().equals(note.getUpdatedAt())) {
            displayDate = "Dibuat: " + formatDate(note.getCreatedAt());
        } else {
            displayDate = "Diperbarui: " + formatDate(note.getUpdatedAt());
        }
        holder.tvDate.setText(displayDate);

        // Klik item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(note);
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDate;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    private String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "hari ini";
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
            return output.format(input.parse(rawDate));
        } catch (Exception e) {
            try {
                SimpleDateFormat inputOld = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
                return output.format(inputOld.parse(rawDate));
            } catch (Exception ex) {
                return rawDate;
            }
        }
    }


    @Override
    public Filter getFilter() {
        return noteFilter;
    }

    private final Filter noteFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Note> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(noteListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase(Locale.ROOT).trim();

                for (Note note : noteListFull) {
                    if ((note.getTitle() != null && note.getTitle().toLowerCase().contains(filterPattern)) ||
                            (note.getContent() != null && note.getContent().toLowerCase().contains(filterPattern))) {
                        filteredList.add(note);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            noteList.clear();
            noteList.addAll((List<Note>) results.values);
            notifyDataSetChanged();
        }
    };


    public void updateData(List<Note> newList) {
        noteList.clear();
        noteList.addAll(newList);

        noteListFull.clear();
        noteListFull.addAll(newList);

        notifyDataSetChanged();
    }
}
