package bonnier.hvadsynes;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bonnier.hvadsynes.drawables.BackgroundBitmapDrawable;
import bonnier.hvadsynes.validators.TextValidator;

public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Dont save this activity in history (so we don't see it by pressing the back key)
        this.getIntent().setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // Set scalable background
        RelativeLayout backgroundLayout = (RelativeLayout)findViewById(R.id.setupBackground);
        Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.question_bg_m);
        BackgroundBitmapDrawable backgroundBitmapDrawable = new BackgroundBitmapDrawable(getResources(), background);
        backgroundLayout.setBackground(backgroundBitmapDrawable);

        final EditText age = (EditText)findViewById(R.id.age);
        final RadioGroup gender = (RadioGroup)findViewById(R.id.gender);

        ((RadioButton)findViewById(R.id.male)).setChecked(true);

        gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int backgroundDrawable = (checkedId == R.id.male) ? R.drawable.question_bg_m : R.drawable.question_bg_f;
                Bitmap background = BitmapFactory.decodeResource(getResources(), backgroundDrawable);

                RelativeLayout backgroundLayout = (RelativeLayout)findViewById(R.id.setupBackground);
                BackgroundBitmapDrawable backgroundBitmapDrawable = new BackgroundBitmapDrawable(getResources(), background);
                backgroundLayout.setBackground(backgroundBitmapDrawable);
            }
        });

        final EditText name = (EditText)findViewById(R.id.name);

        name.addTextChangedListener(new TextValidator(name) {
            @Override
            public void validate(TextView textView, String text) {
                if(text.length() < 2) {
                    textView.setError("Ugyldigt navn");
                } else {
                    textView.setError(null);
                }
            }
        });

        final EditText email = (EditText)findViewById(R.id.email);

        email.addTextChangedListener(new TextValidator(email) {
            @Override
            public void validate(TextView textView, String text) {
                if(!isEmailValid(text)) {
                    textView.setError("Ugyldig e-mail");
                } else {
                    textView.setError(null);
                }
            }
        });


        Button btnSubmit = (Button) findViewById(R.id.save);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( name.getError() == null && email.getError() == null && age.length() > 0) {

                    // Save stuff

                    Settings settings = Settings.getInstance(getApplicationContext());
                    settings.setSetup(true);
                    settings.setEmail(email.getText().toString());
                    settings.setName(name.getText().toString());
                    settings.setAge(Integer.parseInt(age.getText().toString()));
                    settings.setGender((gender.getCheckedRadioButtonId() == R.id.male) ? 1 : 2);

                    settings.save();

                    Intent setupActivity = new Intent(getApplicationContext(), SplashActivity.class);
                    startActivity(setupActivity);
                    finish();
                }
            }
        });

    }

    public boolean isEmailValid(String email)
    {
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        return (matcher.matches());
    }
}
