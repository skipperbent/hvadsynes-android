package bonnier.hvadsynes.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by sessingo on 12/09/15.
 */
public class CustomFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context context;

    private Map<String, Fragment> fragments = new LinkedHashMap<>();

    public CustomFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    public void add(String tabName, Fragment fragment) {
        this.fragments.put(tabName, fragment);
    }

    @Override
    public Fragment getItem(int position) {
        return (new ArrayList<>(this.fragments.values())).get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return (new ArrayList<>(this.fragments.keySet())).get(position);
    }
}
