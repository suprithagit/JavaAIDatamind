package com.srp.datamindAi.service;

import com.srp.datamindAi.config.BackendProperties;
import com.srp.datamindAi.exception.ApiLimitException;
import com.srp.datamindAi.model.AppConfigResponse;
import com.srp.datamindAi.model.ChartData;
import com.srp.datamindAi.model.ChartData.DatasetConfig;
import com.srp.datamindAi.model.DashboardOverview;
import com.srp.datamindAi.model.DashboardOverview.OverviewStat;
import com.srp.datamindAi.model.QueryResponse;
import com.srp.datamindAi.model.QueryRequest;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.tablesaw.api.Table;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.NumericColumn;
import tech.tablesaw.columns.Column;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class QueryService {
    private static final Logger logger = LoggerFactory.getLogger(QueryService.class);
    private static final int MAX_CATEGORIES = 12;
    private static final String EMPTY_MARKER = "<empty>";

    private final BackendProperties backendProperties;
    private Table dataTable;
    private int queryCount;

    public QueryService(BackendProperties backendProperties) {
        this.backendProperties = backendProperties;
        logger.info("QueryService initialized with backend properties");
    }

    public void loadCsv(java.io.InputStream inputStream) throws IOException {
        dataTable = Table.read().csv(inputStream);
        logger.info("CSV loaded successfully: {} rows, {} columns", 
                   dataTable.rowCount(), dataTable.columnCount());
    }

    public QueryResponse processQuery(QueryRequest request) {
        if (queryCount >= backendProperties.getMaxQueriesPerHour()) {
            logger.warn("API query limit exceeded. Count: {}", queryCount);
            throw new ApiLimitException("API query limit exceeded. Please try again later.");
        }

        String query = request.getQuery();
        if (query == null || query.isBlank()) {
            query = "General data overview";
        }

        // Check for inappropriate content
        if (containsInappropriateContent(query)) {
            logger.warn("Inappropriate query detected: {}", query.substring(0, 50) + "...");
            throw new IllegalArgumentException("Your query contains inappropriate content. Please ask questions related to data analysis only.");
        }

        queryCount++;
        logger.debug("Processing query #{}: {}", queryCount, query);

        // Analyze query intent and generate appropriate response
        QueryAnalysis analysis = analyzeQueryIntent(query);
        ChartData chartData = generateChartForIntent(analysis);
        String summary = generateIntelligentSummary(query, analysis);

        return new QueryResponse(summary, chartData);
    }

    public DashboardOverview getDashboardOverview() {
        String summary = "Dashboard Summary";
        
        if (dataTable != null) {
            summary = String.format("Data Overview: %d rows × %d columns", 
                                   dataTable.rowCount(), dataTable.columnCount());
        } else {
            summary = "No data uploaded yet. Please upload a CSV file to begin analysis.";
        }

        List<OverviewStat> statistics = generateDashboardStatistics();
        List<String> insights = generateInsights();
        List<ChartData> charts = generateDashboardCharts();

        return new DashboardOverview(summary, statistics, insights, charts);
    }

    public AppConfigResponse getAppConfig() {
        return new AppConfigResponse(
                backendProperties.getTitle(),
                backendProperties.getEnvironment(),
                backendProperties.getFrontendUrl(),
                backendProperties.getAiModel(),
                backendProperties.getAiTemperature()
        );
    }

    // ============== Chart Generation ==============
    private ChartData analyzeAndGenerateChart(String query) {
        if (dataTable == null) {
            logger.info("No data available, generating sample chart");
            return generateSampleChart(query);
        }

        String queryLower = query.toLowerCase();
        
        if (queryLower.contains("trend") || queryLower.contains("over time") || queryLower.contains("change")) {
            return generateLineChart(query);
        } else if (queryLower.contains("distribution") || queryLower.contains("proportion") || queryLower.contains("percent")) {
            return generatePieChart(query);
        } else if (queryLower.contains("compare") || queryLower.contains("comparison")) {
            return generateBarChart(query);
        } else {
            return autoDetectAndGenerateChart(query);
        }
    }

    private ChartData autoDetectAndGenerateChart(String query) {
        for (String columnName : dataTable.columnNames()) {
            Column<?> column = dataTable.column(columnName);
            
            if (column instanceof StringColumn stringColumn) {
                Map<String, Integer> distribution = analyzeCategoricalColumn(stringColumn);
                
                if (distribution.size() > 1 && distribution.size() <= MAX_CATEGORIES) {
                    logger.info("Generating categorical chart for column: {}", columnName);
                    
                    boolean isManyCategories = distribution.size() > 5;
                    String chartType = isManyCategories ? "bar" : "pie";
                    
                    List<String> labels = new ArrayList<>(distribution.keySet());
                    List<Integer> values = new ArrayList<>(distribution.values());
                    List<DatasetConfig> datasets = List.of(new DatasetConfig("Count", values));
                    
                    String insight = String.format("This chart shows %d distinct values in %s with varying frequencies.",
                                                   distribution.size(), columnName);
                    
                    return new ChartData(chartType, "Distribution of " + columnName, labels, datasets, insight);
                }
            } else if (column instanceof NumericColumn<?>) {
                logger.info("Found numeric column: {}", columnName);
                return generateNumericChart(columnName, (NumericColumn<?>) column);
            }
        }

        return generateSummaryChart(query);
    }

    private ChartData generatePieChart(String query) {
        for (String columnName : dataTable.columnNames()) {
            Column<?> column = dataTable.column(columnName);
            
            if (column instanceof StringColumn stringColumn) {
                Map<String, Integer> distribution = analyzeCategoricalColumn(stringColumn);
                
                if (distribution.size() > 1 && distribution.size() <= MAX_CATEGORIES) {
                    List<String> labels = new ArrayList<>(distribution.keySet());
                    List<Integer> values = new ArrayList<>(distribution.values());
                    List<DatasetConfig> datasets = List.of(new DatasetConfig("Share", values));
                    
                    String insight = String.format("Distribution breakdown showing proportions across %d categories in %s.",
                                                   distribution.size(), columnName);
                    
                    return new ChartData("pie", "Distribution: " + columnName, labels, datasets, insight);
                }
            }
        }
        
        return generateSampleChart(query);
    }

    private ChartData generateBarChart(String query) {
        for (String columnName : dataTable.columnNames()) {
            Column<?> column = dataTable.column(columnName);
            
            if (column instanceof StringColumn stringColumn) {
                Map<String, Integer> distribution = analyzeCategoricalColumn(stringColumn);
                
                if (distribution.size() > 1 && distribution.size() <= MAX_CATEGORIES) {
                    List<String> labels = new ArrayList<>(distribution.keySet());
                    List<Integer> values = new ArrayList<>(distribution.values());
                    List<DatasetConfig> datasets = List.of(new DatasetConfig("Count", values));
                    
                    String insight = String.format("Comparison of frequencies across %d categories. Higher bars indicate more occurrences.",
                                                   distribution.size());
                    
                    return new ChartData("bar", "Comparison: " + columnName, labels, datasets, insight);
                }
            }
        }
        
        return generateSampleChart(query);
    }

    private ChartData generateLineChart(String query) {
        List<NumericColumn<?>> numericColumns = new ArrayList<>();
        
        for (String columnName : dataTable.columnNames()) {
            Column<?> column = dataTable.column(columnName);
            if (column instanceof NumericColumn<?>) {
                numericColumns.add((NumericColumn<?>) column);
            }
        }
        
        if (!numericColumns.isEmpty()) {
            NumericColumn<?> firstNumeric = numericColumns.get(0);
            return generateNumericTrendChart(firstNumeric.name(), firstNumeric);
        }
        
        return generateSampleChart(query);
    }

    private ChartData generateSummaryChart(String query) {
        List<String> labels = List.of("Insight");
        List<Integer> values = List.of(100);
        List<DatasetConfig> datasets = List.of(new DatasetConfig("Summary", values));
        
        String insight = "General overview of dataset. Submit a specific query for detailed analysis.";
        
        return new ChartData("summary", "Data Summary", labels, datasets, insight);
    }

    private ChartData generateNumericChart(String columnName, NumericColumn<?> column) {
        List<String> labels = List.of("Min", "Max", "Count");
        
        double minVal = column.min();
        double maxVal = column.max();
        
        List<Integer> values = List.of(
            (int) minVal,
            (int) maxVal,
            column.size()
        );
        List<DatasetConfig> datasets = List.of(new DatasetConfig("Value", values));
        
        String insight = String.format("Column ranges from %d to %d across %d data points.",
                                      values.get(0), values.get(1), values.get(2));
        
        return new ChartData("bar", "Numeric Summary: " + columnName, labels, datasets, insight);
    }

    private ChartData generateNumericTrendChart(String columnName, NumericColumn<?> column) {
        int size = Math.min(column.size(), 10);
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        
        for (int i = 0; i < size; i++) {
            labels.add("Index " + (i + 1));
            double val = column.getDouble(i);
            values.add((int) val);
        }
        
        List<DatasetConfig> datasets = List.of(new DatasetConfig(columnName, values));
        
        String insight = String.format("Trend visualization of %s over %d data points. Shows pattern and fluctuations.",
                                      columnName, size);
        
        return new ChartData("line", "Trend: " + columnName, labels, datasets, insight);
    }

    private ChartData generateSampleChart(String query) {
        List<String> labels = List.of("Sample A", "Sample B", "Sample C");
        List<Integer> values = List.of(65, 59, 45);
        List<DatasetConfig> datasets = List.of(new DatasetConfig("Values", values));
        
        String insight = "Sample data for demonstration. Please upload a CSV file to analyze your data.";
        
        return new ChartData("pie", "Sample Data", labels, datasets, insight);
    }

    private Map<String, Integer> analyzeCategoricalColumn(StringColumn column) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        
        for (String value : column) {
            String label = (value == null || value.isBlank()) ? EMPTY_MARKER : value.trim();
            counts.merge(label, 1, Integer::sum);
            
            if (counts.size() > MAX_CATEGORIES) {
                break;
            }
        }
        
        return counts;
    }

    private String generateSummary(String query) {
        if (dataTable == null) {
            return String.format("Query: \"%s\". Upload a CSV file to start analysis.", query);
        }

        return String.format("Query: \"%s\". Analyzed %d rows and %d columns.",
                           query, dataTable.rowCount(), dataTable.columnCount());
    }

    // ============== Enhanced Query Processing ==============

    /**
     * QueryAnalysis holds the results of natural language query analysis.
     */
    private static class QueryAnalysis {
        String intent; // "distribution", "trend", "comparison", "summary", "count", etc.
        String targetColumn; // specific column mentioned, or null for auto-detect
        String aggregation; // "count", "sum", "average", "min", "max"
        boolean isTemporal; // whether query involves time/date data
        boolean isComparative; // whether comparing multiple things

        QueryAnalysis(String intent) {
            this.intent = intent;
        }
    }

    /**
     * Analyze natural language query to understand user intent.
     */
    private QueryAnalysis analyzeQueryIntent(String query) {
        String lowerQuery = query.toLowerCase();

        QueryAnalysis analysis = new QueryAnalysis("summary");

        // Detect intent keywords
        if (lowerQuery.contains("distribution") || lowerQuery.contains("breakdown") ||
            lowerQuery.contains("proportion") || lowerQuery.contains("percentage") ||
            lowerQuery.contains("split") || lowerQuery.contains("divide")) {
            analysis.intent = "distribution";
        } else if (lowerQuery.contains("trend") || lowerQuery.contains("over time") ||
                   lowerQuery.contains("change") || lowerQuery.contains("evolution") ||
                   lowerQuery.contains("progress") || lowerQuery.contains("growth")) {
            analysis.intent = "trend";
            analysis.isTemporal = true;
        } else if (lowerQuery.contains("compare") || lowerQuery.contains("comparison") ||
                   lowerQuery.contains("versus") || lowerQuery.contains("vs") ||
                   lowerQuery.contains("difference") || lowerQuery.contains("better")) {
            analysis.intent = "comparison";
            analysis.isComparative = true;
        } else if (lowerQuery.contains("count") || lowerQuery.contains("how many") ||
                   lowerQuery.contains("number of") || lowerQuery.contains("quantity")) {
            analysis.intent = "count";
        } else if (lowerQuery.contains("average") || lowerQuery.contains("mean") ||
                   lowerQuery.contains("avg")) {
            analysis.intent = "average";
            analysis.aggregation = "average";
        } else if (lowerQuery.contains("sum") || lowerQuery.contains("total") ||
                   lowerQuery.contains("amount")) {
            analysis.intent = "sum";
            analysis.aggregation = "sum";
        } else if (lowerQuery.contains("maximum") || lowerQuery.contains("max") ||
                   lowerQuery.contains("highest") || lowerQuery.contains("largest")) {
            analysis.intent = "maximum";
            analysis.aggregation = "max";
        } else if (lowerQuery.contains("minimum") || lowerQuery.contains("min") ||
                   lowerQuery.contains("lowest") || lowerQuery.contains("smallest")) {
            analysis.intent = "minimum";
            analysis.aggregation = "min";
        }

        // Try to identify target column from query
        analysis.targetColumn = identifyTargetColumn(lowerQuery);

        logger.debug("Query analysis - Intent: {}, Column: {}, Aggregation: {}",
                    analysis.intent, analysis.targetColumn, analysis.aggregation);

        return analysis;
    }

    /**
     * Identify which column the user is asking about.
     */
    private String identifyTargetColumn(String query) {
        if (dataTable == null) return null;

        // Look for column names mentioned in the query
        for (String columnName : dataTable.columnNames()) {
            if (query.contains(columnName.toLowerCase())) {
                return columnName;
            }
        }

        // Look for common data types
        if (query.contains("age") || query.contains("year")) {
            for (String columnName : dataTable.columnNames()) {
                if (columnName.toLowerCase().contains("age") ||
                    columnName.toLowerCase().contains("year") ||
                    columnName.toLowerCase().contains("date")) {
                    return columnName;
                }
            }
        }

        if (query.contains("price") || query.contains("cost") || query.contains("amount")) {
            for (String columnName : dataTable.columnNames()) {
                if (columnName.toLowerCase().contains("price") ||
                    columnName.toLowerCase().contains("cost") ||
                    columnName.toLowerCase().contains("amount") ||
                    columnName.toLowerCase().contains("value")) {
                    return columnName;
                }
            }
        }

        if (query.contains("name") || query.contains("category") || query.contains("type")) {
            for (String columnName : dataTable.columnNames()) {
                Column<?> column = dataTable.column(columnName);
                if (column instanceof StringColumn) {
                    return columnName;
                }
            }
        }

        return null; // Auto-detect
    }

    /**
     * Generate chart based on analyzed query intent.
     */
    private ChartData generateChartForIntent(QueryAnalysis analysis) {
        if (dataTable == null) {
            return generateSampleChart("No data available");
        }

        switch (analysis.intent) {
            case "distribution":
                return generateDistributionChart(analysis.targetColumn);
            case "trend":
                return generateTrendChart(analysis.targetColumn);
            case "comparison":
                return generateComparisonChart(analysis.targetColumn);
            case "count":
                return generateCountChart(analysis.targetColumn);
            case "average":
            case "sum":
            case "maximum":
            case "minimum":
                return generateAggregationChart(analysis.targetColumn, analysis.aggregation);
            default:
                return generateAutoChart(analysis.targetColumn);
        }
    }

    /**
     * Generate intelligent summary based on query and analysis.
     */
    private String generateIntelligentSummary(String query, QueryAnalysis analysis) {
        if (dataTable == null) {
            return String.format("Query: \"%s\". Please upload a CSV file to analyze your data.", query);
        }

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Query: \"%s\". ", query));

        switch (analysis.intent) {
            case "distribution":
                summary.append("Here's the distribution breakdown of your data.");
                break;
            case "trend":
                summary.append("Showing trends and changes over time in your dataset.");
                break;
            case "comparison":
                summary.append("Comparing different categories or values in your data.");
                break;
            case "count":
                summary.append("Counting occurrences and frequencies in your dataset.");
                break;
            case "average":
                summary.append("Calculating average values across your data.");
                break;
            case "sum":
                summary.append("Computing total sums and aggregates from your data.");
                break;
            case "maximum":
            case "minimum":
                summary.append("Finding extreme values in your dataset.");
                break;
            default:
                summary.append("Analyzing your data to provide insights.");
        }

        summary.append(String.format(" Dataset contains %d rows and %d columns.",
                                   dataTable.rowCount(), dataTable.columnCount()));

        return summary.toString();
    }

    /**
     * Check if query contains inappropriate content.
     */
    private boolean containsInappropriateContent(String query) {
        if (query == null || query.isEmpty()) return false;

        String lowerQuery = query.toLowerCase();

        // List of inappropriate keywords and phrases
        String[] inappropriateTerms = {
            // Profanity
            "fuck", "shit", "damn", "bitch", "asshole", "bastard", "crap",
            // Sexual content
            "sex", "porn", "nude", "naked", "erotic", "adult", "xxx",
            // Violence
            "kill", "murder", "rape", "abuse", "harm", "hurt", "attack",
            // Hate speech
            "racist", "nigger", "faggot", "cunt", "whore", "slut",
            // Illegal activities
            "drugs", "cocaine", "heroin", "meth", "weed", "marijuana",
            "hack", "exploit", "virus", "malware", "trojan",
            // Personal attacks
            "stupid", "idiot", "dumb", "retard", "moron"
        };

        for (String term : inappropriateTerms) {
            if (lowerQuery.contains(term)) {
                return true;
            }
        }

        // Check for excessive repetition (potential spam)
        String[] words = query.split("\\s+");
        if (words.length > 50) {
            return true; // Too long, likely spam
        }

        // Check for all caps (shouting)
        if (query.equals(query.toUpperCase()) && query.length() > 10) {
            return true;
        }

        return false;
    }

    // ============== Intent-Specific Chart Generation ==============

    private ChartData generateDistributionChart(String targetColumn) {
        if (targetColumn != null) {
            Column<?> column = dataTable.column(targetColumn);
            if (column instanceof StringColumn stringColumn) {
                Map<String, Integer> distribution = analyzeCategoricalColumn(stringColumn);
                if (!distribution.isEmpty()) {
                    List<String> labels = new ArrayList<>(distribution.keySet());
                    List<Integer> values = new ArrayList<>(distribution.values());
                    List<DatasetConfig> datasets = List.of(new DatasetConfig("Count", values));

                    String insight = String.format("Distribution of %s across %d categories. The most common value appears %d times.",
                                                 targetColumn, distribution.size(),
                                                 values.stream().mapToInt(Integer::intValue).max().orElse(0));

                    return new ChartData("pie", "Distribution: " + targetColumn, labels, datasets, insight);
                }
            }
        }

        // Fallback to auto-detection
        return generateAutoChart(targetColumn);
    }

    private ChartData generateTrendChart(String targetColumn) {
        // Look for numeric columns for trends
        for (String columnName : dataTable.columnNames()) {
            Column<?> column = dataTable.column(columnName);
            if (column instanceof NumericColumn<?>) {
                return generateNumericTrendChart(columnName, (NumericColumn<?>) column);
            }
        }

        return generateSampleChart("No numeric data for trend analysis");
    }

    private ChartData generateComparisonChart(String targetColumn) {
        if (targetColumn != null) {
            Column<?> column = dataTable.column(targetColumn);
            if (column instanceof StringColumn stringColumn) {
                Map<String, Integer> distribution = analyzeCategoricalColumn(stringColumn);
                if (distribution.size() > 1) {
                    List<String> labels = new ArrayList<>(distribution.keySet());
                    List<Integer> values = new ArrayList<>(distribution.values());
                    List<DatasetConfig> datasets = List.of(new DatasetConfig("Count", values));

                    String insight = String.format("Comparing %d different categories in %s. Values range from %d to %d.",
                                                 distribution.size(), targetColumn,
                                                 values.stream().mapToInt(Integer::intValue).min().orElse(0),
                                                 values.stream().mapToInt(Integer::intValue).max().orElse(0));

                    return new ChartData("bar", "Comparison: " + targetColumn, labels, datasets, insight);
                }
            }
        }

        return generateAutoChart(targetColumn);
    }

    private ChartData generateCountChart(String targetColumn) {
        return generateDistributionChart(targetColumn); // Count is essentially distribution
    }

    private ChartData generateAggregationChart(String targetColumn, String aggregation) {
        if (targetColumn != null) {
            Column<?> column = dataTable.column(targetColumn);
            if (column instanceof NumericColumn<?> numericColumn) {
                double result = 0;
                String operation = "";

                switch (aggregation) {
                    case "average":
                        result = numericColumn.mean();
                        operation = "Average";
                        break;
                    case "sum":
                        result = numericColumn.sum();
                        operation = "Total Sum";
                        break;
                    case "max":
                        result = numericColumn.max();
                        operation = "Maximum";
                        break;
                    case "min":
                        result = numericColumn.min();
                        operation = "Minimum";
                        break;
                }

                List<String> labels = List.of(operation);
                List<Integer> values = List.of((int) result);
                List<DatasetConfig> datasets = List.of(new DatasetConfig(targetColumn, values));

                String insight = String.format("%s of %s is %.2f across %d data points.",
                                             operation, targetColumn, result, numericColumn.size());

                return new ChartData("bar", operation + ": " + targetColumn, labels, datasets, insight);
            }
        }

        return generateAutoChart(targetColumn);
    }

    private ChartData generateAutoChart(String targetColumn) {
        // Enhanced auto-detection with better logic
        if (targetColumn != null) {
            Column<?> column = dataTable.column(targetColumn);
            if (column instanceof StringColumn stringColumn) {
                return generateDistributionChart(targetColumn);
            } else if (column instanceof NumericColumn<?> numericColumn) {
                return generateAggregationChart(targetColumn, "average");
            }
        }

        // Fallback to first suitable column
        return analyzeAndGenerateChart("auto");
    }

    private List<OverviewStat> generateDashboardStatistics() {
        List<OverviewStat> stats = new ArrayList<>();
        
        if (dataTable != null) {
            stats.add(new OverviewStat("Total Rows", String.valueOf(dataTable.rowCount()), "0%"));
            stats.add(new OverviewStat("Total Columns", String.valueOf(dataTable.columnCount()), "0%"));
            stats.add(new OverviewStat("Queries Processed", String.valueOf(queryCount), String.format("+%d", queryCount)));
        } else {
            stats.add(new OverviewStat("Status", "No Data", "Pending"));
        }
        
        return stats;
    }

    private List<String> generateInsights() {
        List<String> insights = new ArrayList<>();
        
        insights.add("📊 Dashboard ready for analysis");
        
        if (dataTable != null) {
            insights.add("✓ Data loaded and ready for queries");
            insights.add("💡 Ask questions about your data");
            insights.add("📈 Visual summaries update after upload");
        } else {
            insights.add("📤 Upload a CSV file to get started");
            insights.add("🤖 AI-powered data analysis");
        }
        
        insights.add("🔐 Your data stays private");
        
        return insights;
    }

    private List<ChartData> generateDashboardCharts() {
        List<ChartData> charts = new ArrayList<>();

        if (dataTable == null) {
            charts.add(generateSampleChart("Dashboard sample"));
            return charts;
        }

        List<String> categories = collectCategoricalColumns();
        for (String columnName : categories) {
            if (charts.size() >= 3) {
                break;
            }
            charts.add(generateCategoricalChart(columnName, "pie"));
            if (charts.size() >= 3) {
                break;
            }
            charts.add(generateCategoricalChart(columnName, "bar"));
        }

        if (charts.size() < 2) {
            ChartData numericTrend = generateFirstNumericTrendChart();
            if (numericTrend != null) {
                charts.add(numericTrend);
            }
        }

        if (charts.isEmpty()) {
            charts.add(generateSampleChart("Dashboard sample"));
        }

        return charts;
    }

    private List<String> collectCategoricalColumns() {
        List<String> categories = new ArrayList<>();

        if (dataTable == null) {
            return categories;
        }

        for (String columnName : dataTable.columnNames()) {
            Column<?> column = dataTable.column(columnName);
            if (column instanceof StringColumn stringColumn) {
                Map<String, Integer> distribution = analyzeCategoricalColumn(stringColumn);
                if (distribution.size() > 1 && distribution.size() <= MAX_CATEGORIES) {
                    categories.add(columnName);
                }
            }
        }

        return categories;
    }

    private ChartData generateCategoricalChart(String columnName, String chartType) {
        Column<?> column = dataTable.column(columnName);
        if (!(column instanceof StringColumn stringColumn)) {
            return generateSummaryChart("Category chart");
        }

        Map<String, Integer> distribution = analyzeCategoricalColumn(stringColumn);
        List<String> labels = new ArrayList<>(distribution.keySet());
        List<Integer> values = new ArrayList<>(distribution.values());
        List<DatasetConfig> datasets = List.of(new DatasetConfig("Count", values));

        if (chartType.equals("pie")) {
            return new ChartData(
                    "pie",
                    String.format("Category distribution for %s", columnName),
                    labels,
                    datasets,
                    String.format("This pie chart highlights the distribution of %s across %d categories.", columnName, labels.size())
            );
        }

        return new ChartData(
                "bar",
                String.format("Category comparison for %s", columnName),
                labels,
                datasets,
                String.format("This bar chart compares category frequencies for %s.", columnName)
        );
    }

    private ChartData generateFirstNumericTrendChart() {
        for (String columnName : dataTable.columnNames()) {
            Column<?> column = dataTable.column(columnName);
            if (column instanceof NumericColumn<?>) {
                return generateNumericTrendChart(columnName, (NumericColumn<?>) column);
            }
        }
        return null;
    }
}
