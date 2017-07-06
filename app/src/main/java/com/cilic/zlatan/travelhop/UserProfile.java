package com.cilic.zlatan.travelhop;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

import utils.ExpandableGridView;
import utils.GridImageAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserProfile.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserProfile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfile extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    Button editProfile;
    FirebaseAuth firebaseAuth;
    SwipeRefreshLayout swipeRefreshLayout;
    ScrollView scrollView;
    ImageView userAvatarImageView;

    private Integer[] dataImages = {R.drawable.default_user_avatar, R.drawable.default_user_avatar,
            R.drawable.default_user_avatar, R.drawable.default_user_avatar,
            R.drawable.default_user_avatar, R.drawable.default_user_avatar,
            R.drawable.default_user_avatar, R.drawable.default_user_avatar,
            R.drawable.default_user_avatar, R.drawable.default_user_avatar,
            R.drawable.default_user_avatar, R.drawable.default_user_avatar };


    public UserProfile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserProfile.
     */
    // TODO: Rename and change types and number of parameters
    public static UserProfile newInstance(String param1, String param2) {
        UserProfile fragment = new UserProfile();
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
        View profileFragment = inflater.inflate(R.layout.fragment_user_profile, container, false);

        firebaseAuth = FirebaseAuth.getInstance();

        swipeRefreshLayout = (SwipeRefreshLayout) profileFragment.findViewById(R.id.swipe_container_user_profile);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setProgressViewOffset(true, 25, 230);

        ExpandableGridView gw = (ExpandableGridView) profileFragment.findViewById(R.id.grid_view);
        gw.setExpanded(true);
        gw.setAdapter(new GridImageAdapter(getContext(), dataImages));

//        gw.setOnTouchListener(new View.OnTouchListener(){
//
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return event.getAction() == MotionEvent.ACTION_MOVE;
//            }
//        });

        userAvatarImageView = (ImageView) profileFragment.findViewById(R.id.user_profile_avatar);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.default_user_avatar);
        setImage(icon);

        scrollView = (ScrollView) profileFragment.findViewById(R.id.scroll_view_container);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollPosition = scrollView.getScrollY();
                if(scrollPosition == 0) {
                    swipeRefreshLayout.setEnabled(true);
                }
                else {
                    swipeRefreshLayout.setEnabled(false);
                }
            }
        });

        editProfile = (Button) profileFragment.findViewById(R.id.edit_profile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(i);
            }
        });

        TextView signOut = (TextView) profileFragment.findViewById(R.id.sign_out);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getActivity())
                        .setTitle("SIGN OUT")
                        .setIcon(R.drawable.ic_exit_to_app_black_24dp)
                        .setMessage("Do you really want to sign out?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                firebaseAuth.signOut();
                                Intent i = new Intent(getActivity(), AuthActivity.class);
                                startActivity(i);
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();


            }
        });

        return profileFragment;
    }

    @Override
    public void onRefresh() {
        editProfile.setText("AAAAAA");
        swipeRefreshLayout.setRefreshing(false);
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

    private void setImage(Bitmap bitmapImage) {
        bitmapImage = Bitmap.createScaledBitmap(bitmapImage, 420, 420, false);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmapImage);
        final float roundPx = (float) bitmapImage.getWidth() * 0.6f;
        roundedBitmapDrawable.setCornerRadius(roundPx);
        userAvatarImageView.setImageDrawable(roundedBitmapDrawable);
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
