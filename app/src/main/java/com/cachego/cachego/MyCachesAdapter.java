package com.cachego.cachego;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

public class MyCachesAdapter extends ArrayAdapter<Cache> {
    public MyCachesAdapter(@NonNull Context context, ArrayList<Cache> alMyCaches)
    {
        super(context, 0, alMyCaches);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        //what we want to display
        Cache cache_item = getItem(position);
        Context context;

        //how do we want to see it

        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.my_caches_item, parent, false);
        }

        TextView CacheName = convertView.findViewById(R.id.tvCacheName);
        TextView CacheAreaName = convertView.findViewById(R.id.tvAreaName);
        ImageView CacheDifficulty = convertView.findViewById(R.id.ivDifficulty);

        CacheName.setText(cache_item.getName());
        CacheAreaName.setText(cache_item.getArea_name());

        switch (cache_item.getDifficulty()) {
            case "easy":
                CacheDifficulty.setImageResource(R.drawable.map_cache_easy);
                break;
            case "normal":
                CacheDifficulty.setImageResource(R.drawable.map_cache_normal);
                break;
            case "hard":
                CacheDifficulty.setImageResource(R.drawable.map_cache_hard);
                break;
        }

        CardView cvItem = convertView.findViewById(R.id.cvMyCaches);
        cvItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StaticFields sf = new StaticFields();
                sf.setSelectedId("" + cache_item.getId());
                sf.setPreviousFragment(new MyCachesFragment());

                FragmentManager manager = ((MapsActivity)view.getContext()).getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                Fragment OverlayFragment = new CacheDetailsFragment();
                transaction.replace(R.id.overlay_fragment, OverlayFragment);
                transaction.commit();
            }
        });

        return convertView;
    }
}
