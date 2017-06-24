package utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cilic.zlatan.travelhop.R;

import java.util.List;

import models.Post;
import models.PostWithImage;

public class UserFeedListAdapter extends ArrayAdapter<PostWithImage>{

    public UserFeedListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public UserFeedListAdapter(Context context, int resource, List<PostWithImage> items) {
        super(context, resource, items);
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
            TextView tt1 = (TextView) v.findViewById(R.id.id);
            TextView tt2 = (TextView) v.findViewById(R.id.categoryId);
            TextView tt3 = (TextView) v.findViewById(R.id.description);
            ImageView iv1 = (ImageView) v.findViewById(R.id.image);

            if (tt1 != null) {
                tt1.setText(p.getPost().getCaption());
            }

            if (tt2 != null) {
                tt2.setText(p.getPost().getDownloadPath());
            }

            if (tt3 != null) {
                tt3.setText(p.getPost().getDateCreated());
            }

            if (iv1 != null) {
                iv1.setImageBitmap(p.getImage());
            }
        }

        return v;
    }
}
