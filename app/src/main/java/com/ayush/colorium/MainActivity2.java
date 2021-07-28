package com.ayush.colorium;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.opencv.core.Core.bitwise_and;
import static org.opencv.core.Core.bitwise_not;

public class MainActivity2 extends AppCompatActivity {

    ImageView iv;
    ImageView iv2;
    Bitmap bitmapin = null;
    Uri selectedImageUri;
    Button back;
    Bitmap bitmapout;
    int[] bgvalue = new int[3];
    int thresh = 10;
    Button get;
    Button result;
    TextView t1;
    TextView t2;
    TextView t3;
    int x = 0;
    int y = 0;
    int Ravg = 0;
    int Gavg = 0;
    int Bavg = 0;
    int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        iv = (ImageView) findViewById(R.id.Iv);
        iv2 = (ImageView) findViewById(R.id.Iv2);
        back = findViewById(R.id.button3);
        result = findViewById(R.id.resultcolor);
        t1 = (TextView) findViewById(R.id.textView);
        t2 = (TextView) findViewById(R.id.textView2);
        t3 = (TextView) findViewById(R.id.textView3);
        get = findViewById(R.id.button4);
        Intent intent = getIntent();
        selectedImageUri = (Uri) intent.getParcelableExtra("Image");
        x = intent.getIntExtra("x", 0);
        y = intent.getIntExtra("y", 0);
        try {
            bitmapin = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int pickedpixel = bitmapin.getPixel(x, y);
        bgvalue[0] = Color.red(pickedpixel);
        bgvalue[1] = Color.green(pickedpixel);
        bgvalue[2] = Color.blue(pickedpixel);
        setImage();
        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                color = calculateAverageColor(bitmapout, 1);
                Ravg = (color >> 16) & 0xff;
                Gavg = (color >>  8) & 0xff;
                Bavg = (color      ) & 0xff;
                t1.setText(Integer.toString(Ravg)+", ");
                t2.setText(Integer.toString(Gavg)+", ");
                t3.setText(Integer.toString(Bavg));
                result.setBackgroundColor(color);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private void makemask(){
        Mat maskin = new Mat();
        Utils.bitmapToMat(bitmapin, maskin);
        Imgproc.cvtColor(maskin,maskin,Imgproc.COLOR_RGB2BGR);
        Mat maskout = new Mat();
        int perc = 40;
        int v1h = bgvalue[0]+perc+thresh;
        int v2h = bgvalue[1]+perc+thresh;
        int v3h = bgvalue[2]+perc+thresh;
        int v1l = bgvalue[0]-perc-thresh;
        int v2l = bgvalue[1]-perc-thresh;
        int v3l = bgvalue[2]-perc-thresh;
        if(v1h > 255){
            v1h = 255;
        }
        if(v2h > 255){
            v2h = 255;
        }
        if(v2h > 255){
            v2h = 255;
        }
        if(v1l < 0){
            v1h = 0;
        }
        if(v2l < 0){
            v2h = 0;
        }
        if(v3l < 0){
            v3h = 0;
        }
        Core.inRange(maskin, new Scalar(v3l, v2l, v1l), new Scalar(v3h, v2h, v1h), maskout);
        bitwise_not(maskout, maskout);
        Mat maskfinal = new Mat();
        bitwise_and(maskin,maskin,maskfinal, maskout);
        Imgproc.cvtColor(maskfinal,maskfinal,Imgproc.COLOR_BGR2RGB);
        Utils.matToBitmap(maskfinal,bitmapout);
        iv2.setImageBitmap(bitmapout);
    }

    private void setImage(){
        WhiteBalancer(bitmapin);
        bitmapout = bitmapin.copy(Bitmap.Config.ARGB_8888, true);
        iv.setImageBitmap(bitmapin);
        makemask();
    }

    private void WhiteBalancer(Bitmap bitmap) {
        Mat src = new Mat();
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, src);
        List<Mat> splitMat = new ArrayList<Mat>();
        Core.split(src,splitMat);

        double meanB = Core.mean(splitMat.get(0)).val[0];
        double meanG = Core.mean(splitMat.get(1)).val[0];
        double meanR = Core.mean(splitMat.get(2)).val[0];

        double kB = (meanB + meanG + meanR) / (3 * meanB);
        double kG = (meanB + meanG + meanR) / (3 * meanG);
        double kR = (meanB + meanG + meanR) / (3 * meanR);

        List<Mat> mergeMatList = new ArrayList<Mat>();
        Mat merge = new Mat();
        mergeMatList.add(changeRGB(splitMat.get(0), kB));
        mergeMatList.add(changeRGB(splitMat.get(1), kG));
        mergeMatList.add(changeRGB(splitMat.get(2), kR));

        Core.merge(mergeMatList, merge);
        Utils.matToBitmap(merge, bitmap);
    }

    private Mat changeRGB(Mat mat, double k) {
        Mat newMat = new Mat(mat.size(), CvType.CV_8UC1);
        for (int i = 0; i < mat.rows(); i++) {
            for (int j = 0; j < mat.cols(); j++) {
                double[] color = mat.get(i, j);
                color[0] *= k;
                newMat.put(i, j, color);
            }
        }
        return newMat;
    }

    public int calculateAverageColor(android.graphics.Bitmap bitmap, int pixelSpacing) {
        int R = 0; int G = 0; int B = 0;
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int n = 0;
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i += pixelSpacing) {
            int color = pixels[i];
            if(Color.red(color) == 0 && Color.green(color) == 0 && Color.blue(color) == 0){
                continue;
            }
            else {
                R += Color.red(color);
                G += Color.green(color);
                B += Color.blue(color);
                n++;
            }
        }
        return Color.rgb(R / n, G / n, B / n);
    }
}