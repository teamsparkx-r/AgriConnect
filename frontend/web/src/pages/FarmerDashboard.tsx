import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useLanguageStore } from '../store/languageStore';
import api from '../services/api';
import {
  Plus, Package, ClipboardList, IndianRupee, MessageSquare,
  Edit, MapPin, Phone, Leaf, Clock, Bell, Sun, ChevronRight, TrendingUp
} from 'lucide-react';

export const FarmerDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { t, language } = useLanguageStore();
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (user?.id) fetchDashboardData();
  }, [user]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/farmer/dashboard?user_id=${user?.id}`);
      if (response.data.success) setData(response.data);
    } catch (error) {
      console.error('Farmer dashboard fetch failed', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return (
    <div className="flex flex-col justify-center items-center h-[400px]">
       <div className="w-10 h-10 border-4 border-green-100 border-t-green-600 rounded-full animate-spin mb-4"></div>
       <p className="text-gray-400 font-bold uppercase tracking-widest text-[9px]">Analyzing Farm Intelligence...</p>
    </div>
  );

  const stats = [
    { title: language === 'TE' ? 'మొత్తం ఉత్పత్తులు' : 'Total Products', value: data?.stats?.total_products || 0, trend: '+12%', icon: <Package size={18}/>, color: 'green', path: '/farmer/products' },
    { title: language === 'TE' ? 'క్రియాశీల ఉత్పత్తులు' : 'Active Products', value: data?.stats?.active_products || 0, trend: '+8%', icon: <Leaf size={18}/>, color: 'emerald', path: '/farmer/products' },
    { title: language === 'TE' ? 'కోతకు సిద్ధంగా ఉన్నాయి' : 'Harvesting Soon', value: data?.stats?.harvesting_soon || 0, trend: '▲ 2%', icon: <Clock size={18}/>, color: 'orange', path: '/farmer/products' },
    { title: t.total_bookings, value: data?.stats?.total_bookings || 0, trend: '+14%', icon: <ClipboardList size={18}/>, color: 'blue', path: '/farmer/bookings' },
    { title: t.earnings, value: `₹${(data?.stats?.total_earnings || 0).toLocaleString()}`, trend: '+10%', icon: <IndianRupee size={18}/>, color: 'purple', path: '/farmer/bookings' },
    { title: language === 'TE' ? 'చదవని సందేశాలు' : 'Unread Messages', value: data?.stats?.unread_messages || 0, trend: 'View', icon: <MessageSquare size={18}/>, color: 'red', path: '/farmer/bookings' },
  ];

  return (
    <div className="space-y-10 animate-in fade-in duration-500 pb-20 text-left">
      {/* GREETING */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
        <div>
           <h1 className="text-3xl font-black text-gray-900 tracking-tight uppercase tracking-tighter">
             {language === 'TE' ? 'శుభోదయం' : language === 'HI' ? 'सुप्रभात' : 'Good Morning'}, {user?.full_name?.split(' ')[0] || 'Farmer'}! 👋
           </h1>
           <p className="text-[14px] text-gray-500 font-medium italic mt-1">Operational intelligence for your agricultural enterprise.</p>
        </div>
        <div className="flex items-center gap-4">
           <div className="bg-white border border-gray-200 px-5 py-3 rounded-2xl flex items-center gap-4 shadow-sm">
              <div className="w-10 h-10 bg-orange-50 rounded-xl flex items-center justify-center text-orange-500 shadow-inner"><Sun size={20}/></div>
              <div>
                 <p className="text-sm font-black text-gray-900">28°C</p>
                 <p className="text-[10px] font-bold text-gray-400 uppercase tracking-widest leading-none">Guntur, AP</p>
              </div>
           </div>
           <button
             onClick={() => navigate('/farmer/add-product')}
             className="bg-[#16a34a] text-white px-8 py-3.5 rounded-xl font-black text-xs hover:bg-[#15803d] transition-all shadow-xl shadow-green-900/20 flex items-center gap-3 uppercase tracking-widest"
           >
             <Plus size={18}/> {language === 'TE' ? 'కొత్త ఉత్పత్తిని జోడించండి' : 'Add New Product'}
           </button>
        </div>
      </div>

      {/* STAT CARDS */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-5">
         {stats.map((s, i) => (
            <div key={i} onClick={() => navigate(s.path)} className="bg-white p-6 rounded-[28px] border border-gray-100 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all group overflow-hidden relative cursor-pointer">
               <div className="flex items-center justify-between mb-4">
                  <div className={`w-12 h-12 rounded-2xl flex items-center justify-center transition-transform group-hover:scale-110 shadow-inner ${
                     s.color === 'green' ? 'bg-green-50 text-green-600' :
                     s.color === 'emerald' ? 'bg-emerald-50 text-emerald-600' :
                     s.color === 'orange' ? 'bg-orange-50 text-orange-600' :
                     s.color === 'blue' ? 'bg-blue-50 text-blue-600' :
                     s.color === 'purple' ? 'bg-purple-50 text-purple-600' : 'bg-red-50 text-red-600'
                  }`}>
                     {s.icon}
                  </div>
                  <span className={`text-[10px] font-black uppercase ${s.color === 'red' ? 'text-blue-500' : 'text-green-500'}`}>{s.trend}</span>
               </div>
               <div>
                  <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1 italic leading-none">{s.title}</p>
                  <h3 className="text-2xl font-black text-gray-900 leading-none tracking-tight">{s.value}</h3>
               </div>
               <div className="mt-4 h-1 w-full bg-gray-50 rounded-full overflow-hidden">
                  <div className={`h-full opacity-20 ${
                     s.color === 'green' ? 'bg-green-600' : s.color === 'emerald' ? 'bg-emerald-600' : s.color === 'orange' ? 'bg-orange-600' : s.color === 'blue' ? 'bg-blue-600' : s.color === 'purple' ? 'bg-purple-600' : 'bg-red-600'
                  }`} style={{width: '60%'}}></div>
               </div>
            </div>
         ))}
      </div>

      {/* MAIN GRID */}
      <div className="grid lg:grid-cols-12 gap-8">
         {/* Recent Products */}
         <div className="lg:col-span-5 bg-white p-8 rounded-[32px] border border-gray-100 shadow-sm flex flex-col">
            <div className="flex items-center justify-between mb-10 border-b border-gray-50 pb-4">
               <h2 className="text-xl font-black text-gray-900 italic uppercase tracking-tighter flex items-center gap-3">
                  <div className="w-1.5 h-6 bg-green-500 rounded-full" />
                  {t.my_products}
               </h2>
               <Link to="/farmer/products" className="text-blue-600 font-black text-[10px] uppercase tracking-widest hover:underline">Full Inventory →</Link>
            </div>
            <div className="space-y-6 flex-1">
               {(data?.recent_products || []).map((p: any, i: number) => (
                  <div key={i} className="flex gap-5 group cursor-pointer border-b border-gray-50 pb-6 last:border-0 hover:bg-gray-50/50 p-2 rounded-2xl transition-all">
                     <div className="w-16 h-16 bg-gray-100 rounded-2xl overflow-hidden shrink-0 border border-gray-100">
                        <img src={p.images || 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=200'} className="w-full h-full object-cover group-hover:scale-105 transition-all duration-700" alt="" />
                     </div>
                     <div className="flex-1 min-w-0 py-1">
                        <div className="flex items-center gap-3 mb-1.5">
                           <h4 className="text-[15px] font-black text-gray-900 group-hover:text-[#16a34a] truncate uppercase tracking-tight">{p.name}</h4>
                           <span className={`text-[9px] font-black uppercase px-2.5 py-1 rounded-lg border ${p.status === 'active' ? 'bg-green-50 text-green-700 border-green-100' : 'bg-orange-50 text-orange-700 border-orange-100'}`}>
                              {p.status}
                           </span>
                        </div>
                        <p className="text-[11px] font-bold text-gray-400 italic uppercase tracking-tighter">{p.quantity} {p.unit} • ₹{p.expected_price}/{p.unit}</p>
                     </div>
                     <div className="flex items-center opacity-0 group-hover:opacity-100 transition-opacity">
                        <ChevronRight size={20} className="text-gray-300"/>
                     </div>
                  </div>
               ))}
            </div>
         </div>

         {/* Recent Bookings */}
         <div className="lg:col-span-4 bg-white p-8 rounded-[32px] border border-gray-100 shadow-sm flex flex-col">
            <div className="flex items-center justify-between mb-10 border-b border-gray-50 pb-4">
               <h2 className="text-xl font-black text-gray-900 italic uppercase tracking-tighter flex items-center gap-3">
                  <div className="w-1.5 h-6 bg-blue-500 rounded-full" />
                  {t.recent_inquiries}
               </h2>
               <Link to="/farmer/bookings" className="text-blue-600 font-black text-[10px] uppercase tracking-widest hover:underline">View All →</Link>
            </div>
            <div className="space-y-6 flex-1">
               {(data?.recent_bookings || []).map((b: any, i: number) => (
                  <div key={i} className="flex gap-5 group cursor-pointer border-b border-gray-50 pb-6 last:border-0 hover:bg-gray-50/50 p-2 rounded-2xl transition-all items-center">
                     <div className="w-12 h-12 bg-blue-50 rounded-2xl flex items-center justify-center text-blue-600 shrink-0 shadow-inner"><ClipboardList size={22}/></div>
                     <div className="flex-1 min-w-0">
                        <h4 className="text-[14px] font-black text-gray-900 truncate uppercase tracking-tight leading-none mb-1">{b.product_name}</h4>
                        <p className="text-[11px] font-bold text-gray-400 italic leading-none">Buyer: {b.buyer_name}</p>
                     </div>
                     <button className="bg-white border border-green-100 text-[#16a34a] px-4 py-2 rounded-xl text-[10px] font-black uppercase flex items-center gap-2 hover:bg-[#16a34a] hover:text-white transition-all shadow-sm">
                        <Phone size={12}/> {language === 'TE' ? 'కాల్' : 'Call'}
                     </button>
                  </div>
               ))}
            </div>
         </div>

         {/* Alerts */}
         <div className="lg:col-span-3 bg-[#131921] p-8 rounded-[32px] shadow-2xl flex flex-col text-left relative overflow-hidden group">
            <div className="absolute top-0 right-0 w-32 h-32 bg-white/5 rounded-full -mr-16 -mt-16 group-hover:scale-110 transition-transform duration-1000"></div>
            <h2 className="text-lg font-black text-white italic uppercase tracking-tighter mb-10 relative z-10">Audit Alerts</h2>
            <div className="space-y-8 flex-1 relative z-10">
               {(data?.notifications || []).slice(0, 5).map((n: any, i: number) => (
                  <div key={i} className="flex gap-4 group cursor-pointer items-start">
                     <div className="w-10 h-10 rounded-xl flex items-center justify-center shrink-0 bg-white/5 text-gray-400 border border-white/5 shadow-inner group-hover:text-green-400 transition-colors"><Bell size={18}/></div>
                     <div className="flex-1 min-w-0">
                        <h4 className="text-[12px] font-black text-white/90 truncate group-hover:text-green-400 transition-colors uppercase leading-none mb-1.5 tracking-tight">{n.title}</h4>
                        <p className="text-[11px] text-gray-500 font-bold italic truncate leading-none uppercase tracking-tighter">{n.message}</p>
                     </div>
                  </div>
               ))}
               {(!data?.notifications || data.notifications.length === 0) && <p className="text-[11px] text-gray-600 italic">System monitoring active...</p>}
            </div>
         </div>
      </div>
    </div>
  );
};
