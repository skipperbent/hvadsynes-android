package bonnier.hvadsynes.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import bonnier.android.models.QuestionModel;

/**
 * Created by sessingo on 14/08/15.
 */
public class QuestionLoader extends AsyncTaskLoader<QuestionModel> {
    private static final String TAG = "QuestionLoader";

    protected int questionId;
    QuestionModel question;

    public QuestionLoader(int questionId, Context context) {
        super(context);
        this.questionId = questionId;
    }

    @Override
    protected void onStopLoading() {

        cancelLoad();
    }

    @Override
    public QuestionModel loadInBackground() {
        try {
            question = QuestionModel.getById(questionId);
            return question;
        } catch(Exception e) {
            Log.d(TAG, e.getMessage(), e);
        }

        return null;
    }

    @Override
    public void deliverResult(QuestionModel data) {
        question = data; // Caching
        super.deliverResult(data);
    }

    @Override
    public void onCanceled(QuestionModel data) {
        this.question = null;
        super.onCanceled(data);
    }
}
