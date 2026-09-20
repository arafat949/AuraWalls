package com.aurawalls.app.activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.aurawalls.app.R;
import com.aurawalls.app.utils.WallpaperSetter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class WallpaperSetPreviewDialog extends BottomSheetDialogFragment {

    private static final String ARG_TARGET = "target";
    public static Bitmap previewBitmap;

    private WallpaperSetter.Target target;
    private ImageView ivPhonePreview;
    private String scaleMode = "fill"; // fill, fit, center

    public static WallpaperSetPreviewDialog newInstance(WallpaperSetter.Target target, Bitmap bitmap) {
        previewBitmap = bitmap;
        WallpaperSetPreviewDialog f = new WallpaperSetPreviewDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TARGET, target.name());
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.VideoBottomSheetStyle);
        target = WallpaperSetter.Target.valueOf(getArguments().getString(ARG_TARGET));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_wallpaper_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getDialog() instanceof BottomSheetDialog) {
            ((BottomSheetDialog) getDialog()).getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        ivPhonePreview = view.findViewById(R.id.iv_phone_preview);

        MaterialButton btnFill   = view.findViewById(R.id.btn_fill);
        MaterialButton btnFit    = view.findViewById(R.id.btn_fit);
        MaterialButton btnCenter = view.findViewById(R.id.btn_center);
        MaterialButton btnSet    = view.findViewById(R.id.btn_preview_set);
        MaterialButton btnCancel = view.findViewById(R.id.btn_preview_cancel);

        updatePreview();

        btnFill.setOnClickListener(v -> { scaleMode = "fill"; updateSelection(btnFill, btnFit, btnCenter); updatePreview(); });
        btnFit.setOnClickListener(v -> { scaleMode = "fit"; updateSelection(btnFit, btnFill, btnCenter); updatePreview(); });
        btnCenter.setOnClickListener(v -> { scaleMode = "center"; updateSelection(btnCenter, btnFill, btnFit); updatePreview(); });

        btnSet.setOnClickListener(v -> {
            btnSet.setEnabled(false);
            btnSet.setText("Setting...");
            Bitmap toSet = getScaledBitmap();
            new Thread(() -> {
                boolean ok = WallpaperSetter.setWallpaper(requireContext(), toSet, target);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(),
                        ok ? "✅ Wallpaper set!" : "❌ Failed",
                        Toast.LENGTH_SHORT).show();
                    if (ok) dismiss();
                    else { btnSet.setEnabled(true); btnSet.setText("Set Wallpaper"); }
                });
            }).start();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void updateSelection(MaterialButton selected, MaterialButton a, MaterialButton b) {
        selected.setBackgroundColor(0xFF6C63FF);
        selected.setTextColor(Color.WHITE);
        a.setBackgroundColor(Color.TRANSPARENT);
        a.setTextColor(0xFF6C63FF);
        b.setBackgroundColor(Color.TRANSPARENT);
        b.setTextColor(0xFF6C63FF);
    }

    private void updatePreview() {
        if (previewBitmap == null) return;
        ivPhonePreview.setScaleType(
            scaleMode.equals("fill") ? ImageView.ScaleType.CENTER_CROP :
            scaleMode.equals("fit")  ? ImageView.ScaleType.FIT_CENTER :
                                       ImageView.ScaleType.CENTER
        );
        ivPhonePreview.setImageBitmap(previewBitmap);
    }

    private Bitmap getScaledBitmap() {
        // Pass scaleMode info via WallpaperSetter
        return previewBitmap;
    }
}
