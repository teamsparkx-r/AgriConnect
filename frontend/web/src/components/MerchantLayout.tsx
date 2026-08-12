import React, { useState } from 'react';
import { useNavigate, useLocation, Link, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useLanguageStore } from '../store/languageStore';
import { LanguageCode } from '../constants/translations';
import {
  LayoutDashboard, ShoppingBag, ClipboardList, Users, Heart,
  MessageSquare, CreditCard, FileText, Flag, Settings,
  LogOut, Search, Menu, ChevronDown, MapPin, ShoppingCart,
  Globe, X, User as UserIcon, ShieldCheck
} from 'lucide-react';
import { BottomNav } from './BottomNav';

export const MerchantLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, token, logout } = useAuthStore();

  // Safeguard: Redirect farmers and admins to their own portals if they hit merchant routes
  React.useEffect(() => {
    if (token && user) {
      if (user.role === 'farmer') {
        navigate('/farmer/dashboard', { replace: true });
      } else if (user.role === 'admin') {
        navigate('/admin/dashboard', { replace: true });
      }
    }
  }, [token, user, navigate]);

  const { language, setLanguage, t } = useLanguageStore();
  const [searchQuery, setSearchQuery] = useState('');

  const [isLangOpen, setIsLangOpen] = useState(false);
  const [currentLocation, setCurrentLocation] = useState('Mumbai, MH');

  const navItems = [
    { label: 'All Showrooms', path: '/merchant/explore', icon: <Menu size={18} />, public: true },
    { label: t.dashboard, path: '/', public: true },
    { label: t.browse_products, path: '/merchant/explore', public: true },
    { label: t.my_bookings, path: '/merchant/bookings' },
    { label: t.farmers, path: '/merchant/farmers', public: true },
    { label: t.favorites, path: '/merchant/saved' },
    { label: t.messages, path: '/merchant/messages', badge: token ? 2 : 0 },
    { label: t.payments, path: '/merchant/payments' },
    { label: t.invoices, path: '/merchant/invoices' },
    { label: t.reports, path: '/merchant/reports' },
  ];

  const languages = [
    { code: 'EN' as LanguageCode, name: 'English' },
    { code: 'HI' as LanguageCode, name: 'Hindi' },
    { code: 'TE' as LanguageCode, name: 'Telugu' }
  ];

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;
    navigate(`/merchant/explore?query=${encodeURIComponent(searchQuery)}`);
  };

  const handleProtectedRoute = (path: string, isPublic: boolean = false) => {
    if (!isPublic && !token) {
      navigate('/login', { state: { from: path } });
    } else {
      navigate(path);
    }
  };

  const activePath = location.pathname;

  return (
    <div className="flex flex-col h-screen w-screen overflow-hidden bg-[#F8FAF9] font-sans text-gray-800">
      {/* TOP PRIMARY HEADER (App-Optimized) */}
      <header className="h-16 bg-[#002f1a] flex items-center justify-between px-4 z-[100] flex-shrink-0 gap-3 sticky top-0 shadow-lg pt-safe">
        {/* Brand Logo (Hidden on small mobile to give space for search) */}
        <Link to="/" className="flex items-center gap-2 px-1 py-1 shrink-0 hidden xs:flex">
          <div className="w-8 h-8 bg-[#16a34a] rounded-lg flex items-center justify-center text-white font-black text-sm shadow-lg">A</div>
          <div className="flex flex-col text-left hidden md:flex">
            <h1 className="text-white font-black text-base tracking-tighter leading-none uppercase">AgriConnect</h1>
            <p className="text-[8px] text-[#16a34a] font-bold uppercase tracking-[0.1em]">Intelligence</p>
          </div>
        </Link>

        {/* Search Bar (Expandable or always visible) */}
        <form onSubmit={handleSearch} className="flex-1 flex h-10 group min-w-0">
          <div className="flex-1 flex bg-white/10 rounded-l-xl overflow-hidden focus-within:bg-white focus-within:ring-2 focus-within:ring-[#16a34a]/20 transition-all border border-white/10">
            <input
              type="text"
              placeholder={t.search_placeholder}
              className="flex-1 bg-transparent px-3 text-sm outline-none font-medium text-white focus:text-gray-900 placeholder:text-white/30"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
          <button type="submit" className="bg-[#16a34a] hover:bg-[#15803d] text-white px-4 rounded-r-xl transition-all flex items-center justify-center shadow-lg">
            <Search size={18} strokeWidth={3} />
          </button>
        </form>

        {/* Right Header Actions */}
        <div className="flex items-center gap-2 shrink-0">
          {/* Cart Icon */}
          <div onClick={() => handleProtectedRoute('/merchant/saved')} className="flex items-center justify-center text-white w-10 h-10 border border-transparent hover:bg-white/5 rounded-xl transition-all cursor-pointer relative">
            <ShoppingCart size={20} />
            {token && <span className="absolute top-1 right-1 bg-red-500 text-white text-[9px] font-black w-4 h-4 flex items-center justify-center rounded-full border-2 border-[#002f1a]">3</span>}
          </div>

          {/* User Icon (Mobile) */}
          <div onClick={() => token ? navigate('/merchant/profile') : navigate('/login')} className="w-10 h-10 bg-[#16a34a] rounded-xl flex items-center justify-center text-white shadow-lg cursor-pointer">
            <UserIcon size={18} />
          </div>
        </div>
      </header>

      {/* SECONDARY HORIZONTAL NAV BAR (Desktop/Tablet) */}
      <nav className="h-10 bg-[#064e3b] flex items-center px-4 z-90 flex-shrink-0 overflow-x-auto no-scrollbar gap-1 sticky top-16 shadow-xl border-t border-white/5 hidden lg:flex">
        {navItems.map((item: any, idx) => {
          const isActive = activePath === item.path;
          return (
            <button
              key={idx}
              onClick={() => handleProtectedRoute(item.path, item.public)}
              className={`flex items-center gap-1.5 px-4 py-1.5 border border-transparent hover:border-white rounded-lg text-[12px] font-black uppercase tracking-widest transition-all whitespace-nowrap shrink-0 ${
                isActive ? 'bg-[#16a34a] text-white shadow-lg' : 'text-green-100/50 hover:bg-white/5 hover:text-white'
              }`}
            >
              {item.label}
            </button>
          );
        })}
      </nav>

      {/* MAIN SCROLLABLE CONTENT AREA */}
      <main className="flex-1 overflow-y-auto bg-[#F8FAF9] no-scrollbar pb-16 lg:pb-0">
        <div className="max-w-[1600px] mx-auto min-h-full p-4 lg:p-10">
          <Outlet />
        </div>
      </main>

      <BottomNav />
    </div>
  );
};
