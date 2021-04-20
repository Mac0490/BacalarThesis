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
    ArrayList<Integer> titleIDList;

    void boomMenu() {

        imageIDList = new ArrayList<>();
        titleIDList = new ArrayList<Integer>();
        setInitialDataInBoomMenu();

    }

    private void setInitialDataInBoomMenu() {
        imageIDList.add(R.drawable.scalpture);
        imageIDList.add(R.drawable.chair);
        imageIDList.add(R.drawable.vazy);
        imageIDList.add(R.drawable.desks);
        imageIDList.add(R.drawable.lamp);
        imageIDList.add(R.drawable.ic_launcher_foreground);
        

        titleIDList.add(R.string.boommenuArt);
        titleIDList.add(R.string.boommenuChairs);
        titleIDList.add(R.string.boommenuPlants);
        titleIDList.add(R.string.boommenuDesks);
        titleIDList.add(R.string.boommenuLamps);
        titleIDList.add(R.string.boommenuMyUrl);

    }
}
