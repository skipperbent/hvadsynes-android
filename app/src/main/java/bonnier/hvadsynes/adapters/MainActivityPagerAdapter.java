package bonnier.hvadsynes.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.KeyEvent;

import java.util.ArrayList;

import bonnier.hvadsynes.R;
import bonnier.hvadsynes.actionbar.views.TabBarView;

/**
 * Created by sessingo on 12/09/15.
 */
public class MainActivityPagerAdapter extends FragmentPagerAdapter implements TabBarView.IconTabProvider {

    ArrayList<Fragment> views = new ArrayList<Fragment>();

    private int[] tab_icons = {
            R.drawable.ic_question_answer_2x,
            R.drawable.ic_notifications_2x
    };

    public MainActivityPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int pos) {
        return this.views.get(pos);
    }

    @Override
    public int getCount() {
        return this.views.size();
    }

    @Override
    public int getPageIconResId(int position) {
        return tab_icons[position];
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Tilfældige spørgsmål"; // TODO: translate
            case 1:
                return "Din aktivitet"; // TODO: translate
        }
        return null;
    }

    public void addFragment(Fragment fragment) {
        this.views.add(fragment);
    }
}
