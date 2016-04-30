package eu.depa.captionr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;
import com.microsoft.projectoxford.vision.contract.Face;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

// cycle: Describe1 -> SelectPic1 -> Selector <<- Describe1

public class DescribeActivity extends AppCompatActivity {

    private Uri mImageUri;
    private Bitmap mBitmap;
    private TextView mTextView;
    private ImageView mImageView;
    private VisionServiceClient client;
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.describe);

        if (client == null)
            client = new VisionServiceRestClient(Constants.keys[new Random().nextInt(Constants.keys.length)]);
        if (context == null)
            context = this;

        mTextView = (TextView) findViewById(R.id.desc_tv);
        mImageView = (ImageView) findViewById(R.id.selectedImage);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = new Intent(context, SelectPic.class);
        if (getIntent().getBooleanExtra("from_widget", false))
            i.putExtra("from_widget", true);
        startActivityForResult(i, Constants.SELECT_START_ACTIVITY);

        new Thread(new Runnable() { //pesce 2
            @Override
            public void run() {

                final AdView ad = new AdView(context);
                final RelativeLayout mom = (RelativeLayout) findViewById(R.id.desc_mom);
                final AdRequest adRequest = new AdRequest.Builder().build();
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                params.addRule(RelativeLayout.BELOW, R.id.desc_tv);
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                params.setMargins(0, 256, 0, 64);

                ad.setAdUnitId(Constants.SP_ad_unit_id);
                ad.setAdSize(AdSize.BANNER);
                ad.setLayoutParams(params);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mom != null)
                            mom.addView(ad);
                        ad.loadAd(adRequest);
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Constants.isNetworkAvailable(context)) {//ringrazio sivve
            if (requestCode == Constants.SELECT_START_ACTIVITY && resultCode == RESULT_OK) {

                mImageUri = data.getData();

                mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(mImageUri, getContentResolver());

                if (mBitmap != null) {
                    mImageView.setImageBitmap(mBitmap);
                    doDescribe();
                } else finish();
            } else finish();
        } else {
            Toast.makeText(context, R.string.connected_internet, Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }

    public void doDescribe() {

        try {//Derek Banas
            new doRequest(Constants.DESCRIBE).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String process(int type) throws VisionServiceException, IOException {
        Gson gson = new Gson();
        AnalysisResult v = new AnalysisResult();
        client = new VisionServiceRestClient(Constants.keys[new Random().nextInt(Constants.keys.length)]);
        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        if (type == Constants.ANALYSE)
            v = client.analyzeImage(inputStream, Constants.features, null);
        else if (type == Constants.DESCRIBE)
            v = client.describe(inputStream, 1);

        return gson.toJson(v);
    }

    public void share(View view) {

        Intent i = new Intent(Intent.ACTION_SEND);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), mBitmap, "", "");

        try {
            i.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(DescribeActivity.this, "Unable to share", Toast.LENGTH_SHORT).show();
            return;
        }
        i.putExtra(Intent.EXTRA_TEXT, mTextView.getText());
        i.setType("image/jpeg");
        startActivity(i);
    }

    private void bloatShareIn(boolean hide_wheel) {

        View actionButton = findViewById(R.id.action_btn_share),
                wheel = findViewById(R.id.spin_wheel);
        ScaleAnimation bloatIn = (ScaleAnimation) AnimationUtils.loadAnimation(this, R.anim.bloat_in_linear);

        if (hide_wheel)
            if (wheel != null)
                wheel.setVisibility(View.GONE);
            else Toast.makeText(context, "Getting more data...", Toast.LENGTH_LONG).show();
        if (actionButton != null) {
            actionButton.setVisibility(View.VISIBLE);
            actionButton.startAnimation(bloatIn);
        }
    }

    private String getBeginningFromConfidence(double confidence, String description) {
        String temp;
        if (confidence < 0.33)
            temp = "I'm not really sure, but I think this is";
        else if (confidence < 0.75)
            temp = "I think it's";
        else temp = "I'm sure this is";

        if (!description.startsWith("a") && !description.equals(""))
            temp += " a";
        return temp;
    }

    @Override
    public void onBackPressed() {

        mTextView.setText(R.string.thinking_ellipsis);
        ImageView view = (ImageView) findViewById(R.id.selectedImage);
        View btn = findViewById(R.id.action_btn_share),
                wheel = findViewById(R.id.spin_wheel);
        if (view != null)
            view.setImageBitmap(null);
        if (btn != null)
            btn.setVisibility(View.GONE);
        if (wheel != null)
            wheel.setVisibility(View.VISIBLE);
        startActivityForResult(new Intent(this, SelectPic.class), Constants.SELECT_START_ACTIVITY);
    }

    private class doRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;
        private int current_work;

        public doRequest(int pCurrent_work) {
            current_work = pCurrent_work;
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                return process(current_work);
            } catch (Exception e) {
                this.e = e;    // Store error
                return null;
            }
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence

            if (e != null) {
                if (current_work == Constants.DESCRIBE)
                    mTextView.setText(R.string.just_a_moment_ellipsis);
                this.e = null;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        new doRequest(current_work).execute();
                    }
                }).start();
            } else {
                Gson gson = new Gson();
                AnalysisResult result = gson.fromJson(data, AnalysisResult.class);

                if (current_work == Constants.DESCRIBE) {

                    mTextView.setText("");
                    for (Caption caption : result.description.captions)
                        mTextView.append(getBeginningFromConfidence(caption.confidence, caption.text) + " " + caption.text);

                    bloatShareIn(false);

                    new doRequest(Constants.ANALYSE).execute();
                } else if (current_work == Constants.ANALYSE) {

                    for (Face face : result.faces)
                        mTextView.append(
                                "\n" + getString(R.string.the) + " " +
                                        face.gender.toString().toLowerCase() + " " +
                                        getString(R.string.here) + " " +
                                        getString(R.string.looks) + " " +
                                        face.age
                        );

                    mTextView.append("\n" + getString(R.string.dom_col_is) + " " + result.color.dominantColorForeground.toLowerCase());

                    if (result.adult.isAdultContent)
                        mTextView.append("\n" + getBeginningFromConfidence(result.adult.adultScore, "") + " not appropriate!");
                    bloatShareIn(true);
                }
            }
        }
    }
}
