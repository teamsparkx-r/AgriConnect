import React, { useState } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useLanguageStore } from '../store/languageStore';
import { LogOut, User, Home, Search, Heart, Bell, Store, LayoutDashboard, Package, ClipboardList, PlusCircle, Globe, ChevronDown, ShieldCheck } from 'lucide-react';
import { LanguageCode } from '../constants/translations';

export const Navbar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, token, logout } = useAuthStore();
  const { language, setLanguage, t } = useLanguageStore();
  const [isLangOpen, setIsLangOpen] = useState(false);

  const languages = [
    { code: 'EN' as LanguageCode, name: 'English' },
    { code: 'HI' as LanguageCode, name: 'Hindi' },
    { code: 'TE' as LanguageCode, name: 'Telugu' }
  ];

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const isActive = (path: string) => location.pathname === path;

  // The new root is the MerchantPortal for guests and merchants.
  // For farmers, the home path should lead to their dashboard.
  const homePath = user?.role === 'farmer' ? '/farmer/dashboard' : '/';

  return (
    <nav className="bg-[#002f1a] border-b border-white/10 sticky top-0 z-[100] shadow-lg">
      <div className="container mx-auto px-6 h-16 flex items-center justify-between gap-4">
        {/* Brand */}
        <Link to={homePath} className="flex items-center gap-2 group shrink-0">
          <div className="w-9 h-9 bg-[#16a34a] rounded-xl flex items-center justify-center text-white group-hover:rotate-12 transition-transform shadow-lg">
            <span className="text-lg font-black">A</span>
          </div>
          <div className="hidden sm:block">
            <h1 className="text-white font-black text-lg tracking-tighter leading-none uppercase">AgriConnect</h1>
            <p className="text-[9px] text-green-400 font-bold uppercase tracking-widest leading-none mt-0.5">Marketplace</p>
          </div>
        </Link>

        {/* Dynamic Navigation */}
        <div className="hidden lg:flex items-center gap-8 flex-1 justify-center">
          {user?.role === 'farmer' ? (
             <>
               <NavLink to="/farmer/dashboard" active={isActive('/farmer/dashboard')} icon={<LayoutDashboard size={16} />} label={t.dashboard} />
               <NavLink to="/farmer/products" active={isActive('/farmer/products')} icon={<Package size={16} />} label="My Products" />
               <NavLink to="/farmer/bookings" active={isActive('/farmer/bookings')} icon={<ClipboardList size={16} />} label="Inquiries" />
             </>
          ) : (
            <>
               <NavLink to="/" active={isActive('/')} icon={<Home size={16} />} label="Home" />
               <NavLink to="/merchant/explore" active={isActive('/merchant/explore')} icon={<Search size={16} />} label="Explore" />
               {token && (
                 <>
                   <NavLink to="/merchant/saved" active={isActive('/merchant/saved')} icon={<Heart size={16} />} label={t.favorites} />
                   <NavLink to="/merchant/bookings" active={isActive('/merchant/bookings')} icon={<ClipboardList size={16} />} label={t.my_bookings} />
                 </>
               )}
            </>
          )}
        </div>

        {/* Right Actions */}
        <div className="flex items-center gap-4 shrink-0">
          {/* Language Selector */}
          <div className="relative">
            <button
              onClick={() => setIsLangOpen(!isLangOpen)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-white/10 text-[11px] font-black text-white hover:bg-white/5 transition-all uppercase tracking-widest"
            >
              <Globe size={14} className="text-green-500" />
              {language}
              <ChevronDown size={12} className={`text-white/50 transition-transform ${isLangOpen ? 'rotate-180' : ''}`} />
            </button>

            {isLangOpen && (
              <div className="absolute top-full right-0 mt-2 w-32 bg-white rounded-xl shadow-2xl border border-gray-100 py-2 z-[110] animate-in fade-in slide-in-from-top-2">
                {languages.map(lang => (
                  <button
                    key={lang.code}
                    onClick={() => { setLanguage(lang.code); setIsLangOpen(false); }}
                    className="w-full text-left px-4 py-2 text-[12px] font-bold text-gray-700 hover:bg-green-50 hover:text-[#16a34a] transition-colors flex justify-between items-center"
                  >
                    {lang.name}
                    {language === lang.code && <div className="w-1.5 h-1.5 bg-[#16a34a] rounded-full" />}
                  </button>
                ))}
              </div>
            )}
          </div>

          {token && user ? (
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-3 ml-2 cursor-pointer group" onClick={() => navigate(user.role === 'farmer' ? '/farmer/profile' : '/merchant/profile')}>
                <div className="hidden lg:block text-right">
                  <p className="text-[11px] font-black text-white leading-none uppercase tracking-tight group-hover:text-green-400 transition-colors">{user.full_name}</p>
                </div>
                <div className="w-8 h-8 bg-[#16a34a] rounded-lg flex items-center justify-center text-white shadow-lg group-hover:scale-105 transition-transform">
                  <User size={16} />
                </div>
              </div>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <button onClick={() => navigate('/login')} className="text-[11px] font-black text-white hover:text-green-400 px-4 py-2 uppercase tracking-widest transition-colors">
                {t.sign_in}
              </button>
              <button onClick={() => navigate('/buyer/signup')} className="bg-[#16a34a] text-white hover:bg-[#15803d] px-5 py-2 rounded-lg text-[11px] font-black transition-all shadow-xl uppercase tracking-widest">
                {t.join_now}
              </button>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

const NavLink = ({ to, active, icon, label }: any) => (
  <Link
    to={to}
    className={`flex items-center gap-2 text-[12px] font-black uppercase tracking-widest transition-colors ${
      active ? 'text-green-400' : 'text-green-100/50 hover:text-white'
    }`}
  >
    {icon} <span>{label}</span>
  </Link>
);
