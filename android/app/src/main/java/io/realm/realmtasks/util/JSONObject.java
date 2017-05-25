package io.realm.realmtasks.util;


import org.json.JSONException;

public class JSONObject {

    private org.json.JSONObject jsonObject;

    public JSONObject() {
        jsonObject = new org.json.JSONObject();
    }

    public JSONObject(String json) {
        try {
            jsonObject = new org.json.JSONObject(json == null ? "" : json);
        } catch (JSONException e) {
            jsonObject = new org.json.JSONObject();
        }
    }


    public String getString(String name) {
        if (jsonObject != null)
            try {
                return jsonObject.getString(name);
            } catch (JSONException ignored) {
            }
        return "";
    }

    public JSONObject put(String name, Object value)  {
        if (jsonObject != null)
            try {
                jsonObject.put(name, value);
            } catch (JSONException ignored) {
            }
        return this;
    }

    @Override
    public String toString() {
        return jsonObject != null ? jsonObject.toString() : "";
    }
}
