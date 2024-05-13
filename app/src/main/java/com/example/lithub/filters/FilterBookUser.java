package com.example.lithub.filters;

import android.widget.Filter;

import com.example.lithub.adapters.AdapterUser;
import com.example.lithub.models.ModelBook;

import java.util.ArrayList;
import java.util.Locale;

public class FilterBookUser extends Filter {
    ArrayList<ModelBook> filterList;
    AdapterUser adapterUser;

    public FilterBookUser(ArrayList<ModelBook> filterList, AdapterUser adapterUser) {
        this.filterList = filterList;
        this.adapterUser = adapterUser;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults filterResults = new FilterResults();
        if(charSequence!=null||charSequence.length()>0){
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<ModelBook> filteredResults = new ArrayList<>();
            for(int i=0;i<filterList.size();i++){
                if(filterList.get(i).getTitle().toUpperCase().contains(charSequence)){
                    filteredResults.add(filterList.get(i));
                }
            }
            filterResults.count = filteredResults.size();
            filterResults.values = filteredResults;
        }else {
            filterResults.count = filterList.size();
            filterResults.values = filterList;
        }
        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        adapterUser.modelBookArrayList = (ArrayList<ModelBook>)filterResults.values;
        adapterUser.notifyDataSetChanged();
    }
}
