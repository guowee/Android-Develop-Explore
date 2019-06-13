package com.uowee.chapter.one;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.File;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.uowee.chapter.c");
                //intent.setClass(MainActivity.this, SecondActivity.class);
                intent.putExtra("time", System.currentTimeMillis());
                intent.addCategory("com.uowee.category.c");
               /* File newFile = new File("file://abc");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri contentUri = FileProvider.getUriForFile(SecondActivity.this, BuildConfig.APPLICATION_ID + ".provider", newFile);
                    intent.setDataAndType(contentUri, "text/plain");
                } else {
                    intent.setDataAndType(Uri.fromFile(newFile), "text/plain");
                }*/
                //When targeting Android Nougat, file:// URIs are not allowed anymore. We should use content:// URIs instead.
                //Passing file:// URIs outside the package domain may leave the receiver with an unaccessible path. Therefore, attempts to pass a file:// URI trigger a FileUriExposedException.
                // The recommended way to share the content of a private file is using the FileProvider.
                intent.setDataAndType(Uri.parse("content://abc"), "text/plain");

                startActivity(intent);
            }
        });
    }
}
