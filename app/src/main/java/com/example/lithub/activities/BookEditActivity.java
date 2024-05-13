package com.example.lithub.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.lithub.databinding.ActivityBookEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class BookEditActivity extends AppCompatActivity {
    private ActivityBookEditBinding binding;
    private String bookId;
    private ProgressDialog progressDialog;
    private ArrayList<String> categoryTitleArray,categoryIdArray;
    private static final String TAG = "BOOK_EDIT_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bookId = getIntent().getStringExtra("bookId");
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        loadCategories();
        loadBookInfo();
        binding.categorySelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CategoryDialog();
            }
        });
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        binding.update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

    }
    private  String title = "",description = "";
    private void validateData() {
        title = binding.titleEdit.getText().toString().trim();
        description = binding.descriptionEdit.getText().toString();
        if(TextUtils.isEmpty(title)){
            Toast.makeText(this,"Enter Title",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this,"Enter Description",Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(s_categoryId)){
            Toast.makeText(this,"Select Category",Toast.LENGTH_SHORT).show();
        }else{
            updateBook();
        }
    }

    private void updateBook() {
        progressDialog.setMessage("Updating");
        progressDialog.show();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("title",""+title);
        hashMap.put("description",""+description);
        hashMap.put("categoryId",""+s_categoryId);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(BookEditActivity.this,"Updated Successfully",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(BookEditActivity.this,"Failed to Update",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadBookInfo() {
        DatabaseReference refBooks = FirebaseDatabase.getInstance().getReference("Books");
        refBooks.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        s_categoryId = ""+snapshot.child("categoryId").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String title = ""+snapshot.child("title").getValue();
                        binding.titleEdit.setText(title);
                        binding.descriptionEdit.setText(description);
                        DatabaseReference refCat = FirebaseDatabase.getInstance().getReference("Categories");
                        refCat.child(s_categoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String category = ""+snapshot.child("category").getValue();
                                        binding.categorySelect.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String s_categoryId="",s_categoryTitle="";
    private  void CategoryDialog(){
        String[] categoryList = new String[categoryTitleArray.size()];
        for(int i=0;i<categoryTitleArray.size();i++){
            categoryList[i] = categoryTitleArray.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Category")
                .setItems(categoryList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        s_categoryId = categoryIdArray.get(i);
                        s_categoryTitle = categoryTitleArray.get(i);
                        binding.categorySelect.setText(s_categoryTitle);
                    }
                })
                .show();
    }
    private void loadCategories() {
        categoryIdArray = new ArrayList<>();
        categoryTitleArray = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryIdArray.clear();
                categoryTitleArray.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    String id = ""+ds.child("id").getValue();
                    String category = ""+ds.child("category").getValue();
                    categoryIdArray.add(id);
                    categoryTitleArray.add(category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}