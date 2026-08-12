import React, { useState } from 'react';
import { useNavigate, useLocation, Link, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useLanguageStore } from '../store/languageStore';
import { LanguageCode } from '../constants/translations';
import {
  LayoutDashboard, Package, PlusCircle, ClipboardList,
  MessageSquare, IndianRupee, Bell, User, Settings,
  LogOut, Menu, ChevronDown, ShieldCheck, Globe, X, Search
} from 'lucide-react';
import { BottomNav } from './BottomNav';

export const FarmerLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuthStore();
  const { language, setLanguage, t } = useLanguageStore();
  const [searchQuery, setSearchQuery] = useState('');

  const navItems = [
    { label: 'Overview', path: '/farmer/dashboard', icon: <LayoutDashboard size={14} /> },
    { label: t.my_products, path: '/farmer/products', icon: <Package size={14} /> },
    { label: 'Add Listing', path: '/farmer/add-product', icon: <PlusCircle size={14} /> },
    { label: t.my_bookings, path: '/farmer/bookings', icon: <ClipboardList size={14} />, badge: 7 },
    { label: t.messages, path: '/farmer/messages', icon: <MessageSquare size={14} />, badge: 3 },
    { label: t.earnings, path: '/farmer/earnings', icon: <IndianRupee size={14} /> },
    { label: 'Alerts', path: '/farmer/notifications', icon: <Bell size={14} />, badge: 5 },
  ];

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;
    navigate(`/farmer/products?query=${encodeURIComponent(searchQuery)}`);
  };

  const activePath = location.pathname;

  return (
    <div className="flex flex-col h-screen w-screen overflow-hidden bg-[#F8FAF9] font-sans text-gray-800">
      {/* TOP PRIMARY HEADER (Emerald Professional, Mobile Optimized) */}
      <header className="h-16 bg-[#002f1a] flex items-center justify-between px-4 z-[100] flex-shrink-0 gap-3 sticky top-0 shadow-lg pt-safe">
        {/* Brand Logo (Hidden on small mobile) */}
        <Link to="/farmer/dashboard" className="flex items-center gap-2 shrink-0 hidden xs:flex">
          <div className="w-8 h-8 bg-[#16a34a] rounded-lg flex items-center justify-center text-white font-black shadow-lg">A</div>
          <div className="flex flex-col text-left hidden md:flex">
            <h1 className="text-white font-black text-base tracking-tighter leading-none uppercase">AgriConnect</h1>
            <p className="text-[9px] text-green-400 font-bold uppercase tracking-[0.1em] mt-0.5">Farmer Hub</p>
          </div>
        </Link>

        {/* Search / Inventory Query */}
        <form onSubmit={handleSearch} className="flex-1 flex h-10 min-w-0">
          <div className="flex-1 flex bg-white/10 rounded-xl overflow-hidden focus-within:bg-white focus-within:ring-2 focus-within:ring-green-500/20 transition-all border border-white/10 shadow-inner">
            <button type="submit" className="flex items-center px-3 text-green-400">
              <Search size={18} />
            </button>
            <input
              type="text"
              placeholder="Search registry..."
              className="flex-1 bg-transparent px-2 text-sm outline-none font-medium text-white focus:text-gray-900 placeholder:text-white/30"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
        </form>

        {/* Right Actions */}
        <div className="flex items-center gap-2 shrink-0">
          {/* User Account */}
          <div onClick={() => navigate('/farmer/profile')} className="w-10 h-10 bg-[#16a34a] rounded-xl flex items-center justify-center text-white shadow-lg cursor-pointer border-2 border-[#002f1a]">
            <User size={18} />
          </div>
        </div>
      </header>

      {/* SECONDARY HORIZONTAL NAV BAR (Desktop/Tablet Only) */}
      <nav className="h-10 bg-[#064e3b] flex items-center px-4 z-90 flex-shrink-0 overflow-x-auto no-scrollbar gap-1 sticky top-16 shadow-xl border-t border-white/5 hidden lg:flex">
        {navItems.map((item, idx) => {
          const isActive = activePath === item.path;
          return (
            <Link
              key={idx}
              to={item.path}
              className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-[12px] font-black uppercase tracking-widest transition-all whitespace-nowrap shrink-0 ${
                isActive
                  ? 'bg-[#16a34a] text-white shadow-lg'
                  : 'text-green-100/50 hover:bg-white/5 hover:text-white'
              }`}
            >
              {item.label}
              {item.badge && (
                <span className="bg-red-500 text-white text-[9px] px-1.5 py-0.5 rounded font-black shadow-sm">
                  {item.badge}
                </span>
              )}
            </Link>
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
