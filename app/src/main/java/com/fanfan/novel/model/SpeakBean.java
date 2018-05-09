package com.fanfan.novel.model;

public class SpeakBean {

    private String problem;
    private String anwer;
    private long time;
    private boolean action;

    public SpeakBean(String problem, String anwer, long time, boolean action) {
        this.problem = problem;
        this.anwer = anwer;
        this.time = time;
        this.action = action;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getAnwer() {
        return anwer;
    }

    public void setAnwer(String anwer) {
        this.anwer = anwer;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isAction() {
        return action;
    }

    public void setAction(boolean action) {
        this.action = action;
    }
}
