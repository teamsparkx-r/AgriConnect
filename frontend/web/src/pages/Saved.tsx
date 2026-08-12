import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuthStore } from '../store/authStore';
import { Heart, ShoppingBag, MapPin, Trash2, ArrowRight } from 'lucide-react';

export const Saved: React.FC = () => {
  const navigate = useNavigate();
  const { token, user } = useAuthStore();
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!token) {
      navigate('/login');
      return;
    }
    fetchSaved();
  }, [token, navigate]);

  const fetchSaved = async () => {
    try {
      setLoading(true);
      const response = await api.get('/buyer/saved');
      setProducts(response.data.products || []);
    } catch (err) {
      console.error('Failed to load saved products', err);
    } finally {
      setLoading(false);
    }
  };

  const removeSaved = async (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      await api.delete(`/buyer/saved/${id}`);
      fetchSaved();
    } catch (err) {
      alert('Failed to remove item');
    }
  };

  if (loading) return (
    <div className="flex justify-center items-center h-64">
      <div className="w-10 h-10 border-4 border-green-100 border-t-green-600 rounded-full animate-spin"></div>
    </div>
  );

  return (
    <div className="animate-in fade-in duration-500 pb-20">
      <header className="mb-10 text-left">
        <h1 className="text-3xl font-black text-gray-900 mb-1 uppercase tracking-tight italic">Saved Intelligence</h1>
        <p className="text-gray-500 italic font-medium text-[13px]">Review and manage your curated produce supply paths.</p>
      </header>

      {products.length === 0 ? (
          <div className="text-center py-40 bg-white rounded-xl border border-gray-200 shadow-sm">
              <Heart size={60} className="mx-auto mb-6 text-gray-100" />
              <h3 className="text-xl font-black text-gray-900 uppercase tracking-tighter mb-2">Wishlist Empty</h3>
              <p className="text-gray-400 italic font-medium">Keep track of interesting products by hearting them during discovery.</p>
              <button
                onClick={() => navigate('/merchant/portal?view=explore')}
                className="mt-10 bg-gray-900 text-white px-10 py-4 rounded-xl font-black hover:bg-[#16a34a] transition-all shadow-xl uppercase tracking-widest text-[11px]"
              >
                Explore Marketplace
              </button>
          </div>
      ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
              {products.map((product) => (
                  <div
                    key={product.id}
                    onClick={() => navigate(`/merchant/portal?view=product&productId=${product.id}`)}
                    className="bg-white p-5 rounded-2xl border border-gray-200 shadow-sm flex gap-6 group cursor-pointer hover:shadow-2xl hover:border-green-300 transition-all text-left"
                  >
                      <div className="w-32 h-32 rounded-xl overflow-hidden shrink-0 border border-gray-100">
                          <img src={product.images || 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=800'} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700" alt={product.name} />
                      </div>
                      <div className="flex-1 flex flex-col justify-between py-1">
                          <div className="space-y-1">
                              <div className="flex justify-between items-start">
                                  <h3 className="text-lg font-black text-gray-900 group-hover:text-green-700 transition-colors uppercase tracking-tight leading-tight">{product.name}</h3>
                                  <button
                                    onClick={(e) => removeSaved(product.id, e)}
                                    className="p-2 text-gray-300 hover:text-red-500 hover:bg-red-50 rounded-lg transition-all"
                                  >
                                      <Trash2 size={18} />
                                  </button>
                              </div>
                              <div className="flex items-center gap-1.5 text-[10px] font-black text-gray-400 uppercase tracking-widest italic tracking-tighter">
                                  <MapPin size={10} className="text-green-500" /> {product.district}, {product.state}
                              </div>
                          </div>
                          <div className="flex items-end justify-between border-t border-gray-50 pt-4 mt-2">
                              <div className="flex items-baseline gap-1">
                                  <span className="text-xl font-black text-gray-900 tracking-tighter">₹{product.expected_price}</span>
                                  <span className="text-gray-400 text-[10px] font-bold tracking-widest uppercase">/{product.unit}</span>
                              </div>
                              <button className="bg-gray-50 p-2.5 rounded-xl text-gray-400 group-hover:bg-[#febd69] group-hover:text-[#131921] transition-all shadow-inner border border-gray-100">
                                  <ArrowRight size={18} strokeWidth={3} />
                              </button>
                          </div>
                      </div>
                  </div>
              ))}
          </div>
      )}
    </div>
  );
};
