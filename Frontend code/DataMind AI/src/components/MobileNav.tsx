import { Upload, BarChart3, MessageSquare, Brain } from 'lucide-react';
import { useAppStore } from '@/store/useAppStore';
import { ThemeToggle } from '@/components/ThemeToggle';
import { cn } from '@/lib/utils';

const navItems = [
  { id: 'upload' as const, icon: Upload },
  { id: 'dashboard' as const, icon: BarChart3 },
  { id: 'chat' as const, icon: MessageSquare },
];

export function MobileNav() {
  const { activePage, setActivePage } = useAppStore();

  return (
    <>
      {/* Mobile Header */}
      <header className="md:hidden flex items-center justify-between px-4 h-14 border-b border-border bg-card">
        <div className="flex items-center gap-2">
          <div className="h-7 w-7 rounded-md bg-primary flex items-center justify-center">
            <Brain className="h-3.5 w-3.5 text-primary-foreground" />
          </div>
          <span className="font-semibold text-sm">DataMind AI</span>
        </div>
        <ThemeToggle />
      </header>

      {/* Bottom Tab Bar */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 z-50 flex items-center justify-around h-16 border-t border-border bg-card/95 backdrop-blur-lg">
        {navItems.map((item) => (
          <button
            key={item.id}
            onClick={() => setActivePage(item.id)}
            className={cn(
              'flex flex-col items-center gap-1 px-4 py-2 rounded-lg transition-colors',
              activePage === item.id
                ? 'text-primary'
                : 'text-muted-foreground'
            )}
          >
            <item.icon className="h-5 w-5" />
            <span className="text-[10px] font-medium capitalize">{item.id}</span>
          </button>
        ))}
      </nav>
    </>
  );
}
