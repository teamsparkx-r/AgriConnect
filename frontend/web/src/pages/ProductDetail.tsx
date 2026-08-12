import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import api from '../services/api';
import { buyerProducts, bookings } from '../services/productService';
import { MapPin, User, ShieldCheck, X, Lock, Heart, ArrowLeft, Info, Package, Tag, Layers } from 'lucide-react';

interface Props {
  internalId?: string;
}

export const ProductDetail: React.FC<Props> = ({ internalId }) => {
  const params = useParams<{ id: string }>();
  const id = internalId || params.id;
  const navigate = useNavigate();
  const location = useLocation();
  const { token, user } = useAuthStore();

  const [product, setProduct] = useState<any>(null);
  const [isBooked, setIsBooked] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [bookingLoading, setBookingLoading] = useState(false);
  const [bookingSuccess, setBookingSuccess] = useState('');
  const [isSaved, setIsSaved] = useState(false);

  // Unified Common Palette
  const accentColor = '#16a34a';
  const portalThemeColor = '#002f1a';

  useEffect(() => {
    if (id) fetchProduct();
  }, [id]);

  const fetchProduct = async () => {
    try {
      setLoading(true);
      const response = await buyerProducts.getOne(id!);
      setProduct(response.product || response);
    } catch (err: any) {
      setError('Failed to load supply path data');
    } finally {
      setLoading(false);
    }
  };

  const handleBookCrop = async () => {
    if (!token) {
      navigate('/login', { state: { from: location.pathname + location.search } });
      return;
    }

    setBookingLoading(true);
    setError('');
    try {
      const response = await bookings.create(user!.id, {
        product_id: id!,
        terms_accepted: true
      });

      if (response.success) {
        setBookingSuccess('🎉 Crop successfully reserved! AgriConnect will mediate the exchange protocol.');
        setIsBooked(true);
      }
    } catch (err: any) {
      setError(err.response?.data?.detail || 'Booking sequence failed. Please try again.');
    } finally {
      setBookingLoading(false);
    }
  };

  if (loading) return (
    <div className="flex justify-center items-center h-[400px]">
      <div className="w-10 h-10 border-4 border-green-100 border-t-[#16a34a] rounded-full animate-spin"></div>
    </div>
  );

  if (!product) return (
    <div className="py-40 text-center bg-white rounded-xl border border-gray-200">
      <h2 className="text-xl font-black mb-4 text-gray-900 uppercase tracking-widest">Supply Node Missing</h2>
      <button onClick={() => navigate(-1)} className="text-[#16a34a] font-black uppercase text-xs hover:underline">Revert to Discovery</button>
    </div>
  );

  return (
    <div className="animate-in fade-in duration-500 pb-20 text-left">
      <button onClick={() => navigate(-1)} className="mb-8 text-gray-400 hover:text-[#16a34a] font-black uppercase text-[10px] tracking-widest flex items-center gap-2 transition-all group">
        <ArrowLeft size={16} className="group-hover:-translate-x-1 transition-transform"/> Discovery History
      </button>

      <div className="grid lg:grid-cols-12 gap-10">
        {/* LEFT: Imagery & Specs */}
        <div className="lg:col-span-7 space-y-10">
          <div className="relative group">
            <img
              src={product.images || 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=1200'}
              alt={product.name}
              className="w-full aspect-video object-cover rounded-xl shadow-2xl shadow-green-900/10 border border-gray-200"
            />
            <button
              onClick={() => {
                if (!token) { navigate('/login'); return; }
                setIsSaved(!isSaved);
              }}
              className={`absolute top-6 right-6 p-4 rounded-xl backdrop-blur-md transition-all ${isSaved ? 'bg-red-500 text-white shadow-lg' : 'bg-white/80 text-gray-400 hover:text-red-500 shadow-sm'}`}
            >
              <Heart size={20} fill={isSaved ? 'currentColor' : 'none'} />
            </button>
          </div>

          <div className="space-y-8 bg-white p-10 rounded-xl border border-gray-100 shadow-sm">
            <div className="flex justify-between items-start border-b border-gray-50 pb-6 text-left">
               <div className="text-left">
                  <h1 className="text-3xl font-black text-gray-900 uppercase tracking-tight">{product.name}</h1>
                  <div className="flex items-center gap-4 mt-2 text-[10px] font-black text-gray-400 uppercase tracking-widest">
                     <span className="flex items-center gap-1.5"><ShieldCheck size={14} className="text-green-500"/> Farmer ID: {product.farmer_id_alias}</span>
                     <span className="flex items-center gap-1.5"><MapPin size={14} className="text-[#16a34a]"/> {product.district} Sourcing</span>
                  </div>
               </div>
               <span className="bg-gray-50 text-gray-600 px-4 py-2 rounded-lg text-[10px] font-black uppercase tracking-widest border border-gray-100 italic">{product.category}</span>
            </div>

            <p className="text-gray-500 text-lg leading-relaxed italic font-medium">{product.description || 'Premium agricultural supply path. Subject to direct source audit and quality verification.'}</p>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-8 pt-4">
                <SpecBox icon={<Package size={16}/>} label="Inventory Node" value={`${product.quantity} ${product.unit}`} />
                <SpecBox icon={<Tag size={16}/>} label="Registry Group" value={product.category} />
                <SpecBox icon={<Layers size={16}/>} label="Node Grade" value="Standard" />
                <SpecBox icon={<MapPin size={16}/>} label="Sourcing Cell" value={product.district} />
            </div>
          </div>
        </div>

        {/* RIGHT: Booking Card */}
        <div className="lg:col-span-5">
          <div className="bg-white rounded-xl p-8 md:p-10 border border-gray-200 sticky top-24 shadow-2xl shadow-green-900/5 overflow-hidden">
            <div className="absolute top-0 left-0 w-full h-1 bg-[#16a34a]"></div>

            <div className="mb-10 text-left">
              <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest italic mb-2 leading-none">Market Rate Reference</p>
              <div className="flex items-baseline gap-2">
                <span className="text-5xl font-black text-gray-900 tracking-tighter">₹{product.expected_price}</span>
                <span className="text-gray-400 font-black italic text-lg uppercase tracking-widest">/{product.unit}</span>
              </div>
            </div>

            {error && <div className="bg-red-50 text-red-700 p-4 rounded-lg mb-8 text-[10px] font-black uppercase tracking-widest flex items-center gap-2 border border-red-100"><X size={16}/> {error}</div>}
            {bookingSuccess && <div className="bg-green-50 text-green-700 p-4 rounded-lg mb-8 text-[10px] font-black uppercase tracking-widest flex items-center gap-2 border border-green-100"><ShieldCheck size={16}/> {bookingSuccess}</div>}

            {!isBooked ? (
              <div className="space-y-8 text-left">
                <div className="bg-gray-50 p-8 rounded-xl border border-gray-100 shadow-inner">
                  <h4 className="text-[10px] font-black text-gray-400 uppercase tracking-widest mb-2 italic">Anonymous Exchange Protocol</h4>
                  <p className="text-[11px] text-gray-400 italic font-medium leading-relaxed">Book this crop to initiate a reservation. AgriConnect will act as the sole mediator to verify quality and coordinate the exchange. Personal identities remain protected.</p>
                </div>

                <button
                  onClick={handleBookCrop}
                  disabled={bookingLoading}
                  style={{ backgroundColor: portalThemeColor }}
                  className="w-full text-white py-5 rounded-xl font-black text-xs shadow-2xl hover:opacity-90 transition-all uppercase tracking-[0.2em] flex items-center justify-center gap-3"
                >
                  {bookingLoading ? <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div> : <><Package size={18}/> Book This Crop</>}
                </button>
                <div className="flex items-center gap-3 justify-center text-[9px] text-gray-400 font-black uppercase tracking-widest italic">
                   <ShieldCheck size={14} className="text-green-500"/> Direct Mediator Audit • 0% Commissions
                </div>
              </div>
            ) : (
              <div className="animate-in zoom-in-95 duration-500 space-y-8 text-left">
                <div style={{ backgroundColor: portalThemeColor }} className="text-white rounded-2xl p-10 shadow-2xl relative overflow-hidden">
                   <div className="absolute top-0 right-0 w-32 h-32 bg-white/5 rounded-full -mr-16 -mt-16"></div>
                   <h3 className="text-[10px] font-black uppercase tracking-[0.2em] mb-10 flex items-center gap-2 text-[#16a34a]">
                      <Lock size={14} className="opacity-50" /> Crop Reserved
                   </h3>
                   <div className="space-y-10 relative z-10">
                      <div className="flex items-center gap-6">
                         <div className="w-14 h-14 bg-white/5 rounded-xl flex items-center justify-center border border-white/10 shrink-0"><User size={28}/></div>
                         <div className="text-left">
                            <p className="text-[9px] font-black uppercase opacity-30 leading-none mb-2 tracking-widest italic leading-none">Producer Alias</p>
                            <p className="text-xl font-black tracking-tight uppercase leading-none">{product.farmer_id_alias}</p>
                         </div>
                      </div>
                   </div>
                </div>
                <div className="bg-blue-50 border border-blue-100 p-8 rounded-2xl flex gap-6 shadow-sm">
                    <Info size={24} className="text-blue-600 shrink-0 mt-0.5"/>
                    <p className="text-[11px] text-blue-900 font-black italic leading-relaxed uppercase tracking-tighter">
                        Reservation Audit active. AgriConnect agents will now verify the supply node parameters and contact you with fulfillment logistics.
                    </p>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

const SpecBox = ({ icon, label, value }: any) => (
    <div className="bg-white border border-gray-50 p-6 rounded-xl shadow-sm hover:border-[#16a34a]/30 transition-all flex flex-col gap-4 group">
        <div className="w-10 h-10 rounded-xl bg-gray-50 flex items-center justify-center text-gray-400 group-hover:text-[#16a34a] transition-colors shadow-inner border border-gray-100">{icon}</div>
        <div className="text-left">
           <p className="text-[9px] font-black text-gray-400 uppercase tracking-widest leading-none mb-1.5 italic">{label}</p>
           <p className="text-[14px] font-black text-gray-900 leading-none uppercase tracking-tight">{value}</p>
        </div>
    </div>
);
