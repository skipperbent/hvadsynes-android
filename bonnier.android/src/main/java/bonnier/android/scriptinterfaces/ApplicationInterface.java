package bonnier.android.scriptinterfaces;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by sessingo on 19/08/15.
 */
public class ApplicationInterface extends AbstractScriptInterface {

    public ApplicationInterface(Context context) {
        super(context);
    }

    @JavascriptInterface
    public void ShowToast(String toast) {
        ShowToast(toast, Toast.LENGTH_SHORT);
    }

    @JavascriptInterface
    public void ShowToast(String toast, int length)
    {
        Toast.makeText(context, toast, length).show();
    }

}
