package com.example.lithub;

import static com.example.lithub.Constants.MAX_BYTES_PDF;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.lithub.adapters.AdapterBookAdmin;
import com.example.lithub.models.ModelBook;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }
    public static final String formatTimeStamp(long timeStamp){
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timeStamp);
        String date = DateFormat.format("dd/MM/yyyy",calendar).toString();
        return date;
    }
  public static void deleteBook(String bookId, Context context,String bookUrl,String bookTitle) {
      ProgressDialog progressDialog = new ProgressDialog(context);
      progressDialog.setMessage("Please wait");
        progressDialog.setMessage("Deleting "+bookTitle);
        progressDialog.show();
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        reference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
                        ref.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressDialog.dismiss();
                                        Toast.makeText(context,"Deleted Successfully",Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(context,"Failed to delete",Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(context,"Failed to delete",Toast.LENGTH_SHORT).show();
                    }
                });

    }
    public static void loadBookSize(String pdfUrl, String pdfTitle, TextView size) {
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes = storageMetadata.getSizeBytes();
                        double kb = bytes/1024;
                        double mb = kb/1024;
                        if(mb >=1){
                           size.setText(String.format("%.2f",mb)+" MB");
                        }
                        else if(kb >=1){
                            size.setText(String.format("%.2f",kb)+" KB");
                        }
                        else {
                            size.setText(String.format("%.2f",bytes)+" Bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }
   public static void loadBookFromUrlSinglePage(String pdfUrl, String pdfTitle, PDFView pdfView, ProgressBar progressBar) {
        String TAG = "BOOK_IMAGE";
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG,"OnError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                       progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG,"OnPageError: "+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Log.d(TAG,"OnFailure: Failed to got the file");
                    }
                });
    }
    public static void loadCateogory(String id,TextView categoryTV) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category = ""+snapshot.child("category").getValue();
                        categoryTV.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public static void incrementBookViews(String bookId){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
        ref.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String views = ""+snapshot.child("viewsCount").getValue();
                        if(views.equals("")||views.equals("null")){
                            views = "0";
                        }
                        long newViews = Long.parseLong(views)+1;
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("viewsCount",newViews);
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
                        ref.child(bookId)
                                .updateChildren(hashMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    public static void downloadBook(Context context,String bookId,String bookTitle,String bookUrl){
        String nameWithExtension = bookTitle + ".pdf";
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Downloading");
        progressDialog.show();
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        reference.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        saveDownloaded(context,progressDialog,bytes,nameWithExtension,bookId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Failed to download", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static void saveDownloaded(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension, String bookId) {
        try{
            File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadFolder.mkdirs();
            String filePath = downloadFolder.getPath() + "/" + nameWithExtension;
            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();
            Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            incrementBookDownloads(bookId);
        }catch (Exception e){
            Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    private static void incrementBookDownloads(String bookId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Books");
        reference.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String downloads = ""+snapshot.child("downloadsCount").getValue();
                        if (downloads.equals("")||downloads.equals("null")){
                            downloads ="0";
                        }
                        long newDownloads = Long.parseLong(downloads)+1;
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("downloadsCount",newDownloads);
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Books");
                        reference1.child(bookId).updateChildren(hashMap);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
