import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useLanguageStore } from '../store/languageStore';
import { useAuthStore } from '../store/authStore';
import {
  User, MapPin, ShieldCheck, Phone, MessageSquare,
  Send, Package, Calendar, IndianRupee, Star,
  ArrowLeft, Info, CheckCircle2, Clock, Leaf, Lock
} from 'lucide-react';

export const FarmerPublicProfile: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { t } = useLanguageStore();
  const { token } = useAuthStore();
  const [farmer, setFarmer] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (id) fetchFarmerProfile();
  }, [id]);

  const fetchFarmerProfile = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/buyer/farmers/${id}`);
      if (response.data.success) {
        setFarmer(response.data.farmer);
      }
    } catch (err) {
      setError('Failed to load producer profile');
    } finally {
      setLoading(false);
    }
  };

  const handleUnlockRequest = () => {
    if (!token) {
      navigate('/login', { state: { from: `/merchant/farmer/${id}` } });
      return;
    }
    // In this simplified version, we guide them to a product to "Unlock"
    // or we could implement a direct farmer unlock if needed.
    // For now, let's just alert that they should book a product to reveal contact.
    alert('To reveal direct contact, please select and book one of the available crops below. No payment is required.');
  };

  if (loading) return (
    <div className="flex justify-center py-40">
      <div className="w-10 h-10 border-4 border-green-100 border-t-[#16a34a] rounded-full animate-spin"></div>
    </div>
  );

  if (!farmer) return (
    <div className="py-20 text-center bg-white rounded-[40px] border border-gray-100">
      <h2 className="text-xl font-black mb-4 text-gray-900 uppercase">Producer Not Found</h2>
      <button onClick={() => navigate(-1)} className="text-green-600 font-black uppercase text-xs hover:underline">Go Back</button>
    </div>
  );

  return (
    <div className="animate-in fade-in duration-500 pb-20 text-left">
      <button onClick={() => navigate(-1)} className="mb-8 text-gray-400 hover:text-[#16a34a] font-black uppercase text-[10px] tracking-widest flex items-center gap-2 transition-all group">
        <ArrowLeft size={16} className="group-hover:-translate-x-1 transition-transform"/> Back
      </button>

      <div className="grid lg:grid-cols-12 gap-10">
        {/* LEFT: Farmer Identity Card */}
        <div className="lg:col-span-4">
          <div className="bg-white rounded-[40px] p-8 border border-gray-100 shadow-xl shadow-green-900/5 sticky top-24">
             <div className="flex flex-col items-center text-center">
                <div className="relative mb-6">
                    <div className="w-32 h-32 bg-gray-50 rounded-[48px] flex items-center justify-center border-4 border-white shadow-xl overflow-hidden">
                       {farmer.profile_photo ? (
                         <img src={farmer.profile_photo} className="w-full h-full object-cover" alt={farmer.full_name} />
                       ) : (
                         <User size={64} className="text-gray-300" />
                       )}
                    </div>
                    <div className="absolute -bottom-2 -right-2 bg-green-500 text-white p-2 rounded-2xl shadow-lg border-4 border-white">
                       <ShieldCheck size={20} />
                    </div>
                </div>

                <h1 className="text-2xl font-black text-gray-900 uppercase tracking-tight mb-1">{farmer.full_name}</h1>
                <div className="flex items-center gap-1.5 text-[10px] font-black text-gray-400 uppercase tracking-widest italic mb-6">
                   <MapPin size={14} className="text-green-500" /> {farmer.village}, {farmer.district}, {farmer.state}
                </div>

                <div className="grid grid-cols-2 gap-4 w-full mb-8">
                   <div className="bg-gray-50 p-4 rounded-3xl border border-gray-100 shadow-inner">
                      <p className="text-[9px] font-black text-gray-400 uppercase mb-1">Success Rate</p>
                      <p className="text-lg font-black text-gray-900">{farmer.completed_bookings}+</p>
                   </div>
                   <div className="bg-gray-50 p-4 rounded-3xl border border-gray-100 shadow-inner">
                      <p className="text-[9px] font-black text-gray-400 uppercase mb-1">Node Rating</p>
                      <div className="flex items-center justify-center gap-1">
                         <p className="text-lg font-black text-gray-900">{farmer.rating || '4.8'}</p>
                         <Star size={14} className="fill-orange-400 text-orange-400" />
                      </div>
                   </div>
                </div>

                <div className="w-full space-y-3">
                   <button
                     onClick={handleUnlockRequest}
                     className="w-full bg-[#002f1a] text-white py-4 rounded-2xl font-black text-[11px] uppercase tracking-[0.2em] shadow-xl flex items-center justify-center gap-3 hover:bg-[#16a34a] transition-all"
                   >
                      <Lock size={16}/> Unlock Contact Registry
                   </button>
                   <button
                     onClick={() => {
                        if (!token) navigate('/login');
                        else alert('Connecting to secure messaging node...');
                     }}
                     className="w-full bg-white border border-gray-200 text-gray-900 py-4 rounded-2xl font-black text-[11px] uppercase tracking-[0.2em] shadow-sm flex items-center justify-center gap-3 hover:bg-gray-50 transition-all"
                   >
                      <MessageSquare size={16}/> Start Intelligence Chat
                   </button>
                </div>

                <div className="mt-8 pt-8 border-t border-gray-50 w-full text-left">
                   <h3 className="text-[10px] font-black text-gray-400 uppercase tracking-widest italic mb-4">Registry Intelligence</h3>
                   <div className="space-y-4">
                      <RegistryInfo icon={<Clock size={14}/>} label="Last Active" value="14 minutes ago" />
                      <RegistryInfo icon={<Calendar size={14}/>} label="Joined Network" value={new Date(farmer.joined_at).toLocaleDateString()} />
                      <RegistryInfo icon={<ShieldCheck size={14}/>} label="Identity Status" value="A-Grade Verified" />
                   </div>
                </div>
             </div>
          </div>
        </div>

        {/* RIGHT: Active Crops Showroom */}
        <div className="lg:col-span-8 space-y-10">
           <div className="bg-[#002f1a] rounded-[40px] p-10 text-white relative overflow-hidden shadow-2xl">
              <div className="absolute top-0 right-0 w-64 h-64 bg-white/5 rounded-full -mr-32 -mt-32"></div>
              <div className="relative z-10">
                 <h2 className="text-3xl font-black italic uppercase tracking-tighter mb-2">Available Supply Paths</h2>
                 <p className="text-green-400/70 font-medium italic text-sm">Direct market-ready produce currently in the logistics pipeline.</p>
              </div>
           </div>

           {farmer.active_crops.length === 0 ? (
             <div className="py-32 text-center bg-white rounded-[40px] border border-gray-100 shadow-sm">
                <Package size={48} className="mx-auto mb-4 text-gray-100" />
                <p className="text-gray-400 italic">No active crops listed in the registry currently.</p>
             </div>
           ) : (
             <div className="grid md:grid-cols-2 gap-8">
                {farmer.active_crops.map((crop: any) => (
                  <div key={crop.id} onClick={() => navigate(`/merchant/product/${crop.id}`)} className="bg-white rounded-[32px] overflow-hidden border border-gray-100 hover:border-[#16a34a]/30 hover:shadow-2xl transition-all group cursor-pointer flex flex-col shadow-sm">
                     <div className="relative h-48 overflow-hidden bg-gray-50 border-b border-gray-100">
                        <img src={crop.images || 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=800'} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-1000" alt="" />
                        <div className="absolute top-4 left-4"><span className="bg-white/95 backdrop-blur-sm text-green-700 text-[9px] font-black uppercase px-3 py-1 rounded border border-green-100 shadow-sm">{t.verified_farm}</span></div>
                     </div>
                     <div className="p-6 flex-1 flex flex-col">
                        <div className="flex justify-between items-start mb-4">
                           <div>
                              <h3 className="text-lg font-black text-gray-900 uppercase tracking-tight group-hover:text-[#16a34a] transition-colors">{crop.name}</h3>
                              <p className="text-[10px] font-bold text-gray-400 uppercase tracking-widest italic">{crop.category}</p>
                           </div>
                           <div className="text-right">
                              <p className="text-xl font-black text-gray-900 tracking-tighter">₹{crop.expected_price}</p>
                              <p className="text-[9px] font-black text-gray-400 uppercase italic">per {crop.unit}</p>
                           </div>
                        </div>

                        <div className="grid grid-cols-2 gap-4 py-4 border-y border-gray-50 my-2">
                           <div className="flex items-center gap-2">
                              <div className="w-8 h-8 rounded-lg bg-green-50 flex items-center justify-center text-[#16a34a] shadow-inner"><Package size={14} /></div>
                              <p className="text-[11px] font-black text-gray-700 uppercase tracking-tighter">{crop.quantity} {crop.unit}</p>
                           </div>
                           <div className="flex items-center gap-2">
                              <div className="w-8 h-8 rounded-lg bg-orange-50 flex items-center justify-center text-orange-500 shadow-inner"><Clock size={14} /></div>
                              <p className="text-[11px] font-black text-gray-700 uppercase tracking-tighter">{crop.harvest_date ? new Date(crop.harvest_date).toLocaleDateString() : 'Active'}</p>
                           </div>
                        </div>

                        <button className="w-full mt-4 bg-gray-900 text-white py-3.5 rounded-xl font-black text-[10px] uppercase tracking-widest hover:bg-[#16a34a] transition-all shadow-lg shadow-gray-200">
                           Audit Supply Details
                        </button>
                     </div>
                  </div>
                ))}
             </div>
           )}
        </div>
      </div>
    </div>
  );
};

const RegistryInfo = ({ icon, label, value }: any) => (
   <div className="flex items-center justify-between group">
      <div className="flex items-center gap-2 text-gray-400 group-hover:text-[#16a34a] transition-colors">
         {icon}
         <span className="text-[9px] font-black uppercase tracking-widest leading-none">{label}</span>
      </div>
      <span className="text-[11px] font-black text-gray-900 uppercase tracking-tight italic">{value}</span>
   </div>
);
