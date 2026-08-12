import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuthStore } from '../store/authStore';
import { Bell, CheckCircle, Package, ArrowRight, ShieldCheck } from 'lucide-react';

export const Notifications: React.FC = () => {
  const navigate = useNavigate();
  const { user, token } = useAuthStore();
  const [notifications, setNotifications] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!token) {
      navigate('/login');
      return;
    }
    fetchNotifications();
  }, [token, navigate]);

  const fetchNotifications = async () => {
    try {
      setLoading(true);
      const endpoint = user?.role === 'farmer' ? `/farmer/notifications/${user.id}` : `/buyer/notifications/${user.id}`;
      const response = await api.get(endpoint);
      setNotifications(response.data.notifications || []);
    } catch (err) {
      console.error('Failed to load notifications');
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
    <div className="animate-in fade-in duration-500 pb-20 max-w-3xl text-left">
      <header className="mb-10">
        <h1 className="text-3xl font-black text-gray-900 mb-1 uppercase tracking-tight italic">Intelligence Alerts</h1>
        <p className="text-gray-500 italic font-medium text-[13px]">Stay updated with real-time connection audits and system logs.</p>
      </header>

      {notifications.length === 0 ? (
          <div className="text-center py-32 bg-white rounded-2xl border border-gray-200 shadow-sm">
              <Bell size={60} className="mx-auto mb-6 text-gray-100" />
              <h3 className="text-xl font-black text-gray-900 uppercase tracking-tighter mb-2">No active alerts</h3>
              <p className="text-gray-400 italic font-medium">We'll notify you here about new supply path matches and inquiries.</p>
          </div>
      ) : (
          <div className="space-y-4">
              {notifications.map((n) => (
                  <div
                    key={n.id}
                    className={`bg-white p-6 rounded-2xl border shadow-sm flex gap-5 transition-all ${n.is_read ? 'border-gray-100 opacity-60' : 'border-green-100 border-l-4 border-l-[#16a34a]'}`}
                  >
                      <div className={`w-12 h-12 rounded-xl shrink-0 flex items-center justify-center shadow-inner ${n.is_read ? 'bg-gray-50 text-gray-300' : 'bg-green-50 text-[#16a34a]'}`}>
                         {n.notification_type === 'booking_confirmed' ? <Package size={20}/> : <Bell size={20}/>}
                      </div>
                      <div className="flex-1 min-w-0">
                          <div className="flex justify-between items-start mb-2">
                              <h3 className={`text-sm font-black uppercase tracking-tight truncate ${n.is_read ? 'text-gray-400' : 'text-gray-900'}`}>{n.title}</h3>
                              <span className="text-[9px] font-black text-gray-300 uppercase tracking-[0.2em] italic ml-4">{new Date(n.created_at).toLocaleDateString()}</span>
                          </div>
                          <p className={`text-[13px] leading-relaxed mb-5 italic font-medium ${n.is_read ? 'text-gray-400' : 'text-gray-500'}`}>{n.message}</p>
                          <button className="text-[10px] font-black text-blue-600 hover:text-[#16a34a] uppercase tracking-widest flex items-center gap-1.5 transition-all group">
                              Audit Details <ArrowRight size={14} className="group-hover:translate-x-1 transition-transform"/>
                          </button>
                      </div>
                  </div>
              ))}
          </div>
      )}
    </div>
  );
};
