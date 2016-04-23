package eu.depa.captionr;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class AdActivity extends AppCompatActivity {

    private Context context;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ad_activity_layout);
        setTitle(R.string.donate);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (context == null)
            context = this;

        new Thread(new Runnable() {
            @Override
            public void run() {

                final AdView ad = new AdView(context);
                final RelativeLayout mom = (RelativeLayout) findViewById(R.id.ad_act_mom);
                final AdRequest adRequest = new AdRequest.Builder().build();
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                params.addRule(RelativeLayout.CENTER_IN_PARENT);

                ad.setAdUnitId(Constants.SP_ad_unit_id);
                ad.setAdSize(AdSize.MEDIUM_RECTANGLE);
                ad.setLayoutParams(params);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (mom != null)
                            mom.addView(ad);

                        ad.loadAd(adRequest);

                        ad.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                super.onAdClosed();
                            }

                            @Override
                            public void onAdFailedToLoad(int errorCode) {
                                super.onAdFailedToLoad(errorCode);
                            }

                            @Override
                            public void onAdLeftApplication() {
                                super.onAdLeftApplication();
                            }

                            @Override
                            public void onAdOpened() {
                                super.onAdOpened();
                            }

                            @Override
                            public void onAdLoaded() {
                                super.onAdLoaded();
                                View rotella = findViewById(R.id.ad_pb);
                                if (rotella != null)
                                    rotella.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return true;
    }
}
