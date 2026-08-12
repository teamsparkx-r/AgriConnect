import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { farmerProducts } from '../services/productService';
import { Package, Search, Plus, MoreVertical, Edit3, Trash2, Eye, ArrowUpCircle, MapPin, Tag, IndianRupee, Layers } from 'lucide-react';

export const MyProducts: React.FC = () => {
  const navigate = useNavigate();
  const { user, token } = useAuthStore();
  const [products, setProducts] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [activeTab, setActiveTab] = useState('all');

  useEffect(() => {
    if (!token) { navigate('/login'); return; }
    fetchProducts();
  }, [token]);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const response = await farmerProducts.getAll(user!.id);
      setProducts(response.products || []);
    } catch (err) {
      console.error('Failed to load produce inventory');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('Are you sure you want to permanently remove this produce listing?')) return;
    try {
      await farmerProducts.delete(user!.id, id);
      fetchProducts();
    } catch (err) {
      alert('Failed to delete listing record');
    }
  };

  const filteredProducts = products.filter(p => {
    const matchesSearch = p.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesTab = activeTab === 'all' || p.status === activeTab;
    return matchesSearch && matchesTab;
  });

  const tabs = [
    { id: 'all', label: 'Complete Catalog' },
    { id: 'active', label: 'Market Active' },
    { id: 'harvesting_soon', label: 'Imminent Harvest' },
    { id: 'sold', label: 'Completed Sales' },
    { id: 'draft', label: 'Catalog Drafts' },
  ];

  if (loading) return (
    <div className="flex justify-center items-center h-64">
      <div className="w-10 h-10 border-4 border-green-100 border-t-green-600 rounded-full animate-spin"></div>
    </div>
  );

  return (
    <div className="animate-in fade-in duration-500 pb-20 text-left">
      <header className="mb-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div>
          <h1 className="text-3xl font-black text-gray-900 mb-1 uppercase tracking-tight italic">Produce Inventory</h1>
          <p className="text-gray-500 font-medium italic text-[13px]">Manage your supply paths and market availability in real-time.</p>
        </div>
        <button
          onClick={() => navigate('/farmer/add-product')}
          className="bg-[#16a34a] text-white px-8 py-3.5 rounded-xl font-black flex items-center gap-3 hover:bg-[#15803d] transition-all shadow-xl shadow-green-900/20 uppercase tracking-widest text-[11px]"
        >
          <Plus size={20} /> Add New Listing
        </button>
      </header>

      {/* Discovery Toolbar */}
      <div className="flex flex-col lg:flex-row gap-6 mb-10 items-center">
        <div className="flex-1 relative group w-full">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-green-600 transition-colors" size={18} />
          <input
            type="text"
            placeholder="Search inventory records..."
            className="w-full bg-white border border-gray-200 rounded-xl py-3.5 pl-12 pr-4 text-sm outline-none focus:ring-4 focus:ring-green-500/5 transition-all shadow-sm font-medium"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <div className="flex bg-white p-1 rounded-2xl border border-gray-200 shadow-sm overflow-x-auto no-scrollbar w-full lg:w-auto">
          {tabs.map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`px-6 py-2.5 rounded-xl text-[10px] font-black uppercase tracking-widest transition-all whitespace-nowrap ${
                activeTab === tab.id ? 'bg-[#16a34a] text-white shadow-lg' : 'text-gray-400 hover:text-gray-600 hover:bg-gray-50'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {filteredProducts.length === 0 ? (
          <div className="text-center py-32 bg-white rounded-3xl border border-gray-200 shadow-sm shadow-green-900/5">
              <Package size={60} className="mx-auto text-gray-100 mb-6" />
              <h3 className="text-xl font-black text-gray-900 mb-2 uppercase">Inventory Cell Empty</h3>
              <p className="text-gray-400 text-sm italic font-medium mb-10 max-w-xs mx-auto">Commit your first produce listing to start connecting with verified merchants.</p>
              <button onClick={() => navigate('/farmer/add-product')} className="bg-gray-900 text-white px-10 py-4 rounded-xl font-black uppercase tracking-widest text-[11px] hover:bg-[#16a34a] transition-all shadow-2xl">Initialize Listing</button>
          </div>
      ) : (
          <div className="grid gap-6">
            {filteredProducts.map(product => (
              <div key={product.id} className="bg-white rounded-[32px] p-6 md:p-8 border border-gray-200 shadow-sm hover:shadow-2xl hover:border-green-300 transition-all group flex flex-col md:flex-row gap-10 items-start text-left">
                <div className="w-full md:w-48 h-48 rounded-[28px] overflow-hidden shrink-0 shadow-inner bg-gray-50 border border-gray-100 relative">
                  <img src={product.images || 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=800'} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-1000" alt="" />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
                </div>

                <div className="flex-1 space-y-6 w-full">
                  <div className="flex justify-between items-start gap-4">
                    <div>
                      <div className="flex items-center gap-4 mb-3">
                        <span className={`px-4 py-1.5 rounded-lg text-[9px] font-black uppercase tracking-widest shadow-sm border ${
                          product.status === 'active' ? 'bg-green-50 text-green-700 border-green-100' : 'bg-orange-50 text-orange-700 border-orange-100'
                        }`}>
                          {product.status.replace('_', ' ')}
                        </span>
                        <span className="text-[10px] text-gray-300 font-black uppercase tracking-[0.2em] italic">Audit #{product.id.slice(0,8)}</span>
                      </div>
                      <h3 className="text-2xl font-black text-gray-900 leading-tight uppercase tracking-tight group-hover:text-[#16a34a] transition-colors">{product.name}</h3>
                    </div>
                    <div className="flex gap-2">
                      <button onClick={() => navigate(`/farmer/edit-product/${product.id}`)} className="p-3 bg-gray-50 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-2xl transition-all shadow-sm"><Edit3 size={18}/></button>
                      <button onClick={() => handleDelete(product.id)} className="p-3 bg-gray-50 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-2xl transition-all shadow-sm"><Trash2 size={18}/></button>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 lg:grid-cols-4 gap-8 pt-2">
                    <DataPoint icon={<Layers size={14}/>} label="Registry Inventory" value={`${product.quantity} ${product.unit}`} />
                    <DataPoint icon={<IndianRupee size={14}/>} label="Unit Rate" value={`₹${product.expected_price}/${product.unit}`} />
                    <DataPoint icon={<Tag size={14}/>} label="Supply Category" value={product.category} />
                    <DataPoint icon={<MapPin size={14}/>} label="Source Cell" value={product.district} />
                  </div>

                  <div className="pt-8 border-t border-gray-50 flex flex-wrap gap-4">
                     <button
                        onClick={() => navigate(`/product/${product.id}`)}
                        className="bg-gray-900 text-white px-8 py-3.5 rounded-xl font-black text-[11px] uppercase tracking-widest hover:bg-[#16a34a] transition-all flex items-center gap-3 shadow-xl"
                     >
                        <Eye size={16}/> Execution Preview
                     </button>
                     <button
                        onClick={async () => {
                          const newQty = prompt('Update available quantity audit:', product.quantity);
                          if (newQty) {
                            await farmerProducts.update(user!.id, product.id, { quantity: parseFloat(newQty) });
                            fetchProducts();
                          }
                        }}
                        className="bg-white border border-gray-200 text-gray-600 px-8 py-3.5 rounded-xl text-[11px] font-black uppercase tracking-widest hover:bg-gray-50 transition-all flex items-center gap-3 shadow-sm"
                      >
                        Adjust Quantity
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

const DataPoint = ({ icon, label, value }: any) => (
    <div className="space-y-2 group">
        <div className="flex items-center gap-2 text-gray-400">
           {icon}
           <p className="text-[10px] font-black uppercase tracking-widest italic leading-none">{label}</p>
        </div>
        <p className="text-[15px] font-black text-gray-900 leading-none uppercase truncate tracking-tight">{value}</p>
    </div>
);
