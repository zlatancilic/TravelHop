package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
                TimeDifferenceCalculator timeDifferenceCalculator = new TimeDifferenceCalculator();
                tt3.setText(timeDifferenceCalculator.calculateAndFormat(Long.valueOf(p.getPost().getDateCreated())));
            }


            if (iv1 != null) {
                Bitmap tempBitmap = p.getImage();
                if(tempBitmap == null) {
                    tempBitmap = BitmapFactory.decodeResource(applicationContext.getResources(), R.drawable.loading_image);
                }
                iv1.setImageBitmap(tempBitmap);
                ImageTools imageTools = new ImageTools();
                imageTools.scaleImage(iv1, applicationContext);
            }
        }

        return v;
    }
}
