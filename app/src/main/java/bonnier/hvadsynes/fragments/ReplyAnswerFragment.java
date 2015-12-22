package bonnier.hvadsynes.fragments;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import java.util.ArrayList;
import bonnier.android.models.AnswerModel;
import bonnier.hvadsynes.R;
import bonnier.hvadsynes.adapters.MessagesAdapter;
import bonnier.hvadsynes.models.MessageItemListModel;
import bonnier.hvadsynes.models.MessageItemModel;

public class ReplyAnswerFragment extends Fragment {

    public interface LoadAnswerEventHandler {
        void onLoadFinished(AnswerModel answer);
    }

    private static class LoadAnswerHandler extends Handler {
        private LoadAnswerEventHandler event;

        public LoadAnswerHandler(LoadAnswerEventHandler event) {
            this.event = event;
        }

        @Override
        public void handleMessage(Message msg) {
            event.onLoadFinished((AnswerModel) msg.obj);
        }
    }

    private LoadAnswerHandler loadAnswerHandler;

    public static final String ARG_ANSWER = "answer";
    public static final String ARG_ANSWER_ID = "answerId";
    public static final String ARG_MESSAGE_ITEMS = "messageItems";
    public static final String ARG_PAGE = "page";
    static final int UPDATE_UI = 1;
    private static final int ANSWERS_LIMIT = 15;

    protected RelativeLayout view;
    protected View footerView;
    protected LayoutInflater inflater;

    protected int answerId;
    protected AnswerModel answer;
    protected ListView messagesListView;
    protected MessagesAdapter messagesAdapter;
    protected LinearLayout messagesLoader;
    protected int page;
    protected int scrollLastItem;

    protected LoadAnswerEventHandler loadAnswerEventHandler = new LoadAnswerEventHandler() {
        @Override
        public void onLoadFinished(AnswerModel a) {

            if (answer == null) {
                answer = a;

                MessageItemModel messageItem = new MessageItemModel();
                messageItem.message = answer.getComment();
                messageItem.id = answer.getId();
                messageItem.gender = answer.getGender();
                messageItem.name = answer.getName();
                messageItem.email = answer.getEmail();
                messageItem.date = answer.getCreatedDate();
                messageItem.age = answer.getAge();
                messageItem.answer = true;
                messageItem.disableReply = true;
                messagesAdapter.add(messageItem);

                // Hide loader as this is the first item
                messagesLoader.setVisibility(View.GONE);
            }

            boolean hasNext = a.getHasNext();

            if(hasNext) {
                showAnswersLoader();
            } else {
                hideAnswersLoader();
            }

            ArrayList<MessageItemModel> answers = new ArrayList<>();
            for (AnswerModel answer : a.getAnswers()) {

                MessageItemModel messageItem = new MessageItemModel();
                messageItem.message = answer.getComment();
                messageItem.id = answer.getId();
                messageItem.parentId = answer.getParentId();
                messageItem.gender = answer.getGender();
                messageItem.name = answer.getName();
                messageItem.email = answer.getEmail();
                messageItem.answer = true;
                messageItem.date = answer.getCreatedDate();
                messageItem.age = answer.getAge();
                messageItem.reply = true;
                messageItem.disableReply = true;

                answers.add(messageItem);
            }

            // Update question if another page was loaded (used when rotating the screen)
            if(page > 0) {
                answer.getAnswers().addAll(a.getAnswers());
            }

            if(answers.size() > 0) {
                messagesAdapter.addAll(answers);
            }

            messagesAdapter.notifyDataSetChanged();

            if(hasNext) {
                setScrollEvent();
            }
        }
    };

    public static ReplyAnswerFragment newInstance(int answerId) {
        ReplyAnswerFragment fragment = new ReplyAnswerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ANSWER_ID, answerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        // Save items when rotating screen
        MessageItemListModel messageItems = new MessageItemListModel(messagesAdapter.getItems());
        outState.putSerializable(ARG_MESSAGE_ITEMS, messageItems);
        outState.putSerializable(ARG_ANSWER, answer);
        outState.putInt(ARG_PAGE, page);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        messagesAdapter = new MessagesAdapter(getActivity().getApplicationContext(), getActivity().getLayoutInflater());

        if (getArguments() != null) {
            answer = (AnswerModel)getArguments().getSerializable(ARG_ANSWER);
            answerId = getArguments().getInt(ARG_ANSWER_ID);
        }

        if(savedInstanceState != null) {

            page = savedInstanceState.getInt(ARG_PAGE);
            answer = (AnswerModel)savedInstanceState.getSerializable(ARG_ANSWER);
            MessageItemListModel items = (MessageItemListModel)savedInstanceState.getSerializable(ARG_MESSAGE_ITEMS);

            messagesAdapter.addAll(items.getItems());
            messagesAdapter.notifyDataSetChanged();
        }
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
                        loadAnswers(loadAnswerEventHandler);
                    }
                }

            }
        });
    }

    protected void loadAnswers(final LoadAnswerEventHandler event) {

        loadAnswerHandler = new LoadAnswerHandler(event);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AnswerModel answer = AnswerModel.getById(answerId, ANSWERS_LIMIT, page * ANSWERS_LIMIT);
                    loadAnswerHandler.sendMessage(Message.obtain(loadAnswerHandler, UPDATE_UI, answer));
                } catch (Exception e) {
                    Log.d(getClass().getName(), "Failed to load answers", e);
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

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        this.inflater = inflater;
        this.view = (RelativeLayout) inflater.inflate(R.layout.fragment_reply_answer, container, false);

        messagesLoader = (LinearLayout)view.findViewById(R.id.answerProgressContainer);

        messagesListView = (ListView)view.findViewById(R.id.messages);
        messagesListView.setDivider(null);

        // Add footer
        footerView = inflater.inflate(R.layout.messages_footer, null);

        // Add adapter
        messagesListView.setAdapter(messagesAdapter);

        // Do not load answers if the screen orientation was changed
        if(savedInstanceState == null) {
            loadAnswers(loadAnswerEventHandler);
        } else {
            if(answer.getHasNext()) {
                setScrollEvent();
                showAnswersLoader();
            } else {
                hideAnswersLoader();
            }
        }

        return this.view;
    }

}
