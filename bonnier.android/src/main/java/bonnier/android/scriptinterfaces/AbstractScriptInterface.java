package bonnier.android.scriptinterfaces;

import android.content.Context;

/**
 * Created by sessingo on 19/08/15.
 */
public abstract class AbstractScriptInterface {

    protected Context context;

    protected AbstractScriptInterface(Context context)
    {
        this.context = context;
    }

}
