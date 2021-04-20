package com.example.app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;

import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
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


import java.io.IOException;


import at.markushi.ui.CircleButton;


public class MainActivity extends AppCompatActivity {

    RecyclerView myRecyclerView;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;

    private ArFragment arFragment;
    EditText urlTextLinks;
    Button btSaveLink, btBuildLink;
    JSONObject saved = new JSONObject();
    SharedPreferences shrdpreferences;
    SharedPreferences.Editor editor;

    ProgressBar progressBar;

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
        shrdpreferences = getSharedPreferences("TEXT", Context.MODE_PRIVATE);
        editor = shrdpreferences.edit();
        urlTextLinks = findViewById(R.id.url_link);
        btSaveLink = findViewById(R.id.savelink_btn);


    }

    public void myUrlImage() {

        initialize();
        Intent intent = getIntent();
        if (intent.getIntExtra("POSITION", -1) != -1) {
            try {
                String s = urlTextLinks.getText().toString();
                if (!shrdpreferences.getString("SAVED", "").equals(""))
                    saved = new JSONObject(shrdpreferences.getString("SAVED", ""));
                urlTextLinks.setText(saved.getString("SAVED" + intent.getIntExtra("POSITION", 0)));
                s = saved.getString("SAVED" + intent.getIntExtra("POSITION", 0));
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
                        if (!shrdpreferences.getString("SAVED", "").equals("")) {
                            saved = new JSONObject(shrdpreferences.getString("SAVED", ""));
                            saved.put("SAVED" + saved.length(), s);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("TESTING", saved + "");
                    editor.putString("SAVED", saved.toString());
                    editor.apply();
                    urlTextLinks.setText("");
                    Intent intent1 = new Intent(MainActivity.this, SharedPref.class);
                    startActivity(intent1);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.toast,
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
            if (shrdpreferences.getString("SAVED", "").equals("")) {
                Toast.makeText(getApplicationContext(), R.string.toast_next,
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

                Toast.makeText(getApplicationContext(), R.string.toast,
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
                        Toast toast = Toast.makeText(MainActivity.this, R.string.toast_next, Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    }

                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                            "\n" +
                                    getResources().getString(R.string.snackbar), Snackbar.LENGTH_LONG);

                    snackbar.show();



                } else {
                    Toast toast = Toast.makeText(MainActivity.this, R.string.toast_failedtosavescrn + copyResult, Toast.LENGTH_LONG);
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
                    .normalText(getResources().getString(boomMenuclass.titleIDList.get(i)))
                    .normalColor(R.color.black)
                    .listener(new OnBMClickListener() {

                        @Override
                        public void onBoomButtonClick(int index) {

                          //  Toast.makeText(MainActivity.this,"", Toast.LENGTH_SHORT).show();
                            switch (index) {
                                case 0:
                                    reference = firebaseDatabase.getReference("StatuesGallery");
                                    displayGallery();
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
                    Toast.makeText(this, R.string.toast_place, Toast.LENGTH_SHORT).show();
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
            node.setLocalPosition(new Vector3(0, 0, 0));
            node.setLocalRotation(new Quaternion(0, 0, 1, 0));
            node.setParent(anchorNode);
            arFragment.getArSceneView().getScene().addChild(anchorNode);
            node.select();

            removeAnchorNode(node, anchorNode);

        });


    }

    private void removeAnchorNode(Node node, AnchorNode anchorNode) {

        findViewById(R.id.cleanbtn).setOnClickListener(v->{
            arFragment.getArSceneView().getScene().callOnHierarchy(nodes->{
                node.setParent(anchorNode);
                if(nodes instanceof AnchorNode){
                    ((AnchorNode) nodes).getAnchor().detach();
                }
            });
        });

    }


}





