package bonnier.hvadsynes.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import bonnier.android.models.CategoryModel;
import bonnier.hvadsynes.R;

/**
 * Created by sessingo on 09/09/15.
 */
public class CategorySpinnerAdapter extends ArrayAdapter<CategoryModel> {

    private Context context;
    private LayoutInflater inflater;

    public CategorySpinnerAdapter(Context context, LayoutInflater inflater, ArrayList<CategoryModel> categories) {
        super(context, R.layout.spinner_category_item, R.id.categoryName);
        this.context = context;
        this.inflater = inflater;
        this.setDropDownViewResource(R.layout.spinner_category_dropdown_item);
        this.addAll(categories);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        CategoryModel category = this.getItem(position);

        View view = super.getDropDownView(position, convertView, parent);
        TextView categoryName = (TextView)view.findViewById(R.id.categoryName);

        categoryName.setText(category.getName());

        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        CategoryModel category = this.getItem(position);

        View view = inflater.inflate(R.layout.spinner_category_item, null, false);

        TextView categoryName = (TextView)view.findViewById(R.id.categoryName);
        categoryName.setText(category.getName());

        return view;
    }

}
