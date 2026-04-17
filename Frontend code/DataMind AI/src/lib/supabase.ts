import { createClient } from '@supabase/supabase-js';

// Supabase configuration
const supabaseUrl = import.meta.env.VITE_SUPABASE_URL || 'https://your-project-id.supabase.co';
const supabaseKey = import.meta.env.VITE_SUPABASE_ANON_KEY || 'your-anon-key';

// Create Supabase client
export const supabase = createClient(supabaseUrl, supabaseKey);

// Database table name
export const CHAT_MESSAGES_TABLE = 'chat_messages';

// Chat history service functions
export class ChatHistoryService {
  /**
   * Save a user message to Supabase
   */
  static async saveUserMessage(messageId: string, sessionId: string, content: string): Promise<void> {
    const { error } = await supabase
      .from(CHAT_MESSAGES_TABLE)
      .insert({
        message_id: messageId,
        session_id: sessionId,
        role: 'user',
        content: content,
        timestamp: new Date().toISOString(),
        user_agent: navigator.userAgent,
        ip_address: null // Will be set by backend
      });

    if (error) {
      console.error('Error saving user message:', error);
      throw error;
    }
  }

  /**
   * Save an assistant message to Supabase
   */
  static async saveAssistantMessage(
    messageId: string,
    sessionId: string,
    content: string,
    chartData?: any
  ): Promise<void> {
    const { error } = await supabase
      .from(CHAT_MESSAGES_TABLE)
      .insert({
        message_id: messageId,
        session_id: sessionId,
        role: 'assistant',
        content: content,
        chart_type: chartData?.type || null,
        chart_title: chartData?.title || null,
        chart_insight: chartData?.insight || null,
        timestamp: new Date().toISOString()
      });

    if (error) {
      console.error('Error saving assistant message:', error);
      throw error;
    }
  }

  /**
   * Retrieve chat history for a session
   */
  static async getChatHistory(sessionId: string): Promise<any[]> {
    const { data, error } = await supabase
      .from(CHAT_MESSAGES_TABLE)
      .select('*')
      .eq('session_id', sessionId)
      .order('timestamp', { ascending: true });

    if (error) {
      console.error('Error retrieving chat history:', error);
      throw error;
    }

    return data || [];
  }

  /**
   * Get message count for a session
   */
  static async getMessageCount(sessionId: string): Promise<number> {
    const { count, error } = await supabase
      .from(CHAT_MESSAGES_TABLE)
      .select('*', { count: 'exact', head: true })
      .eq('session_id', sessionId);

    if (error) {
      console.error('Error getting message count:', error);
      return 0;
    }

    return count || 0;
  }

  /**
   * Generate a unique session ID
   */
  static generateSessionId(): string {
    return `session-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Generate a unique message ID
   */
  static generateMessageId(): string {
    return `msg-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }
}