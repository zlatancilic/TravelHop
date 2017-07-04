package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
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
import models.UserWithImage;

import static android.R.attr.bitmap;

public class UserSearchListAdapter extends ArrayAdapter<UserWithImage>{

    Context applicationContext;

    public UserSearchListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public UserSearchListAdapter(Context context, int resource, List<UserWithImage> items) {
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
            v = vi.inflate(R.layout.user_list_item, null);
        }

        UserWithImage p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.user_list_username);
            TextView tt2 = (TextView) v.findViewById(R.id.user_list_user_full_name);
            TextView tt3 = (TextView) v.findViewById(R.id.user_list_follow_status);
            ImageView iv1 = (ImageView) v.findViewById(R.id.user_image);

            if (tt1 != null) {
                tt1.setText(p.getUserDetails().getUsername());
            }

            if (tt2 != null) {
                tt2.setText(p.getUserDetails().getFullName());
            }

            if (tt3 != null) {
                tt3.setText(p.getFollowingStatus());
            }


            if (iv1 != null) {
                Bitmap icon = p.getImage();
                if(icon == null) {
                    icon = BitmapFactory.decodeResource(applicationContext.getResources(), R.drawable.default_user_avatar);
                }
                icon = Bitmap.createScaledBitmap(icon, 500, 500, false);
                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(applicationContext.getResources(), icon);
                final float roundPx = (float) icon.getWidth() * 0.6f;
                roundedBitmapDrawable.setCornerRadius(roundPx);
                iv1.setImageDrawable(roundedBitmapDrawable);


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

    private boolean isBetween(long a, long lower, long upper) {
        return a <= upper && a >= lower;
    }
}
