package com.example.lithub.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lithub.activities.BookDetailsActivity;
import com.example.lithub.activities.BookEditActivity;
import com.example.lithub.MyApplication;
import com.example.lithub.databinding.RowPdfAdminBinding;
import com.example.lithub.filters.FilterBookAdmin;
import com.example.lithub.models.ModelBook;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterBookAdmin extends RecyclerView.Adapter<AdapterBookAdmin.HolderBookAdmin> implements Filterable {
    private Context context;
    public ArrayList<ModelBook> bookArrayList,filterList;
    private RowPdfAdminBinding binding;
    private FilterBookAdmin filter;
    private static final String TAG = "PDF_ADAPTER_TAG";
    private ProgressDialog progressDialog;

    public AdapterBookAdmin(Context context, ArrayList<ModelBook> bookArrayList) {
        this.context = context;
        this.bookArrayList = bookArrayList;
        this.filterList = bookArrayList;
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderBookAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false);

        return new HolderBookAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBookAdmin holder, int position) {
        ModelBook modelBook = bookArrayList.get(position);
        String title = modelBook.getTitle();
        String pdfUrl = modelBook.getUrl();
        String pdfId = modelBook.getId();
        String categoryId = modelBook.getCategoryId();
        String description = modelBook.getDescription();
        long timeStamp = modelBook.getTimeStamp();
        String date = MyApplication.formatTimeStamp(timeStamp);
        holder.bookTitle.setText(title);
        holder.description.setText(description);
        holder.date.setText(date);
        MyApplication.loadCateogory(""+categoryId,holder.category);
        MyApplication.loadBookFromUrlSinglePage(""+pdfUrl,""+title,holder.pdfView,holder.progressBar);
        MyApplication.loadBookSize(""+pdfUrl,""+title,holder.size);
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsDialog(modelBook,holder);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, BookDetailsActivity.class);
                intent.putExtra("bookId",pdfId);
                context.startActivity(intent);
            }
        });
    }

    private void moreOptionsDialog(ModelBook modelBook, HolderBookAdmin holder) {
        String bookId = modelBook.getId();
        String bookUrl = modelBook.getUrl();
        String bookTitle = modelBook.getTitle();
        String[] options = {"Edit","Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0){
                            Intent intent = new Intent(context, BookEditActivity.class);
                            intent.putExtra("bookId",bookId);
                            context.startActivity(intent);

                        } else if (i == 1) {

                        }else {
                            MyApplication.deleteBook(""+bookId,context,""+bookUrl,""+bookTitle);
                        }
                    }
                }).show();
    }



    @Override
    public int getItemCount() {
        return bookArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new FilterBookAdmin(filterList,this);
        }
        return filter;
    }

    class HolderBookAdmin extends RecyclerView.ViewHolder{
        PDFView pdfView;
        ProgressBar progressBar;
        TextView bookTitle,description,category,size,date;
        ImageButton more;
        public HolderBookAdmin(@NonNull View itemView) {
            super(itemView);
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
            bookTitle = binding.titleBook;
            description = binding.description;
            category = binding.categoryBook;
            size = binding.sizeBook;
            date = binding.dateBook;
            more = binding.moreBtn;
        }
    }
}
