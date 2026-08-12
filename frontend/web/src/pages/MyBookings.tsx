import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { bookings } from '../services/productService';
import { Package, Calendar, ChevronRight, CheckCircle, AlertTriangle, X, Shield, Lock, MapPin } from 'lucide-react';

export const MyBookings: React.FC = () => {
  const navigate = useNavigate();
  const { token, user } = useAuthStore();
  const [myBookings, setMyBookings] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!token) {
      navigate('/login');
      return;
    }
    fetchBookings();
  }, [token, navigate]);

  const fetchBookings = async () => {
    try {
      setLoading(true);
      const response = await bookings.getAll(user!.id);
      setMyBookings(response.bookings || []);
    } catch (err: any) {
      setError('Failed to load your reservation history');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return (
    <div className="flex justify-center items-center h-64">
      <div className="w-10 h-10 border-4 border-green-100 border-t-green-600 rounded-full animate-spin"></div>
    </div>
  );

  return (
    <div className="animate-in fade-in duration-500 pb-20 text-left">
      <header className="mb-10 flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-black text-gray-900 mb-1 uppercase tracking-tight italic">Crop Reservations</h1>
          <p className="text-gray-500 italic font-medium text-[13px]">Track your anonymous supply path bookings mediated by AgriConnect.</p>
        </div>
        <button onClick={() => navigate('/merchant/explore')} className="text-[10px] font-black text-green-600 hover:text-green-700 uppercase tracking-widest flex items-center gap-1.5 transition-all group">
           Explore More Supply <ChevronRight size={14} className="group-hover:translate-x-1 transition-transform"/>
        </button>
      </header>

      {error && (
        <div className="bg-red-50 border border-red-100 text-red-700 px-6 py-4 rounded-2xl mb-10 text-xs font-bold uppercase tracking-widest flex items-center gap-3">
          <AlertTriangle size={18}/> {error}
        </div>
      )}

      {myBookings.length === 0 ? (
        <div className="text-center py-24 bg-white rounded-xl border border-gray-200 shadow-xl shadow-green-900/5">
          <div className="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-8 text-gray-200 shadow-inner">
             <Package size={40} />
          </div>
          <h3 className="text-xl font-black text-gray-900 mb-2 uppercase">No reservations found</h3>
          <p className="text-gray-400 mb-10 max-w-xs mx-auto italic font-medium text-sm">Crops you book will appear here for AgriConnect mediator tracking.</p>
          <button
            onClick={() => navigate('/merchant/explore')}
            className="bg-[#002f1a] text-white px-10 py-4 rounded-xl font-black hover:bg-[#16a34a] transition-all shadow-xl shadow-gray-200 uppercase tracking-widest text-[11px]"
          >
            Start Sourcing
          </button>
        </div>
      ) : (
        <div className="grid gap-6">
          {myBookings.map((booking) => (
            <div key={booking.booking_id} className="bg-white rounded-[32px] border border-gray-100 shadow-sm overflow-hidden hover:shadow-xl hover:-translate-y-0.5 transition-all group">
              <div className="p-8 md:p-10 flex flex-col lg:flex-row gap-10">
                <div className="flex-1 space-y-6">
                  <div className="flex items-center gap-4">
                    <span className={`px-4 py-1.5 rounded-full text-[9px] font-black uppercase tracking-widest shadow-sm border ${
                      booking.status === 'completed' ? 'bg-gray-50 text-gray-400 border-gray-100' : 'bg-green-50 text-green-700 border-green-100'
                    }`}>
                      {booking.status}
                    </span>
                    <span className="text-[10px] text-gray-400 font-black uppercase tracking-[0.2em] italic">Reservation AGR-{booking.booking_id}</span>
                  </div>

                  <h3 className="text-2xl font-black text-gray-900 group-hover:text-[#16a34a] transition-colors tracking-tight uppercase">{booking.product_name}</h3>

                  <div className="flex items-center gap-6 text-[11px] text-gray-400 font-black uppercase tracking-widest italic">
                    <div className="flex items-center gap-2.5">
                      <Calendar size={14} className="text-green-600" />
                      Booked on {new Date(booking.created_at).toLocaleDateString()}
                    </div>
                  </div>
                </div>

                <div className="lg:w-80 bg-[#002f1a] text-white rounded-[28px] p-8 relative overflow-hidden shadow-2xl shadow-green-900/20 text-left">
                  <div className="absolute top-0 right-0 w-32 h-32 bg-white/5 rounded-full -mr-16 -mt-16"></div>
                  <h4 className="text-[9px] font-black text-green-400 uppercase tracking-[0.2em] mb-8 border-b border-white/10 pb-3 flex items-center gap-2">
                     <Lock size={12} className="opacity-50"/> Identity Registry
                  </h4>
                  <div className="space-y-6 relative z-10">
                    <div>
                        <p className="text-[9px] text-white/40 font-black uppercase leading-none mb-2 tracking-widest italic">Source Origin</p>
                        <div className="flex items-center gap-2">
                            <MapPin size={16} className="text-green-500" />
                            <p className="text-sm font-black uppercase">{booking.farmer_village || 'Protected Node'}</p>
                        </div>
                    </div>
                    <div>
                        <p className="text-[9px] text-white/40 font-black uppercase leading-none mb-2 tracking-widest italic">Mediator Audit</p>
                        <p className="text-xs font-bold text-green-400 italic">AgriConnect agents are verifying parameters.</p>
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
