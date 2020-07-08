package com.afdhal_fa.instagram.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.afdhal_fa.instagram.Fragment.PostDetailFragment;
import com.afdhal_fa.instagram.Model.Post;
import com.afdhal_fa.instagram.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class MyFotosAdapter extends RecyclerView.Adapter<MyFotosAdapter.ViewHolder> {
    private Context mContext;
    private List<Post> mPosts;

    public MyFotosAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fotos, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Post post = mPosts.get(position);
        Glide.with(mContext).load(post.getPostImage()).into(holder.post_image);

        holder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postid", post.getPostId());
                editor.apply();
                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PostDetailFragment()).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView post_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            post_image = itemView.findViewById(R.id.post_image);
        }
    }
}
