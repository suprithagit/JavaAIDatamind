# DataMind AI - REST API Documentation

**Version:** 2.0  
**Last Updated:** 2026-04-16  
**Base URL:** `<backend-url>/api`  
*Use the backend host configured by `VITE_API_BASE_URL` in the frontend and `app.frontend-url` in the backend.*

---

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Endpoints](#endpoints)
4. [Response Formats](#response-formats)
5. [Error Handling](#error-handling)
6. [Examples](#examples)
7. [Accessibility Standards](#accessibility-standards)

---

## Overview

DataMind AI provides a REST API for uploading CSV files, querying data using natural language, and retrieving insights through interactive visualizations.

**Key Features:**
- 📤 CSV file upload and parsing
- 🤖 Natural language query processing
- 📊 Automatic chart generation
- ♿ WCAG 2.1 accessibility compliance
- 🔒 Input validation and error handling

---

## Authentication

Currently, no authentication is required. All endpoints are publicly accessible.

**Future Enhancement:** JWT token-based authentication will be implemented in v3.0.

---

## Endpoints

### 1. Upload CSV File

**Endpoint:** `POST /api/upload`

**Description:** Upload and parse a CSV file for data analysis.

**Request:**
```
Content-Type: multipart/form-data

Parameters:
- file (required): CSV file (max 50 MB)
```

**Success Response (200):**
```json
{
  "message": "CSV uploaded successfully: 'sales_data.csv' parsed and ready for analysis."
}
```

**Error Responses:**
- `400 Bad Request`: File is empty or missing / Invalid file extension
- `413 Payload Too Large`: File exceeds 50 MB limit
- `415 Unsupported Media Type`: Invalid file type (not CSV)
- `500 Internal Server Error`: Error processing file

**Example (cURL):**
```bash
curl -X POST <backend-url>/api/upload \
  -F "file=@sales_data.csv"
```

**Example (JavaScript):**
```javascript
const formData = new FormData();
formData.append('file', csvFile);

const apiBaseUrl = '<backend-url>';
const response = await fetch(`${apiBaseUrl}/api/upload`, {
  method: 'POST',
  body: formData
});

const result = await response.json();
console.log(result.message);
```

---

### 2. Process Query

**Endpoint:** `POST /api/query`

**Description:** Process a natural language query and return analysis with chart visualization.

**Request:**
```json
{
  "query": "Show me sales by region"
}
```

**Request Body Schema:**
```
{
  "query": string (required, 3-500 characters)
}
```

**Success Response (200):**
```json
{
  "summary": "Query: \"Show me sales by region\". Analysis of dataset with 1000 rows and 15 columns completed.",
  "chartData": {
    "chartType": "bar",
    "title": "Sales by Region",
    "labels": ["North", "South", "East", "West"],
    "datasets": [
      {
        "label": "Revenue",
        "data": [45000, 38000, 52000, 41000]
      }
    ],
    "insight": "The Eastern region leads with $52,000 in revenue, followed by the Northern region at $45,000."
  }
}
```

**Response Schema:**
```typescript
{
  summary: string,
  chartData: {
    type: 'bar' | 'line' | 'pie' | 'doughnut' | 'summary',
    title: string,
    labels: string[],
    datasets: Array<{
      label: string,
      data: number[]
    }>,
    insight: string
  }
}
```

**Error Responses:**
- `400 Bad Request`: Query is empty or invalid
- `429 Too Many Requests`: API query limit exceeded (max queries per hour)
- `500 Internal Server Error`: Error processing query

**Chart Types:**
- **bar**: For comparing categories or values
- **line**: For trends over time
- **pie**: For parts-of-a-whole comparisons
- **doughnut**: For hierarchical parts-of-a-whole
- **summary**: For text-based insights

**Example (JavaScript):**
```javascript
const apiBaseUrl = '<backend-url>';
const response = await fetch(`${apiBaseUrl}/api/query`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    query: 'What are the top performing products?'
  })
});

const result = await response.json();
console.log(result.summary);
console.log(result.chartData);
```

---

### 3. Get Dashboard Overview

**Endpoint:** `GET /api/dashboard`

**Description:** Retrieve high-level dashboard statistics and insights without requiring a specific query.

**Request:**
```
GET /api/dashboard
```

**Success Response (200):**
```json
{
  "summary": "Data Overview: 1000 rows × 15 columns",
  "stats": [
    {
      "label": "Total Rows",
      "value": "1000",
      "change": "0%"
    },
    {
      "label": "Total Columns",
      "value": "15",
      "change": "0%"
    },
    {
      "label": "Queries Processed",
      "value": "42",
      "change": "+42"
    }
  ],
  "insights": [
    "📊 Dashboard ready for analysis",
    "✓ Data loaded and ready for queries",
    "💡 Ask questions about your data in natural language",
    "🔐 Your data stays private on this server"
  ]
}
```

**Response Schema:**
```typescript
{
  summary: string,
  stats: Array<{
    label: string,
    value: string,
    change: string
  }>,
  insights: string[]
}
```

**Error Responses:**
- `500 Internal Server Error`: Error retrieving dashboard

**Example (JavaScript):**
```javascript
const apiBaseUrl = '<backend-url>';
const response = await fetch(`${apiBaseUrl}/api/dashboard`);
const dashboard = await response.json();

dashboard.stats.forEach(stat => {
  console.log(`${stat.label}: ${stat.value} (${stat.change})`);
});
```

---

### 4. Get Application Configuration

**Endpoint:** `GET /api/config`

**Description:** Retrieve current application configuration and settings.

**Request:**
```
GET /api/config
```

**Success Response (200):**
```json
{
  "title": "DataMind AI",
  "environment": "production",
  "frontendUrl": "<your-frontend-url>",
  "aiModel": "gemini-pro",
  "aiTemperature": 0.7
}
```

**Response Schema:**
```typescript
{
  title: string,
  environment: string,
  frontendUrl: string,
  aiModel: string,
  aiTemperature: number
}
```

**Error Responses:**
- `500 Internal Server Error`: Error retrieving configuration

**Example (JavaScript):**
```javascript
const apiBaseUrl = '<backend-url>';
const response = await fetch(`${apiBaseUrl}/api/config`);
const config = await response.json();

console.log(`Running: ${config.title} in ${config.environment} mode`);
console.log(`AI Model: ${config.aiModel} (Temperature: ${config.aiTemperature})`);
```

---

## Response Formats

### Success Response Structure

All successful responses follow this structure:

```json
{
  "data": { /* endpoint-specific data */ }
}
```

### Chart Data Object

Charts follow the WCAG 2.1 accessibility standard:

```typescript
{
  "chartType": "bar" | "line" | "pie" | "doughnut" | "summary",
  "title": "Human-readable chart title",
  "labels": ["Category 1", "Category 2", "Category 3"],
  "datasets": [
    {
      "label": "Dataset Series 1",
      "data": [100, 150, 120]
    }
  ],
  "insight": "A professional 2-sentence summary describing the data and its significance."
}
```

---

## Error Handling

### Error Response Format

All error responses return with appropriate HTTP status codes:

```json
{
  "error": "Human-readable error message",
  "statusCode": 400
}
```

### HTTP Status Codes

| Code | Meaning | Solution |
|------|---------|----------|
| 200 | OK | Request successful |
| 400 | Bad Request | Check request format and parameters |
| 413 | Payload Too Large | Reduce file size (max 50 MB) |
| 415 | Unsupported Media Type | Use CSV format only |
| 429 | Too Many Requests | Wait before sending more queries |
| 500 | Internal Server Error | Contact support or try again |

### Common Errors

**Empty Query:**
```json
{
  "error": "Query cannot be empty",
  "statusCode": 400
}
```

**File Too Large:**
```json
{
  "error": "File size exceeds maximum allowed (50 MB)",
  "statusCode": 413
}
```

**Invalid File Type:**
```json
{
  "error": "Only CSV files are allowed. Received: application/pdf",
  "statusCode": 415
}
```

---

## Examples

### Complete Workflow

```javascript
// Step 1: Upload CSV
const uploadFormData = new FormData();
uploadFormData.append('file', csvFile);

const uploadResponse = await fetch('http://localhost:8080/api/upload', {
  method: 'POST',
  body: uploadFormData
});

if (!uploadResponse.ok) {
  console.error('Upload failed:', uploadResponse.status);
  return;
}

console.log('File uploaded successfully');

// Step 2: Get Dashboard Overview
const apiBaseUrl = '<backend-url>';
const dashboardResponse = await fetch(`${apiBaseUrl}/api/dashboard`);
const dashboard = await dashboardResponse.json();

console.log('Dataset size:', dashboard.summary);

// Step 3: Submit Query
const queryResponse = await fetch('http://localhost:8080/api/query', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ query: 'Show me trends by month' })
});

const result = await queryResponse.json();

// Step 4: Display Results
console.log('Analysis:', result.summary);
console.log('Chart Type:', result.chartData.chartType);
console.log('Chart Title:', result.chartData.title);
console.log('Insight:', result.chartData.insight);
```

### Error Handling Example

```javascript
async function queryData(query) {
  try {
    const response = await fetch('http://localhost:8080/api/query', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query })
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(`${error.statusCode}: ${error.error}`);
    }

    return await response.json();

  } catch (error) {
    if (error.message.includes('400')) {
      console.error('Invalid query format');
    } else if (error.message.includes('429')) {
      console.error('Too many requests - please wait');
    } else if (error.message.includes('500')) {
      console.error('Server error - try again later');
    } else {
      console.error('Unexpected error:', error.message);
    }
  }
}
```

---

## Accessibility Standards

### WCAG 2.1 Compliance

All API responses follow WCAG 2.1 Level AA accessibility standards:

**1. Chart Data Includes:**
- **Title**: Human-readable chart title describing content
- **Labels**: Clear, descriptive category names (no cryptic abbreviations)
- **Insight**: Professional 2-sentence summary providing context

**2. Error Messages:**
- Clear, user-friendly descriptions
- Specific guidance on how to resolve
- Not cryptic or technical

**3. Color Contrast:**
- Chart colors meet WCAG AA standards (4.5:1 ratio minimum)
- Colorblind-safe palette

**4. Data Aggregation:**
- Large datasets automatically aggregated
- Maximum 12 categories per chart
- Meaningful rollups provided

### Example Accessible Response

```json
{
  "chartData": {
    "chartType": "bar",
    "title": "Sales Performance by Quarter",
    "labels": [
      "Q1 2026",
      "Q2 2026",
      "Q3 2026",
      "Q4 2026"
    ],
    "datasets": [
      {
        "label": "Revenue (USD)",
        "data": [125000, 142000, 158000, 171000]
      }
    ],
    "insight": "Sales revenue shows consistent growth across all quarters, with Q4 achieving the highest performance at $171,000. This represents a 36.8% increase from Q1 baseline."
  }
}
```

---

## Rate Limiting

**Current Limits:**
- Queries: 100 per hour per client
- File uploads: 10 per hour

**Future Enhancement:** User-based rate limiting will be implemented in v3.0.

---

## CORS Configuration

The API accepts requests from configured frontend origins:

```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, OPTIONS
Access-Control-Allow-Headers: Content-Type
```

Configure frontend URL in `application.properties`:
```properties
app.frontend-url=http://localhost:3000
```

---

## Support & Feedback

For API support or feature requests, contact: **support@datamind-ai.com**

---

## Changelog

### Version 2.0 (2026-04-16)
- ✅ Enhanced error handling with specific HTTP codes
- ✅ Added WCAG 2.1 accessibility compliance
- ✅ Improved chart data structure with insights
- ✅ Better input validation
- ✅ Comprehensive API documentation

### Version 1.0 (Initial Release)
- Basic query and upload endpoints
- Simple chart generation
