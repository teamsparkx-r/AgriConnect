import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { appAuth } from '../services/authService';
import { ShieldCheck, ChevronRight, Lock, Phone, Info } from 'lucide-react';

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { setUser, setToken } = useAuthStore();

  const isFarmerRoute = location.pathname === '/farmer/login';

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [mobile, setMobile] = useState('');
  const [password, setPassword] = useState('');

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const response = await appAuth.login({ mobile_number: mobile, password });

      if (response && response.access_token) {
        setToken(response.access_token);
        setUser(response.user);
        setSuccess('Identity verified. Accessing network...');

        const from = location.state?.from;
        const role = response.user?.role;
        const route = role === 'farmer' ? '/farmer/dashboard' : role === 'admin' ? '/admin/dashboard' : '/';

        // Direct navigation if 'from' is just the root, otherwise respect 'from'
        const targetRoute = (from && from !== '/') ? from : route;

        navigate(targetRoute, { replace: true });
      } else {
        setError('Invalid registry response');
      }
    } catch (err: any) {
      setError(err.response?.data?.detail || 'Authentication sequence failed. Verify credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center pt-10 px-4">
      {/* Brand Header */}
      <Link to="/" className="flex items-center gap-2 mb-10 group">
        <div className="w-10 h-10 bg-[#16a34a] rounded-xl flex items-center justify-center text-white shadow-xl group-hover:rotate-12 transition-transform">
          <span className="text-xl font-black">A</span>
        </div>
        <h1 className="text-xl font-black text-gray-900 uppercase tracking-tighter">AgriConnect</h1>
      </Link>

      <div className="w-full max-w-[380px] bg-white p-8 rounded-2xl shadow-xl border border-gray-100">
        <h2 className="text-2xl font-black text-gray-900 mb-6 uppercase tracking-tight italic">
          {isFarmerRoute ? 'Farmer Registry' : 'Sign-In'}
        </h2>

        {error && (
          <div className="bg-red-50 border border-red-100 p-4 mb-8 rounded-xl flex items-start gap-3 animate-in shake duration-300">
            <Info className="text-red-600 shrink-0 mt-0.5" size={16} />
            <div>
              <p className="text-red-900 text-[10px] font-black uppercase tracking-widest leading-none">Security Alert</p>
              <p className="text-red-700 text-xs mt-1 italic font-medium">{error}</p>
            </div>
          </div>
        )}

        {success && (
          <div className="bg-green-50 border border-green-100 p-4 mb-8 rounded-xl flex items-center gap-3">
             <ShieldCheck className="text-green-600" size={16}/>
             <span className="text-green-800 text-xs font-bold uppercase tracking-widest">{success}</span>
          </div>
        )}

        <form onSubmit={handleLogin} className="space-y-6">
          <div className="space-y-2 text-left">
            <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest italic">Registry Mobile</label>
            <div className="relative">
               <Phone className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-300" size={16}/>
               <input
                 type="tel"
                 className="w-full bg-gray-50 border border-gray-200 rounded-xl py-3.5 pl-12 pr-4 text-sm focus:ring-2 focus:ring-[#16a34a]/10 focus:bg-white focus:border-[#16a34a]/30 outline-none transition-all font-bold"
                 placeholder="10 digit number"
                 value={mobile}
                 onChange={(e) => setMobile(e.target.value)}
                 maxLength={10}
                 required
               />
            </div>
          </div>

          <div className="space-y-2 text-left">
            <div className="flex justify-between items-center">
              <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest italic">Encrypted Key</label>
              <button type="button" className="text-[10px] font-bold text-blue-600 hover:text-[#16a34a] uppercase tracking-widest">Reset?</button>
            </div>
            <div className="relative">
               <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-300" size={16}/>
               <input
                 type="password"
                 className="w-full bg-gray-50 border border-gray-200 rounded-xl py-3.5 pl-12 pr-4 text-sm focus:ring-2 focus:ring-[#16a34a]/10 focus:bg-white focus:border-[#16a34a]/30 outline-none transition-all font-bold"
                 placeholder="Your passcode"
                 value={password}
                 onChange={(e) => setPassword(e.target.value)}
                 required
               />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-[#131921] hover:bg-[#16a34a] text-white py-4 rounded-xl font-black text-[11px] uppercase tracking-[0.2em] shadow-2xl transition-all flex items-center justify-center gap-3"
          >
            {loading ? <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div> : <><ShieldCheck size={18}/> Verify & Connect</>}
          </button>

          <p className="text-[9px] text-gray-400 font-bold leading-relaxed uppercase tracking-tighter text-center">
            Secured by AgriConnect Identity Registry.<br/>
            © 2026 Direct Sourcing Intelligence.
          </p>
        </form>
      </div>

      <div className="w-full max-w-[380px] mt-10 space-y-6">
        <div className="relative">
          <div className="absolute inset-0 flex items-center"><div className="w-full border-t border-gray-200"></div></div>
          <div className="relative flex justify-center text-[10px]"><span className="bg-gray-50 px-3 text-gray-400 font-black uppercase tracking-widest">Registry Admission</span></div>
        </div>

        <button
          onClick={() => navigate(isFarmerRoute ? '/farmer/signup' : '/buyer/signup')}
          className="w-full bg-white border border-gray-200 text-gray-900 py-3 rounded-xl font-black text-[10px] uppercase tracking-widest shadow-sm hover:bg-gray-50 transition-all"
        >
          Initialize New Registry Account
        </button>

        {!isFarmerRoute && (
          <div className="text-center">
             <button
              onClick={() => navigate('/farmer/login')}
              className="text-[10px] font-black text-[#16a34a] hover:text-[#131921] uppercase tracking-widest flex items-center justify-center gap-2 mx-auto group"
            >
              Access Farmer Portal Registry <ChevronRight size={14} className="group-hover:translate-x-1 transition-transform" />
            </button>
          </div>
        )}
      </div>

      <footer className="mt-auto py-10 w-full border-t border-gray-100 bg-white flex flex-col items-center">
          <div className="flex gap-10 mb-4 font-black uppercase tracking-widest text-[9px] text-gray-400">
              <button className="hover:text-gray-900 transition-colors">Protocol</button>
              <button className="hover:text-gray-900 transition-colors">Audit</button>
              <button className="hover:text-gray-900 transition-colors">Intelligence</button>
          </div>
          <p className="text-[9px] font-black text-gray-300 uppercase tracking-[0.3em]">AgriConnect Node Identity Protection System</p>
      </footer>
    </div>
  );
};
