package com.example.app;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;

import java.util.ArrayList;

public class BoomMenu {


    ArrayList<Integer> imageIDList;
    ArrayList<String> titleIDList;

    void boomMenu() {

        imageIDList = new ArrayList<>();
        titleIDList = new ArrayList<String>();
        setInitialDataInBoomMenu();

    }

    private void setInitialDataInBoomMenu() {
        imageIDList.add(R.drawable.scalpture);
        imageIDList.add(R.drawable.chair);
        imageIDList.add(R.drawable.vazy);
        imageIDList.add(R.drawable.desks);
        imageIDList.add(R.drawable.lamp);
        imageIDList.add(R.drawable.ic_launcher_foreground);


        titleIDList.add("Scalptures ");
        titleIDList.add("Chairs ");
        titleIDList.add("Vases ");
        titleIDList.add("Desks ");
        titleIDList.add("Lamps ");
        titleIDList.add("MY3Dimg ");


    }
}
