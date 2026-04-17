package com.srp.datamindAi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class BackendProperties {

    private String title;
    private String environment;
    private String frontendUrl;
    private String aiModel;
    private double aiTemperature;
    private int maxQueriesPerHour = 50;

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

    public int getMaxQueriesPerHour() {
        return maxQueriesPerHour;
    }

    public void setMaxQueriesPerHour(int maxQueriesPerHour) {
        this.maxQueriesPerHour = maxQueriesPerHour;
    }
}
