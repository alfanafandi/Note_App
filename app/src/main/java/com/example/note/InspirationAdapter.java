package com.example.note;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InspirationAdapter extends RecyclerView.Adapter<InspirationAdapter.ViewHolder> {

    private Context context;
    private List<Inspiration> inspirationList;

    public InspirationAdapter(Context context, List<Inspiration> inspirationList) {
        this.context = context;
        this.inspirationList = inspirationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inspiration, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Inspiration item = inspirationList.get(position);
        holder.tvPrompt.setText(item.getPrompt());
        holder.tvAuthor.setText(item.getAuthor());

        holder.btnAddNote.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddNoteActivity.class);
            intent.putExtra("template_text", item.getPrompt());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return inspirationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPrompt, tvAuthor;
        Button btnAddNote;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPrompt = itemView.findViewById(R.id.tvPrompt);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            btnAddNote = itemView.findViewById(R.id.btnAddNote);
        }
    }
}
