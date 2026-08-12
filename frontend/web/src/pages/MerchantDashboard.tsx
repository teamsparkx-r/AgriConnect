import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import api from '../services/api';
import {
  ShoppingBag, ClipboardList, CreditCard, MessageSquare,
  ChevronRight, MapPin, CheckCircle2, ArrowRight, Heart, LayoutGrid
} from 'lucide-react';

export const MerchantDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();

  const [data, setData] = useState<any>(null);
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (user?.id) {
      fetchDashboard();
    }
  }, [user]);

  const fetchDashboard = async () => {
    try {
      setLoading(true);
      const [dashRes, productsRes] = await Promise.all([
        api.get(`/buyer/dashboard/${user?.id}`),
        api.get('/buyer/home')
      ]);

      if (dashRes.data.success) {
        setData(dashRes.data);
      }
      if (productsRes.data.success) {
        setProducts(productsRes.data.products || []);
      }
    } catch (error) {
      console.error('Failed to fetch merchant dashboard', error);
    } finally {
      setLoading(false);
    }
  };

  // Group products by category
  const categories = ['vegetables', 'fruits', 'grains', 'pulses', 'spices'];
  const groupedProducts = categories.reduce((acc: any, cat: string) => {
    const items = products.filter(p => p.category.toLowerCase() === cat);
    if (items.length > 0) acc[cat] = items;
    return acc;
  }, {});

  const summary = [
    { label: 'Total Bookings', value: data?.summary?.total_bookings || 0, icon: <ShoppingBag size={20} />, color: 'text-green-600 bg-green-50', link: '/merchant/bookings' },
    { label: 'Active Bookings', value: data?.summary?.active_bookings || 0, icon: <ClipboardList size={20} />, color: 'text-blue-600 bg-blue-50', link: '/merchant/bookings' },
    { label: 'Completed Bookings', value: data?.summary?.completed_bookings || 0, icon: <CheckCircle2 size={20} />, color: 'text-orange-600 bg-orange-50', link: '/merchant/bookings' },
    { label: 'Amount Spent', value: `₹${(data?.summary?.amount_spent || 0).toLocaleString()}`, icon: <span className="text-lg font-bold">₹</span>, color: 'text-emerald-600 bg-emerald-50', link: '/merchant/payments' },
  ];

  if (loading) return (
    <div className="flex flex-col justify-center items-center h-[500px]">
       <div className="w-10 h-10 border-4 border-green-100 border-t-green-600 rounded-full animate-spin mb-4"></div>
       <p className="text-gray-400 font-black uppercase tracking-widest text-[9px]">Loading Fresh Marketplace...</p>
    </div>
  );

  return (
    <div className="space-y-12 animate-in fade-in duration-500 pb-20">
      {/* Top Section: Hero & Summary (Original Stats) */}
      <div className="grid lg:grid-cols-12 gap-8">
        <div className="lg:col-span-9 bg-white rounded-[32px] overflow-hidden relative shadow-sm border border-gray-100 group min-h-[350px] flex flex-col justify-center px-12">
           <img
             src="https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=1600"
             className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-1000"
             alt=""
           />
           <div className="absolute inset-0 bg-gradient-to-r from-black/40 via-black/10 to-transparent" />
           <div className="relative z-10 max-w-lg text-left">
              <h1 className="text-5xl font-black text-white leading-tight mb-4 tracking-tighter">Fresh Produce.<br/>Direct from Farmers.</h1>
              <p className="text-white/80 text-sm font-medium mb-8 leading-relaxed italic">Connect directly with rural producers and build transparent business relationships.</p>
              <button
                onClick={() => navigate('/merchant/explore')}
                className="bg-[#16a34a] text-white px-8 py-3.5 rounded-xl font-black text-sm hover:bg-[#15803d] transition-all shadow-xl shadow-green-900/20"
              >
                Start Exploring
              </button>
           </div>
        </div>

        <div className="lg:col-span-3 bg-white rounded-[32px] p-8 border border-gray-100 shadow-sm flex flex-col">
           <h2 className="text-xs font-black text-gray-900 uppercase tracking-widest mb-8 italic">Account Intelligence</h2>
           <div className="space-y-6 flex-1">
              {summary.map((item, i) => (
                <div key={i} onClick={() => navigate(item.link)} className="flex items-center gap-5 group cursor-pointer text-left">
                   <div className={`w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 shadow-sm transition-transform group-hover:scale-110 ${item.color}`}>
                      {item.icon}
                   </div>
                   <div>
                      <p className="text-[10px] font-black text-gray-400 uppercase tracking-tighter mb-0.5 leading-none group-hover:text-green-600">{item.label}</p>
                      <p className="text-xl font-black text-gray-900 leading-none">{item.value}</p>
                   </div>
                </div>
              ))}
           </div>
           <button
             onClick={() => navigate('/merchant/bookings')}
             className="mt-10 text-blue-600 font-black text-[11px] uppercase tracking-widest flex items-center gap-2 hover:underline"
           >
             Full Activity Log <ArrowRight size={14}/>
           </button>
        </div>
      </div>

      {/* Main Content Area: Category Wise Showcasing */}
      <div className="grid lg:grid-cols-12 gap-10">
         <div className="lg:col-span-9 space-y-16">
            {Object.entries(groupedProducts).map(([category, items]: [string, any]) => (
               <section key={category} id={category} className="space-y-8 animate-in slide-in-from-bottom-4 duration-700">
                  <div className="flex items-center justify-between border-b border-gray-100 pb-4">
                     <h2 className="text-2xl font-black text-gray-900 capitalize italic flex items-center gap-3">
                        <div className="w-2 h-8 bg-green-500 rounded-full" />
                        {category} Showroom
                     </h2>
                     <button
                       onClick={() => navigate(`/merchant/explore?category=${category}`)}
                       className="text-blue-600 font-black text-[11px] uppercase tracking-widest flex items-center gap-1.5 hover:underline"
                     >
                        View More {category} <ChevronRight size={16}/>
                     </button>
                  </div>
                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                     {items.map((p: any) => (
                        <ProductCard key={p.id} product={p} onClick={() => navigate(`/product/${p.id}`)} />
                     ))}
                  </div>
               </section>
            ))}

            {products.length === 0 && (
               <div className="text-center py-40 bg-white rounded-[40px] border-2 border-dashed border-gray-100">
                  <LayoutGrid size={48} className="mx-auto mb-6 text-gray-200" />
                  <h3 className="text-xl font-black text-gray-900 italic">Marketplace is being restocked...</h3>
                  <p className="text-gray-400 font-medium">Farmers are currently updating their active listings.</p>
               </div>
            )}
         </div>

         {/* Side Context Lists */}
         <div className="lg:col-span-3 space-y-10">
            <div className="bg-white rounded-[32px] p-7 border border-gray-100 shadow-sm">
               <div className="flex items-center justify-between mb-8">
                  <h2 className="text-xs font-black text-gray-900 uppercase italic tracking-widest text-left">Recent Inquiries</h2>
                  <Link to="/merchant/bookings" className="text-blue-600 text-[10px] font-black uppercase tracking-tighter hover:underline">See All</Link>
               </div>
               <div className="space-y-6">
                  {data?.recent_bookings?.map((b: any, i: number) => (
                    <BookingRow key={i} id={b.id} product={b.product} farmer={b.farmer} status={b.status} color={b.color} onClick={() => navigate('/merchant/bookings')} />
                  ))}
               </div>
            </div>

            <div className="bg-white rounded-[32px] p-7 border border-gray-100 shadow-sm">
               <div className="flex items-center justify-between mb-8">
                  <h2 className="text-xs font-black text-gray-900 uppercase italic tracking-widest text-left">Messages</h2>
                  <Link to="/merchant/messages" className="text-blue-600 text-[10px] font-black uppercase tracking-tighter hover:underline">Open Inbox</Link>
               </div>
               <div className="space-y-6 text-left">
                  {data?.messages?.map((m: any, i: number) => (
                    <MessageRow key={i} initial={m.initial} name={m.name} msg={m.msg} time={m.time} unread={m.unread} onClick={() => navigate('/merchant/messages')} />
                  ))}
               </div>
            </div>
         </div>
      </div>
    </div>
  );
};

const ProductCard = ({ product, onClick }: any) => (
   <div
     onClick={onClick}
     className="bg-white rounded-3xl overflow-hidden border border-gray-100 shadow-sm hover:shadow-2xl hover:-translate-y-2 transition-all cursor-pointer group flex flex-col h-full text-left"
   >
      <div className="relative h-44 overflow-hidden bg-gray-100">
         <img src={product.images || 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=400'} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-1000" alt="" />
         <div className="absolute top-3 left-3">
            <span className="bg-white/95 backdrop-blur-sm text-[#16a34a] text-[9px] font-black uppercase px-3 py-1 rounded-full border border-green-100 shadow-sm">Verified Farm</span>
         </div>
      </div>
      <div className="p-5 flex-1 flex flex-col">
         <h3 className="text-sm font-black text-gray-900 mb-1 group-hover:text-green-600 transition-colors truncate">{product.name}</h3>
         <div className="flex items-center gap-1.5 text-[10px] text-gray-400 font-bold italic mb-4">
            <MapPin size={12} className="text-green-500" />
            <span className="truncate">{product.district}, {product.state}</span>
         </div>
         <div className="mt-auto space-y-4">
            <div className="flex justify-between items-center text-[10px] font-black uppercase tracking-tighter">
               <span className="text-gray-400">Stock</span>
               <span className="text-green-700">{product.quantity} {product.unit}</span>
            </div>
            <div className="flex items-center justify-between pt-4 border-t border-gray-50">
               <div className="leading-none">
                  <p className="text-[8px] text-gray-400 font-black uppercase mb-1">Rate</p>
                  <p className="text-lg font-black text-gray-900">₹{product.expected_price || 'N/A'}<span className="text-[10px] text-gray-400 font-bold">/{product.unit}</span></p>
               </div>
               <button className="bg-[#16a34a] text-white px-4 py-2.5 rounded-xl font-black text-[10px] uppercase hover:bg-[#15803d] transition-all shadow-lg shadow-green-100">Book</button>
            </div>
         </div>
      </div>
   </div>
);

const BookingRow = ({ id, product, farmer, status, color, onClick }: any) => (
   <div className="flex flex-col gap-2 group cursor-pointer text-left" onClick={onClick}>
      <div className="flex items-center justify-between">
         <p className="text-[11px] font-black text-gray-900 uppercase tracking-tighter">{id}</p>
         <span className={`text-[8px] font-black uppercase tracking-[0.1em] px-2 py-0.5 rounded-full ${color}`}>{status}</span>
      </div>
      <p className="text-[10px] font-bold text-gray-500 italic truncate group-hover:text-green-600 transition-colors">{product} by {farmer}</p>
   </div>
);

const MessageRow = ({ initial, name, msg, time, unread, onClick }: any) => (
   <div className="flex gap-4 group cursor-pointer items-start" onClick={onClick}>
      <div className="w-10 h-10 bg-gray-50 rounded-2xl flex items-center justify-center text-blue-600 font-black text-sm border border-gray-100 shrink-0 group-hover:scale-110 transition-transform shadow-inner">{initial}</div>
      <div className="flex-1 min-w-0">
         <div className="flex justify-between items-center mb-1">
            <h4 className="text-[11px] font-black text-gray-900 leading-none group-hover:text-green-600 transition-colors">{name}</h4>
            <span className="text-[9px] font-bold text-gray-400 whitespace-nowrap">{time}</span>
         </div>
         <p className="text-[10px] text-gray-400 font-medium italic truncate">{msg}</p>
      </div>
      {unread > 0 && <div className="w-2 h-2 bg-green-600 rounded-full shrink-0 mt-1 shadow-sm" />}
   </div>
);
