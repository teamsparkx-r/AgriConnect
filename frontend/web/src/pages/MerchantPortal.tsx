import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useLanguageStore } from '../store/languageStore';
import api from '../services/api';
import {
  ShoppingBag, ClipboardList, CreditCard,
  ChevronRight, MapPin, CheckCircle2, ArrowRight, Heart, LayoutGrid, Info
} from 'lucide-react';

export const MerchantPortal: React.FC = () => {
  const navigate = useNavigate();
  const { user, token } = useAuthStore();
  const { t } = useLanguageStore();

  // Role-based access control inside the portal
  useEffect(() => {
    if (token && user) {
      if (user.role === 'farmer') navigate('/farmer/dashboard', { replace: true });
      if (user.role === 'admin') navigate('/admin/dashboard', { replace: true });
    }
  }, [token, user, navigate]);

  const [data, setData] = useState<any>(null);
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, [user?.id]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);

      const requests = [];
      requests.push(api.get('/buyer/home'));

      if (user?.id && token) {
        requests.push(api.get(`/buyer/dashboard/${user.id}`));
      }

      const results = await Promise.allSettled(requests);

      if (results[0].status === 'fulfilled') {
        const productsRes = (results[0] as any).value;
        if (productsRes.data.success) {
          setProducts(productsRes.data.products || []);
        }
      }

      if (requests.length > 1 && results[1].status === 'fulfilled') {
        const dashRes = (results[1] as any).value;
        if (dashRes.data.success) {
          setData(dashRes.data);
        }
      }
    } catch (error) {
      console.error('Portal intelligence audit failed', error);
    } finally {
      setLoading(false);
    }
  };

  const categoriesList = ['vegetables', 'fruits', 'grains', 'pulses', 'spices'];
  const groupedProducts = categoriesList.reduce((acc: any, cat: string) => {
    const items = products.filter(p => p.category.toLowerCase() === cat);
    if (items.length > 0) acc[cat] = items;
    return acc;
  }, {});

  const summary = [
    { label: t.total_bookings, value: data?.summary?.total_bookings || 0, icon: <ShoppingBag size={18} />, color: 'text-green-600 bg-green-50', link: '/merchant/bookings' },
    { label: t.active_bookings, value: data?.summary?.active_bookings || 0, icon: <ClipboardList size={18} />, color: 'text-blue-600 bg-blue-50', link: '/merchant/bookings' },
    { label: t.completed_bookings, value: data?.summary?.completed_bookings || 0, icon: <CheckCircle2 size={18} />, color: 'text-orange-600 bg-orange-50', link: '/merchant/bookings' },
    { label: t.amount_spent, value: `₹${(data?.summary?.amount_spent || 0).toLocaleString()}`, icon: <span className="font-bold text-sm">₹</span>, color: 'text-emerald-600 bg-emerald-50', link: '/merchant/payments' },
  ];

  if (loading) return (
    <div className="flex flex-col justify-center items-center h-[400px]">
       <div className="w-10 h-10 border-4 border-green-100 border-t-green-600 rounded-full animate-spin mb-4"></div>
       <p className="text-gray-400 font-black uppercase tracking-widest text-[9px]">Analyzing Marketplace Intelligence...</p>
    </div>
  );

  return (
    <div className="animate-in fade-in duration-500 pb-20">
      {/* Unified Hero Experience */}
      <div className="grid lg:grid-cols-12 gap-6 mb-10">
         <div className={`${token ? 'lg:col-span-9' : 'lg:col-span-12'} bg-white rounded-[32px] overflow-hidden relative shadow-sm border border-gray-100 group min-h-[300px] md:min-h-[350px] flex flex-col justify-center px-8 md:px-12 text-left`}>
            <img src="https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=1600" className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-all duration-[3s]" alt="" />
            <div className="absolute inset-0 bg-gradient-to-r from-black/60 via-black/20 to-transparent" />
            <div className="relative z-10 max-w-lg">
               <h1 className="text-2xl md:text-4xl font-black text-white leading-tight mb-4 tracking-tighter uppercase">{t.fresh_produce}<br/>{t.direct_from_farmers}</h1>
               <p className="text-white/80 text-xs md:text-sm font-medium mb-8 md:mb-10 leading-relaxed italic">{t.hero_desc}</p>
               <button onClick={() => navigate('/merchant/explore')} className="bg-[#16a34a] text-white px-8 md:px-10 py-3.5 md:py-4 rounded-xl font-black text-[11px] md:text-[13px] uppercase tracking-widest hover:bg-[#15803d] transition-all shadow-xl active:scale-95">Start Discovery</button>
            </div>
         </div>

         {token && (
           <div className="lg:col-span-3 bg-white rounded-[32px] p-8 border border-gray-100 shadow-sm flex flex-col text-left hidden lg:flex">
              <h2 className="text-[11px] font-black text-gray-500 uppercase tracking-[0.2em] mb-10 border-b border-gray-100 pb-2 italic">Account Audit</h2>
              <div className="space-y-8 flex-1">
                 {summary.map((item, i) => (
                   <div key={i} onClick={() => navigate(item.link)} className="flex items-center gap-5 group cursor-pointer">
                      <div className={`w-12 h-12 rounded-full flex items-center justify-center shrink-0 shadow-inner transition-transform group-hover:scale-110 border border-gray-100 ${item.color}`}>{item.icon}</div>
                      <div>
                         <p className="text-[10px] font-black text-gray-400 uppercase tracking-tighter mb-0.5 leading-none group-hover:text-green-600 transition-colors">{item.label}</p>
                         <p className="text-xl font-black text-gray-900 leading-none tracking-tight">{item.value}</p>
                      </div>
                   </div>
                 ))}
              </div>
              <button onClick={() => navigate('/merchant/bookings')} className="mt-10 text-blue-600 font-black text-[10px] uppercase tracking-[0.2em] flex items-center gap-2 hover:underline">Full Activity Registry <ArrowRight size={14}/></button>
           </div>
         )}
      </div>

      {/* Mobile Summary Grid (Only for logged in) */}
      {token && (
        <div className="grid grid-cols-2 gap-4 mb-10 lg:hidden">
            {summary.slice(0, 4).map((item, i) => (
                <div key={i} onClick={() => navigate(item.link)} className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm flex flex-col gap-3 active:scale-95 transition-all">
                    <div className={`w-10 h-10 rounded-full flex items-center justify-center shrink-0 ${item.color}`}>{item.icon}</div>
                    <div>
                        <p className="text-[9px] font-black text-gray-400 uppercase leading-none mb-1">{item.label}</p>
                        <p className="text-lg font-black text-gray-900 leading-none">{item.value}</p>
                    </div>
                </div>
            ))}
        </div>
      )}

      {/* Showrooms */}
      <div className="space-y-16">
        {Object.entries(groupedProducts).map(([category, items]: [string, any]) => (
            <section key={category} className="space-y-8 animate-in slide-in-from-bottom-2 duration-700">
                <div className="flex items-center justify-between border-b border-gray-100 pb-4">
                    <h2 className="text-xl md:text-2xl font-black text-gray-900 capitalize italic flex items-center gap-3 text-left">
                        <div className="w-1.5 h-6 md:h-8 bg-green-500 rounded-full" />
                        {category} {t.showroom}
                    </h2>
                    <button onClick={() => navigate(`/merchant/explore?category=${category}`)} className="text-blue-600 font-black text-[10px] md:text-[11px] uppercase tracking-widest flex items-center gap-1.5 hover:underline">{t.view_more} <ChevronRight size={16}/></button>
                </div>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 gap-4 md:gap-6">
                    {items.map((p: any) => <ProductCard key={p.id} product={p} t={t} onClick={() => navigate(`/merchant/product/${p.id}`)} />)}
                </div>
            </section>
        ))}
      </div>
    </div>
  );
};

const ProductCard = ({ product, t, onClick }: any) => (
   <div
     onClick={onClick}
     className="bg-white rounded-3xl overflow-hidden border border-gray-100 shadow-sm hover:shadow-xl hover:border-green-200 transition-all cursor-pointer group flex flex-col h-full text-left active:scale-[0.98]"
   >
      <div className="relative h-40 md:h-44 overflow-hidden bg-gray-50 border-b border-gray-50">
         <img src={product.images || 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=400'} className="w-full h-full object-cover group-hover:scale-105 transition-all duration-1000" alt="" />
         <div className="absolute top-3 left-3"><span className="bg-white/95 backdrop-blur-sm text-green-700 text-[8px] md:text-[9px] font-black uppercase px-2.5 py-1 rounded-lg border border-green-100 shadow-sm">{t.verified_farm}</span></div>
         <button className="absolute top-3 right-3 p-2.5 bg-white/50 backdrop-blur-md rounded-full text-gray-700 hover:bg-white hover:text-red-500 transition-all shadow-sm opacity-0 md:opacity-100 group-hover:opacity-100"><Heart size={16} /></button>
      </div>
      <div className="p-4 md:p-5 flex-1 flex flex-col">
         <h3 className="text-sm md:text-[15px] font-black text-gray-900 mb-1 group-hover:text-green-600 transition-colors truncate uppercase tracking-tight leading-tight">{product.name}</h3>
         <div className="flex items-center gap-1.5 text-gray-400 text-[9px] md:text-[10px] font-bold italic mb-4 md:mb-5 uppercase tracking-tighter">
            <MapPin size={12} className="text-green-500" /> {product.district}, {product.state}
         </div>
         <div className="mt-auto space-y-4">
            <div className="flex justify-between items-center text-[9px] md:text-[10px] font-black uppercase tracking-widest border-t border-gray-50 pt-4">
               <span className="text-gray-400">{t.inventory}</span>
               <span className="text-green-700">{product.quantity} {product.unit}</span>
            </div>
            <div className="flex items-center justify-between">
               <div>
                  <p className="text-[8px] md:text-[9px] text-gray-400 font-black uppercase mb-0.5 tracking-tighter">{t.bulk_rate}</p>
                  <p className="text-lg md:text-xl font-black text-gray-900 leading-none tracking-tighter">₹{product.expected_price}<span className="text-[10px] md:text-[11px] text-gray-400 font-bold ml-0.5">/{product.unit}</span></p>
               </div>
               <button className="bg-gray-900 text-white px-4 md:px-5 py-2 md:py-2.5 rounded-lg font-black text-[9px] md:text-[10px] uppercase tracking-widest shadow transition-all hover:bg-[#16a34a]">Audit Node</button>
            </div>
         </div>
      </div>
   </div>
);
