package bonnier.hvadsynes.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import java.util.ArrayList;
import bonnier.hvadsynes.R;
import bonnier.hvadsynes.models.DrawerItemModel;

/**
 * Created by sessingo on 17/08/15.
 */
public class DrawerAdapter extends ArrayAdapter<DrawerItemModel> {

    Context context;

    ArrayList<DrawerItemModel> items;
    LayoutInflater inflater;

    public DrawerAdapter(Context context, LayoutInflater inflater) {
        super(context, R.layout.drawer_listview_item);
        this.context = context;
        this.items = new ArrayList<>();
        this.inflater = inflater;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = inflater.inflate(R.layout.drawer_listview_item, null, false);

        final DrawerItemModel item = this.getItem(position);

        Button button = (Button)v.findViewById(R.id.itemBtn);
        button.setText(item.title);

        if(item.getEventListener() != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    item.getEventListener().onClick(item, position);
                }
            });
        }

        return v;

    }

    public void add(DrawerItemModel item) {
        super.add(item);
    }
}
