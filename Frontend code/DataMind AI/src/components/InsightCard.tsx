import { useState, useEffect } from 'react';
import { Sparkles } from 'lucide-react';

interface InsightCardProps {
  summary: string;
}

export function InsightCard({ summary }: InsightCardProps) {
  const [displayed, setDisplayed] = useState('');
  const [done, setDone] = useState(false);

  useEffect(() => {
    setDisplayed('');
    setDone(false);
    let i = 0;
    const interval = setInterval(() => {
      i++;
      setDisplayed(summary.slice(0, i));
      if (i >= summary.length) {
        clearInterval(interval);
        setDone(true);
      }
    }, 15);
    return () => clearInterval(interval);
  }, [summary]);

  // Simple markdown bold rendering
  const renderText = (text: string) => {
    const parts = text.split(/(\*\*.*?\*\*)/g);
    return parts.map((part, i) => {
      if (part.startsWith('**') && part.endsWith('**')) {
        return <strong key={i} className="text-foreground font-semibold">{part.slice(2, -2)}</strong>;
      }
      return <span key={i}>{part}</span>;
    });
  };

  return (
    <div className="glass rounded-xl p-5 animate-fade-in">
      <div className="flex items-center gap-2 mb-3">
        <Sparkles className="h-4 w-4 text-primary" />
        <span className="text-xs font-medium text-primary uppercase tracking-wider">AI Insight</span>
      </div>
      <p className="text-sm leading-relaxed text-muted-foreground">
        {renderText(displayed)}
        {!done && <span className="typewriter-cursor" />}
      </p>
    </div>
  );
}
