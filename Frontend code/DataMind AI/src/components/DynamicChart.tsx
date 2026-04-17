import { useMemo } from 'react';
import {
  BarChart, Bar, LineChart, Line, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend,
} from 'recharts';
import type { ChartData } from '@/store/useAppStore';

/**
 * Accessible color palette following WCAG contrast guidelines.
 * Each color has sufficient contrast for visibility and colorblind safety.
 */
const ACCESSIBLE_COLORS = [
  'hsl(217, 91%, 60%)',    // Blue
  'hsl(160, 84%, 39%)',    // Green
  'hsl(43, 96%, 56%)',     // Yellow
  'hsl(291, 64%, 52%)',    // Purple
  'hsl(0, 84%, 60%)',      // Red
  'hsl(190, 80%, 50%)',    // Cyan
  'hsl(25, 100%, 50%)',    // Orange
  'hsl(340, 75%, 55%)',    // Pink
];

interface DynamicChartProps {
  data: ChartData;
}

/**
 * DynamicChart Component - Accessible Multi-Type Chart Renderer
 * 
 * Renders different chart types (bar, line, pie, doughnut, summary) based on data.
 * Implements WCAG 2.1 accessibility standards with ARIA labels and descriptions.
 * 
 * @param data ChartData object with type, labels, datasets, and insight
 */
export function DynamicChart({ data }: DynamicChartProps) {
  const chartType = useMemo(() => {
    return (data as any).type || (data as any).chartType || 'bar';
  }, [data]);

  // Transform datasets to chart-compatible format (supports multiple series)
  const transformedChartData = useMemo(() => {
    return data.labels.map((label, i) => {
      const point: Record<string, unknown> = { name: label };
      
      // Support both old format (array of numbers) and new format (DatasetConfig array)
      if (Array.isArray(data.datasets) && data.datasets.length > 0) {
        if (typeof data.datasets[0] === 'number') {
          // Backward compatibility: old format with array of numbers
          point.value = (data.datasets as number[])[i] || 0;
        } else {
          // New format: array of DatasetConfig objects
          const datasets = data.datasets as any[];
          datasets.forEach((dataset) => {
            if (dataset.label && Array.isArray(dataset.data)) {
              point[dataset.label] = dataset.data[i] || 0;
            }
          });
        }
      }
      return point;
    });
  }, [data]);

  // Generate unique ID for accessibility
  const chartId = useMemo(() => `chart-${Math.random().toString(36).substr(2, 9)}`, []);

  // Common styling for chart components
  const commonProps = {
    style: { fontSize: '12px' },
  };

  // Tooltip styling for accessibility and dark/light mode support
  const tooltipStyle = {
    backgroundColor: 'hsl(var(--card))',
    border: '1px solid hsl(var(--border))',
    borderRadius: '8px',
    color: 'hsl(var(--foreground))',
    padding: '8px 12px',
  };

  /**
   * Render pie chart with accessibility features
   */
  if (chartType === 'pie') {
    return (
      <div
        role="figure"
        aria-labelledby={`${chartId}-title`}
        aria-describedby={`${chartId}-description`}
        className="w-full"
      >
        <ResponsiveContainer width="100%" height={300}>
          <PieChart>
            <Pie
              data={transformedChartData}
              cx="50%"
              cy="50%"
              innerRadius={60}
              outerRadius={110}
              paddingAngle={3}
              dataKey={getFirstDataKey(data.datasets)}
              stroke="none"
            >
              {transformedChartData.map((_, i) => (
                <Cell 
                  key={`cell-${i}`} 
                  fill={ACCESSIBLE_COLORS[i % ACCESSIBLE_COLORS.length]}
                  aria-label={`${data.labels[i]}`}
                />
              ))}
            </Pie>
            <Tooltip
              contentStyle={tooltipStyle}
              formatter={(value) => `${value}`}
              labelFormatter={(label) => `Category: ${label}`}
            />
            <Legend 
              {...commonProps}
              wrapperStyle={{ paddingTop: '16px' }}
            />
          </PieChart>
        </ResponsiveContainer>
        <div className="sr-only" id={`${chartId}-title`}>
          {data.title || 'Distribution Chart'}
        </div>
        <div className="sr-only" id={`${chartId}-description`}>
          {data.insight || 'Interactive pie chart visualization'}
        </div>
      </div>
    );
  }

  /**
   * Render line chart with accessibility features
   */
  if (chartType === 'line') {
    const dataKeyName = getFirstDataKey(data.datasets);
    return (
      <div
        role="figure"
        aria-labelledby={`${chartId}-title`}
        aria-describedby={`${chartId}-description`}
        className="w-full"
      >
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={transformedChartData}>
            <CartesianGrid 
              strokeDasharray="3 3" 
              stroke="hsl(var(--border))"
              aria-hidden="true"
            />
            <XAxis 
              dataKey="name" 
              stroke="hsl(var(--muted-foreground))" 
              {...commonProps}
              aria-label="X-axis"
            />
            <YAxis 
              stroke="hsl(var(--muted-foreground))" 
              {...commonProps}
              aria-label="Y-axis"
            />
            <Tooltip
              contentStyle={tooltipStyle}
              formatter={(value) => `${value}`}
              labelFormatter={(label) => `Point: ${label}`}
            />
            <Line
              type="monotone"
              dataKey={dataKeyName}
              stroke={ACCESSIBLE_COLORS[0]}
              strokeWidth={2.5}
              dot={{ fill: ACCESSIBLE_COLORS[0], r: 4 }}
              activeDot={{ r: 6 }}
              name={typeof data.datasets[0] === 'object' ? data.datasets[0].label : 'Value'}
            />
            <Legend 
              {...commonProps}
              wrapperStyle={{ paddingTop: '16px' }}
            />
          </LineChart>
        </ResponsiveContainer>
        <div className="sr-only" id={`${chartId}-title`}>
          {data.title || 'Trend Chart'}
        </div>
        <div className="sr-only" id={`${chartId}-description`}>
          {data.insight || 'Interactive line chart showing trend data'}
        </div>
      </div>
    );
  }

  /**
   * Render bar chart with accessibility features (default)
   */
  if (chartType === 'bar') {
    const dataKeyName = getFirstDataKey(data.datasets);
    return (
      <div
        role="figure"
        aria-labelledby={`${chartId}-title`}
        aria-describedby={`${chartId}-description`}
        className="w-full"
      >
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={transformedChartData}>
            <CartesianGrid 
              strokeDasharray="3 3" 
              stroke="hsl(var(--border))"
              aria-hidden="true"
            />
            <XAxis 
              dataKey="name" 
              stroke="hsl(var(--muted-foreground))" 
              {...commonProps}
              aria-label="Categories"
            />
            <YAxis 
              stroke="hsl(var(--muted-foreground))" 
              {...commonProps}
              aria-label="Values"
            />
            <Tooltip
              contentStyle={tooltipStyle}
              formatter={(value) => `${value}`}
              labelFormatter={(label) => `${label}`}
            />
            <Bar 
              dataKey={dataKeyName} 
              radius={[6, 6, 0, 0]}
              name={typeof data.datasets[0] === 'object' ? data.datasets[0].label : 'Count'}
            >
              {transformedChartData.map((_, i) => (
                <Cell 
                  key={`bar-${i}`} 
                  fill={ACCESSIBLE_COLORS[i % ACCESSIBLE_COLORS.length]}
                />
              ))}
            </Bar>
            <Legend 
              {...commonProps}
              wrapperStyle={{ paddingTop: '16px' }}
            />
          </BarChart>
        </ResponsiveContainer>
        <div className="sr-only" id={`${chartId}-title`}>
          {data.title || 'Comparison Chart'}
        </div>
        <div className="sr-only" id={`${chartId}-description`}>
          {data.insight || 'Interactive bar chart for data comparison'}
        </div>
      </div>
    );
  }

  /**
   * Render summary card for text-based insights
   */
  if (chartType === 'summary') {
    return (
      <div
        className="bg-card border border-border rounded-lg p-6"
        role="region"
        aria-labelledby={`${chartId}-title`}
        aria-describedby={`${chartId}-description`}
      >
        <h3 
          id={`${chartId}-title`}
          className="text-lg font-semibold text-foreground mb-3"
        >
          {data.title || 'Summary'}
        </h3>
        <p 
          id={`${chartId}-description`}
          className="text-muted-foreground text-sm leading-relaxed"
        >
          {data.insight || 'No summary data available'}
        </p>
        <div className="mt-4 grid grid-cols-2 gap-2">
          {data.labels.map((label, i) => (
            <div 
              key={`stat-${i}`}
              className="bg-muted p-3 rounded text-center"
            >
              <div className="text-xs text-muted-foreground">{label}</div>
              <div className="text-lg font-semibold text-foreground">
                {typeof data.datasets[0] === 'object' 
                  ? data.datasets[0].data?.[i] ?? 'N/A'
                  : (data.datasets as number[])[i] ?? 'N/A'}
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  /**
   * Fallback: render as bar chart if type is unknown
   */
  return (
    <div className="w-full h-80 flex items-center justify-center bg-muted rounded-lg">
      <p className="text-muted-foreground">Chart type "{chartType}" not supported</p>
    </div>
  );
}

/**
 * Helper function to extract the first data key from datasets.
 * Supports both old format (array of numbers) and new format (DatasetConfig array).
 * 
 * @param datasets Array of numbers or DatasetConfig objects
 * @returns The first available data key or 'value'
 */
function getFirstDataKey(datasets: any[]): string {
  if (!datasets || datasets.length === 0) {
    return 'value';
  }

  // Check if it's the new format (array of objects with 'label' and 'data')
  if (typeof datasets[0] === 'object' && datasets[0].label) {
    return datasets[0].label;
  }

  // Default to 'value' for backward compatibility
  return 'value';
}
