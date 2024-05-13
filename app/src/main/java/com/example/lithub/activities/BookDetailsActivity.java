package com.example.lithub.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.lithub.MyApplication;
import com.example.lithub.databinding.ActivityBookDetailsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BookDetailsActivity extends AppCompatActivity {
    private ActivityBookDetailsBinding binding;
    String bookId,bookTitle,bookUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        binding.downloadbtn.setVisibility(View.GONE);
        loadBookDetails();
        MyApplication.incrementBookViews(bookId);
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        binding.readbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(BookDetailsActivity.this, ReadBookActivity.class);
                intent1.putExtra("bookId",bookId);
                startActivity(intent1);
            }
        });
        binding.downloadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.downloadbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (ContextCompat.checkSelfPermission(BookDetailsActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            // Permission is already granted, proceed with the download
                            MyApplication.downloadBook(BookDetailsActivity.this, "" + bookId, "" + bookTitle, "" + bookUrl);
                        } else {
                            // Permission is not granted, request it
                            if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                // Explain to the user why the permission is needed
                                Toast.makeText(BookDetailsActivity.this, "Permission required for downloading books", Toast.LENGTH_SHORT).show();
                            }
                            requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        }
                    }
                });

            }
        });
    }
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),isGranted ->{
                if(isGranted){
                    MyApplication.downloadBook(this,""+bookId,""+bookTitle,""+bookUrl);
                }
                else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private void loadBookDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookTitle = ""+snapshot.child("title").getValue();
                        String description = ""+snapshot.child("description").getValue();
                        String categoryId = ""+snapshot.child("categoryId").getValue();
                        String viewsCount = ""+snapshot.child("viewsCount").getValue();
                        String downloadsCount = ""+snapshot.child("downloadsCount").getValue();
                        bookUrl = ""+snapshot.child("url").getValue();
                        String timeStamp = ""+snapshot.child("timeStamp").getValue();
                        binding.downloadbtn.setVisibility(View.VISIBLE);
                        String date = MyApplication.formatTimeStamp(Long.parseLong(timeStamp));
                        MyApplication.loadCateogory(""+categoryId,binding.categoryText);
                        MyApplication.loadBookFromUrlSinglePage(""+bookUrl,""+bookTitle,binding.pdfView,binding.progressBar);
                        MyApplication.loadBookSize(""+bookUrl,""+bookTitle,binding.sizeText);
                        binding.titleBook.setText(bookTitle);
                        binding.description.setText(description);
                        binding.viewsText.setText(viewsCount.replace("null","N/A"));
                        binding.downloadsText.setText(downloadsCount.replace("null","N/A"));
                        binding.dateText.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}