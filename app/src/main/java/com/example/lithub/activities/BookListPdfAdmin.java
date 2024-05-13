package com.example.lithub.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.example.lithub.adapters.AdapterBookAdmin;
import com.example.lithub.databinding.ActivityBookListPdfAdminBinding;
import com.example.lithub.models.ModelBook;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class BookListPdfAdmin extends AppCompatActivity {
    private ActivityBookListPdfAdminBinding binding;
    private ArrayList<ModelBook> bookArrayList;
    private AdapterBookAdmin adapterBookAdmin;
    private String categoryId,categoryTitle;
    private static final String TAG = "PDF_LIST_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookListPdfAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        categoryId = intent.getStringExtra("categoryId");
        categoryTitle = intent.getStringExtra("categoryTitle");
        loadBookList();
        binding.subtitle.setText(categoryTitle);
        binding.searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try{
                    adapterBookAdmin.getFilter().filter(charSequence);
                }catch (Exception e){
                    Log.d(TAG,"OnTextChanged"+e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadBookList() {
        bookArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.orderByChild("categoryId").equalTo(categoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookArrayList.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ModelBook modelBook = ds.getValue(ModelBook.class);
                            bookArrayList.add(modelBook);
                        }
                        adapterBookAdmin = new AdapterBookAdmin(BookListPdfAdmin.this,bookArrayList);
                        binding.bookList.setAdapter(adapterBookAdmin);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}