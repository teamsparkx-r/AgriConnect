import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams, useLocation } from 'react-router-dom';
import { buyerProducts } from '../services/productService';
import { useLanguageStore } from '../store/languageStore';
import { Search, SlidersHorizontal, MapPin, ArrowRight, ShoppingBag, X, CheckCircle2, Info } from 'lucide-react';

export const Explore: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const queryParam = searchParams.get('query') || '';
  const categoryParam = searchParams.get('category') || '';

  const { t } = useLanguageStore();
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState(queryParam);
  const [showFilters, setShowFilters] = useState(false);

  // Common Palette
  const accentColor = '#16a34a';
  const buttonTextColor = 'white';

  const [filters, setFilters] = useState({
    category: categoryParam,
    state: '',
    district: '',
    min_price: '',
    max_price: '',
  });

  useEffect(() => {
    setSearchTerm(queryParam);
    setFilters(prev => ({ ...prev, category: categoryParam }));
  }, [queryParam, categoryParam]);

  useEffect(() => {
    fetchProducts();
  }, [queryParam, filters.category]);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const cleanFilters: any = {};
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== '') cleanFilters[key] = value;
      });

      const response = await buyerProducts.search(queryParam, cleanFilters);
      setProducts(response.products || []);
    } catch (err) {
      console.error('Search intelligence failure', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    const params = new URLSearchParams(searchParams);
    if (searchTerm) params.set('query', searchTerm);
    else params.delete('query');
    navigate({ pathname: location.pathname, search: params.toString() });
  };

  return (
    <div className="animate-in fade-in duration-500 pb-20 text-left">
      <header className="mb-8 border-b border-gray-200 pb-4 flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div>
          <h1 className="text-3xl font-black text-gray-900 mb-1 uppercase tracking-tight italic">{t.browse_products}</h1>
          <p className="text-gray-500 font-medium italic text-[12px]">Explore verified agricultural supply paths directly from rural producers.</p>
        </div>
      </header>

      {/* Discovery Toolbar */}
      <div className="p-4 rounded-xl border border-gray-200 shadow-sm mb-10 flex flex-wrap gap-4 items-center sticky top-2 z-40 bg-white">
          <div className="flex-1 flex items-center gap-4 text-[10px] font-black text-gray-400 uppercase tracking-widest px-2">
            <Info size={14} className="text-blue-500"/>
            <span>Showing {products.length} active supply nodes</span>
          </div>

          <button
            onClick={() => setShowFilters(!showFilters)}
            className={`px-5 py-2.5 rounded-lg border transition-all flex items-center gap-2 font-black text-[10px] uppercase tracking-widest ${showFilters ? 'bg-[#131921] text-white border-[#131921]' : 'bg-white text-gray-600 border-gray-200 hover:bg-gray-50 shadow-sm'}`}
          >
              <SlidersHorizontal size={14} />
              <span>Refine Intelligence</span>
          </button>
      </div>

      {showFilters && (
          <div className="mb-10 bg-white p-8 rounded-2xl border border-gray-200 shadow-2xl animate-in slide-in-from-top-4 duration-300 text-left relative overflow-hidden">
              <div className="absolute top-0 left-0 w-1 h-full bg-[#16a34a]"></div>
              <div className="flex justify-between items-center mb-8">
                 <h3 className="text-[10px] font-black uppercase text-gray-400 tracking-[0.2em] italic">Search Parameters</h3>
                 <X size={20} className="text-gray-300 cursor-pointer hover:text-gray-900" onClick={() => setShowFilters(false)}/>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
                  <div className="space-y-3">
                      <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest leading-none">Category Group</label>
                      <select
                          className="w-full bg-gray-50 border border-gray-100 rounded-lg p-3.5 text-xs font-bold outline-none"
                          value={filters.category}
                          onChange={(e) => setFilters({...filters, category: e.target.value})}
                      >
                          <option value="">All Supply Paths</option>
                          <option value="vegetables">Vegetables</option>
                          <option value="fruits">Fruits</option>
                          <option value="grains">Grains</option>
                          <option value="pulses">Pulses</option>
                          <option value="spices">Spices</option>
                      </select>
                  </div>
                  <div className="space-y-3">
                      <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest leading-none">State Node</label>
                      <input
                          type="text"
                          className="w-full bg-gray-50 border border-gray-100 rounded-lg p-3.5 text-xs font-bold"
                          placeholder="Node name..."
                          value={filters.state}
                          onChange={(e) => setFilters({...filters, state: e.target.value})}
                      />
                  </div>
                  <div className="space-y-3">
                      <label className="text-[10px] font-black text-gray-400 uppercase tracking-widest leading-none">Rate Floor (₹)</label>
                      <input
                          type="number"
                          className="w-full bg-gray-50 border border-gray-100 rounded-lg p-3.5 text-xs font-bold"
                          placeholder="0.00"
                          value={filters.min_price}
                          onChange={(e) => setFilters({...filters, min_price: e.target.value})}
                      />
                  </div>
                  <div className="flex items-end">
                      <button
                          onClick={() => { fetchProducts(); setShowFilters(false); }}
                          className="w-full bg-[#16a34a] text-white font-black py-3.5 rounded-lg hover:bg-[#15803d] transition-all uppercase tracking-[0.2em] text-[10px] shadow-lg"
                      >
                          Execute Audit
                      </button>
                  </div>
              </div>
          </div>
      )}

      {loading ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {[...Array(8)].map((_, i) => <SkeletonCard key={i} />)}
          </div>
      ) : products.length === 0 ? (
          <div className="text-center py-40 bg-white rounded-2xl border border-gray-200 shadow-sm">
              <ShoppingBag size={60} className="mx-auto mb-6 text-gray-100" />
              <h3 className="text-xl font-black text-gray-900 uppercase tracking-tighter mb-2">No matching paths</h3>
              <p className="text-gray-400 italic font-medium">Adjust your parameters to find active supply nodes.</p>
          </div>
      ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {products.map((product: any) => (
                  <ProductCard
                    key={product.id}
                    product={product}
                    t={t}
                    accentColor={accentColor}
                    buttonTextColor={buttonTextColor}
                    onClick={() => navigate(`/merchant/product/${product.id}`)}
                  />
              ))}
          </div>
      )}
    </div>
  );
};

const ProductCard = ({ product, t, onClick, accentColor, buttonTextColor }: any) => (
  <div
    className="bg-white rounded-xl overflow-hidden border border-gray-200 hover:border-green-300 hover:shadow-2xl hover:-translate-y-1 transition-all duration-300 group cursor-pointer text-left flex flex-col h-full shadow-sm"
    onClick={onClick}
  >
    <div className="relative h-44 overflow-hidden bg-gray-50 border-b border-gray-100">
      <img
        src={product.images || 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=800'}
        alt={product.name}
        className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-1000"
      />
      <div className="absolute top-3 left-3">
        <span className="bg-white/95 backdrop-blur-sm text-green-700 text-[9px] font-black uppercase px-2.5 py-1 rounded border border-green-100 shadow-sm">{t.verified_farm}</span>
      </div>
    </div>
    <div className="p-5 flex-1 flex flex-col">
      <h3 className="text-[15px] font-black text-gray-900 mb-1 line-clamp-1 group-hover:text-green-600 transition-colors uppercase tracking-tight leading-tight">{product.name}</h3>
      <div className="flex items-center gap-1.5 text-gray-400 text-[10px] font-bold mb-5 italic uppercase tracking-tighter">
        <MapPin size={12} className="text-green-500" /> {product.district}, {product.state}
      </div>

      <div className="mt-auto space-y-4">
          <div className="flex justify-between items-center text-[10px] font-black uppercase tracking-widest border-t border-gray-100 pt-4">
             <span className="text-gray-400 italic">Inventory</span>
             <span className="text-green-700">{product.quantity} {product.unit}</span>
          </div>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-[9px] text-gray-400 font-black uppercase mb-0.5 tracking-tighter italic">Registry Rate</p>
              <p className="text-xl font-black text-gray-900 leading-none tracking-tighter">₹{product.expected_price || 'N/A'}<span className="text-[11px] text-gray-400 font-bold">/{product.unit}</span></p>
            </div>
            <button
              style={{ backgroundColor: accentColor, color: buttonTextColor }}
              className="px-5 py-2.5 rounded font-black text-[10px] uppercase tracking-widest shadow-sm transition-all border border-transparent hover:opacity-90"
            >
              View Audit
            </button>
          </div>
      </div>
    </div>
  </div>
);

const SkeletonCard = () => (
    <div className="bg-white rounded-xl overflow-hidden border border-gray-100 p-5 space-y-5 animate-pulse h-[350px]">
        <div className="h-40 bg-gray-50 rounded-lg"></div>
        <div className="h-4 bg-gray-50 rounded-full w-3/4"></div>
        <div className="h-4 bg-gray-50 rounded-full w-1/2"></div>
        <div className="mt-auto h-10 bg-gray-50 rounded-lg"></div>
    </div>
);
