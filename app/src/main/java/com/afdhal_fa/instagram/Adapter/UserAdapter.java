package com.afdhal_fa.instagram.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.afdhal_fa.instagram.Fragment.ProfileFragment;
import com.afdhal_fa.instagram.Model.User;
import com.afdhal_fa.instagram.R;
import com.afdhal_fa.instagram.Utils.Constant;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;

    private FirebaseUser firebaseUser;

    public UserAdapter(Context mContext, List<User> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_user, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final User user = mUsers.get(position);

        holder.btn_follow.setVisibility(View.VISIBLE);

        holder.username.setText(user.getUsername());
        holder.fullname.setText(user.getFullName());
        Glide.with(mContext).load(user.getImageUrl()).disallowHardwareConfig().into(holder.image_profile);

        isFollowing(user.getId(), holder.btn_follow);

        if (user.getId().equals(firebaseUser.getUid())) {
            holder.btn_follow.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", user.getId());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });

        holder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.btn_follow.getText().toString().equals("follow")) {
                    FirebaseDatabase.getInstance().getReference().child(new Constant().FOLLOW).child(firebaseUser.getUid())
                            .child(new Constant().FOLLOWING).child(user.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child(new Constant().FOLLOW).child(user.getId())
                            .child(new Constant().FOLLOWERS).child(firebaseUser.getUid()).setValue(true);

                    addNotification(user.getId());
                } else {
                    FirebaseDatabase.getInstance().getReference().child(new Constant().FOLLOW).child(firebaseUser.getUid())
                            .child(new Constant().FOLLOWING).child(user.getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child(new Constant().FOLLOW).child(user.getId())
                            .child(new Constant().FOLLOWERS).child(firebaseUser.getUid()).removeValue();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public TextView fullname;
        public CircleImageView image_profile;
        public Button btn_follow;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            image_profile = itemView.findViewById(R.id.image_profile);
            btn_follow = itemView.findViewById(R.id.button_follow);
        }
    }

    private void isFollowing(final String userid, final Button button) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child(new Constant().FOLLOW).child(firebaseUser.getUid()).child(new Constant().FOLLOWING);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(userid).exists()) {
                    button.setText("following");
                } else {
                    button.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addNotification(String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);

        reference.push().setValue(hashMap);
    }
}