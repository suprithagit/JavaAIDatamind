package com.srp.datamindAi.model;

import java.util.List;
import com.srp.datamindAi.model.ChartData;

public class DashboardOverview {

    private String summary;
    private List<OverviewStat> stats;
    private List<String> insights;
    private List<ChartData> charts;

    public DashboardOverview() {
    }

    public DashboardOverview(String summary, List<OverviewStat> stats, List<String> insights) {
        this.summary = summary;
        this.stats = stats;
        this.insights = insights;
    }

    public DashboardOverview(String summary, List<OverviewStat> stats, List<String> insights, List<ChartData> charts) {
        this.summary = summary;
        this.stats = stats;
        this.insights = insights;
        this.charts = charts;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<OverviewStat> getStats() {
        return stats;
    }

    public void setStats(List<OverviewStat> stats) {
        this.stats = stats;
    }

    public List<String> getInsights() {
        return insights;
    }

    public void setInsights(List<String> insights) {
        this.insights = insights;
    }

    public List<ChartData> getCharts() {
        return charts;
    }

    public void setCharts(List<ChartData> charts) {
        this.charts = charts;
    }

    public static class OverviewStat {

        private String label;
        private String value;
        private String change;

        public OverviewStat() {
        }

        public OverviewStat(String label, String value, String change) {
            this.label = label;
            this.value = value;
            this.change = change;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getChange() {
            return change;
        }

        public void setChange(String change) {
            this.change = change;
        }
    }
}
