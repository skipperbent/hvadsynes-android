package bonnier.hvadsynes.models;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by sessingo on 05/09/15.
 */
public class MessageItemModel implements Serializable {

    public int id;
    public int parentId;
    public String name;
    public String message;
    public int gender;
    public int age;
    public Date date;
    public boolean reply;
    public String email;
    public boolean answer;
    public boolean disableReply;
    public boolean imageLoaded;
    public String url;

}
