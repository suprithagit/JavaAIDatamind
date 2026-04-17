import { create } from 'zustand';
import { ChatHistoryService } from '@/lib/supabase';

export interface FileMetadata {
  name: string;
  size: number;
  rows: number;
  columns: string[];
  preview: Record<string, string | number>[];
  uploadedAt: Date;
}

/**
 * Dataset configuration for charts.
 * Represents a single data series with label and values.
 */
export interface DatasetConfig {
  label: string;
  data: number[];
}

/**
 * Enhanced ChartData interface supporting WCAG 2.1 accessibility standards.
 * Supports multiple chart types: bar, line, pie, doughnut, summary.
 * 
 * Backward compatible with legacy format where datasets is an array of numbers.
 */
export interface ChartData {
  // Chart type for visualization selection
  type: 'bar' | 'line' | 'pie' | 'doughnut' | 'summary';
  
  // Human-readable chart title (required for accessibility)
  title?: string;
  
  // Category labels for X-axis or legend
  labels: string[];
  
  // Dataset values - supports both old (array of numbers) and new (array of DatasetConfig) formats
  datasets: number[] | DatasetConfig[];
  
  // Professional insight/summary text (accessibility requirement)
  insight?: string;
  
  // Deprecated: use 'insight' instead
  summary?: string;
}

export interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  chartData?: ChartData;
  timestamp: Date;
  isThinking?: boolean;
}

interface AppState {
  // Theme
  theme: 'light' | 'dark';
  toggleTheme: () => void;

  // File
  file: FileMetadata | null;
  setFile: (file: FileMetadata | null) => void;

  // Chat
  messages: ChatMessage[];
  addMessage: (msg: ChatMessage) => void;
  setMessages: (msgs: ChatMessage[]) => void;
  chatSessionId: string;
  setChatSessionId: (sessionId: string) => void;
  newChatSession: () => void;

  // Navigation
  activePage: 'upload' | 'dashboard' | 'chat';
  setActivePage: (page: 'upload' | 'dashboard' | 'chat') => void;
}

export const useAppStore = create<AppState>((set) => ({
  theme: 'dark',
  toggleTheme: () =>
    set((s) => {
      const next = s.theme === 'dark' ? 'light' : 'dark';
      document.documentElement.classList.toggle('dark', next === 'dark');
      return { theme: next };
    }),

  file: null,
  setFile: (file) => set({ file }),

  messages: [],
  addMessage: (msg) => set((s) => ({ messages: [...s.messages, msg] })),
  setMessages: (msgs) => set({ messages: msgs }),

  chatSessionId: typeof window !== 'undefined' && window.localStorage.getItem('datamind-chat-session-id')?.trim()
    ? window.localStorage.getItem('datamind-chat-session-id') as string
    : ChatHistoryService.generateSessionId(),
  setChatSessionId: (chatSessionId) => {
    if (typeof window !== 'undefined') {
      window.localStorage.setItem('datamind-chat-session-id', chatSessionId);
    }
    set({ chatSessionId });
  },
  newChatSession: () => {
    const newId = ChatHistoryService.generateSessionId();
    if (typeof window !== 'undefined') {
      window.localStorage.setItem('datamind-chat-session-id', newId);
    }
    set({ chatSessionId: newId, messages: [] });
  },

  activePage: 'upload',
  setActivePage: (page) => set({ activePage: page }),
}));
