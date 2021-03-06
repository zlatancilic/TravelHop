package com.cilic.zlatan.travelhop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import models.Post;
import models.PostWithImage;
import utils.ImageTools;
import utils.TimeDifferenceCalculator;
import utils.UserFeedListAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SinglePost.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SinglePost#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SinglePost extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "postIdParam";
    private static final String ARG_PARAM2 = "userIdParam";

    // TODO: Rename and change types of parameters
    private String postIdParam;
    private String userIdParam;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    PostWithImage currentPostWithImage;

    ImageView userPhotoImageView;

    private OnFragmentInteractionListener mListener;

    public SinglePost() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param postId Parameter 1.
     * @param userId Parameter 2.
     * @return A new instance of fragment SinglePost.
     */
    // TODO: Rename and change types and number of parameters
    public static SinglePost newInstance(String postId, String userId) {
        SinglePost fragment = new SinglePost();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, postId);
        args.putString(ARG_PARAM2, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postIdParam = getArguments().getString(ARG_PARAM1);
            userIdParam = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View singlePostFragment = inflater.inflate(R.layout.fragment_single_post, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase  = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        final TextView userNameTextView = (TextView) singlePostFragment.findViewById(R.id.post_user_name);
        final TextView postCaptionTextView = (TextView) singlePostFragment.findViewById(R.id.post_caption);
        final TextView dateCreatedTextView = (TextView) singlePostFragment.findViewById(R.id.post_date_created);
        final TextView likeCountTextView = (TextView) singlePostFragment.findViewById(R.id.like_count);
        final ImageView postImageView = (ImageView) singlePostFragment.findViewById(R.id.post_image);
        final ImageView likeButtonImageView = (ImageView) singlePostFragment.findViewById(R.id.like_button);

        final Bitmap photoLikedBitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ic_favorite_black_24dp);
        final Bitmap photoNotLikedBitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ic_favorite_border_black_24dp);
        final ImageTools imageTools = new ImageTools();

        likeButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(likeButtonImageView != null) {
                    if(currentPostWithImage.isLikedByCurrentUser()) {
                        imageTools.animateLike(likeButtonImageView, photoNotLikedBitmap);
                        currentPostWithImage.setLikedByCurrentUser(false);
                        currentPostWithImage.setLikeCount(currentPostWithImage.getLikeCount() - 1);
                        likeCountTextView.setText(String.valueOf(currentPostWithImage.getLikeCount()));
                        databaseReference.child("postLikes").child(currentPostWithImage.getFirebaseId()).child(userIdParam).removeValue();

                    }
                    else {
                        imageTools.animateLike(likeButtonImageView, photoLikedBitmap);
                        currentPostWithImage.setLikedByCurrentUser(true);
                        currentPostWithImage.setLikeCount(currentPostWithImage.getLikeCount() + 1);
                        likeCountTextView.setText(String.valueOf(currentPostWithImage.getLikeCount()));
                        databaseReference.child("postLikes").child(currentPostWithImage.getFirebaseId()).child(userIdParam).setValue(userIdParam);
                    }
                }
            }
        });

        userPhotoImageView = (ImageView) singlePostFragment.findViewById(R.id.user_image);

        if(postIdParam != null && userIdParam != null) {
            firebaseDatabase.getReference("activityStreamPosts/" + userIdParam + "/" + postIdParam).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    final Post currentPost = dataSnapshot.getValue(Post.class);
                    final PostWithImage postWithImage = new PostWithImage();
                    postWithImage.setPost(currentPost);

                    firebaseDatabase.getReference("postLikes/" + postIdParam).addListenerForSingleValueEvent(new ValueEventListener() {
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
                        postWithImage.setLikedByCurrentUser(likedByCurrentUser);
                        postWithImage.setLikeCount(likeCount);
                        postWithImage.setFirebaseId(postIdParam);

                        likeCountTextView.setText(String.valueOf(postWithImage.getLikeCount()));
                        if(postWithImage.isLikedByCurrentUser()) {
                            likeButtonImageView.setImageBitmap(BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ic_favorite_black_24dp));
                        }
                        userNameTextView.setText(postWithImage.getPost().getUsername());
                        String boldText = postWithImage.getPost().getUsername();
                        String normalText = " " + postWithImage.getPost().getCaption();
                        SpannableString str = new SpannableString(boldText + normalText);
                        str.setSpan(new StyleSpan(Typeface.BOLD), 0, boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        postCaptionTextView.setText(str);
                        TimeDifferenceCalculator timeDifferenceCalculator = new TimeDifferenceCalculator();
                        dateCreatedTextView.setText(timeDifferenceCalculator.calculateAndFormat(Long.valueOf(postWithImage.getPost().getDateCreated())));
                        Bitmap tempBitmap = BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(), R.drawable.loading_image);
                        postImageView.setImageBitmap(tempBitmap);
                        final ImageTools imageTools = new ImageTools();
                        imageTools.scaleImage(postImageView, getContext());

                        String downloadPath = currentPost.getDownloadPath().replace("activityStreamThumbnails", "activityStreamImages");
                        final StorageReference imageReference = storageReference.child(downloadPath);
                        final long ONE_MEGABYTE = 1024 * 1024;
                        imageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes , 0, bytes.length);
                                postWithImage.setImage(bitmap);
                                postWithImage.setFirebaseId(dataSnapshot.getKey());

                                postImageView.setImageBitmap(postWithImage.getImage());
                                imageTools.scaleImage(postImageView, getContext());

                                String downloadUserPhotoPath = "userProfileImages/" + postWithImage.getPost().getUserId();
                                final StorageReference userPhotoReference = storageReference.child(downloadUserPhotoPath);
                                userPhotoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap userPhoto = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        postWithImage.setUserPhoto(userPhoto);
                                        currentPostWithImage = postWithImage;
                                        setUserPhoto(userPhoto);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        return singlePostFragment;
    }

    private void setUserPhoto(Bitmap icon) {
        if(icon == null) {
            icon = BitmapFactory.decodeResource(getActivity().getApplicationContext().getResources(), R.drawable.default_user_avatar);
        }
        icon = Bitmap.createScaledBitmap(icon, 500, 500, false);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getActivity().getApplicationContext().getResources(), icon);
        final float roundPx = (float) icon.getWidth() * 0.6f;
        roundedBitmapDrawable.setCornerRadius(roundPx);

        userPhotoImageView.setImageDrawable(roundedBitmapDrawable);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
