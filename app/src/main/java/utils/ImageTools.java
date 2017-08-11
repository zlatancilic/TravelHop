package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.NoSuchElementException;

public class ImageTools {

    public ImageTools() {
    }

    public void scaleImage(ImageView view, Context applicationContext) throws NoSuchElementException {
        Bitmap bitmap = null;

        try {
            Drawable drawing = view.getDrawable();
            bitmap = ((BitmapDrawable) drawing).getBitmap();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("No drawable on given view");
        } catch (ClassCastException e) {
            // Check bitmap is Ion drawable
        }

        // Get current dimensions AND the desired bounding box
        int width = 0;

        try {
            width = bitmap.getWidth();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("Can't find bitmap on given view/drawable");
        }

        int height = bitmap.getHeight();
        DisplayMetrics displayMetrics = applicationContext.getResources().getDisplayMetrics();
        float bounding = displayMetrics.widthPixels;

        if(height > width) {
            float xScale = bounding/ width;
            bounding = xScale * height;
        }

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = bounding / width;
        float yScale = bounding/ height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth(); // re-use
        height = scaledBitmap.getHeight(); // re-use
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);

        // Apply the scaled bitmap
        view.setImageDrawable(result);

        // Now change ImageView's dimensions to match the scaled image
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
    }

    public void animateLike(final ImageView imageView, final Bitmap bitmap) {
        imageView.animate().scaleXBy(-0.3f).scaleYBy(-0.3f).setDuration(170).withEndAction(new Runnable() {
            @Override
            public void run() {
                imageView.animate().scaleXBy(0.45f).scaleYBy(0.45f).setDuration(150).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                        imageView.animate().scaleXBy(-0.15f).scaleYBy(-0.15f).setDuration(150);
                    }
                });
            }
        });
    }
}
