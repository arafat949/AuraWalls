package com.aurawalls.app.activities;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.aurawalls.app.R;
import com.aurawalls.app.adapters.WallpaperAdapter;
import com.aurawalls.app.models.WallpaperModel;
import com.aurawalls.app.utils.FavoritesManager;
import java.util.List;
public class FavoritesActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Favorites");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        List<WallpaperModel> favs = FavoritesManager.getInstance(this).getAll();
        RecyclerView rv = findViewById(R.id.rv_favorites);
        TextView tvEmpty = findViewById(R.id.tv_empty);
        if (favs.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rv.setLayoutManager(new GridLayoutManager(this, 2));
            rv.setAdapter(new WallpaperAdapter(this, favs));
        }
    }
    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
