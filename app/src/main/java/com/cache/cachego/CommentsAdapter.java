package com.cache.cachego;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class CommentsAdapter extends ArrayAdapter<Comment> {
    private Context context;

    public CommentsAdapter(@NonNull Context context, ArrayList<Comment> alComments) {
        super(context, 0, alComments);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //what we want to display
        Comment comment = getItem(position);
        //how do we want to see it

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cache_comment_item, parent, false);
        }

        TextView CommentDate = convertView.findViewById(R.id.tvCommentDate);
        TextView CommentContent = convertView.findViewById(R.id.tvComment);
        TextView CommentAuthor = convertView.findViewById(R.id.tvCommentAuthor);

        CommentDate.setText(comment.getDate());
        CommentContent.setText(comment.getComment());

        String Author = "- " + comment.getAuthor();
        CommentAuthor.setText(Author);

        return convertView;
    }
}
