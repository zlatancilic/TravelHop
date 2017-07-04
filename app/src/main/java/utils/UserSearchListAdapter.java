package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cilic.zlatan.travelhop.R;

import java.util.List;

import models.UserWithImage;

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
}
