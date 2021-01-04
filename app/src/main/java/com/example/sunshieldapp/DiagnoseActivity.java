package com.example.sunshieldapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DiagnoseActivity extends AppCompatActivity {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public ImageView imageView;
    public TextView persenGanas, persenJinak, deskripsi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnose);
        imageView = findViewById(R.id.imageView);
        persenGanas = findViewById(R.id.persentaseGanas);
        persenJinak = findViewById(R.id.persentaseJinak);
        deskripsi = findViewById(R.id.deskripsi);
        takePicture();

    }

    public void takePicture()
    {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(getPackageManager())!=null)
        {
            startActivityForResult(cameraIntent,REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK)
        {
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(bitmap);
            sendImage(bitmap);
        }
        else if (resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    public void sendImage(Bitmap bm)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG,100,stream);
        byte[] byteArray = stream.toByteArray();
        String url = "https://southcentralus.api.cognitive.microsoft.com/customvision/v3.0/Prediction/daf9441a-ffa9-4b2e-b846-0811ecdfd257/classify/iterations/SunShieldCVIteration1/image";
        final OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(byteArray,MediaType.parse("image/*"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Prediction-Key","") //Insert your Azure Cognitive Services Key
                .addHeader("Content-Type","application/octet-stream")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String data = response.body().string();
                double probabilityMalignant=0;
                double probabilityBenign=0;
                try {
                    JSONObject reader = new JSONObject(data);
                    JSONArray array = reader.getJSONArray("predictions");
                    for(int i = 0; i<array.length();i++)
                    {
                        JSONObject current = array.getJSONObject(i);
                        String probability = current.getString("probability");
                        String tag = current.getString("tagName");
                        if (tag.equals("Malignant"))
                        {
                            probabilityMalignant = Double.parseDouble(probability)*100;
                            Log.i("Malignant",Double.toString(probabilityMalignant));
                        }
                        else if (tag.equals("Benign"))
                            {
                                probabilityBenign = Double.parseDouble(probability)*100;
                                Log.i("Benign",Double.toString(probabilityBenign));
                            }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final double finalProbabilityBenign = probabilityBenign;
                final double finalProbabilityMalignant= probabilityMalignant;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        persenJinak.setText(finalProbabilityBenign+"%");
                        persenGanas.setText(finalProbabilityMalignant+"%");
                        if (finalProbabilityMalignant>finalProbabilityBenign)
                        {
                            deskripsi.setText("Kemungkinan lesi kulit bersifat ganas, perlu diagnosis lebih lanjut oleh dokter.");
                        }
                        else if (finalProbabilityMalignant==finalProbabilityBenign)
                        {
                            deskripsi.setText("Tidak dapat menentukan ganas tidaknya lesi kulit, perlu diagnosis lebih lanjut oleh dokter.");
                        }
                        else
                            {
                                deskripsi.setText("Lesi kulit kemungkinan jinak, tetap konsultasi kepada dokter apabila ada gejala seperti gatal, rasa panas, lesi yang berubah bentuk, dan gejala kanker kulit lainnya.");
                            }
                    }
                });

            }
        });
    }

}