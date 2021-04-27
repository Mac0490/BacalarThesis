package com.example.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SharedPref extends AppCompatActivity {

    RecyclerView recyclerView;
    SharedPreferences shrdpreferences;
    JSONObject saved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_pref);

        recyclerView = findViewById(R.id.recycled_view);

        shrdpreferences = getSharedPreferences("TEXT",Context.MODE_PRIVATE);
        try {
            saved = new JSONObject(shrdpreferences.getString("SAVED",""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(SharedPref.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new Adapter());


    }

    public class Adapter extends RecyclerView.Adapter<Adapter.Holder> {
        @NonNull
        @Override
        public Adapter.Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(SharedPref.this)
                    .inflate(R.layout.rowitem,viewGroup,false);
            Holder holder = new Holder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = recyclerView.getChildPosition(v);
                    Intent intentPrev = new Intent(SharedPref.this,
                            MainActivity.class);
                    intentPrev.putExtra("POSITION",position);
                    startActivity(intentPrev);
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull Adapter.Holder holder, int i) {
            try {
                holder.textView.setText(saved.getString("SAVED"+i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return saved.length();
        }

        public class Holder extends RecyclerView.ViewHolder {
            TextView textView, textViewAddTime;


            public Holder(@NonNull View itemView) {
                super(itemView);
                textView =  itemView.findViewById(R.id.text_view);
                textViewAddTime = itemView.findViewById(R.id.txt_date);
                Calendar cal = Calendar.getInstance();
                Date date = cal.getTime();
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                textViewAddTime.setText(dateFormat.format(date));


            }

        }


    }
}