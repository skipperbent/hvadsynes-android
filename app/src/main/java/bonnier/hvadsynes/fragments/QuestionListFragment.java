package bonnier.hvadsynes.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import bonnier.android.MD5Util;
import bonnier.android.models.AnswerModel;
import bonnier.android.models.QuestionListModel;
import bonnier.android.models.QuestionModel;
import bonnier.android.tasks.DownloadImageTask;
import bonnier.hvadsynes.R;
import bonnier.hvadsynes.SetupActivity;
import bonnier.hvadsynes.ViewQuestionActivity;
import bonnier.hvadsynes.adapters.MessagesAdapter;
import bonnier.hvadsynes.adapters.QuestionsListAdapter;
import bonnier.hvadsynes.models.MessageItemListModel;
import bonnier.hvadsynes.models.MessageItemModel;

public class QuestionListFragment extends Fragment {

    public static String ARG_CATEGORY_ID = "categoryId";
    public static String ARG_ORDER = "order";
    public static String ARG_SORT = "sort";
    public static String ARG_QUESTIONS = "questions";
    public static String ARG_PAGE = "page";

    public static final String ARG_MESSAGE_ITEMS = "messageItems";

    private static final int QUESTION_LIMIT = 20;

    public static final int UPDATE_UI = 1;
    public static final int UPDATE_ERROR = 2;

    protected int categoryId;
    protected QuestionModel.Order order;
    protected QuestionModel.Sort sort;

    protected View view;
    protected View footerView;
    protected LayoutInflater inflater;
    protected ProgressBar progress;
    protected ListView questionListView;
    protected QuestionsListAdapter questionsListAdapter;
    protected int page;
    protected int scrollLastItem;

    protected QuestionListModel questions;

    public interface LoadQuestionsEventHandler {
        void onLoadFinished(QuestionListModel questions);
    }

    private static class LoadQuestionsHandler extends Handler {
        private LoadQuestionsEventHandler event;

        public LoadQuestionsHandler(LoadQuestionsEventHandler event) {
            this.event = event;
        }

        @Override
        public void handleMessage(Message msg) {
            event.onLoadFinished((QuestionListModel) msg.obj);
        }
    }

    private LoadQuestionsHandler loadQuestionsHandler = new LoadQuestionsHandler(new LoadQuestionsEventHandler() {
        @Override
        public void onLoadFinished(QuestionListModel q) {

            hideLoader();

            boolean hasNext = q.getHasNext();

            if(hasNext) {
                showFetchNextLoader();
            } else {
                hideFetchNextLoader();
            }

            ArrayList<MessageItemModel> items = new ArrayList<>();

            for (QuestionModel questionModel : q.getQuestions()) {

                MessageItemModel messageItem = new MessageItemModel();

                String message = questionModel.getQuestion();

                message = (message.length() > 100) ? message.substring(0, 100).trim() + " ..." : message;

                messageItem.message = message;
                messageItem.id = questionModel.getId();
                messageItem.gender = questionModel.getGender();
                messageItem.name = questionModel.getName();
                messageItem.email = questionModel.getEmail();
                messageItem.date = questionModel.getCreatedDate();
                messageItem.age = questionModel.getAge();
                messageItem.url = questionModel.getUrl();

                items.add(messageItem);
            }

            if(questions == null) {
                questions = q;
            }

            // Update question if another page was loaded (used when rotating the screen)
            if(page > 0) {
                questions.getQuestions().addAll(q.getQuestions());
            }


            questionsListAdapter.addAll(items);
            questionsListAdapter.notifyDataSetChanged();


            if(hasNext) {
                setScrollEvent();
            }
        }
    });

    protected void setScrollEvent() {
        questionListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                Log.d(getClass().getName(), "Scrolling");
                // Detect if we scrolled to the last item in the list
                final int lastItem = firstVisibleItem + visibleItemCount;
                if (totalItemCount > 0 && lastItem == totalItemCount) {
                    if (scrollLastItem != lastItem) { //to avoid multiple calls for last item
                        Log.d(getClass().getName(), "Scrolled to last question in list");
                        scrollLastItem = lastItem;

                        // Autoload next answers
                        page += 1;
                        loadQuestions();
                    }
                }

            }
        });
    }

    public static QuestionListFragment newInstance(int categoryId, QuestionModel.Order order, QuestionModel.Sort sort) {
        QuestionListFragment fragment = new QuestionListFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_CATEGORY_ID, categoryId);
        bundle.putString(ARG_ORDER, order.toString());
        bundle.putString(ARG_SORT, sort.toString());

        fragment.setArguments(bundle);

        return fragment;
    }

    public static QuestionListFragment newInstance(QuestionModel.Order order, QuestionModel.Sort sort) {
        QuestionListFragment fragment = new QuestionListFragment();

        Bundle bundle = new Bundle();
        bundle.putString(ARG_ORDER, order.toString());
        bundle.putString(ARG_SORT, sort.toString());

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        // Save items when rotating screen
        MessageItemListModel messageItems = new MessageItemListModel(questionsListAdapter.getItems());
        outState.putSerializable(ARG_QUESTIONS, questions);
        outState.putSerializable(ARG_MESSAGE_ITEMS, messageItems);
        outState.putString(ARG_ORDER, this.order.toString());
        outState.putString(ARG_SORT, this.sort.toString());
        outState.putInt(ARG_PAGE, page);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        questionsListAdapter = new QuestionsListAdapter(getActivity().getApplicationContext(), getActivity().getLayoutInflater());

        if (getArguments() != null) {
            if(getArguments().getSerializable(ARG_CATEGORY_ID) != null) {
                this.categoryId = getArguments().getInt(ARG_CATEGORY_ID);
            }

            this.order =  QuestionModel.Order.valueOf(getArguments().getString(ARG_ORDER));
            this.sort = QuestionModel.Sort.valueOf(getArguments().getString(ARG_SORT));
        }

        if(savedInstanceState != null) {

            page = savedInstanceState.getInt(ARG_PAGE);
            questions = (QuestionListModel)savedInstanceState.getSerializable(ARG_QUESTIONS);
            MessageItemListModel items = (MessageItemListModel)savedInstanceState.getSerializable(ARG_MESSAGE_ITEMS);

            // Remember sort
            this.order =  QuestionModel.Order.valueOf(savedInstanceState.getString(ARG_ORDER));
            this.sort = QuestionModel.Sort.valueOf(savedInstanceState.getString(ARG_SORT));

            questionsListAdapter.addAll(items.getItems());
            questionsListAdapter.notifyDataSetChanged();
        }
    }

    protected void showLoader() {
        this.progress.setVisibility(View.VISIBLE);
    }

    protected void hideLoader() {
        this.progress.setVisibility(View.GONE);
    }

    protected void showFetchNextLoader() {
        if(questionListView.getFooterViewsCount() == 0) {
            questionListView.addFooterView(footerView, null, false);
            questionListView.invalidate();
        }
    }

    protected void hideFetchNextLoader() {
        if(questionListView.getFooterViewsCount() > 0) {
            questionListView.removeFooterView(footerView);
            questionListView.invalidate();
        }
    }

    protected void loadQuestions() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    QuestionListModel questions = QuestionModel.get(categoryId, order, sort, QUESTION_LIMIT, page * QUESTION_LIMIT);

                    loadQuestionsHandler.sendMessage(Message.obtain(loadQuestionsHandler, UPDATE_UI, questions));
                } catch (Exception e) {
                    Log.d(getClass().getName(), "Failed to load question", e);

                    // TOOO: implement UPDATE_ERROR
                }
            }
        });

        thread.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(getClass().getName(), String.format("Question list - categoryId: %s, order: %s, sort: %s",this.categoryId, this.order, this.sort));

        this.inflater = inflater;
        this.view = inflater.inflate(R.layout.fragment_question_list, container, false);

        this.progress = (ProgressBar)this.view.findViewById(R.id.questionsProgress);
        this.questionListView = (ListView)this.view.findViewById(R.id.questionsListView);

        this.questionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                QuestionModel question = questions.getQuestions().get(position);

                Intent viewQuestionActivity = new Intent(getActivity().getApplicationContext(), ViewQuestionActivity.class);

                Bundle bundle = new Bundle();
                bundle.putSerializable(ViewQuestionActivity.ARG_QUESTION, question);

                viewQuestionActivity.putExtras(bundle);

                startActivity(viewQuestionActivity);

            }
        });

        this.questionListView.setAdapter(this.questionsListAdapter);

        // Add footer
        footerView = inflater.inflate(R.layout.question_list_footer, null);

        // Only run if the state doesn't contain any items
        if(savedInstanceState == null) {
            // Load questions
            showLoader();
            loadQuestions();
        } else {

            if(questions.getHasNext()) {
                setScrollEvent();
                showFetchNextLoader();
            }

        }

        return this.view;
    }

}
