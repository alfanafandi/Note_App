package com.example.note;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private Context context;
    private DBHelper dbHelper;
    private OnCategoryClickListener listener;

    // Interface untuk menangani klik item kategori (untuk MainActivity & AddNoteActivity)
    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categoryList, Context context, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.context = context;
        this.dbHelper = new DBHelper(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvCategoryName.setText(category.getName());

        // Reset tampilan menu aksi
        holder.layoutActions.setVisibility(View.GONE);


        holder.itemView.setOnClickListener(v -> {
            if (holder.layoutActions.getVisibility() == View.VISIBLE) {
                // Jika menu aksi sedang tampil, klik biasa menutupnya
                holder.layoutActions.setVisibility(View.GONE);
            } else {
                // Jika tidak, kirim event ke listener (MainActivity / AddNoteActivity)
                if (listener != null) listener.onCategoryClick(category);
            }
        });

        // Tekan lama lalu tampilkan menu edit/hapus
        holder.itemView.setOnLongClickListener(v -> {
            holder.layoutActions.setVisibility(
                    holder.layoutActions.getVisibility() == View.VISIBLE
                            ? View.GONE : View.VISIBLE
            );
            return true;
        });

        // Tombol Edit
        holder.btnEditCategory.setOnClickListener(v -> showEditDialog(category, position));

        // Tombol Hapus
        holder.btnDeleteCategory.setOnClickListener(v -> showDeleteConfirmation(category, position));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    // ======================================================
    // ========== DIALOG EDIT & KONFIRMASI HAPUS ============
    // ======================================================

    private void showEditDialog(Category category, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Kategori");

        final EditText input = new EditText(context);
        input.setText(category.getName());
        input.setPadding(50, 40, 50, 40);
        builder.setView(input);

        builder.setPositiveButton("Simpan", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                boolean updated = dbHelper.updateCategory(category.getId(), newName);
                if (updated) {
                    category.setName(newName);
                    notifyItemChanged(position);
                    Toast.makeText(context, "Kategori diperbarui", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Gagal memperbarui kategori", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Nama kategori tidak boleh kosong", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showDeleteConfirmation(Category category, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Hapus Kategori")
                .setMessage("Yakin ingin menghapus \"" + category.getName() + "\"?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    boolean deleted = dbHelper.deleteCategory(category.getId());
                    if (deleted) {
                        categoryList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, categoryList.size());
                        Toast.makeText(context, "Kategori dihapus", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Gagal menghapus kategori", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    // ======================================================
    // ================= VIEW HOLDER ========================
    // ======================================================

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        ImageView btnEditCategory, btnDeleteCategory;
        View layoutActions;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            btnEditCategory = itemView.findViewById(R.id.btnEditCategory);
            btnDeleteCategory = itemView.findViewById(R.id.btnDeleteCategory);
            layoutActions = itemView.findViewById(R.id.layoutActions);
        }
    }
}
