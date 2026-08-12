import React, { useState } from 'react';
import { useNavigate, useLocation, Link, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useLanguageStore } from '../store/languageStore';
import { LanguageCode } from '../constants/translations';
import {
  LayoutDashboard, Users, Store, Package, ClipboardList,
  CreditCard, Flag, Layers, Bell, BarChart3,
  Settings, Search, Menu, History, ChevronDown, Globe, ShieldCheck, LogOut
} from 'lucide-react';
import { BottomNav } from './BottomNav';

export const AdminLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useAuthStore();
  const { language, setLanguage, t } = useLanguageStore();
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const [isLangOpen, setIsLangOpen] = useState(false);

  const navItems = [
    { icon: <LayoutDashboard size={18} />, label: t.dashboard, path: '/admin/dashboard' },
    { icon: <Users size={18} />, label: t.farmers, path: '/admin/manage?tab=farmers' },
    { icon: <Store size={18} />, label: t.total_merchants, path: '/admin/manage?tab=merchants' },
    { icon: <Package size={18} />, label: t.active_products, path: '/admin/manage?tab=products' },
    { icon: <ClipboardList size={18} />, label: t.my_bookings, path: '/admin/manage?tab=bookings' },
    { icon: <CreditCard size={18} />, label: t.payments, path: '/admin/manage?tab=payments' },
    { icon: <Flag size={18} />, label: t.reports, path: '/admin/manage?tab=reports' },
    { icon: <Layers size={18} />, label: t.all_categories, path: '/admin/manage?tab=categories' },
    { icon: <Bell size={18} />, label: 'Broadcasts', path: '/admin/manage?tab=notifications' },
    { icon: <BarChart3 size={18} />, label: 'Analytics Hub', path: '/admin/manage?tab=analytics' },
    { icon: <Settings size={18} />, label: t.account_settings, path: '/admin/manage?tab=settings' },
    { icon: <History size={18} />, label: 'Audit Records', path: '/admin/manage?tab=logs' },
  ];

  const languages = [
    { code: 'EN' as LanguageCode, name: 'English' },
    { code: 'HI' as LanguageCode, name: 'Hindi' },
    { code: 'TE' as LanguageCode, name: 'Telugu' }
  ];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const activePath = location.pathname + (location.search || '');

  return (
    <div className="flex h-screen w-screen overflow-hidden bg-[#F8FAF9] font-sans text-gray-800">
      {/* SIDEBAR (Hidden on mobile) */}
      <aside
        className={`bg-[#002f1a] text-white flex-col flex-shrink-0 transition-all duration-300 z-50 shadow-2xl h-full hidden lg:flex ${
          isSidebarCollapsed ? 'w-20' : 'w-64'
        }`}
      >
        <div className="h-20 flex items-center px-6 gap-3 border-b border-white/5 flex-shrink-0">
          <Link to="/admin/dashboard" className="w-10 h-10 bg-white/10 rounded-xl flex items-center justify-center text-white shrink-0 hover:bg-white/20 transition-all group shadow-lg">
             <div className="w-8 h-8 bg-[#16a34a] rounded-lg flex items-center justify-center text-white font-black text-[12px] group-hover:rotate-12 transition-transform">A</div>
          </Link>
          {!isSidebarCollapsed && (
            <div className="overflow-hidden whitespace-nowrap text-left">
               <h1 className="text-white font-black text-xl tracking-tighter leading-none">AgriConnect</h1>
               <p className="text-[10px] text-green-400 font-bold uppercase tracking-[0.2em] mt-1 italic">Admin Hub</p>
            </div>
          )}
        </div>

        <nav className="flex-1 overflow-y-auto py-6 px-4 space-y-1 no-scrollbar">
          {navItems.map((item) => {
            const isActive = activePath === item.path || (item.path !== '/admin/dashboard' && activePath.startsWith(item.path.split('?')[0]) && activePath.includes(item.path.split('=')[1] || ''));
            return (
              <Link
                key={item.label}
                to={item.path}
                className={`flex items-center gap-4 px-4 py-3 rounded-xl font-bold transition-all group relative ${
                  isActive
                    ? 'bg-[#16a34a] text-white shadow-xl shadow-green-900/40 translate-x-1'
                    : 'text-green-100/40 hover:bg-white/5 hover:text-white'
                }`}
              >
                <div className={`${isActive ? 'scale-110' : 'group-hover:scale-110'} transition-transform shrink-0`}>
                  {item.icon}
                </div>
                {!isSidebarCollapsed && <span className="text-[13px] tracking-wide whitespace-nowrap uppercase">{item.label}</span>}
              </Link>
            );
          })}
        </nav>
      </aside>

      {/* CONTENT AREA */}
      <div className="flex-1 flex flex-col h-full overflow-hidden min-w-0">
        {/* HEADER - STICKY */}
        <header className="h-16 lg:h-20 bg-white border-b border-gray-100 flex items-center justify-between px-4 lg:px-8 z-40 flex-shrink-0 sticky top-0 shadow-sm pt-safe">
           <div className="flex items-center gap-4 lg:gap-6 flex-1">
              <button onClick={() => setIsSidebarCollapsed(!isSidebarCollapsed)} className="p-2 lg:p-2.5 text-gray-400 hover:text-[#16a34a] hover:bg-green-50 rounded-xl transition-all shadow-inner border border-gray-50"><Menu size={20} /></button>
              <div className="relative max-w-lg flex-1 group hidden sm:block">
                 <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-[#16a34a] transition-colors" size={18} />
                 <input type="text" placeholder={t.search_placeholder} className="w-full bg-[#f3f4f6] border border-transparent rounded-xl py-2.5 pl-12 pr-4 text-[13px] outline-none focus:ring-4 focus:ring-green-500/5 focus:bg-white focus:border-green-100 transition-all font-medium text-gray-600" />
              </div>
              <div className="lg:hidden">
                 <h1 className="text-gray-900 font-black text-lg tracking-tighter uppercase">AgriConnect</h1>
              </div>
           </div>

           <div className="flex items-center gap-3 lg:gap-8 shrink-0">
              <div className="relative hidden md:block">
                <button
                  onClick={() => setIsLangOpen(!isLangOpen)}
                  className="flex items-center gap-2 px-3 py-1.5 rounded-xl border border-gray-100 text-[11px] font-black text-gray-600 hover:bg-gray-50 transition-all shadow-sm uppercase tracking-widest"
                >
                  <Globe size={14} className="text-gray-400" />
                  {language}
                </button>
              </div>

              <div
                onClick={() => navigate('/admin/profile')}
                className="flex items-center gap-2 lg:gap-4 bg-[#f3f4f6] p-1 lg:p-1.5 lg:pr-6 rounded-2xl cursor-pointer hover:bg-gray-200 transition-all border border-gray-100 group shadow-sm"
              >
                 <div className="w-8 h-8 lg:w-10 lg:h-10 bg-[#16a34a] rounded-xl flex items-center justify-center text-white font-black text-sm shadow-lg group-hover:scale-105 transition-transform border-2 border-white">AD</div>
                 <div className="hidden xl:block text-left">
                    <p className="text-[12px] font-black text-gray-900 leading-none mb-1 uppercase tracking-tight">Super Control</p>
                    <p className="text-[9px] text-gray-400 font-bold uppercase tracking-widest leading-none italic">System Admin</p>
                 </div>
              </div>
           </div>
        </header>

        <main className="flex-1 overflow-y-auto bg-[#F8FAF9] no-scrollbar pb-16 lg:pb-0">
           <div className="p-4 lg:p-12 max-w-[1600px] mx-auto min-h-full">
              <Outlet />
           </div>
        </main>
      </div>
      <BottomNav />
    </div>
  );
};
