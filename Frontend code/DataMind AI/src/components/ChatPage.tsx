import { useState, useRef, useEffect } from 'react';
import { Send, Bot, User, AlertCircle } from 'lucide-react';
import { useAppStore } from '@/store/useAppStore';
import { queryBackend } from '@/lib/api';
import { ChatHistoryService } from '@/lib/supabase';
import { DynamicChart } from '@/components/DynamicChart';
import { InsightCard } from '@/components/InsightCard';
import { Button } from '@/components/ui/button';

// Empty suggestions - users can enter their own queries
const SUGGESTIONS: string[] = [];

function ThinkingSkeleton() {
  return (
    <div className="flex gap-3 animate-fade-in" role="status" aria-label="Loading response...">
      <div className="h-8 w-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
        <Bot className="h-4 w-4 text-primary" />
      </div>
      <div className="flex-1 space-y-3 py-1">
        <div className="h-3 bg-muted rounded-full w-3/4 animate-pulse-soft" aria-hidden="true" />
        <div 
          className="h-3 bg-muted rounded-full w-1/2 animate-pulse-soft" 
          style={{ animationDelay: '0.2s' }}
          aria-hidden="true"
        />
        <div 
          className="h-40 bg-muted rounded-xl animate-pulse-soft" 
          style={{ animationDelay: '0.4s' }}
          aria-hidden="true"
        />
      </div>
    </div>
  );
}

function ErrorMessage({ message, onDismiss }: { message: string; onDismiss: () => void }) {
  return (
    <div 
      className="flex gap-3 animate-fade-in bg-destructive/10 border border-destructive/20 rounded-lg p-4 mb-4"
      role="alert"
      aria-live="polite"
    >
      <AlertCircle className="h-5 w-5 text-destructive flex-shrink-0 mt-0.5" />
      <div className="flex-1 min-w-0">
        <p className="text-sm text-destructive font-medium">Error</p>
        <p className="text-sm text-muted-foreground mt-1">{message}</p>
      </div>
      <button
        onClick={onDismiss}
        className="text-xs text-muted-foreground hover:text-foreground flex-shrink-0 underline"
        aria-label="Dismiss error"
      >
        Dismiss
      </button>
    </div>
  );
}

export function ChatPage() {
  const { messages, addMessage, setMessages, chatSessionId, setChatSessionId } = useAppStore();
  const [input, setInput] = useState('');
  const [thinking, setThinking] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const bottomRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, thinking]);

  // Load chat history on component mount and when the session changes
  useEffect(() => {
    loadChatHistory();
  }, [chatSessionId]);

  /**
   * Load chat history from Supabase
   */
  const loadChatHistory = async () => {
    try {
      const history = await ChatHistoryService.getChatHistory(chatSessionId);
      const formattedMessages = history.map((msg: any) => ({
        id: msg.message_id,
        role: msg.role,
        content: msg.content,
        chartData: msg.chart_type ? {
          type: msg.chart_type,
          title: msg.chart_title,
          insight: msg.chart_insight,
          labels: [], // Full chart data would need additional storage
          datasets: []
        } : undefined,
        timestamp: new Date(msg.timestamp)
      }));
      setMessages(formattedMessages);
    } catch (error) {
      console.error('Error loading chat history:', error);
      // Continue with empty chat if history loading fails
    }
  };

  const validateQuery = (query: string): string | null => {
    const trimmed = query.trim();
    
    if (!trimmed) {
      return 'Please enter a question about your data.';
    }
    
    if (trimmed.length < 3) {
      return 'Query must be at least 3 characters long.';
    }
    
    if (trimmed.length > 500) {
      return 'Query cannot exceed 500 characters.';
    }
    
    return null;
  };

  const sendQuery = async (query: string) => {
    setErrorMessage(null);

    const validationError = validateQuery(query);
    if (validationError) {
      setErrorMessage(validationError);
      inputRef.current?.focus();
      return;
    }

    if (thinking) return;

    const userMessageId = ChatHistoryService.generateMessageId();

    const userMsg = {
      id: userMessageId,
      role: 'user' as const,
      content: query.trim(),
      timestamp: new Date(),
    };

    addMessage(userMsg);
    setInput('');
    setThinking(true);

    try {
      // Save user message to Supabase if available, but don't block the query.
      try {
        await ChatHistoryService.saveUserMessage(userMessageId, chatSessionId, query.trim());
      } catch (saveError) {
        console.warn('Unable to persist user message to history:', saveError);
      }

      const backendResponse = await queryBackend(query.trim(), chatSessionId, userMessageId);

      if (!backendResponse) {
        throw new Error('Empty response from server');
      }

      const assistantMsg = {
        id: crypto.randomUUID(),
        role: 'assistant' as const,
        content: backendResponse.summary || 'Analysis complete. Review the chart for insights.',
        chartData: backendResponse.chartData,
        timestamp: new Date(),
      };

      addMessage(assistantMsg);

      try {
        await ChatHistoryService.saveAssistantMessage(
          ChatHistoryService.generateMessageId(),
          chatSessionId,
          assistantMsg.content,
          backendResponse.chartData
        );
      } catch (saveError) {
        console.warn('Unable to persist assistant message to history:', saveError);
      }

    } catch (error) {
      let errorText = 'Unable to process your query';

      if (error instanceof Error) {
        if (error.message.includes('NetworkError')) {
          errorText = 'Network error - please check your connection and try again.';
        } else if (error.message.includes('timeout')) {
          errorText = 'Request timed out - please try a simpler query.';
        } else if (error.message.includes('400')) {
          errorText = 'Invalid query format - please rephrase your question.';
        } else if (error.message.includes('500')) {
          errorText = 'Server error - please try again in a moment.';
        } else if (error.message.includes('inappropriate')) {
          errorText = error.message; // Show the inappropriate content message
        } else {
          errorText = `Error: ${error.message}`;
        }
      }

      setErrorMessage(errorText);

      const errorMsg = {
        id: crypto.randomUUID(),
        role: 'assistant' as const,
        content: `⚠️ ${errorText}. Please try again or upload a CSV file if you haven't already.`,
        timestamp: new Date(),
      };

      addMessage(errorMsg);
      
    } finally {
      setThinking(false);
      inputRef.current?.focus();
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (input.trim()) {
      await sendQuery(input);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
      handleSubmit(e as any);
    }
  };

  return (
    <div className="flex-1 flex flex-col h-screen md:h-auto">
      <div className="flex-1 flex flex-col px-4 md:px-8 py-6 pb-[180px]">
        <div className="flex-1 flex flex-col">
          <div className="flex-1 overflow-y-auto rounded-3xl border border-border bg-card p-4 shadow-sm">
            {errorMessage && (
              <div className="max-w-3xl mx-auto mb-4">
                <ErrorMessage
                  message={errorMessage}
                  onDismiss={() => setErrorMessage(null)}
                />
              </div>
            )}

            {messages.length === 0 && !thinking ? (
              <div className="flex flex-col items-center justify-center h-full min-h-[50vh] animate-fade-in">
                <div className="h-16 w-16 rounded-2xl bg-primary/10 flex items-center justify-center mb-6">
                  <Bot className="h-8 w-8 text-primary" />
                </div>
                <h2 className="text-xl font-semibold text-foreground mb-2">
                  Ask anything about your data
                </h2>
                <p className="text-muted-foreground text-sm mb-4 text-center max-w-md">
                  Upload a CSV file and ask questions in natural language to get instant analysis and insights.
                </p>
              </div>
            ) : (
              <div className="space-y-6">
                {messages.map((msg) => (
                  <div key={msg.id} className="animate-fade-in">
                    {msg.role === 'user' ? (
                      <div className="flex gap-3 justify-end">
                        <div className="bg-primary text-primary-foreground rounded-2xl rounded-tr-md px-4 py-2.5 max-w-md">
                          <p className="text-sm">{msg.content}</p>
                          <time className="text-xs opacity-70 mt-1 block">
                            {msg.timestamp.toLocaleTimeString()}
                          </time>
                        </div>
                        <div 
                          className="h-8 w-8 rounded-lg bg-muted flex items-center justify-center flex-shrink-0"
                          aria-label="You"
                        >
                          <User className="h-4 w-4 text-muted-foreground" />
                        </div>
                      </div>
                    ) : (
                      <div className="flex gap-3">
                        <div 
                          className="h-8 w-8 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0"
                          aria-label="Assistant"
                        >
                          <Bot className="h-4 w-4 text-primary" />
                        </div>
                        <div className="flex-1 space-y-4 min-w-0">
                          {msg.chartData && (
                            <div className="glass rounded-xl p-4 overflow-hidden" role="figure">
                              <DynamicChart data={msg.chartData} />
                            </div>
                          )}

                          {msg.content && <InsightCard summary={msg.content} />}

                          <time className="text-xs text-muted-foreground">
                            {msg.timestamp.toLocaleTimeString()}
                          </time>
                        </div>
                      </div>
                    )}
                  </div>
                ))}

                {thinking && <ThinkingSkeleton />}
              </div>
            )}

            <div ref={bottomRef} aria-hidden="true" />
          </div>
        </div>
      </div>

      <div className="fixed bottom-0 left-0 right-0 p-4 bg-gradient-to-t from-background via-background to-transparent pb-20 md:pb-4">
        <form onSubmit={handleSubmit} className="max-w-3xl mx-auto">
          <div className="flex items-center gap-2 glass rounded-xl px-4 py-2 focus-within:ring-2 focus-within:ring-primary/20">
            <input
              ref={inputRef}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Ask about your data..."
              className="flex-1 bg-transparent text-sm text-foreground placeholder:text-muted-foreground outline-none py-2"
              disabled={thinking}
              aria-label="Query input"
              aria-describedby="char-count"
              maxLength={500}
            />
            <span
              id="char-count"
              className="text-xs text-muted-foreground"
              aria-live="polite"
            >
              {input.length}/500
            </span>
            <Button
              type="submit"
              size="icon"
              disabled={!input.trim() || thinking}
              className="h-8 w-8 rounded-lg flex-shrink-0"
              aria-label="Send query"
              title="Send query (Ctrl+Enter)"
            >
              <Send className="h-3.5 w-3.5" />
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
