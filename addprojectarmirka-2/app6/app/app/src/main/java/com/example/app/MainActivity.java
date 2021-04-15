package com.example.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import at.markushi.ui.CircleButton;


public class MainActivity extends AppCompatActivity {

    RecyclerView myRecyclerView;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;

    private ArFragment arFragment;
    EditText urlTextLinks;
    Button btSaveLink, btBuildLink;
    JSONObject saved = new JSONObject();
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    ProgressBar progressBar;

    //String GLTF_ASSET_LION = "https://raw.githubusercontent.com/Mac0490/GLTF_ASSETS/main/lionstatue/lion.gltf";


    BoomMenuButton boombutton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        myRecyclerView = findViewById(R.id.recyclerView);
        myRecyclerView.setHasFixedSize(true);

        myRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseDatabase = FirebaseDatabase.getInstance();


        urlTextLinks = findViewById(R.id.url_link);
        btBuildLink=findViewById(R.id.build_btn);
        btBuildLink.setBackgroundColor(Color.BLACK);
        btBuildLink.setTextColor(Color.GREEN);

        btSaveLink = findViewById(R.id.savelink_btn);
        btSaveLink.setBackgroundColor(Color.BLACK);
        btSaveLink.setTextColor(Color.GREEN);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        myUrlImage();
        TextLinksGalleryData();

        setUpPlane();
        BoomMenuData();
        takeScreenshot();
    }


    private void displayGallery() {

        FirebaseRecyclerOptions<Member> options = new FirebaseRecyclerOptions.Builder<Member>()
                .setQuery(reference, Member.class)
                .build();

        FirebaseRecyclerAdapter<Member, ViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Member, ViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Member model) {

                        holder.setDetails(getApplicationContext(), model.image, model.url);
                        holder.view.setOnClickListener(view -> {
                            progressBar = findViewById(R.id.progressbar);
                            progressBar.setVisibility(View.VISIBLE);
                            //Uri.parse(String.valueOf(urladress));
                            buildARModelAsset(Uri.parse(String.valueOf(model.url)));
                        });
                    }

                    @NonNull
                    @Override
                    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.image, parent, false);
                        return new ViewHolder(view);
                    }
                };


        GridLayoutManager gml = new GridLayoutManager(getApplicationContext(), 5, GridLayoutManager.VERTICAL, false);
        myRecyclerView.setLayoutManager(gml);
        firebaseRecyclerAdapter.startListening();
        myRecyclerView.setAdapter(firebaseRecyclerAdapter);

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
                if (!s.equals("") && URLUtil.isValidUrl(s)) {
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
                } else {
                    Toast.makeText(getApplicationContext(), "Enter url address in this format:" +
                                    "https://raw.githubusercontent.com/Mac0490/GLTF_ASSETS/main/map/map.gltf\n",
                            Toast.LENGTH_SHORT)
                            .show();
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


    private void TextLinksGalleryData() {


        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);
        String urladress = urlTextLinks.getText().toString();


        btBuildLink.setOnClickListener(view -> {

            if(!urladress.equals("") && URLUtil.isValidUrl(urladress)){
            progressBar = findViewById(R.id.progressbar);
            progressBar.setVisibility(View.VISIBLE);
            Uri.parse(String.valueOf(urladress));
            buildARModelAsset(Uri.parse(String.valueOf(urladress)));
            } else {

                Toast.makeText(getApplicationContext(), "Enter url address in this format:" +
                                "https://raw.githubusercontent.com/Mac0490/GLTF_ASSETS/main/map/map.gltf\n",
                        Toast.LENGTH_SHORT)
                        .show();

            }

        });

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
                                    "Screenshot saved -> look in the gallery", Snackbar.LENGTH_LONG);

                    snackbar.show();
                } else {
                    Toast toast = Toast.makeText(MainActivity.this, "Failed to save screenshot! " + copyResult, Toast.LENGTH_LONG);
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
                                    reference = firebaseDatabase.getReference("StatuesGallery");
                                    displayGallery();
                                    //myRecyclerView.setVisibility(View.VISIBLE);
                                    break;
                                case 1:
                                    reference = firebaseDatabase.getReference("ChairsGallery");
                                    displayGallery();

                                    break;
                                case 2:
                                    reference = firebaseDatabase.getReference("PlantsGallery");
                                    displayGallery();
                                    break;
                                case 3:
                                    reference = firebaseDatabase.getReference("DesksGallery");
                                    displayGallery();

                                    break;
                                case 4:
                                    reference = firebaseDatabase.getReference("LampsGallery");
                                    displayGallery();

                                    break;
                                case 5:
                                    Intent intentUrl = new Intent(MainActivity.this, SharedPref.class);
                                    startActivity(intentUrl);
                                    break;

                            }
                        }
                    });

            boombutton.addBuilder(builder);
        }

    }


    private ModelRenderable renderable;

    private void buildARModelAsset(Uri uri) {
        RenderableSource renderableSource = RenderableSource
                .builder()
                .setSource(this, Uri.parse(String.valueOf(uri)), RenderableSource.SourceType.GLTF2)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(uri)
                .build()
                .thenAccept(modelRenderable -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Model build URL", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });

    }

    private void setUpPlane() {
        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());
            TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
            Anchor newAnchor = hitResult.createAnchor();
            node.setRenderable(renderable);
            node.getScaleController().setMinScale(0.4999f);
            node.getScaleController().setMaxScale(0.5000f);
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





