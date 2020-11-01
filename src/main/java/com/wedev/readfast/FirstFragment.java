package com.wedev.readfast;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class FirstFragment extends Fragment {
    EditText paragraph;
    TextView activeWord;
    TextView speed;
    SeekBar sliderValue;
    Button pause;
    Button reset;
    Button photo;
    String path;
    String[] ary;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public float normalizeSliderBarValue()
    {
        int x = sliderValue.getProgress();
        return (float) (30376710+(133.6928-30376710)/(1+Math.pow((x/1611.907), 3.488271)));
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState)
    {

        super.onViewCreated(view, savedInstanceState);
        paragraph = view.findViewById(R.id.paragraph);
        activeWord = view.findViewById(R.id.activeWord);
        speed = view.findViewById(R.id.speed);
        sliderValue = view.findViewById(R.id.sliderValue);
        reset = view.findViewById(R.id.reset);
        pause = view.findViewById(R.id.pause);
        photo = view.findViewById(R.id.photo);

        //choosePdf();

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }


        });
        ary = paragraph.getText().toString().split(" ");

        sliderValue.setProgress(100);
        speed.setText((int)Math.round((1000/normalizeSliderBarValue())*60)+" WPM\n"+ (int)Math.round(normalizeSliderBarValue())+" msPW");

        pause.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                isReset=0;
                if (goBack ==0)
                {
                    pause.setText("Unpause");
                    goBack = 1;
                }
                else
                {
                    pause.setText("Pause");
                    goBack = 0;
                }
            }
        });



        reset.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                i = 0;
                isReset=1;

                firstItt = 1;
                pause.setText("start");
                goBack = 1;
            }
        });

        photo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                takePicture();
            }
        });


        sliderValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser)
            {
                Log.d("viva sliderValue",normalizeSliderBarValue()+"");

                if(sliderValue.getProgress() != 0)
                speed.setText((int)Math.round((1000/normalizeSliderBarValue())*60)+" WPM\n"+ (int)Math.round(normalizeSliderBarValue())+" msPW");
            }
        });
        startTimer();

    }

    private static final int CAMERA_REQUEST = 1888; // field

    private void takePicture(){ //you can call this every 5 seconds using a timer or whenever you want
        Intent cameraIntent = new  Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap picture = (Bitmap) data.getExtras().get("data");//this is your bitmap image and now you can do whatever you want with this


            TextRecognizer textRecognizer = new TextRecognizer.Builder(getContext().getApplicationContext()).build();

            Frame imageFrame = new Frame.Builder()

                    .setBitmap(picture)                 // your image bitmap
                    .build();

            String imageText = "";


            SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);

            for (int i = 0; i < textBlocks.size(); i++) {
                TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                imageText = imageText + " " + textBlock.getValue();                   // return string

            }Log.d("terste",""+ imageText);
            paragraph.setText(imageText);
            ary = imageText.split(" ");;

        }
        if (requestCode == 1001) {
            Uri currFileURI = data.getData();
            path=currFileURI.getPath();
            getTextPdf();
            Log.d("pdf ",path);
        }
    }

    public void choosePdf()
    {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Set your required file type
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "DEMO"),1001);
    }

    public void getTextPdf()
    {

    }


    public boolean isUnpaused()
    {
        if (goBack == 0)
            return true;
        else
            return false;
    }

    public boolean isPaused()
    {
        if (goBack == 1 && firstItt == 1)
            return true;
        else
            return false;
    }



    private int i = 0;
    private int goBack = 1;
    private int firstItt = 1;
    private int isReset = 0;
    private void startTimer() {

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {

                new Handler(Looper.getMainLooper()).post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try {
                            ary = paragraph.getText().toString().split(" ");
                            if (isUnpaused())
                            {
                                firstItt = 1;
                                activeWord.setText(ary[i]);
                                ary[i]="<b>"+ary[i]+"</b>";
                                paragraph.setText(Html.fromHtml(Arrays.toString(ary).replace(",,", "çç").replace(",", "").replace("[", "").replace("]", "").replace("çç", ",").trim()));
                                ary[i]=ary[i].replace("<b>", "").replace("</b>", "");
                                i++;
                            }
                            if (isPaused())
                            {
                                if(i > 5)
                                {
                                    i = i - 5;
                                }
                                else
                                {
                                   i = 0;
                                }
                                firstItt=0;
                                activeWord.setText(ary[i]);
                                ary[i]="<b>"+ary[i]+"</b>";
                                paragraph.setText(Html.fromHtml(Arrays.toString(ary).replace(",,", "çç").replace(",", "").replace("[", "").replace("]", "").replace("çç", ",").trim()));
                                ary[i]=ary[i].replace("<b>", "").replace("</b>", "");
                            }

                        }
                        catch(Exception e)
                        {
                            timer.cancel();
                        }

                    }
                });
                timer.cancel(); // cancel time
                startTimer();   // start the time again with a new period time
            }
        }, (long) normalizeSliderBarValue(), 10);
    }


}