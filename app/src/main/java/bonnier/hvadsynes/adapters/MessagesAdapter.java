package bonnier.hvadsynes.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import bonnier.android.MD5Util;
import bonnier.android.tasks.DownloadImageTask;
import bonnier.hvadsynes.MessageToolbarPopup;
import bonnier.hvadsynes.models.MessageItemModel;
import bonnier.hvadsynes.R;

/**
 * Created by sessingo on 17/08/15.
 */
public class MessagesAdapter extends ArrayAdapter<MessageItemModel> {

    Context context;

    ArrayList<MessageItemModel> items;
    LayoutInflater inflater;

    public MessagesAdapter(Context context, LayoutInflater inflater) {
        super(context, R.layout.drawer_listview_item);
        this.context = context;
        this.items = new ArrayList<>();
        this.inflater = inflater;
    }

    // Make sure that the list-view item is not clickable
    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        final MessageItemModel messageItem = this.getItem(position);

        View v;
        int bgDrawable;
        int noPhoto = (messageItem.gender > 1) ? R.drawable.female : R.drawable.male;

        if(messageItem.reply) {
            v = inflater.inflate(R.layout.messages_answer_reply, null, false);
            bgDrawable = (messageItem.gender > 1) ? R.drawable.bubble_red_right_states : R.drawable.bubble_blue_right_states;
        } else {
            v = inflater.inflate(R.layout.messages_answer, null, false);
            bgDrawable = (messageItem.gender > 1) ? R.drawable.bubble_red_left_states : R.drawable.bubble_blue_left_states;
        }

        // Date
        TextView date = (TextView)v.findViewById(R.id.time);
        date.setText(new SimpleDateFormat("M MMM yyyy", Locale.getDefault()).format(messageItem.date));

        // Name
        TextView name = (TextView)v.findViewById(R.id.userInfo);
        if(messageItem.age > 0) {
            name.setText(String.format("%s, %s", messageItem.name, messageItem.age));
        } else {
            name.setText(messageItem.name);
        }

        // Picture
        ImageView profilePicture = (ImageView) v.findViewById(R.id.picture);

        profilePicture.setImageResource(noPhoto);

        // TODO: store images on device
        if (profilePicture != null && messageItem.email != "") {
            String url = "http://www.gravatar.com/avatar/" + MD5Util.md5(messageItem.email) + "?s=80&d=404";
            new DownloadImageTask(profilePicture, false).execute(url);
        }

        LinearLayout messageBackground = (LinearLayout)v.findViewById(R.id.messageBackground);

        TextView message = (TextView)v.findViewById(R.id.comment);

        messageBackground.setBackground(ContextCompat.getDrawable(getContext(), bgDrawable));

        message.setText(messageItem.message);

        messageBackground.setClickable(true);

        messageBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(getClass().getName(), "Pressing down on message");

                MessageToolbarPopup popup = MessageToolbarPopup.GetInstance();

                if (popup.getPopup() != null && popup.getMessageItem().id == messageItem.id) {
                    popup.hidePopup();
                } else {
                    popup.setMessageItem(messageItem);
                    popup.showPopup(context, inflater, v);
                }
            }
        });

        return v;

    }

    public void add(MessageItemModel message) {
        this.items.add(message);
        super.add(message);
    }

    @Override
    public void addAll(Collection<? extends MessageItemModel> collection) {
        items.addAll(collection);
        super.addAll(collection);
    }

    @Override
    public void addAll(MessageItemModel... items) {
        Collections.addAll(this.items, items);
        super.addAll(items);
    }

    public ArrayList<MessageItemModel> getItems() {
        return items;
    }

}
