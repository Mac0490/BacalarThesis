package com.example.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ScrollingView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.PixelCopy;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import at.markushi.ui.CircleButton;


public class MainActivity extends AppCompatActivity {
    private ArFragment arFragment;
    EditText urlTextLinks;
    Button btSaveLink;
    JSONObject saved = new JSONObject();
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    ProgressBar progressBar;


    public LinearLayout gallery;
    public LinearLayout secondPlantGallery;
    public LinearLayout thirdPlantGallery;
    public LinearLayout fourthStatueGallery;
    public LinearLayout fifthLampGallery;

    BoomMenuButton boombutton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlTextLinks = findViewById(R.id.url_link);
        btSaveLink = findViewById(R.id.savelink_btn);
        btSaveLink.setBackgroundColor(Color.BLACK);
        btSaveLink.setTextColor(Color.GREEN);


        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        myUrlImage();
        InitializeGalleryData();
        setUpPlane();
        BoomMenuData();
        takeScreenshot();
    }

    private void initialize() {
        preferences = getSharedPreferences("text", Context.MODE_PRIVATE);
        editor = preferences.edit();
        urlTextLinks = findViewById(R.id.url_link);
        btSaveLink = findViewById(R.id.savelink_btn);


    }

    public void myUrlImage() {

        initialize();
        Intent intent = getIntent();
        if (intent.getIntExtra("position", -1) != -1) {
            try {
                String s = urlTextLinks.getText().toString();
                if (!preferences.getString("saved", "").equals(""))
                    saved = new JSONObject(preferences.getString("saved", ""));
                urlTextLinks.setText(saved.getString("saved" + intent.getIntExtra("position", 0)));
                s = saved.getString("saved" + intent.getIntExtra("position", 0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        btSaveLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = urlTextLinks.getText().toString();
                if (!s.equals("")) {
                    try {
                        if (!preferences.getString("saved", "").equals("")) {
                            saved = new JSONObject(preferences.getString("saved", ""));
                            saved.put("saved" + saved.length(), s);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("testing", saved + "");
                    editor.putString("saved", saved.toString());
                    editor.apply();
                    urlTextLinks.setText("");
                    Intent intent1 = new Intent(MainActivity.this, SharedPref.class);
                    startActivity(intent1);
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save) {
            if (preferences.getString("saved", "").equals("")) {
                Toast.makeText(getApplicationContext(), "nothing to save",
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                Intent intent = new Intent(MainActivity.this,
                        SharedPref.class);
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private void InitializeGalleryData() {

        FirebaseApp.initializeApp(this);

        InitializeGallery initializeGallery = new InitializeGallery();

        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);
        gallery = (LinearLayout) findViewById(R.id.gallery_layout);
        secondPlantGallery = (LinearLayout) findViewById(R.id.gallery_layout_Chairs);
        thirdPlantGallery = (LinearLayout) findViewById(R.id.gallery_layout_Vases);
        fourthStatueGallery = (LinearLayout) findViewById(R.id.gallery_layout_Statues);
        fifthLampGallery = (LinearLayout) findViewById(R.id.gallery_layout_last);

        ImageView urlLink = new ImageView(this);
        urlLink.setImageResource(R.drawable.ic_launcher_background);
        urlLink.setContentDescription("url");

        ImageView standinglampi = new ImageView(this);
        standinglampi.setImageResource(R.drawable.standinglampip);
        standinglampi.setContentDescription("standinglampi");

        ImageView desktree = new ImageView(this);
        desktree.setImageResource(R.drawable.treedesk);
        desktree.setContentDescription("desktree");

        ImageView coctaildesk = new ImageView(this);
        coctaildesk.setImageResource(R.drawable.bardesk);
        coctaildesk.setContentDescription("coctaildesk");

        ImageView coctailchair = new ImageView(this);
        coctailchair.setImageResource(R.drawable.barchair);
        coctailchair.setContentDescription("coctailchair");

        ImageView paintedart = new ImageView(this);
        paintedart.setImageResource(R.drawable.artpainted);
        paintedart.setContentDescription("paintedart");

        ImageView desklamp = new ImageView(this);
        desklamp.setImageResource(R.drawable.mpoo);
        desklamp.setContentDescription("desklamp");

        ImageView desktransparents = new ImageView(this);
        desktransparents.setImageResource(R.drawable.deskstranspa);
        desktransparents.setContentDescription("desktransparents");

        ImageView coffeplant = new ImageView(this);
        coffeplant.setImageResource(R.drawable.coffeplant);
        coffeplant.setContentDescription("coffeplant");

        ImageView plantherb = new ImageView(this);
        plantherb.setImageResource(R.drawable.plantherb);
        plantherb.setContentDescription("plantherb");

        ImageView lamplight = new ImageView(this);
        lamplight.setImageResource(R.drawable.lamplight);
        lamplight.setContentDescription("lamplight");

        ImageView standinglamp = new ImageView(this);
        standinglamp.setImageResource(R.drawable.standintlamplit);
        standinglamp.setContentDescription("standintlamplit");


        ImageView cactusar = new ImageView(this);
        cactusar.setImageResource(R.drawable.cactuslit);
        cactusar.setContentDescription("cactusar");

        ImageView houseplant = new ImageView(this);
        houseplant.setImageResource(R.drawable.cactuslit);
        houseplant.setContentDescription("houseplant");

        ImageView plant = new ImageView(this);
        plant.setImageResource(R.drawable.plantlit);
        plant.setContentDescription("plant");


        ImageView elephant = new ImageView(this);
        elephant.setImageResource(R.drawable.elephantlit);
        elephant.setContentDescription("elephant");

        ImageView skyscraper = new ImageView(this);
        skyscraper.setImageResource(R.drawable.skyscraperlit);
        skyscraper.setContentDescription("skyscraper");

        ImageView elf = new ImageView(this);
        elf.setImageResource(R.drawable.elflit);
        elf.setContentDescription("elf");

        ImageView bowl = new ImageView(this);
        bowl.setImageResource(R.drawable.bowl);
        bowl.setContentDescription("bowl");

        ImageView desk = new ImageView(this);
        desk.setImageResource(R.drawable.desktransparentasset);
        desk.setContentDescription("woodenbowl");

        ImageView statuelit = new ImageView(this);
        statuelit.setImageResource(R.drawable.statuelit);
        statuelit.setContentDescription("statuelit");

        urlTextLinks.setOnClickListener(view -> {
            progressBar = findViewById(R.id.progressbar);
            progressBar.setVisibility(View.VISIBLE);
            Uri.parse(String.valueOf(urlTextLinks));
            buildARModelAsset(Uri.parse(String.valueOf(urlTextLinks)));

        });

        desktree.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File filedesktree = File.createTempFile("desktree", "glb");

                initializeGallery.assetRefDeskTree.getFile(filedesktree).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARdModelDeskTree(filedesktree);


                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        coctailchair.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File filecoctailchair = File.createTempFile("coctailchair", "glb");

                initializeGallery.assetRefCoctailChair.getFile(filecoctailchair).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARdModelCoctailChair(filecoctailchair);


                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        coctaildesk.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File filecoctaildesk = File.createTempFile("coctaildesk", "glb");

                initializeGallery.assetRefCoctailDesk.getFile(filecoctaildesk).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARdModelCoctailDesk(filecoctaildesk);


                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        paintedart.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File filepaintedart = File.createTempFile("paintedart", "glb");

                initializeGallery.assetRefPaintedArt.getFile(filepaintedart).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARdModelPaintedArt(filepaintedart);


                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        plantherb.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File filePlantHerb = File.createTempFile("plantherb", "glb");

                initializeGallery.assetRefPlantHerb.getFile(filePlantHerb).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        buildARModelPlantHerb(filePlantHerb);


                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        lamplight.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File filelamplight = File.createTempFile("lamplight", "gltf");
                initializeGallery.assetRefLampLight.getFile(filelamplight).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARdModelLampLight(filelamplight);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        houseplant.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File filehouseplant = File.createTempFile("houseplant", "gltf");
                initializeGallery.assetRefHouseplant.getFile(filehouseplant).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARdModelHousePlant(filehouseplant);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        bowl.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File fileBowl = File.createTempFile("bowlpainted", "glb");
                initializeGallery.assetRefBowl.getFile(fileBowl).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARdModelBowl(fileBowl);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        standinglampi.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File filelamp = File.createTempFile("standinglampi", "glb");
                initializeGallery.assetReflamp.getFile(filelamp).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARModelLamp(filelamp);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        desklamp.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File filedesklamp = File.createTempFile("lampdesk", "glb");
                initializeGallery.assetRefdesklamp.getFile(filedesklamp).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARdModelDeskLamp(filedesklamp);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        cactusar.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File file = File.createTempFile("plant", "glb");
                initializeGallery.assetRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARdModel(file);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        plant.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File filePlant = File.createTempFile("plant", "glb");
                initializeGallery.assetRef3.getFile(filePlant).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARdModelPlant(filePlant);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        standinglamp.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File file2 = File.createTempFile("standinglamp", "glb");
                initializeGallery.assetRef2.getFile(file2).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        builARdModel2(file2);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        elf.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File fileElf = File.createTempFile("statueman", "glb");
                initializeGallery.assetRefElf.getFile(fileElf).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARdModelElf(fileElf);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        elephant.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File fileElephant = File.createTempFile("elephant", "glb");
                initializeGallery.assetRefElephant.getFile(fileElephant).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARdModelElephant(fileElephant);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        });


        skyscraper.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File fileSkyscraper = File.createTempFile("skyscraper", "glb");
                initializeGallery.assetRefSkyscraper.getFile(fileSkyscraper).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARdModelSkyscraper(fileSkyscraper);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        coffeplant.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File fileCoffePlant = File.createTempFile("coffeplant", "glb");
                initializeGallery.assetRefCoffePlant.getFile(fileCoffePlant).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARdModelCoffePlant(fileCoffePlant);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        desk.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File fileDesk = File.createTempFile("scene", "gltf");
                initializeGallery.assetRefDesk.getFile(fileDesk).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARdModelDesk(fileDesk);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        statuelit.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File fileStatue = File.createTempFile("statuelittle", "glb");
                initializeGallery.assetStatueLit.getFile(fileStatue).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARdModelStatue(fileStatue);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        desktransparents.setOnClickListener(view -> {
            try {
                progressBar = findViewById(R.id.progressbar);
                progressBar.setVisibility(View.VISIBLE);
                File fileDeskTransparent = File.createTempFile("desktransparents", "glb");
                initializeGallery.assetRefDeskTransparent.getFile(fileDeskTransparent).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                        builARdModelDeskTransparent(fileDeskTransparent);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        thirdPlantGallery.addView(cactusar);
        fifthLampGallery.addView(standinglamp);
        thirdPlantGallery.addView(plant);
        gallery.addView(elf);
        fifthLampGallery.addView(lamplight);
        gallery.addView(elephant);
        fourthStatueGallery.addView(desktransparents);
        gallery.addView(skyscraper);
        fifthLampGallery.addView(desklamp);
        fifthLampGallery.addView(standinglampi);
        gallery.addView(bowl);
        fourthStatueGallery.addView(coctaildesk);
        thirdPlantGallery.addView(coffeplant);
        gallery.addView(statuelit);
        secondPlantGallery.addView(plantherb);
        secondPlantGallery.addView(coctailchair);
        gallery.addView(paintedart);
        gallery.addView(desktree);
        //   gallery.addView(urlLink);


        gallery.setVisibility(View.INVISIBLE);
        secondPlantGallery.setVisibility(View.INVISIBLE);
        thirdPlantGallery.setVisibility(View.INVISIBLE);
        fourthStatueGallery.setVisibility(View.INVISIBLE);
        fifthLampGallery.setVisibility(View.INVISIBLE);


    }

    private void takeScreenshot() {

        CircleButton screenbtn = (CircleButton) findViewById(R.id.screenbtn);
        screenbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
    }

    private void takePhoto() {
        Screenshot scrn = new Screenshot();
        // scrn.generateFilename();
        // Pomocou programu PixelCopy vytvorte obrazovku fotoaparátu a objekt ako bitovú mapu
        final String filename = scrn.generateFilename();
        ArSceneView view = arFragment.getArSceneView();

        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);

        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PixelCopy.request(view, bitmap, (copyResult) -> {
                if (copyResult == PixelCopy.SUCCESS) {
                    try {
                        scrn.saveBitmapToDisk(bitmap, filename);

                        // Skenovanie médií
                        Uri uri = Uri.parse("file://" + filename);
                        Intent i = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        i.setData(uri);
                        sendBroadcast(i);

                    } catch (IOException e) {
                        Toast toast = Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    }

                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                            "\n" +
                                    "Screenshot bol uložený --> pozrite sa do galerie", Snackbar.LENGTH_LONG);

                    snackbar.show();
                } else {
                    Toast toast = Toast.makeText(MainActivity.this, "Nepodarilo sa uložiť snímku obrazovky! " + copyResult, Toast.LENGTH_LONG);
                    toast.show();
                }
                handlerThread.quitSafely();
            }, new Handler(handlerThread.getLooper()));
        }
    }

    private void BoomMenuData() {

        boombutton = (BoomMenuButton) findViewById(R.id.boom);
        BoomMenu boomMenuclass = new BoomMenu();
        boomMenuclass.boomMenu();
        for (int i = 0; i < boombutton.getPiecePlaceEnum().pieceNumber(); i++) {
            TextOutsideCircleButton.Builder builder = new TextOutsideCircleButton.Builder()
                    .normalImageRes(boomMenuclass.imageIDList.get(i))
                    .normalText(boomMenuclass.titleIDList.get(i))
                    .normalColor(R.color.black)
                    .listener(new OnBMClickListener() {

                        @Override
                        public void onBoomButtonClick(int index) {

                            Toast.makeText(MainActivity.this, "Choose picture " + index, Toast.LENGTH_SHORT).show();
                            switch (index) {
                                case 0:
                                    fifthLampGallery.setVisibility(View.INVISIBLE);
                                    thirdPlantGallery.setVisibility(View.INVISIBLE);
                                    fourthStatueGallery.setVisibility(View.INVISIBLE);
                                    gallery.setVisibility(View.VISIBLE);
                                    secondPlantGallery.setVisibility(View.INVISIBLE);

                                    break;
                                case 1:
                                    fifthLampGallery.setVisibility(View.INVISIBLE);
                                    thirdPlantGallery.setVisibility(View.INVISIBLE);
                                    fourthStatueGallery.setVisibility(View.INVISIBLE);
                                    gallery.setVisibility(View.INVISIBLE);
                                    secondPlantGallery.setVisibility(View.VISIBLE);
                                    break;
                                case 2:
                                    fourthStatueGallery.setVisibility(View.INVISIBLE);
                                    fifthLampGallery.setVisibility(View.INVISIBLE);
                                    secondPlantGallery.setVisibility(View.INVISIBLE);
                                    gallery.setVisibility(View.INVISIBLE);
                                    thirdPlantGallery.setVisibility(View.VISIBLE);
                                    break;
                                case 3:
                                    thirdPlantGallery.setVisibility(View.INVISIBLE);
                                    fifthLampGallery.setVisibility(View.INVISIBLE);
                                    secondPlantGallery.setVisibility(View.INVISIBLE);
                                    gallery.setVisibility(View.INVISIBLE);
                                    fourthStatueGallery.setVisibility(View.VISIBLE);
                                    break;
                                case 4:
                                    secondPlantGallery.setVisibility(View.INVISIBLE);
                                    gallery.setVisibility(View.INVISIBLE);
                                    thirdPlantGallery.setVisibility(View.INVISIBLE);
                                    fourthStatueGallery.setVisibility(View.INVISIBLE);
                                    fifthLampGallery.setVisibility(View.VISIBLE);

                                    break;
                                case 5:
                                    secondPlantGallery.setVisibility(View.INVISIBLE);
                                    gallery.setVisibility(View.INVISIBLE);
                                    thirdPlantGallery.setVisibility(View.INVISIBLE);
                                    fourthStatueGallery.setVisibility(View.INVISIBLE);
                                    fifthLampGallery.setVisibility(View.INVISIBLE);
                                    Intent intentUrl = new Intent(MainActivity.this, SharedPref.class);
                                    startActivity(intentUrl);
                                    break;

                            }
                        }
                    });

            boombutton.addBuilder(builder);
        }

    }


    private void buildARModelAsset(Uri parse) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(String.valueOf(urlTextLinks)), RenderableSource.SourceType.GLTF2)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(urlTextLinks)
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build ASSET", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });

    }

    private void builARdModelDeskTree(File filedesktree) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(filedesktree.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(filedesktree.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build filedesktree", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });

    }

    private void builARdModelCoctailChair(File filecoctailchair) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(filecoctailchair.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(filecoctailchair.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build filecoctailchair", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }

    private void builARdModelCoctailDesk(File filecoctaildesk) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(filecoctaildesk.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(filecoctaildesk.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build filecoctaildesk", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }

    private void builARdModelPaintedArt(File filePaintedArt) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(filePaintedArt.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(filePaintedArt.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build filePaintedArt", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }


    private void builARdModelBowl(File fileBowl) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(fileBowl.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(fileBowl.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build Painted Bowl", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }

    private void builARdModelLampLight(File filelamplight) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(filelamplight.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(filelamplight.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build lamplight", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }


    private void buildARModelPlantHerb(File filePlantHerb) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(filePlantHerb.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(filePlantHerb.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build Painted Free3D", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }

    private ModelRenderable renderable;

    private void builARdModel(File file) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(file.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(file.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build CACTUS", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }

    private void builARdModelHousePlant(File filehouseplant) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(filehouseplant.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(filehouseplant.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build houseplant", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }

    private void builARdModelDeskLamp(File filedesklamp) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(filedesklamp.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(filedesklamp.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build desklamp", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }

    private void builARModelLamp(File filelamp) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(filelamp.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(filelamp.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build Lamp", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }

    private void builARdModel2(File file2) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(file2.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(file2.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build LAMP", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }

    private void builARdModelPlant(File filePlant) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(filePlant.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(filePlant.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build PLANT", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }

    private void builARdModelElf(File fileElf) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(fileElf.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(fileElf.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build ELF", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }

    private void builARdModelSkyscraper(File fileSkyscraper) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(fileSkyscraper.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(fileSkyscraper.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build SKYSCRAPER", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }

    private void builARdModelElephant(File fileElephant) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(fileElephant.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(fileElephant.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build ELEPHANT", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }

    private void builARdModelDeskTransparent(File fileDeskTransparent) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(fileDeskTransparent.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(fileDeskTransparent.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build DeskTransparent", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }

    private void builARdModelStatue(File fileStatue) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(fileStatue.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(fileStatue.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build STATUE", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }

    private void builARdModelDesk(File fileDesk) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(fileDesk.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(fileDesk.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build DESK", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }

    private void builARdModelCoffePlant(File fileCoffePlant) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(fileCoffePlant.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(fileCoffePlant.getPath())
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build Coffe plant", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });


    }


    private void setUpPlane() {
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());
            TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
            Anchor newAnchor = hitResult.createAnchor();
            node.setRenderable(renderable);
            node.getScaleController().setMinScale(0.3999f);
            node.getScaleController().setMaxScale(0.4000f);
            node.setParent(anchorNode);
            arFragment.getArSceneView().getScene().addChild(anchorNode);
            node.select();

            findViewById(R.id.cleanbtn).setOnClickListener(v ->
                    arFragment.getArSceneView().getScene().removeChild(anchorNode));

            removeAnchorNode();


            //anchorNode.setRenderable(renderable);
            // arFragment.getArSceneView().getScene().addChild(anchorNode);
            // findViewById(R.id.buttonClean).setOnClickListener(v -> anchorNode.getAnchor().detach());
        });

        // createModel(anchorNode);

    }

    private void removeAnchorNode() {


        /*findViewById(R.id.buttonCleanAll).setOnClickListener(v ->
                arFragment.getArSceneView().getScene().callOnHierarchy(node -> {
                    node.setParent(null);
                    if (node instanceof AnchorNode) {
                        ((AnchorNode) node).getAnchor().detach();
                    }
                }));*/
        //CLEEN  celej plochy
        // Button  clearButtonAnchor = findViewById(R.id.buttonCloudAnchor);
       /*clearButtonAnchor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setCloudAnchor(null);
            }
        });*/


    }

}





