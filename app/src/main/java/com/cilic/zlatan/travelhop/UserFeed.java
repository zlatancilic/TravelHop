package com.cilic.zlatan.travelhop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

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

import java.util.ArrayList;
import java.util.List;

import models.Post;
import models.PostWithImage;
import utils.UserFeedListAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserFeed.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserFeed#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserFeed extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    List<PostWithImage> listOfPosts = new ArrayList<PostWithImage>();
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    ListView userFeedListView;
    SwipeRefreshLayout swipeRefreshLayout;

    public UserFeed() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserFeed.
     */
    // TODO: Rename and change types and number of parameters
    public static UserFeed newInstance(String param1, String param2) {
        UserFeed fragment = new UserFeed();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_user_feed, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase  = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        swipeRefreshLayout = (SwipeRefreshLayout) fragmentView.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);

        userFeedListView = (ListView) fragmentView.findViewById(R.id.userFeedListView);

        final UserFeedListAdapter customAdapter = new UserFeedListAdapter(fragmentView.getContext(), R.layout.item, listOfPosts);

        customAdapter.setAppContext(getActivity().getApplicationContext());

        

        userFeedListView.setAdapter(customAdapter);

        userFeedListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean enabled = false;
                if(userFeedListView != null && userFeedListView.getChildCount() > 0) {
                    boolean firstItemVisible = userFeedListView.getFirstVisiblePosition() == 0;
                    boolean topOfFirstItemVisible = userFeedListView.getChildAt(0).getTop() == 0;
                    enabled = firstItemVisible && topOfFirstItemVisible;
                }
                swipeRefreshLayout.setEnabled(enabled);

            }
        });

        firebaseDatabase.getReference("userFeedPosts/" + firebaseAuth.getCurrentUser().getUid()).orderByChild("dateCreated").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(final DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    final Post currentPost = postSnapshot.getValue(Post.class);
                    final PostWithImage postWithImage = new PostWithImage();
                    postWithImage.setPost(currentPost);
                    listOfPosts.add(0, postWithImage);
                    StorageReference imageReference = storageReference.child(currentPost.getDownloadPath());
                    final long ONE_MEGABYTE = 1024 * 1024;
                    imageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes , 0, bytes.length);
                            int index = listOfPosts.indexOf(postWithImage);
                            listOfPosts.get(index).setImage(bitmap);
                            customAdapter.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        return fragmentView;
    }



    @Override
    public void onRefresh() {
        listOfPosts.clear();
        final UserFeedListAdapter customAdapter = (UserFeedListAdapter) userFeedListView.getAdapter();
        firebaseDatabase.getReference("userFeedPosts/" + firebaseAuth.getCurrentUser().getUid()).orderByChild("dateCreated").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                for(final DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    final Post currentPost = postSnapshot.getValue(Post.class);
                    final PostWithImage postWithImage = new PostWithImage();
                    postWithImage.setPost(currentPost);
                    listOfPosts.add(0, postWithImage);
                    StorageReference imageReference = storageReference.child(currentPost.getDownloadPath());
                    final long ONE_MEGABYTE = 1024 * 1024;
                    imageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes , 0, bytes.length);
                            int index = listOfPosts.indexOf(postWithImage);
                            listOfPosts.get(index).setImage(bitmap);
                            customAdapter.notifyDataSetChanged();
                            Log.i("POSTS SIZE: ", String.valueOf(listOfPosts.size()));
                            Log.i("CHILDREN SIZE: ",String.valueOf(dataSnapshot.getChildrenCount()));
                            if(listOfPosts.size() == dataSnapshot.getChildrenCount()) {
                                System.out.println("SKOLA123");
                                boolean allImagesSet = true;
                                for(PostWithImage post: listOfPosts) {
                                    if(post.getImage() == null) {
                                        allImagesSet = false;
                                        break;
                                    }
                                }
                                if(allImagesSet) {
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    public void scrollToTop() {
        //userFeedListView.setSelectionAfterHeaderView();
        userFeedListView.smoothScrollToPosition(0);
    }
}
