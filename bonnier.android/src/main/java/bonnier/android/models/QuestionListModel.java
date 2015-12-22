package bonnier.android.models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by sessingo on 13/09/15.
 */
public class QuestionListModel implements Serializable {

    private ArrayList<QuestionModel> questions;

    // Pagination
    private int maxRows;
    private boolean hasNext;
    private boolean hasPrevious;

    public QuestionListModel() {
        this.questions = new ArrayList<>();
    }

    public ArrayList<QuestionModel> getQuestions() {
        return questions;
    }

    public void setQuestions(ArrayList<QuestionModel> questions) {
        this.questions = questions;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean getHasPrevious() {
        return hasPrevious;
    }

    public void getHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}
