package com.srp.datamindAi.model;

public class QueryResponse {

    private String summary;
    private ChartData chartData;

    public QueryResponse() {
    }

    public QueryResponse(String summary, ChartData chartData) {
        this.summary = summary;
        this.chartData = chartData;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public ChartData getChartData() {
        return chartData;
    }

    public void setChartData(ChartData chartData) {
        this.chartData = chartData;
    }
}
