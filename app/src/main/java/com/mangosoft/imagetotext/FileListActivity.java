package com.mangosoft.imagetotext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class FileListActivity extends AppCompatActivity {

    String[] mobileArray = {"Android","IPhone","WindowsMobile","Blackberry",
            "WebOS","Ubuntu","Windows7","Max OS X"};
    private String[] filenames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);


        filenames = getFiles();


        ArrayAdapter adapter = null;
        try {
            adapter = new ArrayAdapter<String>(this,
                    R.layout.list_item, filenames);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ListView listView = findViewById(R.id.listview);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String path = Environment.getExternalStorageDirectory().toString()+"/Image2text/text";
                Intent intent = new Intent(Intent.ACTION_EDIT);
                Uri uri = Uri.parse(path+filenames[i]);
                intent.setDataAndType(uri, "text/plain");
                startActivity(intent);
            }
        });

    }


    private String[] getFiles(){
        String path = Environment.getExternalStorageDirectory().toString()+"/Image2text/text";
        //Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        //Log.d("Files", "Size: "+ files.length);

        ArrayList<String> filelist = new ArrayList();

        try{
            for (int i = 0; i < files.length; i++)
            {
                //Log.d("Files", "FileName:" + files[i].getName());
                filelist.add(files[i].getName());
            }

            return  filelist.toArray(new String[0]);
        }
        catch (Exception ex){
            Toast.makeText(getApplicationContext(), "There is no directories !!!", Toast.LENGTH_SHORT).show();
        }
        return null;
    }
}
