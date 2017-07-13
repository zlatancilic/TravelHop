package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cilic.zlatan.travelhop.R;

import java.util.List;

import models.PostWithImage;

public class GridImageAdapter extends BaseAdapter {

    private Context activityContext;

    private List listOfPosts;

    public GridImageAdapter(Context c, List<PostWithImage> listOfPosts) {
        activityContext = c;
        this.listOfPosts = listOfPosts;
    }

    @Override
    public int getCount() {
        return listOfPosts.size();
    }


    public void addElements(List<PostWithImage> posts) {
        listOfPosts = posts;

        notifyDataSetChanged();
    }

    public boolean checkAllDataSet(int childrenCount) {
        if(listOfPosts.size() == childrenCount) {
            return true;
        }
        return false;
    }

    public void clearData() {
        if(listOfPosts != null) {
            listOfPosts.clear();
        }
    }

    @Override
    public Object getItem(int position) {
        return listOfPosts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater layoutInflater = (LayoutInflater) activityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if(convertView == null) {
            gridView = layoutInflater.inflate(R.layout.grid_image, null);
        }
        else {
            gridView = (View) convertView;
        }

        ImageView imageView = (ImageView) gridView.findViewById(R.id.grid_item_image);
        PostWithImage currentPost = (PostWithImage) listOfPosts.get(position);
        Bitmap imageToSet = currentPost.getImage();
        if(imageToSet == null) {
            imageToSet = BitmapFactory.decodeResource(activityContext.getResources(), R.drawable.loading_image);
        }
        imageView.setImageBitmap(imageToSet);

        return gridView;
    }
}
