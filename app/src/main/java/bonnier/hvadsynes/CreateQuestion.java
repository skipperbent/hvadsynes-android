package bonnier.hvadsynes;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

import bonnier.android.FragmentHelper;
import bonnier.android.Registry;
import bonnier.android.models.AnswerModel;
import bonnier.android.models.CategoryModel;
import bonnier.android.models.QuestionModel;
import bonnier.hvadsynes.adapters.CategorySpinnerAdapter;
import bonnier.hvadsynes.adapters.MessagesAdapter;
import bonnier.hvadsynes.fragments.QuestionFragment;
import bonnier.hvadsynes.models.MessageItemModel;

public class CreateQuestion extends AppCompatActivity {

    public static String ARG_CATEGORY_ID = "categoryId";

    public static final int REQUEST_BACK = 1;

    public interface SaveQuestionEventHandler {
        void onSave(QuestionModel result);
    }

    private static class SaveQuestionHandler extends Handler {
        private WeakReference<QuestionModel> question;
        private WeakReference<SaveQuestionEventHandler> callback;

        public SaveQuestionHandler(QuestionModel question, SaveQuestionEventHandler callback) {
            this.question = new WeakReference<>(question);
            this.callback = new WeakReference<>(callback);
        }

        @Override
        public void handleMessage(Message msg) {
            if(this.callback.get() != null) {
                this.callback.get().onSave((QuestionModel) msg.obj);
            }
        }
    }

    private SaveQuestionHandler saveQuestionHandler;
    private int categoryId;
    private Settings settings;

    protected void saveQuestion(final int categoryId, final QuestionModel question) {
        Log.d(getClass().getName(), "Saving question");

        saveQuestionHandler = new SaveQuestionHandler(question, new SaveQuestionEventHandler() {
            @Override
            public void onSave(QuestionModel question) {
                Log.d(getClass().getName(), "Question saved");

                Intent questionActivity = new Intent(getApplicationContext(), ViewQuestionActivity.class);

                Bundle bundle = new Bundle();
                bundle.putSerializable(ViewQuestionActivity.ARG_QUESTION, question);

                questionActivity.putExtras(bundle);
                startActivity(questionActivity);

                // Close current activity
                finish();
            }
        });

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    question.save(categoryId);
                    saveQuestionHandler.sendMessage(Message.obtain(saveQuestionHandler, 0, question));
                }catch(Exception e) {
                    Log.d(getClass().getName(), "Failed to save question", e);
                }
            }
        });

        t.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Avoid back button
        this.getIntent().setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        super.onCreate(savedInstanceState);

        // Show back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if(this.getIntent().getExtras() != null) {
            this.categoryId = this.getIntent().getExtras().getInt(ARG_CATEGORY_ID);
        }

        setContentView(R.layout.activity_create_question);

        settings = Settings.getInstance(getApplicationContext());

        ArrayList<CategoryModel> categories = settings.getCategories();

        final Spinner categoriesSpinner = (Spinner)findViewById(R.id.categories);

        CategorySpinnerAdapter categoriesAdapter = new CategorySpinnerAdapter(getApplicationContext(), getLayoutInflater(), categories);
        categoriesSpinner.setAdapter(categoriesAdapter);

        if(this.categoryId > 0) {
            int activePosition = 0;
            for (CategoryModel category : categories) {
                if(category.getId() == this.categoryId) {
                    break;
                }
                activePosition++;
            }

            categoriesSpinner.setSelection(activePosition);
        }

        final EditText questionText = (EditText)findViewById(R.id.questionText);
        ImageButton saveQuestionButton = (ImageButton)findViewById(R.id.saveQuestionBtn);

        saveQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuestionModel question = new QuestionModel();
                question.setName(settings.getName());
                question.setAge(settings.getAge());
                question.setEmail(settings.getEmail());
                question.setGender(settings.getGender());
                question.setQuestion(questionText.getText().toString());

                saveQuestion(((CategoryModel) categoriesSpinner.getSelectedItem()).getId(), question);
            }
        });

    }

    @Override
    protected void onDestroy() {
        finishActivity(REQUEST_BACK);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(getClass().getName(), "finish activity");
        finish();
        return true;
    }

}
