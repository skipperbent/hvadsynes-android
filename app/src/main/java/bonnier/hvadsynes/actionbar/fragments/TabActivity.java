package bonnier.hvadsynes.actionbar.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import bonnier.hvadsynes.R;

public class TabActivity extends Fragment {

    private String TAG = "fragment_tab_activity";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_activity, container, false);

        ListView list = (ListView)view.findViewById(R.id.questionsListView);

        ArrayList<String> test = new ArrayList<String>();
        test.add("Spørgsmål 1");
        test.add("Spørgsmål 2");
        test.add("Spørgsmål 3");
        test.add("Spørgsmål 4");

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.fragment_activity_listview_item, R.id.Itemname, test);
        list.setAdapter(adapter);

        Log.i(TAG, "fragment_tab_activity: onCreateView");

        return view;
    }

}
