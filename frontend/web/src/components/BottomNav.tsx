import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Home, Search, Heart, ClipboardList, User, Package, PlusCircle, LayoutDashboard, LogIn } from 'lucide-react';
import { useAuthStore } from '../store/authStore';

export const BottomNav: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { token, user } = useAuthStore();

  const isActive = (path: string) => location.pathname === path;

  const authPaths = ['/login', '/buyer/signup', '/farmer/signup', '/farmer/login', '/admin/login'];
  if (authPaths.includes(location.pathname)) return null;

  return (
    <div className="fixed bottom-0 left-0 right-0 bg-[#002f1a] border-t border-white/10 lg:hidden z-50 shadow-[0_-4px_20px_rgba(0,0,0,0.2)] pb-safe">
      <div className="flex justify-around items-center h-16 px-2">
        {!token ? (
            <>
                <NavButton
                  active={isActive('/')}
                  icon={<Home size={20} />}
                  label="Home"
                  onClick={() => navigate('/')}
                />
                <NavButton
                  active={isActive('/merchant/explore')}
                  icon={<Search size={20} />}
                  label="Browse"
                  onClick={() => navigate('/merchant/explore')}
                />
                <NavButton
                  active={isActive('/login')}
                  icon={<LogIn size={20} />}
                  label="Sign In"
                  onClick={() => navigate('/login')}
                />
            </>
        ) : user?.role === 'admin' ? (
            <>
                <NavButton
                  active={isActive('/admin/dashboard')}
                  icon={<LayoutDashboard size={20} />}
                  label="Dash"
                  onClick={() => navigate('/admin/dashboard')}
                />
                <NavButton
                  active={location.search.includes('tab=farmers')}
                  icon={<Package size={20} />}
                  label="Farmers"
                  onClick={() => navigate('/admin/manage?tab=farmers')}
                />
                <NavButton
                  active={location.search.includes('tab=bookings')}
                  icon={<ClipboardList size={20} />}
                  label="Audit"
                  onClick={() => navigate('/admin/manage?tab=bookings')}
                />
                <NavButton
                  active={isActive('/admin/profile')}
                  icon={<User size={20} />}
                  label="Account"
                  onClick={() => navigate('/admin/profile')}
                />
            </>
        ) : user?.role === 'farmer' ? (
            <>
                <NavButton
                  active={isActive('/farmer/dashboard')}
                  icon={<LayoutDashboard size={20} />}
                  label="Dash"
                  onClick={() => navigate('/farmer/dashboard')}
                />
                <NavButton
                  active={isActive('/farmer/products')}
                  icon={<Package size={20} />}
                  label="Stock"
                  onClick={() => navigate('/farmer/products')}
                />
                <div className="relative -top-4">
                  <button
                    onClick={() => navigate('/farmer/add-product')}
                    className="w-14 h-14 bg-[#16a34a] rounded-2xl flex items-center justify-center text-white shadow-xl border-4 border-[#002f1a] transform active:scale-95 transition-all"
                  >
                    <PlusCircle size={28} />
                  </button>
                </div>
                <NavButton
                  active={isActive('/farmer/bookings')}
                  icon={<ClipboardList size={20} />}
                  label="Orders"
                  onClick={() => navigate('/farmer/bookings')}
                />
                <NavButton
                  active={isActive('/farmer/profile')}
                  icon={<User size={20} />}
                  label="Registry"
                  onClick={() => navigate('/farmer/profile')}
                />
            </>
        ) : (
            <>
                <NavButton
                  active={isActive('/')}
                  icon={<Home size={20} />}
                  label="Home"
                  onClick={() => navigate('/')}
                />
                <NavButton
                  active={isActive('/merchant/explore')}
                  icon={<Search size={20} />}
                  label="Browse"
                  onClick={() => navigate('/merchant/explore')}
                />
                <NavButton
                  active={isActive('/merchant/saved')}
                  icon={<Heart size={20} />}
                  label="Saved"
                  onClick={() => navigate('/merchant/saved')}
                />
                <NavButton
                  active={isActive('/merchant/bookings')}
                  icon={<ClipboardList size={20} />}
                  label="History"
                  onClick={() => navigate('/merchant/bookings')}
                />
                <NavButton
                  active={isActive('/merchant/profile')}
                  icon={<User size={20} />}
                  label="Profile"
                  onClick={() => navigate('/merchant/profile')}
                />
            </>
        )}
      </div>
    </div>
  );
};

const NavButton = ({ active, icon, label, onClick }: any) => (
  <button
    onClick={onClick}
    className={`flex flex-col items-center justify-center flex-1 h-full gap-1 transition-all ${
      active ? 'text-[#16a34a]' : 'text-green-100/40'
    }`}
  >
    <div className={`${active ? 'scale-110' : ''} transition-transform`}>
      {icon}
    </div>
    <span className="text-[9px] font-black uppercase tracking-widest">{label}</span>
  </button>
);
