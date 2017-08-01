package utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.widget.Toast;

import com.cilic.zlatan.travelhop.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.List;
import java.util.NoSuchElementException;

import models.PostWithImage;

public class UserFeedListAdapter extends ArrayAdapter<PostWithImage>{

    Context applicationContext;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String currentUserFirebaseId;
    ImageTools imageTools;

    public UserFeedListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public UserFeedListAdapter(Context context, int resource, List<PostWithImage> items) {
        super(context, resource, items);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase  = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        currentUserFirebaseId = firebaseAuth.getCurrentUser().getUid();
        imageTools = new ImageTools();
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

        final PostWithImage p = getItem(position);

        if (p != null) {
            TextView tt1 = (TextView) v.findViewById(R.id.post_user_name);
            TextView tt2 = (TextView) v.findViewById(R.id.post_caption);
            TextView tt3 = (TextView) v.findViewById(R.id.post_date_created);
            final TextView tt4 = (TextView) v.findViewById(R.id.like_count);
            ImageView iv1 = (ImageView) v.findViewById(R.id.post_image);
            ImageView iv2 = (ImageView) v.findViewById(R.id.user_image);
            final ImageView iv3 = (ImageView) v.findViewById(R.id.like_button);

            final Bitmap photoLikedBitmap = BitmapFactory.decodeResource(applicationContext.getResources(), R.drawable.ic_favorite_black_24dp);
            final Bitmap photoNotLikedBitmap = BitmapFactory.decodeResource(applicationContext.getResources(), R.drawable.ic_favorite_border_black_24dp);

            iv3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(iv3 != null) {
                        if(p.isLikedByCurrentUser()) {
                            imageTools.animateLike(iv3, photoNotLikedBitmap);
                            p.setLikedByCurrentUser(false);
                            p.setLikeCount(p.getLikeCount() - 1);
                            tt4.setText(String.valueOf(p.getLikeCount()));
                            databaseReference.child("postLikes").child(p.getFirebaseId()).child(currentUserFirebaseId).removeValue();

                        }
                        else {
                            imageTools.animateLike(iv3, photoLikedBitmap);
                            p.setLikedByCurrentUser(true);
                            p.setLikeCount(p.getLikeCount() + 1);
                            tt4.setText(String.valueOf(p.getLikeCount()));
                            databaseReference.child("postLikes").child(p.getFirebaseId()).child(currentUserFirebaseId).setValue(currentUserFirebaseId);
                        }
                    }
                }
            });


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

            if(tt4 != null) {
                tt4.setText(String.valueOf(p.getLikeCount()));
                System.out.println(String.valueOf(p.getLikeCount()));
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

            if (iv2 != null) {
                Bitmap icon = p.getUserPhoto();
                if(icon == null) {
                    icon = BitmapFactory.decodeResource(applicationContext.getResources(), R.drawable.default_user_avatar);
                }
                icon = Bitmap.createScaledBitmap(icon, 500, 500, false);
                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(applicationContext.getResources(), icon);
                final float roundPx = (float) icon.getWidth() * 0.6f;
                roundedBitmapDrawable.setCornerRadius(roundPx);
                iv2.setImageDrawable(roundedBitmapDrawable);
            }

            if (iv3 != null) {
                if(p.isLikedByCurrentUser()) {
                    iv3.setImageBitmap(photoLikedBitmap);
                }
                else {
                    iv3.setImageBitmap(photoNotLikedBitmap);
                }
            }
        }

        return v;
    }
}
