import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useLanguageStore } from '../store/languageStore';
import {
  Users, ShoppingBasket, AlertTriangle,
  CheckCircle, XCircle, Search, Filter,
  Download, MoreVertical, CreditCard,
  ClipboardList, Layers, Bell, BarChart3, History, Settings as SettingsIcon,
  IndianRupee, Store, Info, Send, Eye, X, MapPin, Phone, Mail, Calendar, Package, Tag, ShieldCheck
} from 'lucide-react';

type Tab = 'farmers' | 'merchants' | 'products' | 'bookings' | 'payments' | 'reports' | 'categories' | 'notifications' | 'analytics' | 'logs' | 'settings';

export const AdminManagement: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const activeTab = (searchParams.get('tab') as Tab) || 'farmers';
  const { t } = useLanguageStore();

  const [data, setData] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedItem, setSelectedItem] = useState<any>(null);
  const [showDetailModal, setShowDetailModal] = useState(false);

  const [settings, setSettings] = useState<any>({});

  useEffect(() => {
    fetchData();
  }, [activeTab]);

  const fetchData = async () => {
    try {
      setLoading(true);
      let endpoint = activeTab;
      if (activeTab === 'farmers') endpoint = 'users?role=farmer';
      if (activeTab === 'merchants') endpoint = 'users?role=buyer';

      const response = await api.get(`/admin/${endpoint}`);

      if (activeTab === 'settings') {
        setSettings(response.data.settings || {});
      } else {
        const dataKey = (activeTab === 'farmers' || activeTab === 'merchants') ? 'users' : activeTab;
        const resultData = response.data[dataKey] || [];
        setData(resultData);
      }
    } catch (error) {
      console.error(`Admin fetch error for ${activeTab}`, error);
      setData([]);
    } finally {
      setLoading(false);
    }
  };

  const viewDetail = async (id: string, type: 'user' | 'product') => {
    try {
      setLoading(true);
      const endpoint = type === 'user' ? `/admin/users/${id}` : `/admin/products/${id}`;
      const response = await api.get(endpoint);
      setSelectedItem(type === 'user' ? response.data.user : response.data.product);
      setShowDetailModal(true);
    } catch (err) {
      alert('Failed to load intelligence details');
    } finally {
      setLoading(false);
    }
  };

  const updateStatus = async (id: string, status: string) => {
    try {
       if (activeTab === 'farmers' || activeTab === 'merchants' || (selectedItem && !selectedItem.category)) {
         await api.patch(`/admin/users/${id}/status`, { status });
       } else if (activeTab === 'reports') {
         await api.post(`/admin/reports/${id}/resolve`, { status });
       }

       if (showDetailModal) {
         viewDetail(id, selectedItem?.category ? 'product' : 'user');
       } else {
         fetchData();
       }
       alert('Command executed successfully');
    } catch (err) {
       alert('Operational error');
    }
  };

  const deleteProduct = async (id: string) => {
    if (!window.confirm('Commit permanent record deletion?')) return;
    try {
      await api.delete(`/admin/products/${id}`);
      fetchData();
      if (showDetailModal) setShowDetailModal(false);
      alert('Product record removed');
    } catch (err) {
      alert('Deletion command failed');
    }
  };

  const filteredData = Array.isArray(data) ? data.filter(item => {
    const search = searchTerm.toLowerCase();
    if (activeTab === 'farmers' || activeTab === 'merchants') return item.full_name?.toLowerCase().includes(search) || item.mobile?.includes(search);
    if (activeTab === 'products') return item.name?.toLowerCase().includes(search) || item.farmer?.toLowerCase().includes(search);
    if (activeTab === 'bookings') return item.booking_id?.toLowerCase().includes(search) || item.product_name?.toLowerCase().includes(search);
    if (activeTab === 'payments') return item.booking_id?.toLowerCase().includes(search) || item.merchant?.toLowerCase().includes(search);
    return true;
  }) : [];

  return (
    <div className="animate-in fade-in duration-500 pb-20 text-left">
      <header className="flex flex-col md:flex-row md:items-center justify-between gap-6 mb-10">
        <div>
           <h1 className="text-2xl font-black text-gray-900 capitalize tracking-tight">{activeTab.replace('_', ' ')}</h1>
           <p className="text-[12px] text-gray-500 font-medium italic">Administrative command center for {activeTab}.</p>
        </div>
        <div className="flex items-center gap-3">
           {['settings', 'analytics', 'notifications'].indexOf(activeTab) === -1 && (
             <div className="relative group">
                <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-green-600 transition-colors" size={14} />
                <input
                  type="text"
                  placeholder={t.search_placeholder}
                  className="bg-white border border-gray-200 rounded-xl py-2 pl-10 pr-4 text-[12px] outline-none focus:ring-2 focus:ring-green-500/10 focus:border-green-500 w-72 transition-all font-medium"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
             </div>
           )}
           <button onClick={() => fetchData()} className="p-2.5 bg-white border border-gray-200 rounded-xl text-gray-400 hover:text-green-600 transition-colors shadow-sm"><History size={16}/></button>
           <button className="p-2.5 bg-white border border-gray-200 rounded-xl text-gray-400 hover:text-gray-900 shadow-sm"><Download size={16}/></button>
        </div>
      </header>

      {loading ? (
        <div className="flex flex-col justify-center items-center h-[300px] gap-3 bg-white rounded-[32px] border border-gray-100">
           <div className="w-8 h-8 border-4 border-green-100 border-t-green-600 rounded-full animate-spin"></div>
           <p className="text-gray-400 font-black uppercase tracking-widest text-[8px]">Fetching intelligence...</p>
        </div>
      ) : activeTab === 'notifications' ? (
        <div className="bg-white rounded-[32px] p-8 border border-gray-100 shadow-sm max-w-2xl mx-auto text-left">
           <h2 className="text-xl font-black text-gray-900 mb-6 flex items-center gap-2 italic"><Send size={18} className="text-blue-600" /> Broadcast</h2>
           <form className="space-y-5" onSubmit={(e) => { e.preventDefault(); alert('Broadcast sequence initiated'); }}>
              <select className="w-full bg-gray-50 border border-gray-100 rounded-xl p-3.5 text-xs outline-none focus:ring-2 focus:ring-blue-500/10"><option>Target: All Users</option></select>
              <input type="text" className="w-full bg-gray-50 border border-gray-100 rounded-xl p-3.5 text-xs outline-none" placeholder="Subject Line" />
              <textarea className="w-full bg-gray-50 border border-gray-100 rounded-xl p-3.5 text-xs outline-none min-h-[120px]" placeholder="Detailed message..."></textarea>
              <button type="submit" className="w-full bg-blue-600 text-white py-3.5 rounded-xl font-black text-[11px] uppercase tracking-widest shadow-lg">Broadcast Now</button>
           </form>
        </div>
      ) : activeTab === 'settings' ? (
        <div className="max-w-2xl bg-white rounded-[32px] border border-gray-100 shadow-sm overflow-hidden divide-y divide-gray-50 mx-auto text-left">
           {Object.entries(settings).map(([key, item]: [string, any]) => (
              <div key={key} className="p-6 flex items-center justify-between group hover:bg-gray-50/50 transition-all">
                 <div>
                    <h3 className="text-xs font-black text-gray-900 uppercase tracking-widest mb-0.5">{key.replace(/_/g, ' ')}</h3>
                    <p className="text-[10px] text-gray-400 italic">{item.desc}</p>
                 </div>
                 <span className="bg-green-50 text-green-700 px-3 py-1.5 rounded-lg font-black text-[11px]">{item.value}</span>
              </div>
           ))}
        </div>
      ) : filteredData.length === 0 ? (
        <div className="text-center py-32 bg-white rounded-[32px] border border-gray-100">
           <Search size={40} className="mx-auto mb-4 text-gray-200" />
           <h3 className="text-lg font-black text-gray-900">No matching records</h3>
        </div>
      ) : (
        <div className="bg-white rounded-[32px] border border-gray-100 shadow-lg shadow-green-900/5 overflow-hidden">
           <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                 <thead className="bg-gray-50/50 border-b border-gray-100">
                    <tr>
                       {activeTab === 'farmers' && <><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">{t.farmers}</th><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Status</th><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Joined</th></>}
                       {activeTab === 'merchants' && <><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">{t.total_merchants}</th><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Status</th><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Joined</th></>}
                       {activeTab === 'products' && <><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Produce Listing</th><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">{t.farmers}</th><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Status</th></>}
                       {activeTab === 'bookings' && <><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Booking Reference</th><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Buyer</th><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Status</th></>}
                       {activeTab === 'payments' && <><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Payment Audit</th><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Merchant</th><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Amount</th></>}
                       {activeTab === 'reports' && <><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Reporter</th><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Reason</th><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Status</th></>}
                       {activeTab === 'categories' && <><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Category</th><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Active Paths</th><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Visibility</th></>}
                       {activeTab === 'logs' && <><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Admin</th><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Action</th><th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic">Time</th></>}
                       <th className="p-6 text-[9px] font-black uppercase text-gray-400 tracking-widest italic text-right">Actions</th>
                    </tr>
                 </thead>
                 <tbody className="divide-y divide-gray-50 text-[11px] font-medium">
                    {filteredData.map((item) => (
                       <tr key={item.id || item.booking_id} className="hover:bg-gray-50/30 transition-colors group cursor-pointer" onClick={() => (activeTab === 'farmers' || activeTab === 'merchants') ? viewDetail(item.id, 'user') : activeTab === 'products' ? viewDetail(item.id, 'product') : null}>
                          {activeTab === 'farmers' && (
                             <>
                                <td className="p-6 flex items-center gap-4">
                                   <div className="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center text-blue-600 font-black">{item.full_name?.charAt(0)}</div>
                                   <div className="text-left">
                                      <p className="font-black text-gray-900 leading-none mb-1">{item.full_name}</p>
                                      <p className="text-[9px] text-gray-400 font-bold italic">{item.mobile}</p>
                                   </div>
                                </td>
                                <td className="p-6 text-left"><StatusBadge status={item.status} /></td>
                                <td className="p-6 text-left text-[10px] font-black text-gray-400 italic">{new Date(item.created_at).toLocaleDateString()}</td>
                             </>
                          )}
                          {activeTab === 'merchants' && (
                             <>
                                <td className="p-6 flex items-center gap-4">
                                   <div className="w-10 h-10 bg-emerald-50 rounded-xl flex items-center justify-center text-emerald-600 font-black">{item.full_name?.charAt(0)}</div>
                                   <div className="text-left">
                                      <p className="font-black text-gray-900 leading-none mb-1">{item.full_name}</p>
                                      <p className="text-[9px] text-gray-400 font-bold italic">{item.mobile}</p>
                                   </div>
                                </td>
                                <td className="p-6 text-left"><StatusBadge status={item.status} /></td>
                                <td className="p-6 text-left text-[10px] font-black text-gray-400 italic">{new Date(item.created_at).toLocaleDateString()}</td>
                             </>
                          )}
                          {activeTab === 'products' && (
                             <>
                                <td className="p-6 text-left">
                                   <p className="font-black text-gray-900 leading-none mb-1.5 uppercase tracking-tight">{item.name}</p>
                                   <p className="text-[9px] text-gray-400 font-black uppercase tracking-widest italic">{item.category}</p>
                                </td>
                                <td className="p-6 text-left text-[11px] font-black text-gray-600 italic">{item.farmer}</td>
                                <td className="p-6 text-left"><StatusBadge status={item.status} /></td>
                             </>
                          )}
                          {activeTab === 'bookings' && (
                             <>
                                <td className="p-6 text-left text-[11px] font-black text-gray-900 uppercase">AGR-{item.booking_id}</td>
                                <td className="p-6 text-left">
                                   <p className="font-black text-gray-900 leading-none mb-1.5 uppercase">{item.product_name}</p>
                                   <p className="text-[9px] text-gray-400 font-bold italic">Merchant: {item.buyer_name}</p>
                                </td>
                                <td className="p-6 text-left"><StatusBadge status={item.status} /></td>
                             </>
                          )}
                          {activeTab === 'payments' && (
                             <>
                                <td className="p-6 text-left font-black text-gray-900 text-[11px] uppercase">PAY-{item.id?.slice(0,6).toUpperCase()}</td>
                                <td className="p-6 text-left text-[11px] font-black text-gray-600 italic uppercase">{item.merchant}</td>
                                <td className="p-6 text-left font-black text-green-600 text-[11px]">₹{item.amount}</td>
                             </>
                          )}
                          {activeTab === 'categories' && (
                             <>
                                <td className="p-6 text-left font-black text-gray-900 text-[12px] capitalize italic">{item.name}</td>
                                <td className="p-6 text-left font-black text-gray-600 bg-gray-50 px-3 py-1.5 rounded-lg w-fit text-[10px]">{item.listings} Active Paths</td>
                                <td className="p-6 text-left"><StatusBadge status={item.status} /></td>
                             </>
                          )}
                          {activeTab === 'logs' && (
                             <>
                                <td className="p-6 text-left font-black text-gray-900 text-[11px] uppercase">{item.admin}</td>
                                <td className="p-6 text-left font-black text-blue-600 uppercase text-[9px] tracking-widest">{item.action}</td>
                                <td className="p-6 text-left text-[10px] font-black text-gray-400 italic">{new Date(item.time).toLocaleString()}</td>
                             </>
                          )}
                          {activeTab === 'reports' && (
                             <>
                                <td className="p-6 text-left font-black text-gray-900 text-[11px] uppercase">{item.reporter}</td>
                                <td className="p-6 text-left text-[10px] font-black text-red-600 italic bg-red-50/30 px-3 py-1.5 rounded-lg w-fit uppercase">{item.reason}</td>
                                <td className="p-6 text-left"><StatusBadge status={item.status} /></td>
                             </>
                          )}
                          <td className="p-6 text-right">
                             <div className="flex justify-end gap-2 opacity-0 group-hover:opacity-100 transition-all scale-95 group-hover:scale-100">
                                {(activeTab === 'farmers' || activeTab === 'merchants') && (
                                   <button
                                     onClick={(e) => { e.stopPropagation(); updateStatus(item.id, item.status === 'active' ? 'suspended' : 'active'); }}
                                     className={`px-3 py-1.5 rounded-lg text-[9px] font-black uppercase tracking-widest transition-all shadow-sm ${
                                       item.status === 'active' ? 'bg-red-50 text-red-600 hover:bg-red-100' : 'bg-green-600 text-white shadow-green-900/10'
                                     }`}
                                   >
                                      {item.status === 'active' ? 'Suspend' : 'Authorize'}
                                   </button>
                                )}
                                <button className="p-2 bg-gray-50 text-gray-400 rounded-lg hover:text-gray-900"><MoreVertical size={14}/></button>
                             </div>
                          </td>
                       </tr>
                    ))}
                 </tbody>
              </table>
           </div>
        </div>
      )}

      {/* DETAIL MODAL (Deep Intelligence) */}
      {showDetailModal && selectedItem && (
        <div className="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm animate-in fade-in duration-300">
           <div className="bg-white w-full max-w-3xl rounded-[40px] shadow-2xl overflow-hidden animate-in zoom-in-95 duration-300">
              <div className="bg-gray-50 p-8 flex justify-between items-start border-b border-gray-100">
                 <div className="flex gap-6 text-left">
                    <div className="w-16 h-16 bg-white rounded-3xl flex items-center justify-center shadow-sm border border-gray-100">
                       {selectedItem.category ? <Package className="text-orange-500" size={32}/> : <Users className="text-blue-500" size={32}/>}
                    </div>
                    <div className="text-left">
                       <h2 className="text-2xl font-black text-gray-900 leading-none mb-2 uppercase tracking-tight">{selectedItem.full_name || selectedItem.name}</h2>
                       <div className="flex gap-3">
                          <StatusBadge status={selectedItem.status} />
                          {selectedItem.verified && <span className="bg-blue-50 text-blue-600 px-3 py-1 rounded-full text-[9px] font-black uppercase tracking-widest italic border border-blue-100 flex items-center gap-1 shadow-sm"><ShieldCheck size={10}/> Verified Sourcing</span>}
                       </div>
                    </div>
                 </div>
                 <button onClick={() => setShowDetailModal(false)} className="p-3 bg-white rounded-2xl text-gray-400 hover:text-gray-900 shadow-sm border border-gray-100 transition-all"><X size={20}/></button>
              </div>

              <div className="p-10 text-left">
                 {selectedItem.category ? (
                    <div className="grid md:grid-cols-2 gap-10">
                       <div className="space-y-6">
                          <InfoRow icon={<Tag size={16}/>} label="Market Category" value={selectedItem.category} />
                          <InfoRow icon={<IndianRupee size={16}/>} label="Discovery Price" value={`₹${selectedItem.price} / ${selectedItem.unit}`} />
                          <InfoRow icon={<Package size={16}/>} label="Inventory Stock" value={`${selectedItem.quantity} ${selectedItem.unit}`} />
                          <InfoRow icon={<MapPin size={16}/>} label="Pickup Origin" value={selectedItem.location} />
                       </div>
                       <div className="space-y-6">
                          <div className="bg-gray-50 p-6 rounded-3xl border border-gray-100 shadow-inner">
                             <h4 className="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em] mb-4">Seller Registry</h4>
                             <p className="font-black text-gray-900 mb-1 uppercase leading-none">{selectedItem.farmer}</p>
                             <p className="text-xs font-bold text-gray-500 italic mb-4 leading-none">{selectedItem.farmer_mobile}</p>
                          </div>
                          <button onClick={() => deleteProduct(selectedItem.id)} className="w-full bg-red-600 text-white py-4 rounded-2xl font-black uppercase tracking-widest text-xs shadow-xl shadow-red-100 hover:bg-red-700 transition-all">Force Record Removal</button>
                       </div>
                    </div>
                 ) : (
                    <div className="grid md:grid-cols-2 gap-10">
                       <div className="space-y-6">
                          <div className="bg-gray-50/50 p-6 rounded-3xl border border-gray-100 space-y-6">
                             <InfoRow icon={<Phone size={16}/>} label="Mobile Registry" value={selectedItem.mobile} />
                             <InfoRow icon={<Mail size={16}/>} label="Email Index" value={selectedItem.email || 'None provided'} />
                             <InfoRow icon={<Calendar size={16}/>} label="Platform Onboarding" value={new Date(selectedItem.created_at).toLocaleDateString()} />
                             <InfoRow icon={<MapPin size={16}/>} label="Operational Area" value={selectedItem.profile ? `${selectedItem.profile.district}, ${selectedItem.profile.state}` : 'N/A'} />
                          </div>
                       </div>
                       <div className="space-y-6">
                          <div className="grid grid-cols-2 gap-4">
                             <div className="bg-white p-5 rounded-3xl text-center border border-gray-100 shadow-sm">
                                <p className="text-[10px] font-black text-gray-400 uppercase mb-1 tracking-widest">Orders</p>
                                <p className="text-2xl font-black text-gray-900">{selectedItem.profile?.bookings_count || selectedItem.profile?.completed_bookings || 0}</p>
                             </div>
                             <div className="bg-white p-5 rounded-3xl text-center border border-gray-100 shadow-sm">
                                <p className="text-[10px] font-black text-gray-400 uppercase mb-1 tracking-widest">Health</p>
                                <p className="text-xl font-black text-green-600 italic uppercase">Optimal</p>
                             </div>
                          </div>
                          <div className="flex gap-4">
                             <button
                                onClick={() => updateStatus(selectedItem.id, selectedItem.status === 'active' ? 'suspended' : 'active')}
                                className={`flex-1 py-4 rounded-2xl font-black uppercase tracking-widest text-[10px] shadow-xl transition-all ${selectedItem.status === 'active' ? 'bg-orange-500 text-white shadow-orange-100 hover:bg-orange-600' : 'bg-[#16a34a] text-white shadow-green-100 hover:bg-[#15803d]'}`}
                             >
                                {selectedItem.status === 'active' ? 'Deactivate Access' : 'Restore Permissions'}
                             </button>
                             <button onClick={() => alert('Broadcasting user-specific ping...')} className="p-4 bg-gray-900 text-white rounded-2xl shadow-lg hover:bg-gray-800 transition-all"><Bell size={18}/></button>
                          </div>
                       </div>
                    </div>
                 )}
              </div>
           </div>
        </div>
      )}
    </div>
  );
};

const InfoRow = ({ icon, label, value }: any) => (
   <div className="flex items-center gap-4 group">
      <div className="w-10 h-10 bg-white rounded-xl flex items-center justify-center text-gray-400 group-hover:text-green-600 transition-colors shadow-sm border border-gray-100">{icon}</div>
      <div className="text-left">
         <p className="text-[9px] font-black text-gray-400 uppercase tracking-widest leading-none mb-1.5 italic">{label}</p>
         <p className="text-[13px] font-black text-gray-800 leading-none uppercase tracking-tight">{value}</p>
      </div>
   </div>
);

const StatusBadge = ({ status }: any) => {
  const colors: any = {
    active: 'bg-green-50 text-green-700 border-green-100',
    suspended: 'bg-red-50 text-red-700 border-red-100',
    submitted: 'bg-yellow-50 text-yellow-700 border-yellow-100',
    confirmed: 'bg-purple-50 text-purple-700 border-purple-100',
  };
  return <span className={`px-4 py-1.5 rounded-full text-[9px] font-black uppercase tracking-[0.1em] italic border shadow-sm ${colors[status] || 'bg-gray-50 text-gray-400 border-gray-100'}`}>{status}</span>;
};
