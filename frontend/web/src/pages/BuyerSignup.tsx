import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useLanguageStore } from '../store/languageStore';
import { buyerAuth } from '../services/authService';
import { ShieldCheck, ShoppingBag, ArrowLeft } from 'lucide-react';

export const BuyerSignup: React.FC = () => {
  const navigate = useNavigate();
  const { setUser, setToken } = useAuthStore();
  const { t } = useLanguageStore();

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [mobile, setMobile] = useState('');
  const [formData, setFormData] = useState({
    full_name: '',
    password: '',
    location: '',
    confirmPassword: '',
  });

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await buyerAuth.register({
        mobile,
        full_name: formData.full_name,
        password: formData.password,
        location: formData.location,
      });

      if (response && response.access_token) {
        setToken(response.access_token);
        setUser(response.user);
        setTimeout(() => navigate('/merchant/portal'), 500);
      }
    } catch (err: any) {
      setError(err.response?.data?.detail || 'Merchant registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#F8FAF9] flex flex-col items-center pt-12 px-4 pb-20 font-sans">
      <Link to="/" className="flex items-center gap-2 mb-12 group">
        <div className="w-10 h-10 bg-[#16a34a] rounded-xl flex items-center justify-center text-white shadow-lg shadow-green-100 group-hover:rotate-12 transition-transform">
          <span className="text-xl font-black">A</span>
        </div>
        <h1 className="text-xl font-black text-gray-900 tracking-tighter uppercase">AgriConnect</h1>
      </Link>

      <div className="w-full max-w-[400px]">
        <div className="bg-white border border-gray-100 rounded-[32px] p-10 shadow-2xl shadow-green-900/5 text-left relative overflow-hidden">
          <div className="absolute top-0 right-0 w-24 h-24 bg-blue-50 rounded-full -mr-12 -mt-12 opacity-50" />

          <h2 className="text-2xl font-black text-gray-900 mb-2 flex items-center gap-3 italic">
             <ShoppingBag size={24} className="text-blue-600" /> {t.join_now}
          </h2>
          <p className="text-gray-400 text-xs font-medium italic mb-8">Join as a verified merchant to source premium quality produce directly.</p>

          {error && (
            <div className="bg-red-50 border border-red-100 p-4 mb-8 rounded-2xl flex items-start gap-3">
              <ShieldCheck className="text-red-600 shrink-0 mt-0.5" size={16} />
              <p className="text-red-700 text-xs font-bold leading-relaxed">{error}</p>
            </div>
          )}

          <form onSubmit={handleSignup} className="space-y-5">
            <div className="space-y-2">
              <label className="block text-[10px] font-black text-gray-400 uppercase tracking-widest italic">{t.personal_details}</label>
              <input
                type="text"
                className="w-full bg-gray-50 border border-gray-100 rounded-xl py-3 px-4 outline-none focus:ring-2 focus:ring-green-500/10 font-bold text-xs"
                placeholder="Merchant Full Name"
                value={formData.full_name}
                onChange={(e) => setFormData({ ...formData, full_name: e.target.value })}
                required
              />
            </div>

            <div className="space-y-2">
              <label className="block text-[10px] font-black text-gray-400 uppercase tracking-widest italic">{t.mobile_number}</label>
              <input
                type="tel"
                className="w-full bg-gray-50 border border-gray-100 rounded-xl py-3 px-4 outline-none focus:ring-2 focus:ring-green-500/10 font-bold text-xs"
                placeholder="10 digit mobile"
                value={mobile}
                onChange={(e) => setMobile(e.target.value)}
                maxLength={10}
                required
              />
            </div>

            <div className="space-y-2">
              <label className="block text-[10px] font-black text-gray-400 uppercase tracking-widest italic">{t.location}</label>
              <input
                type="text"
                className="w-full bg-gray-50 border border-gray-100 rounded-xl py-3 px-4 outline-none focus:ring-2 focus:ring-green-500/10 font-bold text-xs"
                placeholder="City / District"
                value={formData.location}
                onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                required
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
               <div className="space-y-2">
                 <label className="block text-[10px] font-black text-gray-400 uppercase tracking-widest italic">Password</label>
                 <input
                   type="password"
                   className="w-full bg-gray-50 border border-gray-100 rounded-xl py-3 px-4 outline-none font-bold text-xs"
                   value={formData.password}
                   onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                   required
                 />
               </div>
               <div className="space-y-2">
                 <label className="block text-[10px] font-black text-gray-400 uppercase tracking-widest italic">Confirm</label>
                 <input
                   type="password"
                   className="w-full bg-gray-50 border border-gray-100 rounded-xl py-3 px-4 outline-none font-bold text-xs"
                   value={formData.confirmPassword}
                   onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                   required
                 />
               </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-gray-900 hover:bg-[#16a34a] text-white py-4 rounded-xl font-black text-[11px] uppercase tracking-widest shadow-xl transition-all mt-4"
            >
              {loading ? 'Initializing Profile...' : 'Join as Merchant'}
            </button>

            <div className="pt-8 text-center">
               <Link to="/login" className="text-[10px] font-black text-gray-400 uppercase tracking-widest hover:text-[#16a34a] flex items-center justify-center gap-2">
                 Already have an account? <span className="text-[#16a34a] underline">Sign In Now</span>
               </Link>
            </div>
          </form>
        </div>

        <button onClick={() => navigate(-1)} className="mt-8 flex items-center gap-2 text-gray-400 hover:text-gray-900 transition-colors mx-auto font-black uppercase tracking-widest text-[9px]">
           <ArrowLeft size={14}/> Back to Marketplace
        </button>
      </div>
    </div>
  );
};
