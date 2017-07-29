package com.cilic.zlatan.travelhop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import models.Post;
import models.PostWithImage;
import models.UserDetails;
import models.UserWithImage;
import utils.SearchUsersListAdapter;
import utils.UserSearchListAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchUsers.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchUsers#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchUsers extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    ListView userListView;
    ArrayList<UserWithImage> listOfUsers = new ArrayList<UserWithImage>();

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private FirebaseAuth firebaseAuth;
    EditText searchEditText;

    public SearchUsers() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchUsers.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchUsers newInstance(String param1, String param2) {
        SearchUsers fragment = new SearchUsers();
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
        View userListFragmentView = inflater.inflate(R.layout.fragment_search_users, container, false);

        userListView = (ListView) userListFragmentView.findViewById(R.id.user_list_search);

        final SearchUsersListAdapter cAdapter = new SearchUsersListAdapter(userListFragmentView.getContext(), listOfUsers);

//        final UserSearchListAdapter customAdapter = new UserSearchListAdapter(userListFragmentView.getContext(), R.layout.item, listOfUsers);
//
//        customAdapter.setAppContext(getActivity().getApplicationContext());

        userListView.setAdapter(cAdapter);
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getContext(), cAdapter.getFirebaseId(position), Toast.LENGTH_SHORT).show();
                onUserClicked(cAdapter.getFirebaseId(position), cAdapter.getFollowingStatus(position));
            }
        });

        searchEditText = (EditText) userListFragmentView.findViewById(R.id.search_users_edit_text);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase  = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        listOfUsers.clear();
        final String followers_img_path = "userDetails/" + firebaseAuth.getCurrentUser().getUid() + "/following/";
        DatabaseReference followersList = firebaseDatabase.getReference(followers_img_path);
        followersList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                final List followersList = dataSnapshot.getValue(t);
                if( followersList == null ) {

                }
                else {
                    firebaseDatabase.getReference("userDetails/").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(final DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                if(!firebaseAuth.getCurrentUser().getUid().equals(postSnapshot.getKey())) {
                                    final UserDetails currentUserDetails = postSnapshot.getValue(UserDetails.class);
                                    final UserWithImage userWithImage = new UserWithImage();
                                    userWithImage.setUserDetails(currentUserDetails);
//                                    if (followersList.contains(postSnapshot.getKey())) {
//                                        userWithImage.setFollowingStatus("Following");
//                                    } else {
//                                        userWithImage.setFollowingStatus("Not following");
//                                    }
                                    userWithImage.setFollowingStatus(followersList.contains(postSnapshot.getKey()));
                                    userWithImage.setFirebaseId(postSnapshot.getKey());
                                    listOfUsers.add(0, userWithImage);
                                    StorageReference imageReference = storageReference.child("userProfileImages/" + postSnapshot.getKey());
                                    final long ONE_MEGABYTE = 1024 * 1024;
                                    imageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            int index = listOfUsers.indexOf(userWithImage);
                                            listOfUsers.get(index).setImage(bitmap);
                                            cAdapter.notifyDataSetChanged();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        return  userListFragmentView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onUserClicked(String firebaseId, boolean followingStatus) {
        if (mListener != null) {
            mListener.openUserProfile(firebaseId, followingStatus);
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
        void openUserProfile(String firebaseId, boolean followingStatus);
    }
}
