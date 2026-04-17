import { useState, useEffect } from 'react';
import { History, MessageSquare, Bot, User, Calendar, Trash2 } from 'lucide-react';
import { ChatHistoryService } from '@/lib/supabase';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Separator } from '@/components/ui/separator';
import { cn } from '@/lib/utils';

/**
 * ChatHistory Component - Displays chat history below the main chat interface
 *
 * Features:
 * - Shows all previous chat sessions
 * - Displays message counts and timestamps
 * - Allows loading specific chat sessions
 * - Follows WCAG 2.1 accessibility standards
 * - Responsive design for mobile and desktop
 */
export function ChatHistory({
  currentSessionId,
  onLoadSession,
  className
}: {
  currentSessionId: string;
  onLoadSession: (sessionId: string, messages: any[]) => void;
  className?: string;
}) {
  const [sessions, setSessions] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [expandedSession, setExpandedSession] = useState<string | null>(null);

  // Load chat sessions on component mount and when the current session changes
  useEffect(() => {
    loadChatSessions();
  }, [currentSessionId]);

  /**
   * Load all chat sessions from Supabase
   */
  const loadChatSessions = async () => {
    try {
      setLoading(true);

      // Get all unique sessions with their message counts
      const { data, error } = await ChatHistoryService.supabase
        .from('chat_messages')
        .select('session_id, timestamp')
        .order('timestamp', { ascending: false });

      if (error) throw error;

      // Group by session and count messages
      const sessionMap = new Map<string, { count: number; lastMessage: string }>();

      data?.forEach((msg: any) => {
        const sessionId = msg.session_id;
        const current = sessionMap.get(sessionId) || { count: 0, lastMessage: msg.timestamp };
        sessionMap.set(sessionId, {
          count: current.count + 1,
          lastMessage: msg.timestamp > current.lastMessage ? msg.timestamp : current.lastMessage
        });
      });

      // Convert to array and sort by last message
      const sessionArray = Array.from(sessionMap.entries())
        .map(([sessionId, info]) => ({
          sessionId,
          messageCount: info.count,
          lastMessage: new Date(info.lastMessage),
          isCurrent: sessionId === currentSessionId
        }))
        .sort((a, b) => b.lastMessage.getTime() - a.lastMessage.getTime());

      setSessions(sessionArray);

    } catch (error) {
      console.error('Error loading chat sessions:', error);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Load messages for a specific session
   */
  const loadSessionMessages = async (sessionId: string) => {
    try {
      setLoading(true);
      const messages = await ChatHistoryService.getChatHistory(sessionId);

      // Convert database format to frontend format
      const formattedMessages = messages.map((msg: any) => ({
        id: msg.message_id,
        role: msg.role,
        content: msg.content,
        chartData: msg.chart_type ? {
          type: msg.chart_type,
          title: msg.chart_title,
          insight: msg.chart_insight,
          labels: [], // Would need additional storage for full chart data
          datasets: []
        } : undefined,
        timestamp: new Date(msg.timestamp)
      }));

      onLoadSession(sessionId, formattedMessages);
      setExpandedSession(null);

    } catch (error) {
      console.error('Error loading session messages:', error);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Format relative time for display
   */
  const formatRelativeTime = (date: Date): string => {
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / (1000 * 60));
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;

    return date.toLocaleDateString();
  };

  /**
   * Get preview of first user message in session
   */
  const getSessionPreview = async (sessionId: string): Promise<string> => {
    try {
      const { data } = await ChatHistoryService.supabase
        .from('chat_messages')
        .select('content')
        .eq('session_id', sessionId)
        .eq('role', 'user')
        .order('timestamp', { ascending: true })
        .limit(1)
        .single();

      return data?.content ? data.content.substring(0, 50) + '...' : 'No messages';
    } catch {
      return 'No messages';
    }
  };

  return (
    <Card className={cn("w-full", className)}>
      <CardHeader className="pb-3">
        <CardTitle className="flex items-center gap-2 text-lg">
          <History className="h-5 w-5" />
          Chat History
          <Badge variant="secondary" className="ml-auto">
            {sessions.length} sessions
          </Badge>
        </CardTitle>
      </CardHeader>

      <CardContent className="p-0">
        <ScrollArea className="h-96">
          {loading ? (
            <div className="flex items-center justify-center p-6">
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
              <span className="ml-2 text-sm text-muted-foreground">Loading history...</span>
            </div>
          ) : sessions.length === 0 ? (
            <div className="flex flex-col items-center justify-center p-6 text-center">
              <MessageSquare className="h-8 w-8 text-muted-foreground mb-2" />
              <p className="text-sm text-muted-foreground">No chat history yet</p>
              <p className="text-xs text-muted-foreground mt-1">
                Start a conversation to see your chat history here
              </p>
            </div>
          ) : (
            <div className="space-y-1 p-2">
              {sessions.map((session) => (
                <div key={session.sessionId}>
                  <button
                    onClick={() => setExpandedSession(
                      expandedSession === session.sessionId ? null : session.sessionId
                    )}
                    className={cn(
                      "w-full p-3 rounded-lg border text-left transition-colors hover:bg-muted/50",
                      session.isCurrent && "border-primary bg-primary/5",
                      expandedSession === session.sessionId && "bg-muted/50"
                    )}
                    aria-expanded={expandedSession === session.sessionId}
                    aria-label={`Chat session with ${session.messageCount} messages from ${formatRelativeTime(session.lastMessage)}`}
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2 min-w-0 flex-1">
                        <div className="flex-shrink-0">
                          {session.isCurrent ? (
                            <div className="h-2 w-2 bg-primary rounded-full" aria-label="Current session" />
                          ) : (
                            <MessageSquare className="h-4 w-4 text-muted-foreground" />
                          )}
                        </div>
                        <div className="min-w-0 flex-1">
                          <div className="flex items-center gap-2">
                            <span className="text-sm font-medium truncate">
                              Session {session.sessionId.split('-').pop()}
                            </span>
                            <Badge variant="outline" className="text-xs">
                              {session.messageCount} msgs
                            </Badge>
                          </div>
                          <div className="flex items-center gap-1 text-xs text-muted-foreground">
                            <Calendar className="h-3 w-3" />
                            {formatRelativeTime(session.lastMessage)}
                          </div>
                        </div>
                      </div>
                    </div>
                  </button>

                  {expandedSession === session.sessionId && (
                    <div className="ml-6 mt-2 p-3 bg-muted/30 rounded-lg border-l-2 border-primary/20">
                      <div className="flex items-center gap-2 mb-2">
                        <Button
                          size="sm"
                          onClick={() => loadSessionMessages(session.sessionId)}
                          disabled={loading}
                          className="text-xs"
                        >
                          Load Session
                        </Button>
                        {session.isCurrent && (
                          <Badge variant="secondary" className="text-xs">
                            Current
                          </Badge>
                        )}
                      </div>
                      <p className="text-xs text-muted-foreground">
                        Click "Load Session" to view and continue this conversation
                      </p>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </ScrollArea>
      </CardContent>
    </Card>
  );
}