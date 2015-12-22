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


public class QuestionModel implements Serializable {

    private static String DATE_FORMAT = "yyyy-MM-dd k:m:s";

    public enum Order {
        created_date,
        changed_date,
        comment_count,
        age,
        id,
        random
    }

    public enum Sort {
        desc,
        asc
    }

    public static Date parseDate(String date) throws ParseException {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        return format.parse(date);
    }

    private int id;
    private String question;
    private String url;
    private String name;
    private String email;
    private int age;
    private int gender;
    private Date createdDate;
    private Date updatedDate;
    private ArrayList<AnswerModel> answers;
    private ArrayList<CategoryModel> categories;

    // Pagination
    private int maxRows;
    private boolean hasNext;
    private boolean hasPrevious;

    public QuestionModel() {

        this.answers = new ArrayList<>();
        this.createdDate = new Date();
        this.updatedDate = new Date();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setUrl(String url) {
        this.url = url.trim();
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public void setEmail(String email) {
        this.email = email.trim();
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getId() {
        return id;
    }

    public String getQuestion() {
        return this.question;
    }

    public String getUrl() {
        return this.url;
    }

    public String getName() {
        // TODO: move to strings.xml
        if(this.name == null || this.name.trim().isEmpty()) {
            return "Anonym";
        }
        return this.name;
    }

    public String getEmail() { return this.email; }

    public int getAge() { return this.age; }

    public int getGender() { return this.gender; }

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

    public void setAnswers(ArrayList<AnswerModel> answers) {
        this.answers = answers;
    }

    public ArrayList<AnswerModel> getAnswers() {
        return answers;
    }

    public ArrayList<CategoryModel> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<CategoryModel> categories) {
        this.categories = categories;
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

    public static QuestionModel setModel(QuestionModel question, JSONObject result) throws Exception {

        if(result != null) {

            question.setQuestion(Html.fromHtml(result.getString("question")).toString().trim());
            question.setId(result.getInt("id"));
            question.setUrl(result.getString("url"));
            if (!result.isNull("age")) {
                question.setAge(result.getInt("age"));
            }

            if (!result.isNull("name")) {
                question.setName(result.getString("name"));
            }

            if (result.getString("email") != "") {
                question.setEmail(result.getString("email"));
            }

            if (!result.isNull("gender")) {
                question.setGender(result.getInt("gender"));
            }

            if(!result.isNull("created_date")) {
                question.setCreatedDate(parseDate(result.getString("created_date")));
            }

            if(!result.isNull("updated_date")) {
                question.setUpdatedDate(parseDate(result.getString("updated_date")));
            }

            // Fetch answers
            ArrayList<AnswerModel> answers = new ArrayList<>();
            if (!result.isNull("comments")) {

                JSONObject comments = result.getJSONObject("comments");

                if(!comments.isNull("maxRows")) {
                    question.setMaxRows(comments.getInt("maxRows"));
                }

                if(!comments.isNull("hasNext")) {
                    question.setHasNext(comments.getBoolean("hasNext"));
                }

                if(!comments.isNull("hasPrevious")) {
                    question.setHasPrevious(comments.getBoolean("hasPrevious"));
                }

                if(!comments.isNull("rows")) {

                    JSONArray rows = comments.getJSONArray("rows");

                    for (int i = 0; i < rows.length(); i++) {
                        JSONObject answerResponse = rows.getJSONObject(i);

                        AnswerModel answer = AnswerModel.setModel(new AnswerModel(), answerResponse);

                        answers.add(answer);
                    }

                }
            }

            question.setAnswers(answers);

            // Fetch categories

            ArrayList<CategoryModel> categories = new ArrayList<>();

            if (!result.isNull("categories")) {

                JSONArray rows = result.getJSONArray("categories");

                for (int i = 0; i < rows.length(); i++) {
                    JSONObject questionResponse = result.getJSONArray("categories").getJSONObject(i);

                    CategoryModel category = CategoryModel.createModel(questionResponse);

                    categories.add(category);
                }
            }

            question.setCategories(categories);
        }

        return question;
    }

    public void save(int categoryId) throws Exception{
        ServiceHvadSynes service = new ServiceHvadSynes();
        String[] params = new String[] {
                "gender=" + this.gender,
                "name=" + this.name,
                "email=" + this.email,
                "age=" + this.age,
                "question=" + this.question,
                "category_id=" + categoryId

        };

        JSONObject response = service.api("questions", ServiceHvadSynes.PostType.POST, params);

        if(!response.isNull("id")) {
            setModel(this, response);
        }
    }

    public static QuestionModel getById(int id) throws Exception{
        return getById(id, null, null);
    }

    public static QuestionModel getById(int id, Integer rows, Integer skip) throws Exception{
        ServiceHvadSynes service = new ServiceHvadSynes();

        String _rows = (rows == null) ? "" : String.valueOf(rows);
        String _skip = (skip == null) ? "" : String.valueOf(skip);

        JSONObject result = service.api("questions/"+String.valueOf(id), ServiceHvadSynes.PostType.GET, new String[] { "skip=" + _skip, "limit=" + _rows });
        return setModel(new QuestionModel(), result);
    }

    public static QuestionListModel get(Integer categoryId, Order order, Sort sort, Integer rows, Integer skip) throws Exception {
        ServiceHvadSynes service = new ServiceHvadSynes();
        QuestionListModel list = new QuestionListModel();

        String _categoryId = (categoryId == null) ? "" : String.valueOf(categoryId);
        String _rows = (rows == null) ? "" : String.valueOf(rows);
        String _skip = (skip == null) ? "" : String.valueOf(skip);

        JSONObject result = service.api("questions", ServiceHvadSynes.PostType.GET, new String[] { "order=" + order.toString(), "category_id=" + _categoryId, "sort=" + sort, "skip=" + _skip, "limit=" + _rows, "_rand=" + Math.random() });

        if(!result.isNull("maxRows")) {
            list.setMaxRows(result.getInt("maxRows"));
        }

        if(!result.isNull("hasNext")) {
            list.setHasNext(result.getBoolean("hasNext"));
        }

        if(!result.isNull("hasPrevious")) {
            list.setHasPrevious(result.getBoolean("hasPrevious"));
        }

        JSONArray rowsResponse = result.getJSONArray("rows");
        for (int i = 0; i < rowsResponse.length(); i++ ) {
            JSONObject question = result.getJSONArray("rows").getJSONObject(i);
            list.getQuestions().add(setModel(new QuestionModel(), question));
        }

        return list;
    }

    public static QuestionListModel getRandom(int rows) throws Exception {
        return get(null, Order.random, Sort.desc, rows, null);
    }
}
