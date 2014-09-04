package com.kdeveloper.utils;

/**
 * Created by kirill on 30.08.14.
 */
public class MethodData {

    public String getAccess() {
        return access;
    }

    public String getName() {
        return name;
    }

    public String getParameters() {
        return parameters;
    }

    public String getReturn() {
        return ret;
    }

    private String access;
    private String name;
    private String parameters;
    private String ret;

    public MethodData(String access, String name, String parameters, String ret) {
        this.access = access;
        this.name = name;
        this.parameters = parameters;
        this.ret = ret;
    }

}
