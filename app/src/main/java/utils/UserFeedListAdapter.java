package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cilic.zlatan.travelhop.R;

import java.util.List;
import java.util.NoSuchElementException;

import models.PostWithImage;

public class UserFeedListAdapter extends ArrayAdapter<PostWithImage>{

    Context applicationContext;

    public UserFeedListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public UserFeedListAdapter(Context context, int resource, List<PostWithImage> items) {
        super(context, resource, items);
    }

    public void setAppContext(Context appContext) {
        applicationContext = appContext;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.item, null);
        }

        PostWithImage p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.post_user_name);
            TextView tt2 = (TextView) v.findViewById(R.id.post_caption);
            TextView tt3 = (TextView) v.findViewById(R.id.post_date_created);
            ImageView iv1 = (ImageView) v.findViewById(R.id.post_image);

            if (tt1 != null) {
                tt1.setText(p.getPost().getUsername());
            }

            if (tt2 != null) {
                String boldText = p.getPost().getUsername();
                String normalText = " " + p.getPost().getCaption();
                SpannableString str = new SpannableString(boldText + normalText);
                str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tt2.setText(str);
            }

            if (tt3 != null) {
                tt3.setText(p.getPost().getDateCreated());
            }


            if (iv1 != null) {
                iv1.setImageBitmap(p.getImage());
                scaleImage(iv1);
            }
        }

        return v;
    }

    private void scaleImage(ImageView view) throws NoSuchElementException {
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

        Log.i("Test", "original width = " + Integer.toString(width));
        Log.i("Test", "original height = " + Integer.toString(height));
        Log.i("Test", "bounding = " + Float.toString(bounding));

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = bounding / width;
        float yScale = bounding/ height;
        float scale = (xScale <= yScale) ? xScale : yScale;
        Log.i("Test", "xScale = " + Float.toString(xScale));
        Log.i("Test", "yScale = " + Float.toString(yScale));
        Log.i("Test", "scale = " + Float.toString(scale));

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth(); // re-use
        height = scaledBitmap.getHeight(); // re-use
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);
        Log.i("Test", "scaled width = " + Integer.toString(width));
        Log.i("Test", "scaled height = " + Integer.toString(height));

        // Apply the scaled bitmap
        view.setImageDrawable(result);

        // Now change ImageView's dimensions to match the scaled image
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);

        Log.i("Test", "done");
    }
}
