package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.cilic.zlatan.travelhop.R;

import java.util.ArrayList;

import models.UserWithImage;

public class SearchUsersListAdapter extends BaseAdapter implements Filterable{

    Context applicationContext;
    LayoutInflater layoutInflater;
    private ArrayList<UserWithImage> itemsToDisplay;
    private ArrayList<UserWithImage> itemsOriginal;

    public SearchUsersListAdapter(Context context, ArrayList<UserWithImage> items) {
        applicationContext = context;
        itemsToDisplay = items;
        itemsOriginal = items;
        layoutInflater = LayoutInflater.from(context);

    }

    public void setAppContext(Context appContext) {
        applicationContext = appContext;
    }

    @Override
    public int getCount() {
        return itemsToDisplay.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            v = layoutInflater.inflate(R.layout.user_list_item, null);
        }

        UserWithImage p = itemsToDisplay.get(position);

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


    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {
                itemsToDisplay = (ArrayList<UserWithImage>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<UserWithImage> filteredArrList = new ArrayList<UserWithImage>();

                if (itemsOriginal == null) {
                    itemsOriginal = new ArrayList<UserWithImage>(itemsToDisplay);
                }
                if (constraint == null || constraint.length() == 0) {
                    results.count = itemsOriginal.size();
                    results.values = itemsOriginal;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < itemsOriginal.size(); i++) {
                        String data = itemsOriginal.get(i).getUserDetails().getUsername();
                        if (data.toLowerCase().startsWith(constraint.toString())) {
                            UserWithImage tempUser = new UserWithImage(itemsOriginal.get(i).getUserDetails(), itemsOriginal.get(i).getImage());
                            tempUser.setFollowingStatus(itemsOriginal.get(i).getFollowingStatus());
                            filteredArrList.add(tempUser);
                        }
                    }
                    results.count = filteredArrList.size();
                    results.values = filteredArrList;
                }
                return results;
            }
        };
        return filter;
    }
}
