package com.srp.datamindAi.model;

public class AppConfigResponse {

    private String title;
    private String environment;
    private String frontendUrl;
    private String aiModel;
    private double aiTemperature;

    public AppConfigResponse() {
    }

    public AppConfigResponse(String title, String environment, String frontendUrl, String aiModel, double aiTemperature) {
        this.title = title;
        this.environment = environment;
        this.frontendUrl = frontendUrl;
        this.aiModel = aiModel;
        this.aiTemperature = aiTemperature;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getFrontendUrl() {
        return frontendUrl;
    }

    public void setFrontendUrl(String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    public String getAiModel() {
        return aiModel;
    }

    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }

    public double getAiTemperature() {
        return aiTemperature;
    }

    public void setAiTemperature(double aiTemperature) {
        this.aiTemperature = aiTemperature;
    }
}
