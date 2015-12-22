package bonnier.hvadsynes;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import bonnier.android.models.QuestionModel;
import bonnier.hvadsynes.fragments.QuestionFragment;

public class ViewQuestionActivity extends AppCompatActivity {

    public static String ARG_QUESTION = "question";
    private QuestionModel question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_question);

        // Show back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        question = (QuestionModel)getIntent().getExtras().getSerializable(ARG_QUESTION);

        QuestionFragment questionFragment = QuestionFragment.newInstance(question);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.messagesContainer, questionFragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
