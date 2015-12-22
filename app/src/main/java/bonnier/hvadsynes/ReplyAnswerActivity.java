package bonnier.hvadsynes;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import bonnier.hvadsynes.fragments.ReplyAnswerFragment;

public class ReplyAnswerActivity extends AppCompatActivity {

    public static final int REQUEST_UPDATE = 1;
    public static String ARG_ANSWER_ID = "answerId";
    private int answerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reply_answer);

        // Show back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        answerId = getIntent().getExtras().getInt(ARG_ANSWER_ID);

        ReplyAnswerFragment answerFragment = ReplyAnswerFragment.newInstance(answerId);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.replyAnswerFragmentContainer, answerFragment).commit();
    }

    @Override
    protected void onDestroy() {
        // TODO: check if new post was added
        if(true) {
            finishActivity(REQUEST_UPDATE);
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

}
