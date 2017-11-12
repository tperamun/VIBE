package com.example.timalperamune.interestedornot;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {


    //    private static List<String> receiverTexts = new ArrayList<String>();
    private static Map<String, List<String>> receiverTexts = new HashMap<String, List<String>>();
    private static List<String> senderTexts = new ArrayList<String>();
    private static final int PERMISSION_REQUEST_CODE = 123;
    //    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    int i = 0;
    List<String> message;

    String timalString = "";
    Button bt1;
    EditText editText;
    JSONObject jsonObj;
    JSONArray jsonArr;
    List<Double> angerArr;
    List<Double> disgustArr;
    List<Double> fearArr;
    List<Double> joyArr;
    List<Double> sadnessArr;
    TextView text;
    String keywords = "";
    String emotion = "";
    static double angerAvg = 0.0;
    static double disgustAvg = 0.0;
    static double fearAvg = 0.0;
    static double joyAvg = 0.0;
    static double sadnessAvg = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt1 = (Button) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.editText);
        text =  (TextView)findViewById(R.id.textView2);

//        StrictMode.setThreadPolicy(policy);
        this.angerArr = new ArrayList<Double>();
        this.disgustArr = new ArrayList<>();
        this.fearArr = new ArrayList<>();
        this.joyArr = new ArrayList<>();
        this.sadnessArr = new ArrayList<>();

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timalString = editText.getText().toString();

//                Log.i("",timalString);
//                hiText.setText(timalString);
                fetchInbox(timalString);
                Intent myIntent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(myIntent);

            }
        });


    }


    public void fetchInbox(String str) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_SMS}, PERMISSION_REQUEST_CODE);
        }

        Uri uri = Uri.parse("content://sms/inbox");
        Cursor cursor = getContentResolver().query(uri, new String[]{"_id", "address", "body"}, null, null, null);


        if (cursor.moveToFirst()) {
            do {

                String address = cursor.getString(1);
                String body = cursor.getString(2);
                List<String> sms = null;
                if (!this.receiverTexts.containsKey(address)) {
                    sms = new ArrayList<String>();
                    sms.add(body);
                    this.receiverTexts.put(address, sms);
                } else {
                    this.receiverTexts.get(address).add(body);
                }


            }
            while (cursor.moveToNext());
        }
        cursor.close();

        for (Map.Entry<String, List<String>> entry : this.receiverTexts.entrySet()) {

            String number = entry.getKey();
            this.message = entry.getValue();

            if (number.equals(str)) {



                Runnable networkThread = new Runnable() {
                    public void run() {

                        for (i = 0; i < message.size(); i++) {
                            NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27, "2956a622-1c77-4244-9d9e-d12520a30cd4", "6jbKoOJl2RCs");
                            String text = message.get(i);

                            if (text.length() < 15) {
                                text += "this is a neutral sentence";
                            }

                            KeywordsOptions keywordsOptions = new KeywordsOptions.Builder()
//
                                    .emotion(true)
                                    .build();

                            Features features = new Features.Builder()
                                    .keywords(keywordsOptions)
                                    .build();

                            AnalyzeOptions parameters = new AnalyzeOptions.Builder()
                                    .text(text)
                                    .features(features)
                                    .build();

                            AnalysisResults response = service
                                    .analyze(parameters)
                                    .execute();
//                            System.out.println(response);
//                        Log.i("HELLO!!!!",text);
//                            Log.i("CREATE", response.toString());

                            try {
                                jsonObj = new JSONObject(response.toString());
                                jsonArr = jsonObj.getJSONArray("keywords");
////
                                for (int i = 0; i < jsonArr.length(); i++) {

//                                    JSONObject tempObj = jsonArr.getJSONObject(); //emotion
                                    JSONObject tempObj = jsonArr.getJSONObject(i).getJSONObject("emotion");
                                    angerArr.add((Double) tempObj.get("anger"));
                                    disgustArr.add((Double) tempObj.get("disgust"));
                                    fearArr.add((Double) tempObj.get("fear"));
                                    joyArr.add((Double) tempObj.get("joy"));
                                    sadnessArr.add((Double) tempObj.get("sadness"));


                                }

                                double angerSum = 0.0, disgustSum = 0.0, fearSum = 0.0, joySum = 0.0, sadnessSum = 0.0;
                                for (int i = 0; i < angerArr.size(); i++) {
                                    angerSum += angerArr.get(i);
                                    disgustSum += disgustArr.get(i);
                                    fearSum += fearArr.get(i);
                                    joySum += joyArr.get(i);
                                    sadnessSum += sadnessArr.get(i);

                                }
                                angerAvg = (angerSum / angerArr.size()) * 100;
                                disgustAvg = (disgustSum / disgustArr.size()) * 100;
                                joyAvg = (joySum / joyArr.size()) * 100;
                                fearAvg = (fearSum / fearArr.size()) * 100;
                                sadnessAvg = (sadnessSum / sadnessArr.size()) * 100;

//                                Log.i("VALUES", Double.toString(angerAvg));
//                                Log.i("VALUES", Double.toString(disgustAvg));
//                                Log.i("VALUES", Double.toString(joyAvg));
//                                Log.i("VALUES", Double.toString(fearAvg));
//                                Log.i("VALUES", Double.toString(sadnessAvg));


                            } catch (JSONException e) {
//                                e.printStackTrace();
                                Log.i("EXCEPTION", "Some shit happened");
                            }
                        }
                    }


                };
                Thread thread = new Thread(networkThread);
                thread.start();


            }
        }
    }


}


