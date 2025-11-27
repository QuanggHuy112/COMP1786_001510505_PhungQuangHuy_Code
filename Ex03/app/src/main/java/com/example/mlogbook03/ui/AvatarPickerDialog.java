package com.example.mlogbook03.ui;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.mlogbook03.R;

public class AvatarPickerDialog extends DialogFragment {

    public interface AvatarPickListener {
        void onAvatarPicked(int resId);
    }

    private AvatarPickListener listener;
    private int[] avatarResIds = {
            R.drawable.avatar_01,
            R.drawable.avatar_02,
            R.drawable.avatar_03,
            R.drawable.avatar_04
            // thêm nhiều avatar ở đây
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AvatarPickListener) {
            listener = (AvatarPickListener) context;
        } else if (getParentFragment() instanceof AvatarPickListener) {
            listener = (AvatarPickListener) getParentFragment();
        } else {
            // fallback: không crash, but nothing will happen
            listener = null;
        }
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_avatar_picker, null);
        GridView grid = v.findViewById(R.id.avatar_grid);
        AvatarGridAdapter adapter = new AvatarGridAdapter(getContext(), avatarResIds);
        grid.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setTitle("Chọn avatar")
                .setView(v)
                .setNegativeButton("Hủy", (d, which) -> dismiss());

        grid.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            int resId = avatarResIds[position];
            if (listener != null) listener.onAvatarPicked(resId);
            dismiss();
        });

        return builder.create();
    }
}

