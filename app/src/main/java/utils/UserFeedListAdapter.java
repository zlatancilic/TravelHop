package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cilic.zlatan.travelhop.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import models.PostWithImage;

public class UserFeedListAdapter extends ArrayAdapter<PostWithImage>{

    Context applicationContext;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String currentUserFirebaseId;
    ImageTools imageTools;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

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

    public void downloadImages() {
        int size = getCount();
        for(int i = 0; i < size; i++) {
            final PostWithImage currentPost = getItem(i);
            if(currentPost.getImage() == null) {
                StorageReference imageReference = storageReference.child(currentPost.getPost().getDownloadPath());
                final long ONE_MEGABYTE = 1024 * 1024;
                imageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes , 0, bytes.length);
                        currentPost.setImage(bitmap);
                        notifyDataSetChanged();
                    }
                });
            }
        }
        downloadUserImages();
    }

    public void downloadUserImages() {
        int size = getCount();
        for(int i = 0; i < size; i++) {
            final PostWithImage currentPost = getItem(i);
            if(currentPost.getUserPhoto() == null) {
                boolean imageSetFast = false;
                for(int j = 0; j < size; j++) {
                    final PostWithImage currentPostInner = getItem(j);
                    if(currentPost.getPost().getUserId().equals(currentPostInner.getPost().getUserId()) && currentPostInner.getUserPhoto() != null) {
                        currentPost.setUserPhoto(currentPostInner.getUserPhoto());
                        imageSetFast = true;
                        break;
                    }
                }
                if(!imageSetFast) {
                    String downloadUserPhotoPath = "userProfileImages/" + currentPost.getPost().getUserId();
                    final StorageReference userPhotoReference = storageReference.child(downloadUserPhotoPath);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    userPhotoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 1;
                            final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                            currentPost.setUserPhoto(bitmap);
                            notifyDataSetChanged();
                        }
                    });
                }
            }
        }
        downloadLikes();
    }

    public void downloadLikes() {
        int size = getCount();
        for(int i = 0; i < size; i++) {
            final PostWithImage currentPost = getItem(i);
            firebaseDatabase.getReference("postLikes/" + currentPost.getFirebaseId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshotInner) {
                    boolean likedByCurrentUser = false;
                    long likeCount = 0;
                    if (dataSnapshotInner != null) {
                        for (DataSnapshot currentLike : dataSnapshotInner.getChildren()) {
                            if (currentLike.getKey().equals(firebaseAuth.getCurrentUser().getUid())) {
                                likedByCurrentUser = true;
                                break;
                            }
                        }
                        likeCount = dataSnapshotInner.getChildrenCount();
                    }
                    currentPost.setLikedByCurrentUser(likedByCurrentUser);
                    currentPost.setLikeCount(likeCount);
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
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
            final ImageView iv1 = (ImageView) v.findViewById(R.id.post_image);
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
                icon = Bitmap.createScaledBitmap(icon, 300, 300, false);
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
