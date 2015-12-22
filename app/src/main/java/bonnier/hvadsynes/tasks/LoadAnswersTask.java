package bonnier.hvadsynes.tasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;

import bonnier.android.models.AnswerModel;
import bonnier.android.models.QuestionModel;
import bonnier.hvadsynes.fragments.QuestionFragment;
import bonnier.hvadsynes.models.MessageItemModel;

/**
 * Created by sessingo on 07/09/15.
 */
public class LoadAnswersTask extends AsyncTask<String, Void, ArrayList<MessageItemModel>> {

    public interface LoadAnswersEventHandler {
        void onLoadFinished(ArrayList<MessageItemModel> answers);
    }

    protected LoadAnswersEventHandler event;

    public LoadAnswersTask(LoadAnswersEventHandler event) {
        this.event = event;
    }

    @Override
    protected ArrayList<MessageItemModel> doInBackground(String... params) {
        try {
            QuestionModel q = QuestionModel.getById(Integer.parseInt(params[0]));

            ArrayList<MessageItemModel> items = new ArrayList<>();

            for (AnswerModel answer : q.getAnswers()) {

                MessageItemModel messageItem = new MessageItemModel();
                messageItem.message = answer.getComment();
                messageItem.id = answer.getId();
                messageItem.parentId = answer.getParentId();
                messageItem.gender = answer.getGender();
                messageItem.name = answer.getName();
                messageItem.reply = (answer.getParentId() > 0);
                messageItem.email = answer.getEmail();
                messageItem.answer = true;
                messageItem.age = answer.getAge();

                items.add(messageItem);
            }

            return items;
        } catch (Exception e) {
            Log.d(getClass().getName(), "Failed to load question", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<MessageItemModel> messageItemModels) {
        this.event.onLoadFinished(messageItemModels);
    }
}
