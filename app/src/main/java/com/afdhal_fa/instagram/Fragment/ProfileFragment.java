package com.afdhal_fa.instagram.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afdhal_fa.instagram.Adapter.MyFotosAdapter;
import com.afdhal_fa.instagram.EditProfileActivity;
import com.afdhal_fa.instagram.FollowersActivity;
import com.afdhal_fa.instagram.Model.Post;
import com.afdhal_fa.instagram.Model.User;
import com.afdhal_fa.instagram.OptionsActivity;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ProfileFragment extends Fragment {

    ImageView image_profile, options;
    TextView posts, followers, following, fullname, bio, username;
    Button edit_profile;
    ImageButton my_fotos, saved_fotos;

    private List<String> _mySaves;

    RecyclerView recyclerView_saves;
    MyFotosAdapter myFotosAdapter_saves;
    List<Post> postList_saves;

    RecyclerView recyclerView;
    MyFotosAdapter myFotosAdapter;
    List<Post> postList;

    FirebaseUser firebaseUser;
    String profileid;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences preferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = preferences.getString("profileid", "none");

        image_profile = view.findViewById(R.id.image_profile);
        options = view.findViewById(R.id.options);

        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        fullname = view.findViewById(R.id.fullname);
        bio = view.findViewById(R.id.bio);
        username = view.findViewById(R.id.username);
        edit_profile = view.findViewById(R.id.edit_profile);

        my_fotos = view.findViewById(R.id.my_fotos);
        saved_fotos = view.findViewById(R.id.saved_fotos);

        recyclerView = view.findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(view.getContext(), 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        myFotosAdapter = new MyFotosAdapter(view.getContext(), postList);
        recyclerView.setAdapter(myFotosAdapter);

        recyclerView_saves = view.findViewById(R.id.recycle_view_save);
        recyclerView_saves.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager_saves = new GridLayoutManager(view.getContext(), 3);
        recyclerView_saves.setLayoutManager(linearLayoutManager_saves);
        postList_saves = new ArrayList<>();
        myFotosAdapter_saves = new MyFotosAdapter(view.getContext(), postList_saves);
        recyclerView_saves.setAdapter(myFotosAdapter_saves);

        recyclerView.setVisibility(View.VISIBLE);
        recyclerView_saves.setVisibility(View.GONE);

        userInfo();
        getFollowers();
        getnrPosts();
        myFotos();
        mySaves();

        if (profileid.equals(firebaseUser.getUid())) {
            edit_profile.setText("Edit Profile");
        } else {
            checkFollow();
            saved_fotos.setVisibility(View.GONE);
        }

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btn = edit_profile.getText().toString();
                if (btn.equals("Edit Profile")) {
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                } else if (btn.equals("follow")) {
                    FirebaseDatabase.getInstance().getReference().child(new Constant().FOLLOW).child(firebaseUser.getUid())
                            .child(new Constant().FOLLOWING)
                            .child(profileid)
                            .setValue(true);
                    FirebaseDatabase.getInstance().getReference().child(new Constant().FOLLOW).child(profileid)
                            .child(new Constant().FOLLOWERS)
                            .child(firebaseUser.getUid())
                            .setValue(true);

                    addNotification();

                } else if (btn.equals("following")) {
                    FirebaseDatabase.getInstance().getReference().child(new Constant().FOLLOW).child(firebaseUser.getUid())
                            .child(new Constant().FOLLOWING)
                            .child(profileid)
                            .removeValue();
                    FirebaseDatabase.getInstance().getReference().child(new Constant().FOLLOW).child(profileid)
                            .child(new Constant().FOLLOWERS)
                            .child(firebaseUser.getUid())
                            .removeValue();

                }
            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), OptionsActivity.class);
                startActivity(intent);
            }
        });
        my_fotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView_saves.setVisibility(View.GONE);
            }
        });

        saved_fotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.GONE);
                recyclerView_saves.setVisibility(View.VISIBLE);
            }
        });

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "followers");
                startActivity(intent);
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileid);
                intent.putExtra("title", "following");
                startActivity(intent);
            }
        });
    }

    private void userInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(profileid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null) {
                    return;
                }

                User user = snapshot.getValue(User.class);
                assert user != null;
                Glide.with(getContext()).load(user.getImageUrl()).into(image_profile);
                username.setText(user.getUsername());
                fullname.setText(user.getFullName());
                bio.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkFollow() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow")
                .child(firebaseUser.getUid())
                .child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(profileid).exists()) {
                    edit_profile.setText("following");
                } else {
                    edit_profile.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowers() {
        DatabaseReference referenceFollowers = FirebaseDatabase.getInstance().getReference()
                .child("Follow")
                .child(firebaseUser.getUid())
                .child("followers");

        referenceFollowers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followers.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference referenceFollowing = FirebaseDatabase.getInstance().getReference()
                .child("Follow")
                .child(firebaseUser.getUid())
                .child("following");

        referenceFollowing.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                following.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getnrPosts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i = 0;
                for (DataSnapshot j : snapshot.getChildren()) {
                    Post post = j.getValue(Post.class);
                    if (post.getPublisher().equals(profileid)) {
                        i++;
                    }
                }

                posts.setText("" + i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void myFotos() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot i : snapshot.getChildren()) {
                    Post post = i.getValue(Post.class);
                    if (post.getPublisher().equals(profileid)) {
                        postList.add(post);
                    }
                }
                Collections.reverse(postList);
                myFotosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void mySaves() {
        _mySaves = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                _mySaves.clear();
                for (DataSnapshot i : snapshot.getChildren()) {
                    _mySaves.add(i.getKey());
                }
                readSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readSaves() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList_saves.clear();
                for (DataSnapshot i : snapshot.getChildren()) {
                    Post post = i.getValue(Post.class);
                    for (String id : _mySaves) {
                        if (post.getPostId().equals(id)) {
                            postList_saves.add(post);
                        }
                    }
                }

                myFotosAdapter_saves.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addNotification() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(profileid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);

        reference.push().setValue(hashMap);
    }
}