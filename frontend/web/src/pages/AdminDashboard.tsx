import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useLanguageStore } from '../store/languageStore';
import api from '../services/api';
import {
  Users, ShoppingBasket, ClipboardList, AlertTriangle,
  IndianRupee, Store, Calendar, CheckCircle2, ChevronDown, Package, Flag
} from 'lucide-react';

export const AdminDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { t } = useLanguageStore();
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    try {
      setLoading(true);
      const response = await api.get('/admin/dashboard');
      if (response.data.success) {
        setData(response.data);
      }
    } catch (error) {
      console.error('Failed to fetch dashboard', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return (
    <div className="flex flex-col justify-center items-center h-[400px]">
       <div className="w-10 h-10 border-4 border-green-100 border-t-green-600 rounded-full animate-spin mb-4"></div>
       <p className="text-gray-400 font-bold uppercase tracking-widest text-[9px]">Initializing Intelligence...</p>
    </div>
  );

  const stats = [
    { title: t.farmers, value: data?.stats?.total_farmers || 0, trend: '8.2% vs last month', icon: <Users size={18} className="text-[#16a34a]" />, tab: 'farmers' },
    { title: t.total_merchants, value: data?.stats?.total_merchants || 0, trend: '5.4% vs last month', icon: <Store size={18} className="text-[#3b82f6]" />, tab: 'merchants' },
    { title: t.active_products, value: data?.stats?.active_products || 0, trend: '12% vs last month', icon: <ShoppingBasket size={18} className="text-[#f59e0b]" />, tab: 'products' },
    { title: t.total_bookings, value: data?.stats?.total_bookings || 0, trend: '14% vs last month', icon: <ClipboardList size={18} className="text-[#8b5cf6]" />, tab: 'bookings' },
    { title: t.booking_revenue, value: `₹${(data?.stats?.total_revenue || 0).toLocaleString()}`, trend: '10% vs last month', icon: <IndianRupee size={18} className="text-[#16a34a]" />, tab: 'payments' },
    { title: t.pending_reports, value: data?.stats?.pending_reports || 0, trend: 'Needs review', icon: <AlertTriangle size={18} className="text-[#ef4444]" />, tab: 'reports', alert: (data?.stats?.pending_reports > 0) },
  ];

  return (
    <div className="space-y-6 animate-in fade-in duration-500 pb-10">
      <div className="flex flex-col lg:flex-row lg:items-end justify-between gap-4">
        <div className="text-left">
           <h1 className="text-2xl font-black text-gray-900 tracking-tight mb-0.5">{t.dashboard}</h1>
           <p className="text-[13px] text-gray-500 font-medium italic">{t.platform_intelligence}</p>
        </div>
        <button onClick={() => alert('Calendar filter active')} className="bg-white border border-gray-200 px-4 py-2 rounded-xl flex items-center gap-2.5 text-[11px] font-black text-gray-600 shadow-sm hover:bg-gray-50 transition-all border-b-2">
           <Calendar size={14} className="text-gray-400"/> 13 Jul — 19 Jul 2026 <ChevronDown size={14} className="text-gray-300" />
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        {stats.map((s, i) => (
          <div
            key={i}
            onClick={() => navigate(`/admin/manage?tab=${s.tab}`)}
            className="bg-white p-5 rounded-[24px] border border-gray-100 shadow-sm hover:shadow-lg hover:-translate-y-0.5 transition-all group overflow-hidden relative cursor-pointer text-left"
          >
             <div className="flex items-center justify-between mb-3 text-left">
                <div className="w-10 h-10 rounded-xl bg-gray-50 flex items-center justify-center group-hover:scale-105 transition-transform text-left">
                   {s.icon}
                </div>
                {s.alert && <div className="w-1.5 h-1.5 bg-red-500 rounded-full animate-ping"></div>}
             </div>
             <div className="text-left">
                <p className="text-[9px] font-black text-gray-400 uppercase tracking-widest mb-1 italic leading-none">{s.title}</p>
                <h3 className="text-xl font-black text-gray-900 leading-none mb-2">{s.value}</h3>
                <p className={`text-[8px] font-black uppercase tracking-tight flex items-center gap-1 ${s.alert ? 'text-red-500' : 'text-[#16a34a]'}`}>
                   {!s.alert && '▲'} {s.trend}
                </p>
             </div>
          </div>
        ))}
      </div>

      <div className="grid lg:grid-cols-12 gap-6">
         <div className="lg:col-span-5 bg-white p-7 rounded-[32px] border border-gray-100 shadow-sm text-left">
            <h2 className="text-base font-black text-gray-900 flex items-center gap-2 italic mb-8">
               <Calendar size={16} className="text-[#16a34a]" /> Bookings History
            </h2>
            <div className="h-[200px] flex items-end justify-between px-2 gap-2">
               {[40, 70, 45, 90, 65, 80, 50].map((h, i) => (
                  <div key={i} className="flex-1 bg-green-50 rounded-t-lg relative group transition-all hover:bg-green-100" style={{height: `${h}%`}}></div>
               ))}
            </div>
         </div>

         <div className="lg:col-span-4 bg-white p-7 rounded-[32px] border border-gray-100 shadow-sm text-left">
            <h2 className="text-base font-black text-gray-900 italic mb-8">Produce Breakdown</h2>
            <div className="flex flex-col items-center gap-6">
               <div className="relative w-36 h-36">
                  <svg viewBox="0 0 36 36" className="w-full h-full transform -rotate-90">
                     <circle cx="18" cy="18" r="15.9" fill="transparent" stroke="#F3F4F6" strokeWidth="5"></circle>
                     <circle cx="18" cy="18" r="15.9" fill="transparent" stroke="#16a34a" strokeWidth="5" strokeDasharray="60 100"></circle>
                  </svg>
                  <div className="absolute inset-0 flex flex-col items-center justify-center">
                     <p className="text-xl font-black text-gray-900 leading-none">{data?.stats?.active_products || 0}</p>
                     <p className="text-[9px] text-gray-400 font-bold uppercase mt-1">Items</p>
                  </div>
               </div>
            </div>
         </div>

         <div className="lg:col-span-3 bg-white p-7 rounded-[32px] border border-gray-100 shadow-sm text-left">
            <h2 className="text-base font-black text-gray-900 italic mb-8">{t.recent_activity}</h2>
            <div className="space-y-6">
               {data?.recent_activity?.slice(0,4).map((item: any, idx: number) => (
                  <div key={idx} onClick={() => navigate(item.type === 'user' ? '/admin/manage?tab=farmers' : '/admin/manage?tab=bookings')} className="flex gap-4 group cursor-pointer border-l-2 border-transparent hover:border-green-500 pl-2 transition-all">
                     <div className="flex-1 min-w-0">
                        <h4 className="text-[12px] font-black text-gray-900 leading-none mb-1 group-hover:text-[#16a34a] truncate">{item.message}</h4>
                        <p className="text-[9px] font-black text-gray-300 uppercase">{new Date(item.time).toLocaleTimeString()}</p>
                     </div>
                  </div>
               ))}
            </div>
         </div>
      </div>

      <div className="grid lg:grid-cols-3 gap-6 text-left">
         <div className="bg-white p-7 rounded-[32px] border border-gray-100 shadow-sm cursor-pointer hover:shadow-md transition-all" onClick={() => navigate('/admin/manage?tab=bookings')}>
            <h2 className="text-base font-black text-gray-900 italic mb-8">Booking Status</h2>
            <div className="space-y-6">
                <Progress label="Confirmed" percent={75} color="bg-green-500" />
                <Progress label="Pending" percent={20} color="bg-yellow-500" />
            </div>
         </div>

         <div className="bg-white p-7 rounded-[32px] border border-gray-100 shadow-sm cursor-pointer hover:shadow-md transition-all" onClick={() => navigate('/admin/manage?tab=farmers')}>
            <h2 className="text-base font-black text-gray-900 italic mb-8">{t.top_districts}</h2>
            <div className="space-y-6">
                <Progress label="Pune, MH" percent={85} color="bg-blue-500" />
                <Progress label="Guntur, AP" percent={70} color="bg-blue-500" />
            </div>
         </div>

         <div className="bg-white p-7 rounded-[32px] border border-gray-100 shadow-sm space-y-4">
            <h2 className="text-base font-black text-gray-900 italic mb-6">Security Alerts</h2>
            <div onClick={() => navigate('/admin/manage?tab=reports')} className="p-4 bg-orange-50 border border-orange-100 rounded-2xl flex items-center gap-4 cursor-pointer hover:bg-orange-100 transition-all">
               <AlertTriangle className="text-orange-600" size={20}/>
               <div>
                  <h4 className="text-[11px] font-black text-orange-900 uppercase">{data?.stats?.pending_reports || 0} Reports Pending</h4>
                  <p className="text-[9px] text-orange-700 font-bold italic">Manual review required</p>
               </div>
            </div>
         </div>
      </div>

      <footer className="pt-6 border-t border-gray-100 flex justify-between items-center text-[10px] text-gray-400 font-bold uppercase tracking-widest px-2">
         <p>© 2026 AgriConnect Intelligence</p>
         <p className="italic opacity-50">v2.3.0</p>
      </footer>
    </div>
  );
};

const Progress = ({ label, percent, color }: any) => (
    <div className="space-y-2">
       <div className="flex justify-between text-[10px] font-black text-gray-700 uppercase italic">
          <span>{label}</span>
          <span>{percent}%</span>
       </div>
       <div className="h-1.5 bg-gray-50 rounded-full overflow-hidden border border-gray-100">
          <div className={`h-full ${color} rounded-full`} style={{ width: `${percent}%` }}></div>
       </div>
    </div>
);
