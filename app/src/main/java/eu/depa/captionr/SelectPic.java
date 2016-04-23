package eu.depa.captionr;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.IOException;

public class SelectPic extends AppCompatActivity {

    private Uri mUriPhotoTaken;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_pic);

        setTitle(R.string.caption_new_pic);

        if (context == null)
            context = this;

        if (getIntent().getBooleanExtra("from_widget", false))
            takePhoto(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ImageUri", mUriPhotoTaken);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUriPhotoTaken = savedInstanceState.getParcelable("ImageUri");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            Uri imageUri = null;
            if (data == null || data.getData() == null)
                imageUri = mUriPhotoTaken;
            else if (data.getData() != null)
                imageUri = data.getData();
            Intent intent = new Intent();
            intent.setData(imageUri);
            setResult(RESULT_OK, intent);
            finish();
        } else Toast.makeText(SelectPic.this, "there was an unexpected error", Toast.LENGTH_SHORT).show();
    }

    public void takePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null) {
            // Save the photo taken to a temporary file.
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                File file = File.createTempFile("IMG_", ".jpg", storageDir);
                mUriPhotoTaken = Uri.fromFile(file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriPhotoTaken);
                startActivityForResult(intent, Constants.REQUEST_TAKE_PHOTO);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void selectImageInAlbum(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(intent, Constants.REQUEST_SELECT_IMAGE_IN_ALBUM);
    }

    @Override
    public void onBackPressed() {
        setResult(Constants.FINISH_APP);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        else if (item.getItemId() == R.id.menu_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.themedDialog);
            TextView view = new TextView(context);
            view.setMovementMethod(LinkMovementMethod.getInstance());
            view.setText(R.string.info_main_str);
            view.setPadding(64, 64, 64, 64);
            view.setTextSize(20);
            view.setTextColor(getResources().getColor(R.color.darkGray));
            builder.setView(view);
            builder.show();
        }

        else if (item.getItemId() == R.id.menu_donate)
            startActivity(new Intent(context, AdActivity.class));

        return super.onOptionsItemSelected(item);
    }
}