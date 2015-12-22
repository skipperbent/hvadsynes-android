package bonnier.hvadsynes;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.io.Serializable;

import bonnier.android.models.CategoryModel;
import bonnier.android.models.QuestionModel;
import bonnier.hvadsynes.adapters.CustomFragmentPagerAdapter;
import bonnier.hvadsynes.fragments.QuestionListFragment;

public class CategoryActivity extends AppCompatActivity {

    public static String ARG_CATEGORY = "category";



    protected CategoryModel category;
    private CustomFragmentPagerAdapter fragmentPagerAdapter;
    private Settings settings;

    private ViewPager viewPager;

    protected void load() {
        viewPager.setAdapter(null);
        this.fragmentPagerAdapter = new CustomFragmentPagerAdapter(getSupportFragmentManager(), this);

        // Add tabs
        // TODO: add to strings.xml
        this.fragmentPagerAdapter.add("Nyeste", QuestionListFragment.newInstance(this.category.getId(), QuestionModel.Order.created_date, QuestionModel.Sort.desc));
        this.fragmentPagerAdapter.add("Populære", QuestionListFragment.newInstance(this.category.getId(), QuestionModel.Order.comment_count, QuestionModel.Sort.desc));

        viewPager.setAdapter(this.fragmentPagerAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // Show back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Serializable categorySerialized = getIntent().getExtras().getSerializable(ARG_CATEGORY);

        // If no question is provided we close the window
        if(categorySerialized == null) {
            finish();
            return;
        }

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.categoryViewPager);

        settings = Settings.getInstance(getApplicationContext());

        this.category = (CategoryModel)categorySerialized;

        setTitle(this.category.getName());

        load();

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.categoryTabs);
        tabLayout.setupWithViewPager(viewPager);

        // TODO: add to style.xml
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor(((settings.getGender() > 0) ? "#009ee3" : "#c7265a" )));
        tabLayout.setSmoothScrollingEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    public void createQuestion(View view) {
        Intent setupActivity = new Intent(getApplicationContext(), CreateQuestion.class);

        Bundle bundle = new Bundle();
        bundle.putInt(CreateQuestion.ARG_CATEGORY_ID, this.category.getId());

        setupActivity.putExtras(bundle);

        startActivityForResult(setupActivity, CreateQuestion.REQUEST_BACK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Refresh the view
        if(requestCode == CreateQuestion.REQUEST_BACK) {
            load();
            Log.d(getClass().getName(), "Request refresh: reloading categories");
        }
    }
}
