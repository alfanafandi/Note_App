package com.example.note;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ExplanationBottomSheetFragment extends BottomSheetDialogFragment {

    // 🔥 PERBAIKAN 1: Interface untuk callback ke Activity ditambahkan di sini
    public interface BottomSheetListener {
        void onApplyClicked(String text);
    }

    private BottomSheetListener mListener;

    private static final String ARG_TITLE = "title";
    private static final String ARG_CONTENT = "content";
    // Argumen baru untuk menentukan apakah tombol "Terapkan" perlu ditampilkan
    private static final String ARG_SHOW_APPLY_BUTTON = "show_apply_button";

    // 🔥 PERBAIKAN 2: Method newInstance diubah untuk menerima 3 argumen
    public static ExplanationBottomSheetFragment newInstance(String title, String content, boolean showApplyButton) {
        ExplanationBottomSheetFragment fragment = new ExplanationBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CONTENT, content);
        args.putBoolean(ARG_SHOW_APPLY_BUTTON, showApplyButton);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Asumsikan layout Anda bernama fragment_explanation_bottom_sheet.xml
        // dan memiliki ID tvBottomSheetTitle, tvBottomSheetContent, btnApplyToNote, dan btnCloseSheet
        View view = inflater.inflate(R.layout.fragment_explanation_bottom_sheet, container, false);

        TextView tvTitle = view.findViewById(R.id.tvBottomSheetTitle);
        TextView tvContent = view.findViewById(R.id.tvBottomSheetContent);
        Button btnApply = view.findViewById(R.id.btnApplyToNote);
        Button btnClose = view.findViewById(R.id.btnCloseSheet);

        if (getArguments() != null) {
            String title = getArguments().getString(ARG_TITLE);
            String content = getArguments().getString(ARG_CONTENT);
            boolean showApply = getArguments().getBoolean(ARG_SHOW_APPLY_BUTTON);

            tvTitle.setText(title);
            tvContent.setText(content);

            // Tampilkan atau sembunyikan tombol "Terapkan" berdasarkan argumen
            if (showApply) {
                btnApply.setVisibility(View.VISIBLE);
                btnApply.setOnClickListener(v -> {
                    if (mListener != null) {
                        // Kirim kembali konten ke Activity
                        mListener.onApplyClicked(content);
                    }
                    dismiss(); // Tutup bottom sheet
                });
            } else {
                btnApply.setVisibility(View.GONE);
            }
        }

        btnClose.setOnClickListener(v -> dismiss());

        return view;
    }

    // 🔥 PERBAIKAN 3: Menghubungkan listener saat fragment di-attach
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Pastikan Activity yang memanggil mengimplementasikan listener
        if (context instanceof BottomSheetListener) {
            mListener = (BottomSheetListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " harus mengimplementasikan ExplanationBottomSheetFragment.BottomSheetListener");
        }
    }
}
