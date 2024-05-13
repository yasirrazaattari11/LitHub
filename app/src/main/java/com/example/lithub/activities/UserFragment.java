package com.example.lithub.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.lithub.R;
import com.example.lithub.adapters.AdapterUser;
import com.example.lithub.databinding.FragmentUserBinding;
import com.example.lithub.models.ModelBook;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    // TODO: Rename and change types of parameters
    private String id;
    private String category;
    private String uid;
    private ArrayList<ModelBook> modelBookArrayList;
    private AdapterUser adapterUser;
    private FragmentUserBinding binding;
    public UserFragment() {
        // Required empty public constructor
    }
    public static UserFragment newInstance(String id, String category,String uid) {
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString("category", category);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id = getArguments().getString("id");
            category = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserBinding.inflate(LayoutInflater.from(getContext()),container,false);
        if(category.equals("All")){
            loadAllbooks();
        }else if(category.equals("Most Viewed")){
            loadMostViewedbooks("viewsCount");
        }else if(category.equals("Most Downloaded")){
            loadMostdownloadedBooks("downloadsCount");
        }else{
            loadCategorizeBooks();
        }
        binding.searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try{
                    adapterUser.getFilter().filter(charSequence);

                }catch (Exception e){

                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    private void loadCategorizeBooks() {
        modelBookArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.orderByChild("categoryId").equalTo(id).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        modelBookArrayList.clear();
                        for(DataSnapshot ds:snapshot.getChildren()){
                            ModelBook modelBook = ds.getValue(ModelBook.class);
                            modelBookArrayList.add(modelBook);
                        }
                        adapterUser = new AdapterUser(getContext(),modelBookArrayList);
                        binding.bookRV.setAdapter(adapterUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadMostdownloadedBooks(String orderBy) {
        modelBookArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.orderByChild(orderBy).limitToLast(10).
                addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelBookArrayList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelBook modelBook = ds.getValue(ModelBook.class);
                    modelBookArrayList.add(modelBook);
                }
                adapterUser = new AdapterUser(getContext(),modelBookArrayList);
                binding.bookRV.setAdapter(adapterUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadMostViewedbooks(String viewsCount) {
        modelBookArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.orderByChild(viewsCount).limitToLast(10).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        modelBookArrayList.clear();
                        for(DataSnapshot ds:snapshot.getChildren()){
                            ModelBook modelBook = ds.getValue(ModelBook.class);
                            modelBookArrayList.add(modelBook);
                        }
                        adapterUser = new AdapterUser(getContext(),modelBookArrayList);
                        binding.bookRV.setAdapter(adapterUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllbooks() {
        modelBookArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                modelBookArrayList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelBook modelBook = ds.getValue(ModelBook.class);
                    modelBookArrayList.add(modelBook);
                }
                adapterUser = new AdapterUser(getContext(),modelBookArrayList);
                binding.bookRV.setAdapter(adapterUser);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}