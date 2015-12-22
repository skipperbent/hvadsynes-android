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
import bonnier.hvadsynes.R;
import bonnier.hvadsynes.models.MessageItemModel;

/**
 * Created by sessingo on 17/08/15.
 */
public class QuestionsListAdapter extends ArrayAdapter<MessageItemModel> {

    private Context context;
    private ArrayList<MessageItemModel> items;
    private LayoutInflater inflater;

    public QuestionsListAdapter(Context context, LayoutInflater inflater) {
        super(context, R.layout.drawer_listview_item);
        this.context = context;
        this.items = new ArrayList<>();
        this.inflater = inflater;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final MessageItemModel messageItem = this.getItem(position);

        View v = inflater.inflate(R.layout.question_list_item, null, false);

        int bgDrawable = (messageItem.gender > 1) ? R.drawable.bubble_red_left : R.drawable.bubble_blue_left;
        int noPhoto = (messageItem.gender > 1) ? R.drawable.female : R.drawable.male;

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

        // TODO: only enable when saving cache on device, as this will too much memory otherwise
        /*if (profilePicture != null && messageItem.email != "") {
            String url = "http://www.gravatar.com/avatar/" + MD5Util.md5(messageItem.email) + "?s=80&d=404";
            new DownloadImageTask(profilePicture, false).execute(url);
        }*/

        LinearLayout messageBackground = (LinearLayout)v.findViewById(R.id.messageBackground);

        TextView message = (TextView)v.findViewById(R.id.question);

        messageBackground.setBackground(ContextCompat.getDrawable(getContext(), bgDrawable));

        message.setText(messageItem.message);

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
