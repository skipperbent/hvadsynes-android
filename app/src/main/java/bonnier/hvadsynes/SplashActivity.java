package bonnier.hvadsynes;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import bonnier.android.Registry;
import bonnier.android.models.CategoryModel;
import bonnier.android.models.QuestionModel;
import bonnier.hvadsynes.models.MessageItemModel;
import me.alexrs.wavedrawable.WaveDrawable;

public class SplashActivity extends AppCompatActivity {

    private WaveDrawable waveDrawable;
    private static final int UPDATE_UI_SUCCESS = 1;
    private static final int UPDATE_UI_ERROR = 2;
    private static final int WAIT_TIMEOUT_MSG = 8000;

    public interface LoadCategoriesEventHandler {
        void onLoadFinished(ArrayList<CategoryModel> categories);
    }

    private static class LoadQuestionCompleteHandler extends Handler {
        private WeakReference<SplashActivity> activity;

        public LoadQuestionCompleteHandler(SplashActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if(this.activity.get() != null) {
                switch (msg.what) {
                    case UPDATE_UI_SUCCESS:
                        Intent mainActivity = new Intent(this.activity.get().getApplicationContext(), MainActivity.class);
                        this.activity.get().startActivity(mainActivity);
                        Log.d(getClass().getName(), "Start main activity");
                        this.activity.get().finish();
                        break;
                    case UPDATE_UI_ERROR:
                        // Todo: add to strings.xml
                        if (!this.activity.get().stopThread) {
                            Toast.makeText(this.activity.get().getApplicationContext(), "Kunne ikke hente spørgsmål, prøver igen...", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }
    }

    private static class LoadCategoriesHandler extends Handler {
        private SplashActivity activity;
        private LoadCategoriesEventHandler event;

        public LoadCategoriesHandler(SplashActivity activity, LoadCategoriesEventHandler event) {
            this.activity = activity;
            this.event = event;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_UI_SUCCESS:
                    event.onLoadFinished((ArrayList<CategoryModel>) msg.obj);
                    break;
                case UPDATE_UI_ERROR:
                    // Todo: add to strings.xml
                    if (!this.activity.stopThread) {
                        Toast.makeText(this.activity.getApplicationContext(), "Kunne ikke hente kategorier, prøver igen...", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }


        }
    }

    private LoadCategoriesHandler loadCategoriesHandler;
    private LoadQuestionCompleteHandler loadQuestionCompleteHandler = new LoadQuestionCompleteHandler(this);

    private boolean stopThread;
    private Settings settings;

    @Override
    protected void onPause() {
        this.stopThread = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        this.stopThread = false;
        loadCategories();
        super.onResume();
    }

    protected void loadCategories() {

        this.loadCategoriesHandler = new LoadCategoriesHandler(this, new LoadCategoriesEventHandler() {
            @Override
            public void onLoadFinished(ArrayList<CategoryModel> categories) {
                settings.setCategories(categories);
                loadQuestions();
            }
        });

        // Load categories thread
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){

                boolean hasCategories = false;

                ArrayList<CategoryModel> categories = null;

                while(!hasCategories && !stopThread) {

                    try {
                        categories = CategoryModel.get();
                        hasCategories = true;
                    } catch (Exception e) {
                        Log.d(getClass().getName(), "Failed to fetch categories", e);

                        loadCategoriesHandler.sendMessage(Message.obtain(loadCategoriesHandler, UPDATE_UI_ERROR));

                        try {
                            Thread.sleep(WAIT_TIMEOUT_MSG);
                        } catch (Exception ex) {
                            // ignore
                        }
                    }

                }

                if(hasCategories) {
                    loadCategoriesHandler.sendMessage(Message.obtain(loadCategoriesHandler, UPDATE_UI_SUCCESS, categories));
                }
            }
        });

        thread.start();
    }

    protected void loadQuestions() {

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){

                boolean hasQuestions = false;

                while(!hasQuestions && !stopThread) {
                    try {
                        Registry.GetInstance().set("questions", QuestionModel.getRandom(10).getQuestions());
                        hasQuestions = true;
                    } catch (Exception e) {
                        Log.d("API", e.getMessage(), e);
                        loadQuestionCompleteHandler.sendMessage(Message.obtain(loadQuestionCompleteHandler, UPDATE_UI_ERROR));

                        try {
                            Thread.sleep(WAIT_TIMEOUT_MSG);
                        } catch (Exception ex) {
                            // Ignore
                        }
                    }
                }

                if(hasQuestions) {
                    loadQuestionCompleteHandler.sendMessage(Message.obtain(loadQuestionCompleteHandler, UPDATE_UI_SUCCESS));
                }
            }
        });

        thread.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Dont save this activity in history (so we don't see it by pressing the back key)
        this.getIntent().setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        super.onCreate(savedInstanceState);

        settings = Settings.getInstance(getApplicationContext());

        // Start setup activity if this is the first launch
        if(!settings.getSetup()) {
            Intent setupActivity = new Intent(getApplicationContext(), SetupActivity.class);
            startActivity(setupActivity);
            finish();
            return;
        }

        setContentView(R.layout.activity_splash);

        LinearLayout mainLayout = (LinearLayout)findViewById(R.id.mainLayout);
        LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService(getApplicationContext().LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.loader, mainLayout, true);

        ImageView imageView = (ImageView)view.findViewById(R.id.image);

        waveDrawable = new WaveDrawable(Color.parseColor("#19526D"), 370); // TODO: implement in styles.xml
        imageView.setBackground(waveDrawable);
        waveDrawable.setWaveInterpolator(new LinearInterpolator());
        waveDrawable.startAnimation();
    }

}
