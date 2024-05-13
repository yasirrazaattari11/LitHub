package com.example.lithub.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.lithub.databinding.ActivityDashBoardUserBinding;
import com.example.lithub.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashBoardUserActivity extends AppCompatActivity {
    private ActivityDashBoardUserBinding binding;
    public ArrayList<ModelCategory> categoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashBoardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        setupViewPagerAdapter(binding.viewPager);
        binding.tablayout.setupWithViewPager(binding.viewPager);
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                checkUser();
            }
        });
    }
    private  void setupViewPagerAdapter(ViewPager viewPager){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this);
        categoryArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Categories");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();
                ModelCategory modelAll = new ModelCategory("01","All",1,"");
                ModelCategory modelMostViewed = new ModelCategory("02","Most Viewed",1,"");
                ModelCategory modelMostDownloaded = new ModelCategory("03","Most Downloaded",1,"");
                categoryArrayList.add(modelAll);
                categoryArrayList.add(modelMostViewed);
                categoryArrayList.add(modelMostDownloaded);
                viewPagerAdapter.addFragment(UserFragment.newInstance(""+modelAll.getId(),""+modelAll.getCategory(),""+modelAll.getUid()),modelAll.getCategory());
                viewPagerAdapter.addFragment(UserFragment.newInstance(""+modelMostViewed.getId(),""+modelMostViewed.getCategory(),""+modelMostViewed.getUid()),modelMostViewed.getCategory());
                viewPagerAdapter.addFragment(UserFragment.newInstance(""+modelMostDownloaded.getId(),""+modelMostDownloaded.getCategory(),""+modelMostDownloaded.getUid()),modelMostDownloaded.getCategory());
                viewPagerAdapter.notifyDataSetChanged();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelCategory modelCategory = ds.getValue(ModelCategory.class);
                    categoryArrayList.add(modelCategory);
                    viewPagerAdapter.addFragment(UserFragment.newInstance(""+modelCategory.getId(),""+modelCategory.getCategory(),""+modelCategory.getUid()),modelCategory.getCategory());
                    viewPagerAdapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        viewPager.setAdapter(viewPagerAdapter);
    }
    public  class ViewPagerAdapter extends FragmentPagerAdapter{
        private ArrayList<UserFragment> fragmentArrayList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;
        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior,Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentArrayList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentArrayList.size();
        }
        private void addFragment(UserFragment userFragment,String title){
            fragmentArrayList.add(userFragment);
            fragmentTitleList.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }
    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser == null){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        else {
            String email = firebaseUser.getEmail();
            binding.email.setText(email);

        }
    }
}