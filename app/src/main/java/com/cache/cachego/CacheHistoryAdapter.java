package com.cache.cachego;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class CacheHistoryAdapter extends ArrayAdapter<Cache> {
    private boolean isPressed;
    ArrayList<String> alFavourites;
    StaticFields sf = new StaticFields();

    public CacheHistoryAdapter(@NonNull Context context, ArrayList<Cache> alCacheHistory) {
        super(context, 0, alCacheHistory);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        alFavourites = sf.getAlFavorites();

        //what we want to display
        Cache cache_item = getItem(position);
        Context context;

        //how do we want to see it

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.cache_history_item, parent, false);
        }

        TextView CacheName = convertView.findViewById(R.id.tvCacheName);
        TextView CacheDateFound = convertView.findViewById(R.id.tvFoundDate);
        ImageView CacheDifficulty = convertView.findViewById(R.id.ivDifficulty);

        CacheName.setText(cache_item.getName());
        CacheDateFound.setText("12/10/2022");

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


        ImageButton imgFavorite = convertView.findViewById(R.id.ibFavorite);

        try {
            for (int i = 0; i < alFavourites.size(); i++) {
                if (cache_item.getId().equals(alFavourites.get(i))) {
                    imgFavorite.setImageResource(R.drawable.cache_favorite_selected);
                    isPressed = true;
                    Log.d(TAG, "FAVOURITE: FOUND");
                }
                else {
                    isPressed = false;
                }
            }
        } catch (Exception e) {

        }

        imgFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable selected = getContext().getDrawable(R.drawable.cache_favorite_selected);
                Drawable unselected = getContext().getDrawable(R.drawable.cache_favorite_unselected);

                if (isPressed) {
                    imgFavorite.setImageResource(R.drawable.cache_favorite_unselected);
                    isPressed = false;
                    //Remove Favourite
                    RemoveFavourite(cache_item);
                } else {
                    imgFavorite.setImageResource(R.drawable.cache_favorite_selected);
                    isPressed = true;
                    //Set Favourite
                    SetAsFavourite(cache_item);
                }
            }
        });

        CardView cvItem = convertView.findViewById(R.id.cvCacheHistory);
        cvItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sf.setSelectedId("" + cache_item.getId());
                sf.setPreviousFragment(new CacheHisoryFragment());

                FragmentManager manager = ((MapsActivity)view.getContext()).getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                Fragment OverlayFragment = new CacheDetailsFragment();
                transaction.replace(R.id.overlay_fragment, OverlayFragment);
                transaction.commit();
            }
        });

        return convertView;
    }



    //Set Cache as Favourite
    private void SetAsFavourite(Cache c) {
        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("users");
        refUsers.child("" + sf.getAuthor()).child("favourites").child("" + c.getId()).child("id").setValue("" + c.getId());
    }

    private void RemoveFavourite(Cache c) {
        DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("users");
        refUsers.child("" + sf.getAuthor()).child("favourites").child("" + c.getId()).removeValue();
    }
}
