package bonnier.android.models;

import android.text.Html;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import bonnier.android.services.ServiceHvadSynes;

public class AnswerModel implements Serializable {

    private static String DATE_FORMAT = "yyyy-MM-dd k:m:s";

    public static Date parseDate(String date) throws ParseException {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        return format.parse(date);
    }

    private int id;
    private int questionId;
    private int userId;
    private String email;
    private String name;
    private int age;
    private int gender;
    private String comment;
    private int rating;
    private int parentId;
    private Date createdDate;
    private Date updatedDate;
    private ArrayList<AnswerModel> answers;

    // Pagination
    private int maxRows;
    private boolean hasNext;
    private boolean hasPrevious;

    public AnswerModel() {
        this.createdDate = new Date();
        this.updatedDate = new Date();
    }

    public int getId() {
        return id;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        // TODO: move to strings.xml
        if(this.name == null || this.name.trim().isEmpty()) {
            return "Anonym";
        }
        return name;
    }

    public int getAge() {
        return age;
    }

    public int getGender() {
        return gender;
    }

    public String getComment() {
        return comment;
    }

    public int getRating() {
        return rating;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email.trim();
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setComment(String comment) {
        this.comment = comment.trim();
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public ArrayList<AnswerModel> getAnswers() {
        return answers;
    }

    public void setAnswers(ArrayList<AnswerModel> answers) {
        this.answers = answers;
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

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public static AnswerModel setModel(AnswerModel answer, JSONObject response) throws Exception {

        answer.setId(response.getInt("id"));
        //answer.setQuestionId(questionId);
        answer.setComment(Html.fromHtml(response.getString("comment")).toString().trim());

        if (!response.isNull("question_id")) {
            answer.setQuestionId(response.getInt("question_id"));
        }

        if (!response.isNull("age")) {
            answer.setAge(response.getInt("age"));
        }

        if (!response.isNull("gender")) {
            answer.setGender(response.getInt("gender"));
        }

        if (!response.isNull("name")) {
            answer.setName(response.getString("name"));
        }

        if (response.getString("email") != "") {
            answer.setEmail(response.getString("email"));
        }

        if(!response.isNull("parent_id")) {
            answer.setParentId(response.getInt("parent_id"));
        }

        if(!response.isNull("created_date")) {
            answer.setCreatedDate(parseDate(response.getString("created_date")));
        }

        if(!response.isNull("updated_date")) {
            answer.setUpdatedDate(parseDate(response.getString("updated_date")));
        }

        ArrayList<AnswerModel> answers = new ArrayList<>();

        // Loop through answers
        if (!response.isNull("comments")) {
            JSONObject comments = response.getJSONObject("comments");

            if(!comments.isNull("maxRows")) {
                answer.setMaxRows(comments.getInt("maxRows"));
            }

            if(!comments.isNull("hasNext")) {
                answer.setHasNext(comments.getBoolean("hasNext"));
            }

            if(!comments.isNull("hasPrevious")) {
                answer.setHasPrevious(comments.getBoolean("hasPrevious"));
            }

            if(!comments.isNull("rows")) {

                JSONArray rows = comments.getJSONArray("rows");

                for (int i = 0; i < rows.length(); i++) {
                    JSONObject replyResponse = rows.getJSONObject(i);

                    answers.add(setModel(new AnswerModel(), replyResponse));
                }
            }
        }

        answer.setAnswers(answers);
        return answer;
    }

    public static AnswerModel getById(int id) throws Exception{
        return getById(id, null, null);
    }

    public static AnswerModel getById(int id, Integer rows, Integer skip) throws Exception{
        ServiceHvadSynes service = new ServiceHvadSynes();

        String _rows = (rows == null) ? "" : String.valueOf(rows);
        String _skip = (skip == null) ? "" : String.valueOf(skip);

        JSONObject result = service.api("answers/" + String.valueOf(id), ServiceHvadSynes.PostType.GET, new String[] { "skip=" + _skip, "limit=" + _rows });
        return setModel(new AnswerModel(), result);
    }

    public void save() throws Exception{
        ServiceHvadSynes service = new ServiceHvadSynes();
        String[] params = new String[] {
                "gender=" + this.gender,
                "name=" + this.name,
                "email=" + this.email,
                "age=" + this.age,
                "comment=" + this.comment,
                "question_id=" + this.questionId,
                "parent_id=" + this.parentId

        };

        JSONObject response = service.api("answers", ServiceHvadSynes.PostType.POST, params);

        if(!response.isNull("result") && response.getBoolean("result")) {
            if(!response.isNull("id")) {
                this.id = response.getInt("id");
            }
        }
    }
}
