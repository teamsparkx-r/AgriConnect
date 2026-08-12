import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import api from '../services/api';
import { User, Mail, MapPin, Shield, CheckCircle, Save, Store, ArrowLeft, Phone, LogOut } from 'lucide-react';

export const Profile: React.FC = () => {
  const navigate = useNavigate();
  const { user, token, setUser, logout } = useAuthStore();
  const [profile, setProfile] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [formData, setFormData] = useState({
    full_name: '',
    email: '',
    state: '',
    district: '',
    village: '',
    farm_address: '',
    share_farm_address: true,
  });

  useEffect(() => {
    if (!token) {
      navigate('/login');
      return;
    }
    fetchProfile();
  }, [token, navigate]);

  const fetchProfile = async () => {
    if (!user) return;
    try {
      setLoading(true);
      const endpoint = user.role === 'farmer' ? `/farmer/profile/${user.id}` : `/buyer/profile/${user.id}`;
      const response = await api.get(endpoint);
      if (response.data.success) {
        const p = response.data.profile;
        setProfile(p);
        setFormData({
          full_name: p.full_name || '',
          email: p.email || '',
          state: p.state || '',
          district: p.district || '',
          village: p.village || '',
          farm_address: p.farm_address || '',
          share_farm_address: p.share_farm_address ?? true,
        });
      }
    } catch (err) {
      setError('Failed to load identity record');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;
    setSaving(true);
    setError('');
    setSuccess('');

    try {
      const endpoint = user.role === 'farmer' ? `/farmer/profile/${user.id}` : `/buyer/profile/${user.id}`;
      const response = await api.put(endpoint, formData);
      if (response.data.success) {
        setSuccess('Profile updated successfully');
        if (user) {
          setUser({ ...user, full_name: formData.full_name });
        }
      }
    } catch (err) {
      setError('Failed to commit profile updates');
    } finally {
      setSaving(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  if (loading) return (
    <div className="flex justify-center items-center h-[400px]">
      <div className="w-10 h-10 border-4 border-green-100 border-t-green-600 rounded-full animate-spin"></div>
    </div>
  );

  return (
    <div className="animate-in fade-in duration-500 pb-20 text-left">
      <header className="mb-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
           <h1 className="text-3xl font-black text-gray-900 mb-1 uppercase tracking-tight italic">Account Intelligence</h1>
           <p className="text-gray-500 font-medium italic text-[13px]">Manage your personal and professional identity on the AgriConnect supply network.</p>
        </div>
        <button
          onClick={handleLogout}
          className="bg-red-50 text-red-600 px-6 py-3 rounded-xl font-black text-[11px] uppercase tracking-widest flex items-center gap-2 hover:bg-red-100 transition-all shadow-sm border border-red-100"
        >
          <LogOut size={16}/> Sign Out Registry
        </button>
      </header>

      <div className="max-w-4xl">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden">
          <div className="bg-[#002f1a] p-10 text-white relative overflow-hidden">
            <div className="absolute top-0 right-0 w-40 h-40 bg-white/5 rounded-full -mr-20 -mt-20"></div>
            <div className="flex items-center gap-8 relative z-10">
              <div className="w-20 h-20 bg-white/10 backdrop-blur-md rounded-3xl flex items-center justify-center border border-white/10 shadow-2xl">
                <User size={40} className="text-[#16a34a]" />
              </div>
              <div>
                <h2 className="text-3xl font-black tracking-tight uppercase">{profile?.full_name}</h2>
                <p className="text-gray-400 font-black uppercase tracking-[0.2em] text-[10px] flex items-center gap-2 mt-2">
                   <Shield size={14} className="text-green-500"/> {user?.role.toUpperCase()} NETWORK REGISTRY
                </p>
              </div>
            </div>
          </div>

          <div className="p-8 md:p-12">
            {error && <div className="bg-red-50 text-red-700 p-4 rounded-xl mb-10 text-xs font-bold uppercase tracking-widest border border-red-100">{error}</div>}
            {success && <div className="bg-green-50 text-green-700 p-4 rounded-xl mb-10 text-xs font-black uppercase tracking-widest flex items-center gap-2 border border-green-100"><CheckCircle size={18}/> {success}</div>}

            <form onSubmit={handleSave} className="space-y-12">
              <div className="grid md:grid-cols-2 gap-12">
                <div className="space-y-8">
                  <h3 className="text-[11px] font-black text-gray-400 uppercase tracking-[0.2em] italic border-b border-gray-100 pb-2">Core Identity</h3>
                  <div className="space-y-6">
                    <div className="space-y-2">
                       <label className="text-[10px] font-black text-gray-500 uppercase tracking-widest">Display Name</label>
                       <div className="relative">
                          <User className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-300" size={16} />
                          <input type="text" className="w-full bg-gray-50 border border-gray-100 rounded-xl py-3.5 pl-12 pr-4 font-bold text-xs outline-none focus:ring-2 focus:ring-[#16a34a]/10 focus:bg-white transition-all" value={formData.full_name} onChange={(e) => setFormData({...formData, full_name: e.target.value})} required />
                       </div>
                    </div>
                    <div className="space-y-2">
                       <label className="text-[10px] font-black text-gray-500 uppercase tracking-widest">Email Audit</label>
                       <div className="relative">
                          <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-300" size={16} />
                          <input type="email" className="w-full bg-gray-50 border border-gray-100 rounded-xl py-3.5 pl-12 pr-4 font-bold text-xs outline-none focus:ring-2 focus:ring-[#16a34a]/10 focus:bg-white transition-all" value={formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})} placeholder="registry@email.com" />
                       </div>
                    </div>
                  </div>
                </div>

                <div className="space-y-8">
                  <h3 className="text-[11px] font-black text-gray-400 uppercase tracking-[0.2em] italic border-b border-gray-100 pb-2">Location Logistics</h3>
                  <div className="grid grid-cols-2 gap-6">
                    <div className="space-y-2">
                       <label className="text-[10px] font-black text-gray-500 uppercase tracking-widest">State Node</label>
                       <input type="text" className="w-full bg-gray-50 border border-gray-100 rounded-xl py-3.5 px-5 font-bold text-xs outline-none focus:ring-2 focus:ring-[#16a34a]/10 focus:bg-white transition-all" value={formData.state} onChange={(e) => setFormData({...formData, state: e.target.value})} />
                    </div>
                    <div className="space-y-2">
                       <label className="text-[10px] font-black text-gray-500 uppercase tracking-widest">District Cell</label>
                       <input type="text" className="w-full bg-gray-50 border border-gray-100 rounded-xl py-3.5 px-5 font-bold text-xs outline-none focus:ring-2 focus:ring-[#16a34a]/10 focus:bg-white transition-all" value={formData.district} onChange={(e) => setFormData({...formData, district: e.target.value})} />
                    </div>
                  </div>
                </div>
              </div>

              {user?.role === 'farmer' && (
                <div className="bg-[#EAEDED] rounded-2xl p-10 space-y-8 border border-gray-200">
                  <h3 className="text-[11px] font-black text-gray-900 uppercase tracking-[0.2em] italic flex items-center gap-3 border-b border-gray-300 pb-3"><Store size={18} className="text-[#16a34a]"/> Agricultural Production Unit</h3>
                  <div className="grid md:grid-cols-2 gap-10">
                     <div className="space-y-3">
                        <label className="text-[10px] font-black text-gray-500 uppercase tracking-widest">Village / Farm Origin</label>
                        <input type="text" className="w-full bg-white border border-gray-200 rounded-xl py-3.5 px-5 font-bold text-xs outline-none shadow-inner" value={formData.village} onChange={(e) => setFormData({...formData, village: e.target.value})} />
                     </div>
                     <div className="space-y-3">
                        <label className="text-[10px] font-black text-gray-500 uppercase tracking-widest">Exact Pickup Registry</label>
                        <textarea className="w-full bg-white border border-gray-200 rounded-xl py-3.5 px-5 font-bold text-xs outline-none shadow-inner min-h-[100px]" value={formData.farm_address} onChange={(e) => setFormData({...formData, farm_address: e.target.value})} placeholder="Point of collection for logistics..." />
                     </div>
                  </div>
                </div>
              )}

              <div className="pt-10 border-t border-gray-100 flex justify-end">
                <button type="submit" disabled={saving} className="bg-[#002f1a] text-white px-12 py-4 rounded-xl font-black uppercase tracking-[0.2em] text-[11px] shadow-2xl hover:bg-[#16a34a] transition-all flex items-center gap-4">
                  {saving ? <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div> : <Save size={20}/>}
                  Commit Intelligence Update
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};
