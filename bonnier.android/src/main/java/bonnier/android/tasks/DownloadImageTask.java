package bonnier.android.tasks;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;
import android.view.animation.GridLayoutAnimationController;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by sessingo on 17/08/15.
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    boolean fade;
    protected int duration;

    public DownloadImageTask(ImageView bmImage) {
        this(bmImage, true);
    }

    public DownloadImageTask(ImageView bmImage, boolean fade) {

        this.bmImage = bmImage;
        this.fade = fade;

        if(this.fade) {
            this.bmImage.setAlpha(0f);
        }
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap image = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            image = BitmapFactory.decodeStream(in);
            in.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return image;
    }

    protected void onPostExecute(Bitmap result) {
        if(result != null) {
            bmImage.setImageBitmap(result);
        }

        if (this.fade) {
            ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
            a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    bmImage.setAlpha(animation.getAnimatedFraction());
                }
            });
            a.setDuration(500);
            a.start();
        } else {
            bmImage.setAlpha(1f);
        }
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}