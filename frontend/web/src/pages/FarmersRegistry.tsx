import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { User, MapPin, ShieldCheck, Search, ChevronRight, Phone, Info } from 'lucide-react';

export const FarmersRegistry: React.FC = () => {
  const navigate = useNavigate();
  const [farmers, setFarmers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchFarmers();
  }, []);

  const fetchFarmers = async () => {
    try {
      setLoading(true);
      const response = await api.get('/admin/users?role=farmer');
      setFarmers(response.data.users || []);
    } catch (error) {
      console.error('Registry intelligence audit failure', error);
    } finally {
      setLoading(false);
    }
  };

  const filtered = farmers.filter(f =>
    f.full_name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    f.mobile?.includes(searchTerm)
  );

  if (loading) return (
    <div className="flex justify-center py-40">
      <div className="w-10 h-10 border-4 border-green-100 border-t-[#16a34a] rounded-full animate-spin"></div>
    </div>
  );

  return (
    <div className="animate-in fade-in duration-500 pb-20 text-left">
      <header className="mb-10 border-b border-gray-200 pb-6 flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div>
          <h1 className="text-3xl font-black text-gray-900 mb-1 uppercase tracking-tight italic">Producer Registry</h1>
          <p className="text-gray-500 font-medium italic text-[13px]">Explore and audit verified agricultural supply nodes within the network.</p>
        </div>
        <div className="flex items-center gap-3 px-5 py-2.5 bg-white rounded-xl border border-gray-100 shadow-sm text-[10px] font-black text-gray-400 uppercase tracking-widest">
           <Info size={14} className="text-blue-500"/>
           {farmers.length} Nodes Registered
        </div>
      </header>

      <div className="relative mb-12 group max-w-2xl">
        <Search className="absolute left-5 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-[#febd69] transition-colors" size={20} />
        <input
          type="text"
          placeholder="Filter by name or registry mobile..."
          className="w-full bg-white border border-gray-200 rounded-xl py-4 pl-14 pr-4 text-sm outline-none focus:ring-4 focus:ring-[#febd69]/10 focus:border-[#febd69]/30 transition-all shadow-sm font-medium"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8">
        {filtered.map((farmer) => (
          <div key={farmer.id} onClick={() => navigate(`/merchant/farmer/${farmer.id}`)} className="bg-white p-8 rounded-[40px] border border-gray-100 shadow-sm hover:shadow-2xl hover:border-[#febd69]/30 transition-all group flex gap-6 items-center relative overflow-hidden cursor-pointer">
             <div className="absolute top-0 right-0 w-24 h-24 bg-[#16a34a]/5 rounded-full -mr-12 -mt-12 group-hover:scale-110 transition-transform"></div>

             <div className="w-20 h-20 bg-gray-50 rounded-[28px] flex items-center justify-center text-[#16a34a] shadow-inner group-hover:scale-105 transition-transform border border-gray-100 shrink-0">
                <User size={32} />
             </div>

             <div className="flex-1 min-w-0">
                <div className="flex items-center gap-3 mb-2">
                   <h3 className="text-lg font-black text-gray-900 truncate uppercase tracking-tight">{farmer.full_name}</h3>
                   {farmer.verified && <ShieldCheck size={18} className="text-green-500 shrink-0" />}
                </div>
                <div className="flex items-center gap-1.5 text-[10px] font-bold text-gray-400 uppercase tracking-widest italic mb-6">
                   <MapPin size={12} className="text-green-500" /> Active Supply Node
                </div>
                <div className="flex items-center justify-between border-t border-gray-50 pt-5">
                    <div className="flex items-center gap-2 text-gray-300">
                       <Phone size={14}/>
                       <span className="text-[11px] font-black italic tracking-widest uppercase">Registry Protected</span>
                    </div>
                    <button className="p-3 bg-gray-50 rounded-2xl text-gray-400 group-hover:bg-[#131921] group-hover:text-white transition-all shadow-sm border border-gray-100">
                       <ChevronRight size={20} strokeWidth={3} />
                    </button>
                </div>
             </div>
          </div>
        ))}
        {filtered.length === 0 && (
          <div className="col-span-full py-40 text-center bg-white rounded-[40px] border border-gray-100 italic text-gray-400">
             <Search size={60} className="mx-auto mb-6 opacity-10" />
             <p className="text-xl font-black uppercase tracking-widest">No matching supply nodes found</p>
          </div>
        )}
      </div>
    </div>
  );
};
