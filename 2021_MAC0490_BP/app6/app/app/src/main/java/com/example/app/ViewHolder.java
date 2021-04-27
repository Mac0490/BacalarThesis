package com.example.app;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

public class ViewHolder extends RecyclerView.ViewHolder {
    View view;

    TextView textView;
    ImageView imageView;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        view = itemView;
    }


    public void setDetails(Context context,String image, String url){

        textView = view.findViewById(R.id.textview);
        imageView = view.findViewById(R.id.imageview);
        textView.setText(url);
        Picasso.get().load(image).into(imageView);

        Animation animation = AnimationUtils.loadAnimation(context,android.R.anim.slide_in_left);
        itemView.startAnimation(animation);

    }

}
