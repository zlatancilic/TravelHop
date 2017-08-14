package com.cilic.zlatan.travelhop;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

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
    private int preLast;
    private boolean allPostsLoaded = false;

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

    private Runnable createRunnable(final int paramStr){

        Runnable aRunnable = new Runnable(){
            public void run(){
                if(paramStr == listOfPosts.size()) {
                    allPostsLoaded = true;
                }
                System.out.println("ALL POSTS LOADED" + allPostsLoaded);
            }
        };

        return aRunnable;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_user_feed, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase  = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();


        swipeRefreshLayout = (SwipeRefreshLayout) fragmentView.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setProgressViewOffset(true, 25, 230);

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

                final int lastItem = firstVisibleItem + visibleItemCount;

                if(lastItem == totalItemCount)
                {
                    if(preLast!=lastItem)
                    {
                        //to avoid multiple calls for last item
                        loadData();
                        preLast = lastItem;
                    }
                }

            }
        });

        loadData();

        return fragmentView;
    }



    @Override
    public void onRefresh() {
        listOfPosts.clear();
        preLast = 0;
        loadData();
        UserFeedListAdapter customAdapter = (UserFeedListAdapter) userFeedListView.getAdapter();
        while(!customAdapter.allImagesSet()) {

        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadData() {
        if(!allPostsLoaded) {
            String endingTimestampPlaceholder = "0";
            final int initialListSize = listOfPosts.size();
            if (initialListSize != 0) {
                endingTimestampPlaceholder = listOfPosts.get(listOfPosts.size() - 1).getPost().getDateCreated();
            }
            final String endingTimestamp = endingTimestampPlaceholder;
            final UserFeedListAdapter customAdapter = (UserFeedListAdapter) userFeedListView.getAdapter();
            firebaseDatabase.getReference("userFeedPosts/" + firebaseAuth.getCurrentUser().getUid()).orderByChild("dateCreated").limitToLast(initialListSize + 5).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final List<DataSnapshot> tempList = new ArrayList<DataSnapshot>();
                    for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        tempList.add(0, postSnapshot);
                    }
                    getActivity().runOnUiThread(createRunnable((int)dataSnapshot.getChildrenCount()));
                    for (final DataSnapshot postSnapshot : tempList) {
                        final Post currentPost = postSnapshot.getValue(Post.class);
                        final PostWithImage postWithImage = new PostWithImage();
                        postWithImage.setPost(currentPost);
                        postWithImage.setFirebaseId(postSnapshot.getKey());
                        if (initialListSize == 0 || Long.valueOf(postWithImage.getPost().getDateCreated()) < Long.valueOf(endingTimestamp)) {
                            listOfPosts.add(postWithImage);
                            customAdapter.notifyDataSetChanged();
                            customAdapter.downloadImages();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
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
