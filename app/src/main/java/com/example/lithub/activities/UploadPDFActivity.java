package com.example.lithub.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.lithub.databinding.ActivityUploadPdfactivityBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class UploadPDFActivity extends AppCompatActivity {
    private ActivityUploadPdfactivityBinding binding;
    private FirebaseAuth firebaseAuth;
    private static final int PDF_PICk_CODE = 1000;
    private ProgressDialog progressDialog;
    private Uri pdfUri = null;
    private ArrayList<String> categoryTitleArrayList,categoryIdArrayList;
    private static  final String TAG = "ADD_PDF_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadPdfactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadCategories();
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        binding.attachPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfPickIntent();
            }
        });
        binding.categorySelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });
        binding.submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }
    private  String title = "",description = "";
    private void validateData() {
        title = binding.editBook.getText().toString().trim();
        description = binding.editDesc.getText().toString().trim();
        if(TextUtils.isEmpty(title)){
            Toast.makeText(UploadPDFActivity.this,"Enter title",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(UploadPDFActivity.this,"Enter description",Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(selectedCategoryTitle)) {
            Toast.makeText(UploadPDFActivity.this,"Select category",Toast.LENGTH_SHORT).show();
        } else if (pdfUri == null) {
            Toast.makeText(UploadPDFActivity.this,"Select book",Toast.LENGTH_SHORT).show();
        }else {
            uploadBook();
        }
    }

    private void uploadBook() {
        progressDialog.setMessage("Uploading book");
        progressDialog.show();
        long timeStamp = System.currentTimeMillis();
        String filePathAndName = "Books/"+ timeStamp;
        StorageReference reference = FirebaseStorage.getInstance().getReference(filePathAndName);
        reference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedPdfUrl = "" + uriTask.getResult();
                        uploadPdfToDb(uploadedPdfUrl,timeStamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(UploadPDFActivity.this,"Upload Failed "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadPdfToDb(String uploadedPdfUrl, long timeStamp) {
        progressDialog.setMessage("Uploading");
        String uid = firebaseAuth.getUid();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("uid",""+uid);
        hashMap.put("id",""+timeStamp);
        hashMap.put("title",""+title);
        hashMap.put("description",""+description);
        hashMap.put("categoryId",""+selectedCategoryId);
        hashMap.put("url",""+uploadedPdfUrl);
        hashMap.put("timeStamp",timeStamp);
        hashMap.put("viewsCount",0);
        hashMap.put("downloadsCount",0);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(""+timeStamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(UploadPDFActivity.this,"Uploaded successfully",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadPDFActivity.this,"Uploading Failed",Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void loadCategories() {
        Log.d(TAG,"Loading Categories");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    String categoryId = ""+ds.child("id").getValue();
                    String categoryTitle = ""+ds.child("category").getValue();
                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArrayList.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private String selectedCategoryId,selectedCategoryTitle;
    private void categoryPickDialog() {
        Log.d(TAG ,"Showing category");
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i< categoryTitleArrayList.size(); i++){
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedCategoryTitle = categoryTitleArrayList.get(i);
                        selectedCategoryId = categoryIdArrayList.get(i);
                        binding.categorySelect.setText(selectedCategoryTitle);
                    }
                })
                .show();
        Log.d(TAG,"Selected");
    }

    private void pdfPickIntent() {
        Log.d(TAG,"Upload Book");
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Book"),PDF_PICk_CODE );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if (requestCode == PDF_PICk_CODE){
                Log.d(TAG,"PDF Picked");
                pdfUri = data.getData();
                Log.d(TAG,"URI: "+pdfUri);
            }

        }else{
            Log.d(TAG,"Selection Failed");
            Toast.makeText(UploadPDFActivity.this,"Selection Failed",Toast.LENGTH_SHORT).show();
        }
    }
}