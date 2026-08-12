import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { bookings } from '../services/productService';
import { ClipboardList, Phone, User, Calendar, MapPin, MessageSquare, ShieldCheck, X, CheckCircle2, Lock } from 'lucide-react';

export const FarmerBookings: React.FC = () => {
  const navigate = useNavigate();
  const { user, token } = useAuthStore();
  const [merchantBookings, setFarmerBookings] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('all');

  useEffect(() => {
    if (!token) { navigate('/login'); return; }
    fetchBookings();
  }, [token]);

  const fetchBookings = async () => {
    try {
      setLoading(true);
      const response = await bookings.getFarmerBookings(user!.id);
      setFarmerBookings(response.bookings || []);
    } catch (err) {
      console.error('Failed to load inquiries');
    } finally {
      setLoading(false);
    }
  };

  const filteredBookings = merchantBookings.filter(b => {
    if (activeTab === 'all') return true;
    return b.status === activeTab;
  });

  const tabs = [
    { id: 'all', label: 'All Inquiries' },
    { id: 'confirmed', label: 'Active Reservations' },
    { id: 'completed', label: 'Archived Sales' },
  ];

  if (loading) return (
    <div className="flex justify-center items-center h-64">
      <div className="w-10 h-10 border-4 border-green-100 border-t-green-600 rounded-full animate-spin"></div>
    </div>
  );

  return (
    <div className="animate-in fade-in duration-500 pb-20 text-left">
      <header className="mb-10">
        <h1 className="text-3xl font-black text-gray-900 mb-1 uppercase tracking-tight italic">Marketplace Inquiries</h1>
        <p className="text-gray-500 font-medium italic text-[13px]">Anonymous reservation requests for your supply nodes mediated by AgriConnect.</p>
      </header>

      <div className="flex bg-white p-1 rounded-2xl border border-gray-200 shadow-sm w-fit mb-10 overflow-x-auto no-scrollbar">
          {tabs.map(tab => (
              <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`px-8 py-3 rounded-xl text-[10px] font-black uppercase tracking-widest transition-all whitespace-nowrap ${
                      activeTab === tab.id ? 'bg-[#16a34a] text-white shadow-lg' : 'text-gray-400 hover:text-gray-600 hover:bg-gray-50'
                  }`}
              >
                  {tab.label}
              </button>
          ))}
      </div>

      {filteredBookings.length === 0 ? (
          <div className="text-center py-32 bg-white rounded-3xl border border-gray-200 shadow-xl shadow-green-900/5">
              <ClipboardList size={60} className="mx-auto mb-6 text-gray-100" />
              <h3 className="text-xl font-black text-gray-900 uppercase mb-2">No active inquiries</h3>
              <p className="text-gray-400 italic text-sm">AgriConnect will notify you when a verified merchant reserves your produce.</p>
          </div>
      ) : (
          <div className="grid gap-6">
              {filteredBookings.map((booking) => (
                  <div key={booking.booking_id} className="bg-white rounded-[32px] p-8 md:p-10 border border-gray-100 shadow-sm hover:shadow-2xl transition-all group overflow-hidden relative text-left">
                      <div className="absolute top-0 right-0 w-40 h-40 bg-green-500/5 rounded-full -mr-20 -mt-20 group-hover:scale-110 transition-transform duration-700"></div>

                      <div className="flex flex-col lg:flex-row gap-10 lg:items-center relative z-10">
                          <div className="flex-1 space-y-4">
                              <div className="flex items-center gap-4">
                                  <span className={`px-4 py-1.5 rounded-lg text-[9px] font-black uppercase tracking-widest shadow-sm border ${
                                      booking.status === 'completed' ? 'bg-gray-50 text-gray-400 border-gray-100' : 'bg-green-50 text-green-700 border-green-100'
                                  }`}>
                                      {booking.status}
                                  </span>
                                  <span className="text-[10px] text-gray-400 font-black uppercase tracking-[0.2em] italic">Reference AGR-{booking.booking_id}</span>
                              </div>

                              <h3 className="text-2xl font-black text-gray-900 group-hover:text-[#16a34a] transition-colors uppercase tracking-tight">{booking.product_name}</h3>

                              <div className="flex flex-wrap gap-8 text-[11px] text-gray-400 font-black uppercase tracking-widest italic">
                                  <div className="flex items-center gap-2.5"><Calendar size={16} className="text-green-600"/> Reserved {new Date(booking.created_at).toLocaleDateString()}</div>
                                  <div className="flex items-center gap-2.5"><MapPin size={16} className="text-green-600"/> {booking.buyer_district || 'Regional Merchant'}</div>
                              </div>
                          </div>

                          <div className="lg:w-[450px] bg-[#002f1a] text-white rounded-[32px] p-8 flex flex-col sm:flex-row gap-8 items-center shadow-2xl shadow-green-900/20">
                              <div className="w-20 h-20 bg-white/10 rounded-3xl flex items-center justify-center text-green-400 shadow-xl border border-white/10 shrink-0">
                                  <Lock size={32} />
                              </div>
                              <div className="flex-1 text-center sm:text-left">
                                  <p className="text-[10px] font-black text-white/40 uppercase tracking-widest leading-none mb-2 italic">Discovery Registry</p>
                                  <p className="text-2xl font-black text-white mb-1 leading-none tracking-tight uppercase">{booking.buyer_id_alias}</p>
                                  <div className="flex items-center gap-2 justify-center sm:justify-start mb-6">
                                     <ShieldCheck size={14} className="text-green-500" />
                                     <p className="text-[11px] font-black text-green-400 uppercase tracking-tighter">Verified Wholesale Hub</p>
                                  </div>

                                  <div className="p-4 bg-white/5 rounded-xl border border-white/5">
                                      <p className="text-[10px] text-white/50 font-bold italic leading-relaxed uppercase tracking-tighter">
                                          Reservation mediated by AgriConnect. Our agents will coordinate with you for quality audit and logistics.
                                      </p>
                                  </div>
                              </div>
                          </div>
                      </div>
                  </div>
              ))}
          </div>
      )}
    </div>
  );
};
