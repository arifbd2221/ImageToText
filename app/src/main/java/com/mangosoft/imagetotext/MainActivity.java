package com.mangosoft.imagetotext;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //private static final String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 101;

    LinearLayout camera_gallery, uploadLinearLayout;
    BottomSheetDialog bottomSheetDialog;
    EditText editText;

    ImageView takenimage;

    AlertDialog dialog;
    String string = "";
    FloatingActionButton fab,options;

    FloatingActionButton copy,share,clear,file;

    private InterstitialAd mInterstitialAd;


    Boolean isFABOpen=false;
    String existfile = "Select Existing File";
    ArrayList<String> allfiles;

    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-8750090969618404/4472432639");


        editText = findViewById(R.id.editText);
        fab = findViewById(R.id.floating_action_button);
        options = findViewById(R.id.options);
        takenimage = findViewById(R.id.takenimage);

        copy = findViewById(R.id.copy);
        share = findViewById(R.id.share);
        clear = findViewById(R.id.clear);
        file = findViewById(R.id.file);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (!string.isEmpty()){
                    ClipData clip = ClipData.newPlainText("Text", string);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(), "Successfully copied to clipboard", Toast.LENGTH_SHORT).show();
                    mInterstitialAd.show();
                }


            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                if (!string.isEmpty()){
                    shareIntent.putExtra(Intent.EXTRA_TEXT, string);
                    shareIntent.setType("text/plain");
                    startActivity(shareIntent);
                }


            }
        });


        file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeDialog("Save to file","Save","Cancel","Nothing found from the image !!!!");
                dialog.show();
            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                bottomSheetDialog.show();
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                string="";
                editText.setText("");
            }
        });


        createBottomSheetDialog();


    }


    public static boolean checkAndRequestPermissions(final Activity context) {
        int ExtstorePermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int cameraPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA);
        int internetPermission = ContextCompat.checkSelfPermission(context,Manifest.permission.INTERNET);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (ExtstorePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded
                    .add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (internetPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded
                    .add(Manifest.permission.INTERNET);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context, listPermissionsNeeded
                            .toArray(new String[listPermissionsNeeded.size()]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    private void showFABMenu(){
        isFABOpen=true;
        copy.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        share.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        clear.animate().translationY(-getResources().getDimension(R.dimen.standard_155));
        file.animate().translationY(-getResources().getDimension(R.dimen.standard_205));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        copy.animate().translationY(0);
        share.animate().translationY(0);
        clear.animate().translationY(0);
        file.animate().translationY(0);
    }

    private void createBottomSheetDialog() {
        if (bottomSheetDialog == null) {
            View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet, null);
            camera_gallery = view.findViewById(R.id.camera_gallery);
            uploadLinearLayout = view.findViewById(R.id.uploadLinearLayout);

            camera_gallery.setOnClickListener(this);
            uploadLinearLayout.setOnClickListener(this);

            bottomSheetDialog = new BottomSheetDialog(this);
            bottomSheetDialog.setContentView(view);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.developer) {
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.show();
            startActivity(new Intent(this,DeveloperActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS:
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                            "FlagUp Requires Access to Camara.", Toast.LENGTH_SHORT)
                            .show();
                    finish();
                } else if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(),
                            "FlagUp Requires Access to Your Storage.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    //doWork();
                }
                break;
        }
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.camera_gallery:
                if(checkAndRequestPermissions(MainActivity.this)){
                    onSelectImageClick();
                    bottomSheetDialog.dismiss();
                }

                break;
            case R.id.uploadLinearLayout:
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                mInterstitialAd.show();
                startActivity(new Intent(this, FileListActivity.class));
                bottomSheetDialog.dismiss();
                break;

        }
    }



    public void onSelectImageClick() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle("Crop Image")
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setCropMenuCropButtonTitle("Done")
                .setRequestedSize(400, 400)
                .setCropMenuCropButtonIcon(R.drawable.crop)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult data = CropImage.getActivityResult(result);
            if (resultCode == RESULT_OK) {
                Bitmap bitmap  = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getUri());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                takenimage.setImageBitmap(bitmap);
                getText(bitmap);
                mInterstitialAd.show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = data.getError();
            }
        }


    }



    public void getText(Bitmap image){
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        Frame imageFrame = new Frame.Builder()

                .setBitmap(image)                 // your image bitmap
                .build();




        SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
        Log.d("text",textBlocks.size()+"");
        //Toast.makeText(getApplicationContext(), "textBlocks size: "+textBlocks.size(), Toast.LENGTH_LONG).show();
        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
            StringBuilder imageText = new StringBuilder();
            imageText.append(string);                   // return string
            imageText.append(textBlock.getValue());
            imageText.append("\n\n");
            string = imageText.toString();

        }
        editText.setText(string);
    }



    private void writeOnFile(String filename,String fileContents){

        if (filename.contains(".txt")){
            Log.d("filename",filename);
            String[] arr = filename.split("\\.");
            filename = arr[0];
        }


        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File (sdCard.getAbsolutePath() + "/Image2text/text");
        FileOutputStream outputStream = null;

        try {

            //File  rootPath =new File( getApplicationContext().getFilesDir().getPath()+"/image2text/text/");

            if (!dir.exists()) {
                dir.mkdirs();
            }

            File f = new File(dir, filename+".txt");
            if (f.exists()) {
                outputStream = new FileOutputStream(f,true);
            }
            else {
                f.createNewFile();
                outputStream = new FileOutputStream(f);
            }




            outputStream.write(fileContents.getBytes());
            outputStream.flush();
            outputStream.close();
            Toast.makeText(getApplicationContext(), "Successfully saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void makeDialog(String title,String yes,String no,final String msg){
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog, (ViewGroup) findViewById(R.id.dialog));
//layout_root should be the name of the "top-level" layout node in the dialog_layout.xml file.
        final EditText filename = layout.findViewById(R.id.filename);
        final Spinner filenameList = layout.findViewById(R.id.filenames);

        allfiles = getFiles();

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, allfiles);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        filenameList.setAdapter(dataAdapter);

        filenameList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        position=i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        }

        );

        //Building dialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        builder.setTitle(title);
        builder.setPositiveButton(yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!string.isEmpty()){
                    if(!filename.getText().toString().isEmpty())
                    writeOnFile(filename.getText().toString(), string);
                    else if (position != 0){
                        writeOnFile(allfiles.get(position), string);
                    }
                }
                else
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });
        builder.setNegativeButton(no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog = builder.create();
    }


    private ArrayList<String> getFiles(){
        String path = Environment.getExternalStorageDirectory().toString()+"/Image2text/text";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        //Log.d("Files", "Size: "+ files.length);

        ArrayList<String> filelist = new ArrayList();
        try{
            filelist.add(0,existfile);
            int ln = files.length+1;
            for (int i = 1; i < ln; i++)
            {
                //Log.d("Files", "FileName:" + files[i-1].getName());
                filelist.add(files[i-1].getName());
            }
        }catch (Exception ex){
            Toast.makeText(getApplicationContext(), "There is no directories !!!", Toast.LENGTH_SHORT).show();
        }


        return  filelist;
    }

}
