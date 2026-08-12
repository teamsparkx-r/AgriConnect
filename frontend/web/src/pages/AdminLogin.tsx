import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { appAuth } from '../services/authService';
import { ShieldAlert, Lock, Mail, ShieldCheck } from 'lucide-react';

export const AdminLogin: React.FC = () => {
  const navigate = useNavigate();
  const { setUser, setToken } = useAuthStore();

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
        if (response.user.role !== 'admin') {
           setError('Access Denied: Not an administrator account.');
           setLoading(false);
           return;
        }
        setToken(response.access_token);
        setUser(response.user);
        setSuccess('Administrator access granted. Redirecting...');
        setTimeout(() => navigate('/admin/dashboard'), 1000);
      }
    } catch (err: any) {
      setError('Invalid admin credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-900 flex flex-col items-center justify-center px-4 relative overflow-hidden">
      {/* Decorative Background Elements */}
      <div className="absolute top-0 left-0 w-full h-full opacity-10 pointer-events-none">
          <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-green-500 rounded-full blur-[120px]"></div>
          <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-emerald-500 rounded-full blur-[120px]"></div>
      </div>

      <div className="z-10 w-full max-w-md">
        <div className="text-center mb-10">
          <div className="w-16 h-16 bg-green-600 rounded-[24px] flex items-center justify-center text-white shadow-2xl mx-auto mb-6 shadow-green-500/20">
            <ShieldCheck size={32} />
          </div>
          <h1 className="text-3xl font-black text-white tracking-tight mb-2">AgriConnect</h1>
          <p className="text-slate-400 font-bold uppercase tracking-[0.2em] text-xs">Admin Control Center</p>
        </div>

        <div className="bg-white/5 backdrop-blur-xl border border-white/10 rounded-[40px] p-8 md:p-12 shadow-2xl">
          <h2 className="text-xl font-black text-white mb-8 italic">Secure Login</h2>

          {error && (
            <div className="bg-red-500/10 border border-red-500/20 text-red-400 p-4 rounded-2xl mb-8 flex items-center gap-3 text-sm font-bold animate-shake">
              <ShieldAlert size={20} /> {error}
            </div>
          )}

          {success && (
            <div className="bg-green-500/10 border border-green-500/20 text-green-400 p-4 rounded-2xl mb-8 flex items-center gap-3 text-sm font-bold">
              <ShieldCheck size={20} /> {success}
            </div>
          )}

          <form onSubmit={handleLogin} className="space-y-6">
            <div className="space-y-2">
              <label className="block text-[10px] font-black text-slate-500 uppercase tracking-widest ml-1">Admin Identity (Mobile)</label>
              <div className="relative group">
                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500 group-focus-within:text-green-500 transition-colors" size={18} />
                <input
                  type="tel"
                  className="w-full bg-slate-800/50 border border-white/5 rounded-[20px] py-4 pl-12 pr-4 text-white outline-none focus:ring-2 focus:ring-green-500/20 focus:border-green-500/50 transition-all font-bold placeholder:text-slate-600"
                  placeholder="9999999999"
                  value={mobile}
                  onChange={(e) => setMobile(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="space-y-2">
               <label className="block text-[10px] font-black text-slate-500 uppercase tracking-widest ml-1">Security Key</label>
               <div className="relative group">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500 group-focus-within:text-green-500 transition-colors" size={18} />
                <input
                  type="password"
                  className="w-full bg-slate-800/50 border border-white/5 rounded-[20px] py-4 pl-12 pr-4 text-white outline-none focus:ring-2 focus:ring-green-500/20 focus:border-green-500/50 transition-all font-bold placeholder:text-slate-600"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-green-600 hover:bg-green-500 text-white py-5 rounded-[24px] font-black text-sm uppercase tracking-widest shadow-xl shadow-green-900/20 transition-all flex items-center justify-center gap-3 mt-4 disabled:opacity-50"
            >
              {loading ? <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div> : 'Initialize Dashboard'}
            </button>
          </form>

          <div className="mt-10 text-center">
             <Link to="/" className="text-slate-500 hover:text-white transition-colors text-xs font-bold uppercase tracking-widest">Return to Marketplace</Link>
          </div>
        </div>

        <p className="mt-12 text-center text-slate-600 text-[10px] font-bold uppercase tracking-[0.3em]">
            © 2026 AgriConnect Systems. All Rights Reserved.
        </p>
      </div>
    </div>
  );
};
