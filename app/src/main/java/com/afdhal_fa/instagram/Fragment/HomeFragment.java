package com.afdhal_fa.instagram.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afdhal_fa.instagram.Adapter.PostAdapter;
import com.afdhal_fa.instagram.Adapter.StoryAdapter;
import com.afdhal_fa.instagram.Model.Post;
import com.afdhal_fa.instagram.Model.Story;
import com.afdhal_fa.instagram.R;
import com.afdhal_fa.instagram.Utils.Constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    private RecyclerView recyclerView_story;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;


    private List<String> followingList;

    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.recycle_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerView.setAdapter(postAdapter);
        postAdapter.notifyDataSetChanged();

        recyclerView_story = view.findViewById(R.id.recycle_view_story);
        recyclerView_story.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManagerStory = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView_story.setLayoutManager(linearLayoutManagerStory);
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(), storyList);
        recyclerView_story.setAdapter(storyAdapter);

        progressBar = view.findViewById(R.id.progress_circular);

        checkFollowing();


        return view;
    }

    private void checkFollowing() {
        followingList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(new Constant().FOLLOW)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(new Constant().FOLLOWING);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followingList.clear();
                for (DataSnapshot i : snapshot.getChildren()) {
                    followingList.add(i.getKey());
                }

                readPosts();
                reedStory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readPosts() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(new Constant().POSTS);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    for (String id : followingList) {
                        if (post != null) {
                            if (post.getPublisher().equals(id)) {
                                postList.add(post);
                            }
                        }
                    }
                }
                postAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void reedStory() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long timecurrent = System.currentTimeMillis();
                storyList.clear();
                storyList.add(new Story("", 0, 0, "", FirebaseAuth.getInstance().getCurrentUser().getUid()));

                for (String id : followingList) {
                    int countStory = 0;
                    Story story = null;
                    for (DataSnapshot j : snapshot.child(id).getChildren()) {
                        story = j.getValue(Story.class);
                        if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) {
                            countStory++;
                        }
                    }
                    if (countStory > 0) {
                        storyList.add(story);
                    }
                }
                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}