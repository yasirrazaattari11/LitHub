package com.example.lithub.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lithub.MyApplication;
import com.example.lithub.activities.BookDetailsActivity;
import com.example.lithub.databinding.RowPdfUserBinding;
import com.example.lithub.filters.FilterBookUser;
import com.example.lithub.models.ModelBook;
import com.github.barteksc.pdfviewer.PDFView;

import java.util.ArrayList;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.HolderUser>implements Filterable {
    private Context context;
    private RowPdfUserBinding binding;
    private FilterBookUser filterBookUser;
    public ArrayList<ModelBook> modelBookArrayList,filterList;

    public AdapterUser(Context context, ArrayList<ModelBook> modelBookArrayList) {
        this.context = context;
        this.modelBookArrayList = modelBookArrayList;
        this.filterList = modelBookArrayList;
    }



    @NonNull
    @Override
    public HolderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderUser(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderUser holder, int position) {
        ModelBook modelBook = modelBookArrayList.get(position);
        String title = modelBook.getTitle();
        String bookId = modelBook.getId();
        String description = modelBook.getDescription();
        String url = modelBook.getUrl();
        String c_id = modelBook.getCategoryId();
        long timeStamp = modelBook.getTimeStamp();
        String date = MyApplication.formatTimeStamp(timeStamp);
        holder.title.setText(title);
        holder.description.setText(description);
        holder.date.setText(date);
        MyApplication.loadBookFromUrlSinglePage(""+url,""+title,holder.pdfView,holder.progressBar);
        MyApplication.loadCateogory(""+c_id,holder.category);
        MyApplication.loadBookSize(""+url,""+title,holder.size);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, BookDetailsActivity.class);
                intent.putExtra("bookId",bookId);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return modelBookArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filterBookUser == null){
            filterBookUser = new FilterBookUser(filterList,this);
        }
        return filterBookUser;
    }

    class HolderUser extends RecyclerView.ViewHolder{
        TextView title,description,category,size,date;
        PDFView pdfView;
        ProgressBar progressBar;
        public HolderUser(@NonNull View itemView) {
            super(itemView);
            title = binding.titleBook;
            description = binding.description;
            category = binding.categoryBook;
            size = binding.sizeBook;
            date = binding.dateBook;
            pdfView = binding.pdfView;
            progressBar = binding.progressBar;
        }
    }
}
