package com.example.lithub.filters;

import android.widget.Filter;

import com.example.lithub.adapters.AdapterBookAdmin;
import com.example.lithub.adapters.AdapterCategory;
import com.example.lithub.models.ModelBook;
import com.example.lithub.models.ModelCategory;

import java.util.ArrayList;

public class FilterBookAdmin extends Filter {
    ArrayList<ModelBook> filterList;
    AdapterBookAdmin adapterBookAdmin;


    public FilterBookAdmin(ArrayList<ModelBook> filterList, AdapterBookAdmin adapterBookAdmin) {
        this.filterList = filterList;
        this.adapterBookAdmin = adapterBookAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        if(charSequence != null && charSequence.length() > 0){
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelBook> filteredResults = new ArrayList<>();
            for(int i=0;i<filterList.size();i++){
                if(filterList.get(i).getTitle().toUpperCase().contains(charSequence)){
                    filteredResults.add(filterList.get(i));

                }
            }
            results.count = filteredResults.size();
            results.values = filteredResults;

        }else{
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
       adapterBookAdmin.bookArrayList = (ArrayList<ModelBook>)filterResults.values;
        adapterBookAdmin.notifyDataSetChanged();
    }
}
