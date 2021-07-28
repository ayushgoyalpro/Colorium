package com.ayush.colorium;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import org.opencv.android.OpenCVLoader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button BSelectImage;
    Button next;
    ImageView IVPreviewImage;
    int SELECT_PICTURE = 200;
    Bitmap bitmap = null;
    Uri selectedImageUri;
    int x = 0;
    int y = 0;
    int finalHeight, finalWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();
        setContentView(R.layout.activity_main);
        BSelectImage = findViewById(R.id.button);
        next = findViewById(R.id.button2);
        IVPreviewImage = findViewById(R.id.previewImg);
        BSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bitmap != null){
                    Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                    intent.putExtra("Image", selectedImageUri);
                    intent.putExtra("x", x);
                    intent.putExtra("y", y);
                    startActivity(intent);
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please Select an image first", Toast.LENGTH_LONG).show();
                }
            }
        });
        IVPreviewImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                x = (int)event.getX();
                y = (int)event.getY();
                int[] posXY = new int[2];
                IVPreviewImage.getLocationOnScreen(posXY);
                //x = x - posXY[0];
                //y = y - posXY[1];
                Toast.makeText(getApplicationContext(), "Color picked", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    useImage(selectedImageUri);
                }
            }
        }
    }
    void useImage(Uri uri)
    {
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        IVPreviewImage.getLayoutParams().width = bitmap.getWidth();
        IVPreviewImage.getLayoutParams().height = bitmap.getHeight();
        IVPreviewImage.setImageBitmap(bitmap);
    }
}