package utils;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.cilic.zlatan.travelhop.R;

/**
 * Created by zlatan on 06/07/2017.
 */

public class GridImageAdapter extends BaseAdapter {

    private Context activityContext;

    private Integer[] dataImages;

    public GridImageAdapter(Context c, Integer[] images) {
        activityContext = c;
        dataImages = images;
    }

    @Override
    public int getCount() {
        return dataImages.length;
    }

    @Override
    public Object getItem(int position) {
        return dataImages[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater layoutInflater = (LayoutInflater) activityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if(convertView == null) {
            gridView = new View(activityContext);
            gridView = layoutInflater.inflate(R.layout.grid_image, null);
            ImageView imageView = (ImageView) gridView.findViewById(R.id.grid_item_image);
            imageView.setImageResource(dataImages[position]);
        }
        else {
            gridView = (View) convertView;
        }

        return gridView;
    }
}
