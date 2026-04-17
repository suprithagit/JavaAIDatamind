import { useEffect } from 'react';
import { useAppStore } from '@/store/useAppStore';
import { AppSidebar } from '@/components/AppSidebar';
import { MobileNav } from '@/components/MobileNav';
import { UploadPage } from '@/components/UploadPage';
import { DashboardPage } from '@/components/DashboardPage';
import { ChatPage } from '@/components/ChatPage';

const Index = () => {
  const { activePage, theme } = useAppStore();

  useEffect(() => {
    document.documentElement.classList.toggle('dark', theme === 'dark');
  }, [theme]);

  const renderPage = () => {
    switch (activePage) {
      case 'upload': return <UploadPage />;
      case 'dashboard': return <DashboardPage />;
      case 'chat': return <ChatPage />;
    }
  };

  return (
    <div className="flex min-h-screen bg-background">
      <AppSidebar />
      <div className="flex-1 flex flex-col min-h-screen">
        <MobileNav />
        {renderPage()}
      </div>
    </div>
  );
};

export default Index;
