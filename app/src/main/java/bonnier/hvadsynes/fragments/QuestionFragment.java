package bonnier.hvadsynes.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import bonnier.android.MD5Util;
import bonnier.android.models.AnswerModel;
import bonnier.android.models.QuestionModel;
import bonnier.android.tasks.DownloadImageTask;
import bonnier.hvadsynes.MessageToolbarPopup;
import bonnier.hvadsynes.ReplyAnswerActivity;
import bonnier.hvadsynes.models.MessageItemListModel;
import bonnier.hvadsynes.models.MessageItemModel;
import bonnier.hvadsynes.R;
import bonnier.hvadsynes.adapters.MessagesAdapter;

/**
 * Created by sessingo on 17/08/15.
 */
public class QuestionFragment extends Fragment {

    public interface LoadQuestionEventHandler {
        void onLoadFinished(QuestionModel question);
    }

    private static class LoadQuestionHandler extends Handler {
        private LoadQuestionEventHandler event;

        public LoadQuestionHandler(LoadQuestionEventHandler event) {
            this.event = event;
        }

        @Override
        public void handleMessage(Message msg) {
            event.onLoadFinished((QuestionModel) msg.obj);
        }
    }

    private LoadQuestionHandler loadQuestionHandler;

    public static final String ARG_QUESTION = "question";
    public static final String ARG_QUESTION_ID = "questionId";
    public static final String ARG_MESSAGE_ITEMS = "messageItems";
    public static final String ARG_PAGE = "page";
    static final int UPDATE_UI = 1;
    private static final int ANSWERS_LIMIT = 15;

    protected RelativeLayout view;
    protected View footerView;
    protected LayoutInflater inflater;

    protected int questionId;
    protected QuestionModel question;
    protected ListView messagesListView;
    protected MessagesAdapter messagesAdapter;
    protected LinearLayout messagesLoader;
    protected int page;
    protected int scrollLastItem;

    protected LoadQuestionEventHandler loadQuestionEventHandler = new LoadQuestionEventHandler() {
        @Override
        public void onLoadFinished(QuestionModel q) {

            if (question == null) {
                question = q;
                setQuestionView(view, inflater);
            }

            boolean hasNext = q.getHasNext();

            if(hasNext) {
                showAnswersLoader();
            } else {
                hideAnswersLoader();
            }

            ArrayList<MessageItemModel> answers = new ArrayList<>();
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
                messageItem.date = answer.getCreatedDate();
                messageItem.age = answer.getAge();

                answers.add(messageItem);

                // Add replys
                answers.addAll(getReplys(answer));
            }

            // Update question if another page was loaded (used when rotating the screen)
            if(page > 0) {
                ArrayList<AnswerModel> oldAnswers = question.getAnswers();

                oldAnswers.addAll(q.getAnswers());

                question = q;
                question.setAnswers(oldAnswers);
            }

            messagesAdapter.addAll(answers);
            messagesAdapter.notifyDataSetChanged();

            if(hasNext) {
                setScrollEvent();
            }
        }
    };

    public static QuestionFragment newInstance(int questionId) {
        QuestionFragment fragment = new QuestionFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_QUESTION_ID, questionId);
        fragment.setArguments(args);
        return fragment;
    }

    public static QuestionFragment newInstance(QuestionModel question) {
        QuestionFragment fragment = new QuestionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_QUESTION, question);
        args.putSerializable(ARG_QUESTION_ID, question.getId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        // Save items when rotating screen
        MessageItemListModel messageItems = new MessageItemListModel(messagesAdapter.getItems());
        outState.putSerializable(ARG_MESSAGE_ITEMS, messageItems);
        outState.putSerializable(ARG_QUESTION, question);
        outState.putInt(ARG_PAGE, page);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        messagesAdapter = new MessagesAdapter(getActivity().getApplicationContext(), getActivity().getLayoutInflater());

        if (getArguments() != null) {
            question = (QuestionModel)getArguments().getSerializable(ARG_QUESTION);
            questionId = getArguments().getInt(ARG_QUESTION_ID);
        }

        if(savedInstanceState != null) {

            page = savedInstanceState.getInt(ARG_PAGE);
            question = (QuestionModel)savedInstanceState.getSerializable(ARG_QUESTION);
            MessageItemListModel items = (MessageItemListModel)savedInstanceState.getSerializable(ARG_MESSAGE_ITEMS);

            messagesAdapter.addAll(items.getItems());
            messagesAdapter.notifyDataSetChanged();

        }
    }

    protected ArrayList<MessageItemModel> getReplys(AnswerModel answer) {

        ArrayList<MessageItemModel> items = new ArrayList<>();

        for(AnswerModel reply : answer.getAnswers()) {
            MessageItemModel messageItem = new MessageItemModel();
            messageItem.message = reply.getComment();
            messageItem.id = reply.getId();
            messageItem.parentId = reply.getParentId();
            messageItem.gender = reply.getGender();
            messageItem.name = reply.getName();
            messageItem.reply = (reply.getParentId() > 0);
            messageItem.email = reply.getEmail();
            messageItem.answer = true;
            messageItem.date = reply.getCreatedDate();
            messageItem.age = reply.getAge();

            items.add(messageItem);

            items.addAll(getReplys(reply));
        }

        return items;
    }

    protected void setScrollEvent() {
        messagesListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                // Detect if we scrolled to the last item in the list
                final int lastItem = firstVisibleItem + visibleItemCount;
                if (totalItemCount > 0 && lastItem == totalItemCount) {
                    if (scrollLastItem != lastItem) { //to avoid multiple calls for last item
                        Log.d(getClass().getName(), "Scrolled to last item in message list");
                        scrollLastItem = lastItem;

                        // Autoload next answers
                        page += 1;
                        loadQuestion(loadQuestionEventHandler);
                    }
                }

            }
        });
    }

    protected void loadQuestion(final LoadQuestionEventHandler event) {

        loadQuestionHandler = new LoadQuestionHandler(event);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    QuestionModel question = QuestionModel.getById(questionId, ANSWERS_LIMIT, page * ANSWERS_LIMIT);
                    loadQuestionHandler.sendMessage(Message.obtain(loadQuestionHandler, UPDATE_UI, question));
                } catch (Exception e) {
                    Log.d(getClass().getName(), "Failed to load question", e);
                    // TOOO: implement UPDATE_ERROR
                }
            }
        });

        thread.start();
    }

    protected void showAnswersLoader() {
        if(messagesListView.getFooterViewsCount() == 0) {
            messagesListView.addFooterView(footerView, null, false);
            messagesListView.invalidate();
        }
    }

    protected void hideAnswersLoader() {
        if(messagesListView.getFooterViewsCount() > 0) {
            messagesListView.removeFooterView(footerView);
            messagesListView.invalidate();
        }
    }

    protected void setQuestionView(View view, final LayoutInflater inflater) {

        messagesLoader.setVisibility(View.GONE);

        messagesListView = (ListView)view.findViewById(R.id.messages);

        messagesListView.setDivider(null);

        // Add question (header)
        View headerView = inflater.inflate(R.layout.messages_question, null);

        // Add footer
        footerView = inflater.inflate(R.layout.messages_footer, null);

        showAnswersLoader();

        final MessageItemModel messageItem = new MessageItemModel();
        messageItem.message = question.getQuestion();
        messageItem.id = question.getId();
        messageItem.gender = question.getGender();
        messageItem.name = question.getName();
        messageItem.email = question.getEmail();
        messageItem.date = question.getCreatedDate();
        messageItem.url = question.getUrl();
        messageItem.age = question.getAge();

        int noPhoto = (messageItem.gender > 1) ? R.drawable.female : R.drawable.male;

        // Date
        TextView date = (TextView)headerView.findViewById(R.id.time);
        date.setText(new SimpleDateFormat("M MMM yyyy", Locale.getDefault()).format(question.getCreatedDate()));

        // Name
        TextView name = (TextView)headerView.findViewById(R.id.userInfo);
        if(messageItem.age > 0) {
            name.setText(String.format("%s, %s", messageItem.name, messageItem.age));
        } else {
            name.setText(messageItem.name);
        }

        // Picture
        ImageView profilePicture = (ImageView) headerView.findViewById(R.id.picture);

        profilePicture.setImageResource(noPhoto);

        // TODO: store images on device
        if (profilePicture != null && question.getEmail() != "") {
            String url = "http://www.gravatar.com/avatar/" + MD5Util.md5(question.getEmail()) + "?s=80&d=404";
            new DownloadImageTask(profilePicture, false).execute(url);
        }

        LinearLayout messageBackground = (LinearLayout)headerView.findViewById(R.id.messageBackground);

        final TextView message = (TextView)headerView.findViewById(R.id.question);
        int bgDrawable = (messageItem.gender > 1) ? R.drawable.bubble_red_left_states : R.drawable.bubble_blue_left_states;
        messageBackground.setBackground(ContextCompat.getDrawable(getActivity().getApplicationContext(), bgDrawable));

        messagesListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // We need this check as the viewpager might trigger onscroll
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    MessageToolbarPopup.GetInstance().hidePopup();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //MessageToolbarPopup.GetInstance().hidePopup();
            }
        });

        messageBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(getClass().getName(), "Clicked on question");

                MessageToolbarPopup popup = MessageToolbarPopup.GetInstance();

                if (popup.getPopup() != null && popup.getMessageItem().id == messageItem.id) {
                    popup.hidePopup();
                } else {
                    popup.setMessageItem(messageItem);
                    popup.showPopup(getActivity().getApplicationContext(), inflater, v);
                }

            }
        });

        message.setText(messageItem.message);

        messagesListView.addHeaderView(headerView, null, false);

        // Add adapter
        messagesListView.setAdapter(messagesAdapter);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        this.inflater = inflater;
        this.view = (RelativeLayout) inflater.inflate(R.layout.fragment_question, container, false);

        messagesLoader = (LinearLayout)view.findViewById(R.id.questionProgressContainer);

        if(question != null) {
            setQuestionView(view, inflater);
        }

        // Do not load answers if the screen orientation was changed
        if(savedInstanceState == null) {
            loadQuestion(loadQuestionEventHandler);
        } else {
            if(question.getHasNext()) {
                setScrollEvent();
                showAnswersLoader();
            } else {
                hideAnswersLoader();
            }
        }

        return this.view;
    }

    @Override
    public void onDestroy() {
        // Close the toolbar or the application will get an Exception
        MessageToolbarPopup.GetInstance().hidePopup();
        super.onDestroy();
    }
}
