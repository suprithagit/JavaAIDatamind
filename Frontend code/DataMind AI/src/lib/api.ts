import type { ChartData } from '@/store/useAppStore';

export type ChatResponse = {
  summary: string;
  chartData?: ChartData;
};

export type OverviewStat = {
  label: string;
  value: string;
  change?: string;
};

export type DashboardOverview = {
  summary?: string;
  stats?: OverviewStat[];
  insights?: string[];
};

export function getApiBaseUrl(): string {
  const raw = import.meta.env.VITE_API_BASE_URL;
  return typeof raw === 'string' ? raw.replace(/\/+$|^\s+|\s+$/g, '') : '';
}

function buildUrl(path: string): string {
  const baseUrl = getApiBaseUrl();
  if (!baseUrl) {
    throw new Error('VITE_API_BASE_URL is not configured. Set it in your .env file.');
  }
  return `${baseUrl.replace(/\/$/, '')}/${path.replace(/^\//, '')}`;
}

export async function queryBackend(query: string, sessionId?: string, messageId?: string): Promise<ChatResponse> {
  const requestBody: any = { query };
  if (sessionId) requestBody.sessionId = sessionId;
  if (messageId) requestBody.messageId = messageId;

  const response = await fetch(buildUrl('/api/query'), {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(requestBody),
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Backend request failed with status ${response.status}`);
  }

  return response.json();
}

export async function uploadCsv(file: File): Promise<string> {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch(buildUrl('/api/upload'), {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Upload failed with status ${response.status}`);
  }

  const result = await response.json();
  if (typeof result === 'string') {
    return result;
  }
  return result?.message ?? JSON.stringify(result);
}

export async function fetchDashboardOverview(): Promise<DashboardOverview> {
  const response = await fetch(buildUrl('/api/dashboard'));

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Dashboard request failed with status ${response.status}`);
  }

  return response.json();
}
