package bonnier.hvadsynes.validators;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

/**
 * Created by sessingo on 05/09/15.
 */
public abstract class TextValidator implements TextWatcher {
    private TextView textView;

    public TextValidator(TextView textView) {
        this.textView = textView;
    }

    public abstract void validate(TextView textView, String text);

    @Override
    final public void afterTextChanged(Editable s) {
        String text = textView.getText().toString();
        validate(textView, text);
    }

    @Override
    final public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Ignore
    }

    @Override
    final public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Ignore
    }
}