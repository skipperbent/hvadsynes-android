package bonnier.hvadsynes.models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by sessingo on 08/09/15.
 */
public class MessageItemListModel implements Serializable {

    public ArrayList<MessageItemModel> items;

    public MessageItemListModel(ArrayList<MessageItemModel> items) {
        this.items = items;
    }

    public ArrayList<MessageItemModel> getItems() {
        return items;
    }

    public void setItems(ArrayList<MessageItemModel> items) {
        this.items = items;
    }
}
