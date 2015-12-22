package bonnier.android;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sessingo on 17/08/15.
 */
public class Registry {

    protected static Registry instance;

    protected Map<String, Object> objects;

    public static Registry GetInstance() {
        if(instance == null) {
            instance = new Registry();
        }
        return instance;
    }

    public Registry() {
        this.objects = new HashMap<>();
    }

    public void set(String key, Object value) {
        this.objects.put(key, value);
    }

    public <T> T get(String key) {
        return (T)this.objects.get(key);
    }

    public int size() {
        return this.objects.size();
    }
}
