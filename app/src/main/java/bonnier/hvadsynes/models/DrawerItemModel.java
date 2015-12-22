package bonnier.hvadsynes.models;

/**
 * Created by sessingo on 17/08/15.
 */
public class DrawerItemModel {

    public interface DrawerItemEventListener {
        void onClick(DrawerItemModel item, int position);
    }

    private DrawerItemEventListener eventListener;
    public String title;

    public DrawerItemModel(String title) {
        this.title = title;
    }

    public DrawerItemModel(String title, DrawerItemEventListener event) {
        this.title = title;
        this.eventListener = event;
    }

    public DrawerItemEventListener getEventListener() {
        return eventListener;
    }

    public void setEventListener(DrawerItemEventListener eventListener) {
        this.eventListener = eventListener;
    }
}
