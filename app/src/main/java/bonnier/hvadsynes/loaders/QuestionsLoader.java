package bonnier.hvadsynes.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.ArrayList;

import bonnier.android.models.QuestionModel;

/**
 * Created by sessingo on 14/08/15.
 */
public class QuestionsLoader extends AsyncTaskLoader<ArrayList<QuestionModel>> {
    private static final String TAG = "QuestionLoader";

    ArrayList<QuestionModel> questions;

    public QuestionsLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public ArrayList<QuestionModel> loadInBackground() {
        questions = new ArrayList<QuestionModel>();
        try {
            questions = QuestionModel.getRandom(8).getQuestions();
        } catch(Exception e) {
            Log.d(TAG, e.getMessage(), e);
        }

        return questions;
    }

    @Override
    public void deliverResult(ArrayList<QuestionModel> data) {
        questions = data; // Caching
        super.deliverResult(data);
    }

    @Override
    public void onCanceled(ArrayList<QuestionModel> data) {
        this.questions = null;
        super.onCanceled(data);
    }
}
