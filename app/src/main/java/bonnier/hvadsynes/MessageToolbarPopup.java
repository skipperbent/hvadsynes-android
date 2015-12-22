package bonnier.hvadsynes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import bonnier.hvadsynes.models.MessageItemModel;

/**
 * Created by sessingo on 06/09/15.
 */
public class MessageToolbarPopup {

    public enum ButtonType {
        reply,
        like,
        dislike,
        share,
        copy
    }

    public interface MessageToolbarEvent {
        void onClick(ButtonType buttonType, MessageItemModel messageItem);
    }

    private static MessageToolbarPopup instance;

    PopupWindow popup;
    private MessageItemModel messageItem;
    private MessageToolbarEvent messageToolbarEventListener;

    public static Rect locateView(View v)
    {
        int[] loc_int = new int[2];
        if (v == null) return null;
        try
        {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException npe)
        {
            //Happens when the view doesn't exist on screen anymore.
            return null;
        }
        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top + v.getHeight();
        return location;
    }

    public void showPopup(Context context, LayoutInflater inflater, View v) {
        showPopup(context, inflater, v, null, null);
    }

    public void showPopup(final Context context, LayoutInflater inflater, View v, Integer width, Integer height) {
        hidePopup();

        int layoutFile = (this.messageItem.answer) ? R.layout.message_answer_toolbar : R.layout.message_question_toolbar;

        View layout = inflater.inflate(layoutFile, null, false);

        if(this.messageItem.answer) {

            if(!messageItem.disableReply) {
                // Answer specific stuff
                layout.findViewById(R.id.toolbarReply).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (messageToolbarEventListener != null) {
                            messageToolbarEventListener.onClick(ButtonType.reply, messageItem);
                        }

                        Intent replyAnswerActivity = new Intent(context, ReplyAnswerActivity.class);
                        replyAnswerActivity.putExtra(ReplyAnswerActivity.ARG_ANSWER_ID, messageItem.id);
                        replyAnswerActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(replyAnswerActivity);

                        hidePopup();
                    }
                });
            }

            layout.findViewById(R.id.toolbarLike).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(messageToolbarEventListener != null) {
                        messageToolbarEventListener.onClick(ButtonType.like, messageItem);
                    }
                    Toast.makeText(context, "Like logik her...", Toast.LENGTH_SHORT).show();
                    hidePopup();
                }
            });

            layout.findViewById(R.id.toolbarDislike).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(messageToolbarEventListener != null) {
                        messageToolbarEventListener.onClick(ButtonType.dislike, messageItem);
                    }
                    Toast.makeText(context, "Dislike logik her...", Toast.LENGTH_SHORT).show();
                    hidePopup();
                }
            });
        } else {
            // Question specific stuff

            // Share
            layout.findViewById(R.id.toolbarShare).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (messageToolbarEventListener != null) {
                        messageToolbarEventListener.onClick(ButtonType.share, messageItem);
                    }
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, messageItem.url);
                    sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    sendIntent.setType("text/plain");
                    context.startActivity(sendIntent);
                    hidePopup();
                }
            });
        }

        layout.findViewById(R.id.toolbarCopyText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(messageToolbarEventListener != null) {
                    messageToolbarEventListener.onClick(ButtonType.copy, messageItem);
                }
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("message", messageItem.message);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(context, "Besked kopieret", Toast.LENGTH_SHORT).show();
                hidePopup();
                // TODO: move to strings.xml
            }
        });

        popup = new PopupWindow(layout);

        if(width != null) {
            popup.setWidth(width);
        }

        if(height != null) {
            popup.setHeight(height);
        }

        if(width == null && height == null) {
            popup.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        Rect location = locateView(v);
        popup.showAtLocation(v, Gravity.NO_GRAVITY, location.left, location.top - 150);
    }

    public void hidePopup() {
        if (popup != null) {
            popup.dismiss();
            popup = null;
        }
    }

    public PopupWindow getPopup() {
        return this.popup;
    }

    public MessageItemModel getMessageItem() {
        return messageItem;
    }

    public void setMessageItem(MessageItemModel messageItem) {
        this.messageItem = messageItem;
    }

    public void setPopup(PopupWindow popup) {
        this.popup = popup;
    }

    public static MessageToolbarPopup GetInstance() {
        if(instance == null) {
           instance = new MessageToolbarPopup();
        }
        return instance;
    }

    public void setMessageToolbarEventListener(MessageToolbarEvent messageToolbarEventListener) {
        this.messageToolbarEventListener = messageToolbarEventListener;
    }
}
