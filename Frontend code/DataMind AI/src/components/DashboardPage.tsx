import { useEffect, useState } from 'react';
import { fetchDashboardOverview, getApiBaseUrl } from '@/lib/api';
import { InsightCard } from '@/components/InsightCard';
import { DynamicChart } from '@/components/DynamicChart';

type OverviewStat = {
  label: string;
  value: string;
  change?: string;
};

type DashboardOverview = {
  summary?: string;
  stats?: OverviewStat[];
  insights?: string[];
  charts?: import('@/store/useAppStore').ChartData[];
};

export function DashboardPage() {
  const [overview, setOverview] = useState<DashboardOverview | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const apiBaseUrl = getApiBaseUrl();

  useEffect(() => {
    if (!apiBaseUrl) return;

    const fetchOverview = async () => {
      setLoading(true);
      setError('');

      try {
        const data = await fetchDashboardOverview();
        setOverview(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unable to load dashboard overview.');
      } finally {
        setLoading(false);
      }
    };

    fetchOverview();
  }, [apiBaseUrl]);

  return (
    <div className="flex-1 p-4 md:p-8 overflow-y-auto pb-24 md:pb-8">
      <div className="max-w-6xl mx-auto space-y-6">
        <div className="animate-fade-in">
          <h1 className="text-2xl font-bold text-foreground">Dashboard</h1>
          <p className="text-sm text-muted-foreground mt-1">
            This page is ready to render analytics from your backend when the API is available.
          </p>
        </div>

        {!apiBaseUrl ? (
          <div className="glass rounded-xl p-8 text-center">
            <p className="text-base text-foreground font-medium mb-2">Backend base URL is not configured.</p>
            <p className="text-sm text-muted-foreground">
              Add <code className="rounded bg-muted px-1 py-0.5">VITE_API_BASE_URL</code> to your <code className="rounded bg-muted px-1 py-0.5">.env</code> file and reload the app.
            </p>
          </div>
        ) : loading ? (
          <div className="glass rounded-xl p-8 text-center">
            <p className="text-base text-foreground font-medium">Loading dashboard overview…</p>
          </div>
        ) : error ? (
          <div className="glass rounded-xl p-8 text-center text-destructive">
            <p className="text-base font-semibold">Unable to load dashboard data</p>
            <p className="text-sm text-muted-foreground mt-2">{error}</p>
          </div>
        ) : !overview ? (
          <div className="glass rounded-xl p-8 text-center">
            <p className="text-base text-foreground font-medium">No dashboard data available yet.</p>
            <p className="text-sm text-muted-foreground mt-2">
              Start by uploading data and using the analytics view.
            </p>
          </div>
        ) : (
          <div className="space-y-6">
            {overview.summary && (
              <div className="glass rounded-xl p-6">
                <h2 className="text-lg font-semibold text-foreground mb-3">Overview</h2>
                <p className="text-sm text-muted-foreground">{overview.summary}</p>
              </div>
            )}

            {overview.stats?.length ? (
              <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
                {overview.stats.map((stat) => (
                  <div key={stat.label} className="glass rounded-xl p-4">
                    <p className="text-xs uppercase tracking-[0.16em] text-muted-foreground">{stat.label}</p>
                    <p className="text-2xl font-semibold text-foreground mt-3">{stat.value}</p>
                    {stat.change && <p className="text-sm text-success mt-1">{stat.change}</p>}
                  </div>
                ))}
              </div>
            ) : null}

            {overview.charts?.length ? (
              <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
                {overview.charts.map((chart, index) => (
                  <div key={chart.title ?? index} className="glass rounded-xl p-5">
                    <div className="mb-4">
                      <h3 className="text-base font-semibold text-foreground">{chart.title}</h3>
                      {chart.insight ? (
                        <p className="text-sm text-muted-foreground mt-1">{chart.insight}</p>
                      ) : null}
                    </div>
                    <DynamicChart data={chart} />
                  </div>
                ))}
              </div>
            ) : null}

            {overview.insights?.length ? (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                {overview.insights.map((insight, index) => (
                  <div key={index} className="glass rounded-xl p-5">
                    <InsightCard summary={insight} />
                  </div>
                ))}
              </div>
            ) : null}
          </div>
        )}
      </div>
    </div>
  );
}
