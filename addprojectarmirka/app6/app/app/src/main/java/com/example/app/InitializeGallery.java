package com.example.app;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ComponentActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class InitializeGallery {

    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    StorageReference assetRef = firebaseStorage.getReference().child("cactusar.glb");
    StorageReference assetRef2 = firebaseStorage.getReference().child("standinglamp.glb");
    StorageReference assetRef3 = firebaseStorage.getReference().child("plant.glb");
    StorageReference assetRefElephant = firebaseStorage.getReference().child("elephant.glb");
    StorageReference assetRefElf = firebaseStorage.getReference().child("statueman.glb");
    StorageReference assetRefSkyscraper = firebaseStorage.getReference().child("skyscraper.glb");
    StorageReference assetRefBowl = firebaseStorage.getReference().child("bowlpainted.glb");
    StorageReference assetRefDesk = firebaseStorage.getReference().child("scene.gltf");
    StorageReference assetStatueLit = firebaseStorage.getReference().child("statuelittle.glb");
    StorageReference assetRefHouseplant = firebaseStorage.getReference().child("houseplant.gltf");
    StorageReference assetReflamp = firebaseStorage.getReference().child("standinglampi.glb");
    StorageReference assetRefdesklamp = firebaseStorage.getReference().child("lampdesk.glb");
    StorageReference assetRefDeskTransparent = firebaseStorage.getReference().child("desktransparents.glb");
    StorageReference assetRefLampLight = firebaseStorage.getReference().child("lamplight.gltf");
    StorageReference assetRefCoffePlant = firebaseStorage.getReference().child("coffeplant.glb");
    StorageReference assetRefPlantHerb = firebaseStorage.getReference().child("plantherb.glb");
    StorageReference assetRefCoctailChair = firebaseStorage.getReference().child("barchair.glb");
    StorageReference assetRefPaintedArt = firebaseStorage.getReference().child("paintedart.glb");
    StorageReference assetRefCoctailDesk = firebaseStorage.getReference().child("coctaildesk.glb");
    StorageReference assetRefDeskTree = firebaseStorage.getReference().child("desktree.glb");

}