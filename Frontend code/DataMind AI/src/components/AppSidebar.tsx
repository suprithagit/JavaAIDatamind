import { Upload, BarChart3, MessageSquare, Brain, Plus } from 'lucide-react';
import { useAppStore } from '@/store/useAppStore';
import { ThemeToggle } from '@/components/ThemeToggle';
import { ChatHistory } from '@/components/ChatHistory';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

const navItems = [
  { id: 'upload' as const, label: 'Upload', icon: Upload },
  { id: 'dashboard' as const, label: 'Dashboard', icon: BarChart3 },
  { id: 'chat' as const, label: 'Chat', icon: MessageSquare },
];

export function AppSidebar() {
  const { activePage, setActivePage, setMessages, chatSessionId, setChatSessionId, newChatSession } = useAppStore();

  return (
    <aside className="hidden md:flex flex-col w-80 border-r border-border bg-sidebar h-screen sticky top-0 overflow-hidden">
      {/* Logo */}
      <div className="flex items-center gap-2.5 px-5 h-14 border-b border-border">
        <div className="h-8 w-8 rounded-lg bg-primary flex items-center justify-center">
          <Brain className="h-4 w-4 text-primary-foreground" />
        </div>
        <span className="font-semibold text-foreground tracking-tight">DataMind AI</span>
      </div>

      {/* Navigation */}
      <nav className="px-3 py-4 space-y-1">
        {navItems.map((item) => (
          <button
            key={item.id}
            onClick={() => setActivePage(item.id)}
            className={cn(
              'flex items-center gap-3 w-full px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150',
              activePage === item.id
                ? 'bg-sidebar-accent text-sidebar-accent-foreground'
                : 'text-muted-foreground hover:text-foreground hover:bg-muted/50'
            )}
          >
            <item.icon className="h-4 w-4" />
            {item.label}
          </button>
        ))}

        <div className="mt-4 px-1">
          <Button
            variant="secondary"
            onClick={() => {
              newChatSession();
              setActivePage('chat');
            }}
            className="w-full justify-center gap-2"
          >
            <Plus className="h-4 w-4" />
            New Chat
          </Button>
        </div>
      </nav>

      <div className="flex-1 overflow-hidden px-3 pb-4">
        <ChatHistory
          currentSessionId={chatSessionId}
          onLoadSession={(newSessionId, sessionMessages) => {
            setChatSessionId(newSessionId);
            setMessages(sessionMessages);
            setActivePage('chat');
          }}
          className="mt-4"
        />
      </div>

      {/* Footer */}
      <div className="px-3 py-3 border-t border-border flex items-center justify-between">
        <span className="text-xs text-muted-foreground">v1.0.0</span>
        <ThemeToggle />
      </div>
    </aside>
  );
}
