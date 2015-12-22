package bonnier.android;

/**
 * Created by sessingo on 07/09/15.
 */
public class FragmentHelper {

    public static String makeFragmentName(int viewPagerId, int index) {
        return "android:switcher:" + viewPagerId + ":" + index;
    }

}
