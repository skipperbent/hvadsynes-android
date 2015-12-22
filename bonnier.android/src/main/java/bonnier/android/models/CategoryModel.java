package bonnier.android.models;

import android.app.Service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import bonnier.android.services.ServiceHvadSynes;

/**
 * Created by sessingo on 09/09/15.
 */
public class CategoryModel implements Serializable {
    private int id;
    private String name;
    private String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description.trim();
    }

    protected static CategoryModel createModel(JSONObject result) throws Exception {
        CategoryModel category = new CategoryModel();

        if(result != null) {

            if (!result.isNull("id")) {
                category.setId(result.getInt("id"));
            }

            if (!result.isNull("name")) {
                category.setName(result.getString("name"));
            }

            if (!result.isNull("description")) {
                category.setDescription(result.getString("description"));
            }

        }

        return category;
    }

    public static ArrayList<CategoryModel> get() throws Exception {
        ArrayList<CategoryModel> categories = new ArrayList<>();

        ServiceHvadSynes service = new ServiceHvadSynes();
        JSONObject result = service.api("categories");

        JSONArray _rows = result.getJSONArray("rows");
        for (int i = 0; i < _rows.length(); i++ ) {
            JSONObject category = result.getJSONArray("rows").getJSONObject(i);
            categories.add(createModel(category));
        }

        return categories;
    }
}
