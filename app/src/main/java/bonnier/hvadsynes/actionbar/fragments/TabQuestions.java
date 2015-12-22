package bonnier.hvadsynes.actionbar.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONObject;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;

import bonnier.android.FragmentHelper;
import bonnier.android.Registry;
import bonnier.android.models.AnswerModel;
import bonnier.android.models.QuestionModel;
import bonnier.hvadsynes.R;
import bonnier.hvadsynes.MessageToolbarPopup;
import bonnier.hvadsynes.Settings;
import bonnier.hvadsynes.adapters.MessagesAdapter;
import bonnier.hvadsynes.fragments.QuestionFragment;
import bonnier.hvadsynes.models.MessageItemModel;

/**
 * Created by sessingo on 17/08/15.
 */
public class TabQuestions extends Fragment {

    public interface SaveAnswerEventHandler {
        void onSave(JSONObject result, AnswerModel answer);
    }

    private static class PreloadQuestionsHandler extends Handler {
        private MyAdapter adapter;

        public PreloadQuestionsHandler(MyAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public void handleMessage(Message msg) {
            ArrayList<QuestionModel> questions = Registry.GetInstance().get("questions");
            questions.addAll((ArrayList<QuestionModel>)msg.obj);
            Registry.GetInstance().set("questions", questions);
            this.adapter.notifyDataSetChanged();
        }
    }

    private static class SaveAnswerHandler extends Handler {
        private WeakReference<AnswerModel> answer;
        private WeakReference<SaveAnswerEventHandler> callback;

        public SaveAnswerHandler(AnswerModel answer, SaveAnswerEventHandler callback) {
            this.answer = new WeakReference<>(answer);
            this.callback = new WeakReference<>(callback);
        }

        @Override
        public void handleMessage(Message msg) {
            if(this.callback.get() != null) {
                this.callback.get().onSave((JSONObject) msg.obj, this.answer.get());
            }
        }
    }

    private SaveAnswerHandler saveAnswerHandler;
    private PreloadQuestionsHandler preloadQuestionsHandler;

    private static String TAG = TabQuestions.class.getName();

    private int preloadOffset = 3;

    MyAdapter mAdapter;
    ViewPager mPager;
    View tabQuestionsView;

    protected void saveAnswer() {
        Log.d(getClass().getName(), "Saving message");

        ArrayList<QuestionModel> questions = Registry.GetInstance().get("questions");
        QuestionModel question = questions.get(mPager.getCurrentItem());

        Settings settings = Settings.getInstance(getActivity().getApplicationContext());

        final EditText answerEditText = (EditText) tabQuestionsView.findViewById(R.id.answerText);

        String answerComment = (answerEditText.getText().toString());

        final AnswerModel answer = new AnswerModel();
        answer.setQuestionId(question.getId());
        answer.setComment(answerComment);
        answer.setName(settings.getName());
        answer.setEmail(settings.getEmail());
        answer.setAge(settings.getAge());
        answer.setGender(settings.getGender());

        saveAnswerHandler = new SaveAnswerHandler(answer, new SaveAnswerEventHandler() {
            @Override
            public void onSave(JSONObject result, AnswerModel answer) {
                if (!result.isNull("error")) {
                    try {
                        Toast.makeText(getActivity().getApplicationContext(), "Fejl: " + result.getString("error"), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.d(getClass().getName(), "Error saving answer", e);
                    }
                } else {

                    MessageItemModel messageItem = new MessageItemModel();
                    messageItem.message = answer.getComment();
                    messageItem.id = answer.getId();
                    messageItem.gender = answer.getGender();
                    messageItem.name = answer.getName();
                    messageItem.email = answer.getEmail();
                    messageItem.answer = true;
                    messageItem.date = new Date();
                    messageItem.age = answer.getAge();

                    // TODO: add nice animation

                    String tag = FragmentHelper.makeFragmentName(mPager.getId(), mPager.getCurrentItem());

                    QuestionFragment fragment = (QuestionFragment) getChildFragmentManager().findFragmentByTag(tag);
                    ListView messages = (ListView) fragment.getView().findViewById(R.id.messages);

                    MessagesAdapter messagesAdapter = (MessagesAdapter) ((HeaderViewListAdapter) messages.getAdapter()).getWrappedAdapter();

                    messagesAdapter.add(messageItem);
                    messagesAdapter.notifyDataSetChanged();

                    messages.setSelection(messagesAdapter.getCount() - 1);
                    answerEditText.setText("");

                    Log.d(getClass().getName(), "Answer saved");
                }
            }
        });

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    answer.save();
                    saveAnswerHandler.sendMessage(Message.obtain(saveAnswerHandler, 0, answer));
                }catch(Exception e) {
                    Log.d(getClass().getName(), "Failed to save answer", e);
                }
            }
        });

        t.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater layoutInflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {

        mAdapter = new MyAdapter(getChildFragmentManager());

        tabQuestionsView = layoutInflater.inflate(R.layout.fragment_tab_questions, container, false);
        mPager = (ViewPager)tabQuestionsView.findViewById(R.id.messagesPager);
        mPager.setAdapter(mAdapter);

        tabQuestionsView.findViewById(R.id.saveAnswerBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAnswer();
            }
        });

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mPager.setScrollY(0);

                // Hide popup
                MessageToolbarPopup.GetInstance().hidePopup();
            }

            @Override
            public void onPageSelected(int position) {

                Log.d(getClass().getName(), "Page selected " + position);

                // preload next questions
                if (position + preloadOffset >= mAdapter.getCount()) {
                    preloadQuestions();
                }

                mPager.setScrollY(0);
                mPager.setScrollX(0);
                mPager.scrollTo(0, 0);
                mPager.scrollBy(0, 0);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mPager.setPageTransformer(false, new ViewPager.PageTransformer() {

            float minimumScale = 0.80f;
            float animationSpeed = 0.10f;

            @Override
            public void transformPage(View page, float position) {

                int pageWidth = page.getWidth();
                int pageHeight = page.getHeight();

                if (position <= 1) { // [-1,1]
                    // Modify the default slide transition to shrink the page as well
                    float pos = 1 - Math.abs(position);
                    float scaleFactor = Math.max(minimumScale, animationSpeed + (1 - animationSpeed) * pos);
                    float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                    float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                    if (position < 0) {
                        page.setTranslationX(horzMargin - vertMargin / 2);
                    } else {
                        page.setTranslationX(-horzMargin + vertMargin / 2);
                    }

                    // Scale the page down (between MIN_SCALE and 1)
                    page.setScaleX(scaleFactor);
                    page.setScaleY(scaleFactor);

                }
            }
        });

        return tabQuestionsView;
    }

    protected void preloadQuestions() {

        preloadQuestionsHandler = new PreloadQuestionsHandler(mAdapter);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<QuestionModel> newQuestions = QuestionModel.getRandom(8).getQuestions();
                    preloadQuestionsHandler.sendMessage(Message.obtain(preloadQuestionsHandler, 0, newQuestions));
                } catch(Exception e) {
                    Log.d(TAG, e.getMessage(), e);
                }
            }
        });

        thread.start();
    }

    public static class MyAdapter extends FragmentStatePagerAdapter {

        FragmentManager fragmentManager;

        //protected ArrayList<QuestionFragment> fragments = new ArrayList<>();

        public MyAdapter(FragmentManager fm) {
            super(fm);
            this.fragmentManager = fm;
        }

        @Override
        public Fragment getItem(int position) {
            /*if(fragments.size() > position) {
                return fragments.get(position);
            }*/
            ArrayList<QuestionModel> questions = Registry.GetInstance().get("questions");
            return QuestionFragment.newInstance(questions.get(position));
            //fragments.add(fragment);
            //return fragment;
        }

        @Override
        public int getCount() {
            return ((ArrayList<QuestionModel>)Registry.GetInstance().get("questions")).size();
        }
    }

}
