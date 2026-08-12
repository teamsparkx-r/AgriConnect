import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { useLanguageStore } from '../store/languageStore';
import { buyerProducts } from '../services/productService';
import { Search, ShoppingBag, Leaf, ShieldCheck, ArrowRight, MapPin } from 'lucide-react';

export const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const { token, user } = useAuthStore();
  const { t } = useLanguageStore();
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    if (token && user) {
      if (user.role === 'buyer') navigate('/merchant/portal');
      else if (user.role === 'farmer') navigate('/farmer/dashboard');
      else if (user.role === 'admin') navigate('/admin/dashboard');
      return;
    }
    fetchFeaturedProducts();
  }, [token, user, navigate]);

  const fetchFeaturedProducts = async () => {
    try {
      setLoading(true);
      const response = await buyerProducts.getHome();
      setProducts(response.products || []);
    } catch (err: any) {
      console.error('Failed to load listings');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;
    navigate(`/explore?query=${encodeURIComponent(searchQuery)}`);
  };

  return (
    <div className="min-h-screen bg-[#F8FAF9]">
      {/* Hero Section */}
      <section className="bg-gradient-to-br from-[#002f1a] to-[#064e3b] text-white overflow-hidden">
        <div className="container mx-auto px-6 py-16 lg:py-24 max-w-[1440px]">
          <div className="grid lg:grid-cols-2 gap-12 items-center">
            <div className="z-10 text-left">
              <span className="inline-flex items-center gap-2 rounded-full bg-green-500/20 text-green-400 border border-green-500/30 px-4 py-1.5 text-[11px] font-black uppercase tracking-widest mb-8 backdrop-blur-sm">
                100% Direct Farm Connections
              </span>
              <h1 className="text-4xl lg:text-6xl font-black mb-6 leading-[1.1] tracking-tighter">
                {t.zero_commission}
              </h1>
              <p className="text-lg text-green-100/70 mb-10 max-w-lg leading-relaxed italic">
                {t.home_desc}
              </p>
              <div className="flex flex-wrap gap-4">
                <button
                  onClick={() => navigate('/buyer/signup')}
                  className="bg-[#16a34a] text-white hover:bg-[#15803d] font-black py-4 px-8 rounded-2xl flex items-center gap-2 transition-all shadow-xl shadow-green-900/40 text-sm"
                >
                  {t.start_shopping} <ArrowRight size={18} />
                </button>
                <button
                  onClick={() => navigate('/farmer/signup')}
                  className="bg-white/5 text-white hover:bg-white/10 border border-white/20 font-black py-4 px-8 rounded-2xl flex items-center gap-2 transition-all text-sm"
                >
                  {t.sell_as_farmer}
                </button>
              </div>
            </div>

            <div className="hidden lg:block relative h-[400px]">
               <img src="https://images.unsplash.com/photo-1595147389795-37094173bfd8?w=800" className="absolute inset-0 w-full h-full object-cover rounded-[40px] shadow-2xl border border-white/10" alt="Fresh Farm" />
               <div className="absolute -bottom-6 -left-6 bg-white p-6 rounded-[32px] shadow-2xl flex items-center gap-4 border border-gray-100 animate-bounce-slow">
                  <div className="w-12 h-12 bg-green-50 rounded-2xl flex items-center justify-center text-green-600"><Leaf /></div>
                  <div>
                    <p className="text-gray-900 font-black text-sm">Verified Farm</p>
                    <p className="text-gray-400 text-[10px] font-bold uppercase italic">Direct Sourcing</p>
                  </div>
               </div>
            </div>
          </div>
        </div>
      </section>

      {/* Main Grid */}
      <main className="container mx-auto px-6 py-16 max-w-[1440px]">
        <header className="mb-16 flex flex-col md:flex-row md:items-end justify-between gap-8">
           <div className="text-left">
              <h2 className="text-3xl font-black text-gray-900 tracking-tight mb-2">{t.freshly_harvested}</h2>
              <p className="text-gray-500 font-medium italic">Premium produce directly from India's agricultural heartlands.</p>
           </div>
           <form onSubmit={handleSearch} className="relative w-full max-w-lg group">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-green-600 transition-colors" size={18} />
              <input
                type="text"
                className="w-full bg-white border border-gray-200 rounded-2xl py-3.5 pl-12 pr-4 text-sm outline-none focus:ring-4 focus:ring-green-500/5 transition-all shadow-sm"
                placeholder={t.search_placeholder}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
           </form>
        </header>

        {loading ? (
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8">
            {[...Array(8)].map((_, i) => <SkeletonCard key={i} />)}
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
            {products.map((product: any) => (
              <ProductCard key={product.id} product={product} t={t} onClick={() => navigate(`/product/${product.id}`)} />
            ))}
          </div>
        )}
      </main>

      {/* Trust Badges */}
      <section className="bg-white border-y border-gray-100 py-16">
        <div className="container mx-auto px-6 max-w-[1440px]">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-12 grayscale opacity-40 hover:grayscale-0 hover:opacity-100 transition-all duration-700">
             <TrustBadge icon={<ShieldCheck size={32}/>} text="Verified Farmers" />
             <TrustBadge icon={<Leaf size={32}/>} text="Direct Sourcing" />
             <TrustBadge icon={<ShoppingBag size={32}/>} text="Zero Commission" />
             <TrustBadge icon={<ArrowRight size={32}/>} text="Instant Logistics" />
          </div>
        </div>
      </section>

      <footer className="bg-[#002f1a] text-white py-20">
         <div className="container mx-auto px-6 max-w-[1440px]">
            <div className="grid md:grid-cols-4 gap-16 mb-20 text-left">
               <div className="md:col-span-2 space-y-8">
                  <div className="flex items-center gap-2">
                    <div className="w-10 h-10 bg-green-600 rounded-xl flex items-center justify-center text-white"><span className="text-xl font-black">A</span></div>
                    <h2 className="text-2xl font-black tracking-tighter">AgriConnect</h2>
                  </div>
                  <p className="text-green-100/40 text-sm leading-relaxed max-w-xs italic">Empowering India's rural producers through technology and transparent, commission-free trade.</p>
               </div>
               <div className="space-y-6">
                  <h4 className="text-[10px] font-black uppercase tracking-widest text-green-500 italic">Platform</h4>
                  <ul className="space-y-4 text-xs font-bold text-green-100/60">
                     <li><button className="hover:text-white transition-colors">How it Works</button></li>
                     <li><button className="hover:text-white transition-colors">Farmer Verification</button></li>
                     <li><button className="hover:text-white transition-colors">Success Stories</button></li>
                  </ul>
               </div>
               <div className="space-y-6">
                  <h4 className="text-[10px] font-black uppercase tracking-widest text-green-500 italic">Resources</h4>
                  <ul className="space-y-4 text-xs font-bold text-green-100/60">
                     <li><button className="hover:text-white transition-colors">Privacy Policy</button></li>
                     <li><button className="hover:text-white transition-colors">Terms of Use</button></li>
                     <li><button className="hover:text-white transition-colors">Help Center</button></li>
                  </ul>
               </div>
            </div>
            <div className="pt-10 border-t border-white/5 flex flex-col md:flex-row justify-between items-center gap-6 text-[10px] font-black uppercase tracking-[0.3em] text-green-100/20">
               <p>© 2026 AgriConnect. Distributed rural intelligence.</p>
               <div className="flex gap-8 italic">
                  <span>0% Middlemen</span><span>Direct Access</span><span>Verified Supply</span>
               </div>
            </div>
         </div>
      </footer>
    </div>
  );
};

const ProductCard = ({ product, t, onClick }: any) => (
  <div
    className="bg-white rounded-[32px] overflow-hidden border border-gray-100 hover:border-green-200 hover:shadow-2xl hover:-translate-y-2 transition-all duration-500 group cursor-pointer text-left"
    onClick={onClick}
  >
    <div className="relative h-48 overflow-hidden bg-gray-50">
      <img
        src={product.images || 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=800'}
        alt={product.name}
        className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-1000"
      />
      <div className="absolute top-4 left-4">
        <span className="bg-white/95 backdrop-blur-sm text-green-700 text-[10px] font-black uppercase px-3 py-1 rounded-xl border border-green-100 shadow-sm">{t.verified_farm}</span>
      </div>
    </div>
    <div className="p-6">
      <h3 className="text-[16px] font-black text-gray-900 mb-1 line-clamp-1 group-hover:text-green-700 transition-colors uppercase tracking-tight">{product.name}</h3>
      <div className="flex items-center gap-1.5 text-gray-400 text-[10px] font-bold mb-5 italic uppercase">
        <MapPin size={12} className="text-green-500" /> {product.district}, {product.state}
      </div>
      <div className="flex items-center justify-between pt-5 border-t border-gray-50">
        <div>
          <p className="text-gray-400 text-[8px] uppercase font-black mb-1">Market Rate</p>
          <p className="text-xl font-black text-gray-900 leading-none">₹{product.expected_price || 'N/A'}<span className="text-[10px] text-gray-400 font-bold">/{product.unit}</span></p>
        </div>
        <button className="bg-[#16a34a] text-white p-3 rounded-2xl shadow-lg shadow-green-100 group-hover:scale-110 transition-transform"><ArrowRight size={20} /></button>
      </div>
    </div>
  </div>
);

const TrustBadge = ({ icon, text }: any) => (
  <div className="flex flex-col items-center gap-3">
    <div className="text-green-600">{icon}</div>
    <span className="text-[10px] font-black uppercase tracking-widest text-gray-600 italic leading-none">{text}</span>
  </div>
);

const SkeletonCard = () => (
    <div className="bg-white rounded-[32px] overflow-hidden border border-gray-50 p-6 space-y-6 animate-pulse">
        <div className="h-40 bg-gray-50 rounded-2xl"></div>
        <div className="h-4 bg-gray-50 rounded-full w-3/4"></div>
        <div className="h-2 bg-gray-50 rounded-full w-1/2"></div>
    </div>
);
