package com.kdeveloper.utils;

/**
 * Created by kirill on 17.09.14.
 */
public class ReplaceData {

    private String name;
    private String replacename;
    private String type;
    private String additional1 = "";
    private String additional2 = "";

    public ReplaceData(String name, String replacename, String type, String additional1, String additional2) {
        this.name = name;
        this.replacename = replacename;
        this.type = type;
        if (additional1 != null) this.additional1 = additional1;
        if (additional2 != null) this.additional2 = additional2;
    }

    public String getName() {
        return name;
    }

    public String getReplacename() {
        return replacename;
    }

    public String getType() {
        return type;
    }

    public String getAdditional1() {
        return additional1;
    }

    public String getAdditional2() {
        return additional2;
    }
}
