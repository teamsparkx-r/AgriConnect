import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { farmerProducts } from '../services/productService';
import { Camera, MapPin, Sprout, Package, IndianRupee, Save, Send, ArrowLeft, Trash2, ShieldCheck, Calendar, Info, Plus } from 'lucide-react';
import api from '../services/api';

export const AddProduct: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEdit = !!id;
  const { user, token } = useAuthStore();
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(false);
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
    name: '',
    category: '',
    description: '',
    quantity: '',
    unit: 'kg',
    expected_price: '',
    harvest_date: '',
    status: 'draft',
    images: '',
    state: '',
    district: '',
    village: '',
    farm_address: ''
  });

  useEffect(() => {
    if (!token) { navigate('/login'); return; }
    if (isEdit) fetchProduct();
    else fetchProfileDefaults();
  }, [id, token]);

  const fetchProfileDefaults = async () => {
    try {
      const response = await api.get(`/farmer/profile/${user?.id}`);
      if (response.data.success) {
        const p = response.data.profile;
        setFormData(prev => ({
          ...prev,
          state: p.state || '',
          district: p.district || '',
          village: p.village || '',
          farm_address: p.farm_address || ''
        }));
      }
    } catch (e) { console.error('Failed to load profile defaults'); }
  };

  const fetchProduct = async () => {
    try {
      setFetching(true);
      const response = await farmerProducts.getOne(user!.id, id!);
      const p = response.product || response;
      setFormData({
        name: p.name || '',
        category: p.category || '',
        description: p.description || '',
        quantity: p.quantity?.toString() || '',
        unit: p.unit || 'kg',
        expected_price: p.expected_price?.toString() || '',
        harvest_date: p.harvest_date ? p.harvest_date.split('T')[0] : '',
        status: p.status || 'draft',
        images: p.images || '',
        state: p.state || '',
        district: p.district || '',
        village: p.village || '',
        farm_address: p.farm_address || ''
      });
    } catch (err) {
      setError('Failed to load product for auditing');
    } finally {
      setFetching(false);
    }
  };

  const handleSubmit = async (status: string) => {
    if (!user) return;
    setLoading(true);
    setError('');

    // Validate required fields
    if (!formData.name || !formData.category || !formData.quantity || !formData.unit) {
       setError('Please populate all mandatory fields (Name, Category, Quantity, Unit)');
       setLoading(false);
       return;
    }

    try {
      const data = {
        ...formData,
        expected_price: formData.expected_price ? parseFloat(formData.expected_price) : null,
        quantity: parseFloat(formData.quantity) || 0,
        status: status,
        harvest_date: formData.harvest_date || null
      };

      let response;
      if (isEdit) {
        response = await farmerProducts.update(user.id, id!, data);
      } else {
        response = await farmerProducts.create(user.id, data);
      }

      if (response.success) {
        navigate('/farmer/products');
      } else {
        setError(response.message || 'Operation failed');
      }
    } catch (err: any) {
      console.error('Submission error:', err);
      setError(err.response?.data?.detail || 'Fulfillment error. Check registry constraints.');
    } finally {
      setLoading(false);
    }
  };

  if (fetching) return (
    <div className="flex justify-center items-center h-[400px]">
      <div className="w-10 h-10 border-4 border-green-100 border-t-[#16a34a] rounded-full animate-spin"></div>
    </div>
  );

  return (
    <div className="animate-in fade-in duration-500 pb-20 text-left">
      <header className="mb-10">
        <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-gray-400 hover:text-[#16a34a] transition-colors mb-4 font-black uppercase tracking-widest text-[10px] group">
          <ArrowLeft size={16} className="group-hover:-translate-x-1 transition-transform" /> Discovery History
        </button>
        <h1 className="text-3xl font-black text-gray-900 mb-1 uppercase tracking-tight italic">{isEdit ? 'Audit Produce Listing' : 'Initialize Supply Path'}</h1>
        <p className="text-gray-500 font-medium italic text-[13px]">Define your agricultural produce parameters for market discovery.</p>
      </header>

      <div className="max-w-5xl">
        <div className="space-y-10">
          {error && (
            <div className="bg-red-50 border border-red-100 text-red-700 px-6 py-4 rounded-2xl shadow-sm flex items-center gap-4 text-xs font-black uppercase tracking-widest">
              <Info size={20} /> {error}
            </div>
          )}

          <div className="grid lg:grid-cols-2 gap-10">
             <div className="space-y-10">
                {/* Images */}
                <section className="bg-white rounded-[40px] p-10 shadow-xl shadow-green-900/5 border border-gray-100 relative overflow-hidden">
                  <div className="absolute top-0 right-0 w-1 h-full bg-[#16a34a]"></div>
                  <h3 className="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em] mb-8 italic flex items-center gap-2"><Camera size={14}/> Produce Imagery</h3>
                  <div
                     className="aspect-video bg-gray-50 border-2 border-dashed border-gray-200 rounded-[32px] flex flex-col items-center justify-center gap-4 cursor-pointer hover:bg-green-50 hover:border-[#16a34a]/30 transition-all group overflow-hidden shadow-inner"
                     onClick={() => {
                       const url = prompt('Enter Produce Image URL (Audit Standard):');
                       if (url) setFormData({...formData, images: url});
                     }}
                  >
                      {formData.images ? (
                          <img src={formData.images} className="w-full h-full object-cover" alt="Audit Preview" />
                      ) : (
                          <>
                              <div className="w-16 h-16 bg-white rounded-3xl shadow-xl flex items-center justify-center text-gray-300 group-hover:text-[#16a34a] group-hover:scale-110 transition-all border border-gray-100"><Plus size={32}/></div>
                              <p className="text-[10px] font-black text-gray-400 uppercase tracking-widest italic">Attach Intelligence File</p>
                          </>
                      )}
                  </div>
                </section>

                {/* Core Details */}
                <section className="bg-white rounded-[40px] p-10 shadow-xl shadow-green-900/5 border border-gray-100">
                  <h3 className="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em] mb-8 italic flex items-center gap-2"><Sprout size={14}/> Identity Parameters</h3>
                  <div className="space-y-8">
                     <FormField label="Produce Label" value={formData.name} onChange={(v: string) => setFormData({...formData, name: v})} placeholder="e.g. Sona Masuri Rice" />
                     <div className="space-y-3">
                          <label className="block text-[10px] font-black text-gray-400 uppercase tracking-widest italic leading-none">Registry Group</label>
                          <select
                            className="w-full bg-gray-50 border border-gray-100 rounded-2xl py-4 px-6 outline-none focus:ring-4 focus:ring-[#16a34a]/10 focus:bg-white transition-all font-bold text-xs uppercase tracking-tight"
                            value={formData.category}
                            onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                          >
                            <option value="">Select supply category...</option>
                            <option value="vegetables">Vegetables</option>
                            <option value="fruits">Fruits</option>
                            <option value="grains">Grains</option>
                            <option value="pulses">Pulses</option>
                            <option value="spices">Spices</option>
                            <option value="oilseeds">Oil Seeds</option>
                          </select>
                     </div>
                     <div className="space-y-3">
                          <label className="block text-[10px] font-black text-gray-400 uppercase tracking-widest italic leading-none">Intelligence details</label>
                          <textarea
                            className="w-full bg-gray-50 border border-gray-100 rounded-2xl py-4 px-6 outline-none focus:ring-4 focus:ring-[#16a34a]/10 focus:bg-white transition-all min-h-[120px] font-bold text-xs"
                            placeholder="Grade, quality standards, and harvest audit..."
                            value={formData.description}
                            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                          />
                     </div>
                  </div>
                </section>
             </div>

             <div className="space-y-10">
                {/* Metrics */}
                <section className="bg-white rounded-[40px] p-10 shadow-xl shadow-green-900/5 border border-gray-100 relative overflow-hidden">
                   <div className="absolute top-0 right-0 w-1 h-full bg-[#febd69]"></div>
                   <h3 className="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em] mb-8 italic flex items-center gap-2"><Package size={14}/> Supply Metrics</h3>
                   <div className="grid grid-cols-2 gap-8 mb-8">
                      <FormField label="Inventory Node" type="number" value={formData.quantity} onChange={(v: string) => setFormData({...formData, quantity: v})} placeholder="0.00" />
                      <div className="space-y-3">
                          <label className="block text-[10px] font-black text-gray-400 uppercase tracking-widest italic leading-none">Audit Unit</label>
                          <select className="w-full bg-gray-50 border border-gray-100 rounded-2xl py-4 px-6 outline-none font-bold text-xs uppercase tracking-widest" value={formData.unit} onChange={(e) => setFormData({...formData, unit: e.target.value})}>
                              <option value="kg">KG</option>
                              <option value="tonne">Tonne</option>
                              <option value="quintal">Quintal</option>
                              <option value="box">Box</option>
                          </select>
                      </div>
                   </div>
                   <FormField label="Discovery Rate (₹)" type="number" value={formData.expected_price} onChange={(v: string) => setFormData({...formData, expected_price: v})} placeholder="Standard node rate" />
                </section>

                {/* Sourcing */}
                <section className="bg-white rounded-[40px] p-10 shadow-xl shadow-green-900/5 border border-gray-100">
                   <h3 className="text-[10px] font-black text-gray-400 uppercase tracking-[0.2em] mb-8 italic flex items-center gap-2"><MapPin size={14}/> Origin Node</h3>
                   <div className="grid grid-cols-2 gap-8">
                      <FormField label="State Hub" value={formData.state} onChange={(v: string) => setFormData({...formData, state: v})} placeholder="State" />
                      <FormField label="District Cell" value={formData.district} onChange={(v: string) => setFormData({...formData, district: v})} placeholder="District" />
                   </div>
                   <div className="mt-8">
                      <FormField label="Fulfillment Village" value={formData.village} onChange={(v: string) => setFormData({...formData, village: v})} placeholder="Origin Name" />
                   </div>
                </section>

                {/* Status */}
                <section className="bg-[#131921] rounded-[40px] p-10 shadow-2xl relative overflow-hidden group">
                   <div className="absolute top-0 right-0 w-32 h-32 bg-white/5 rounded-full -mr-16 -mt-16 group-hover:scale-110 transition-transform duration-1000"></div>
                   <h3 className="text-[10px] font-black text-green-400 uppercase tracking-[0.2em] mb-8 italic flex items-center gap-2 relative z-10"><ShieldCheck size={16}/> Network Availability</h3>
                   <div className="flex gap-4 relative z-10">
                      <button
                        type="button"
                        onClick={() => setFormData({...formData, status: 'active'})}
                        className={`flex-1 py-4 rounded-2xl font-black text-[10px] uppercase tracking-widest transition-all ${formData.status === 'active' ? 'bg-[#16a34a] text-white shadow-xl shadow-green-900/40' : 'bg-white/5 text-white/30 border border-white/5'}`}
                      >
                          Active Pipeline
                      </button>
                      <button
                        type="button"
                        onClick={() => setFormData({...formData, status: 'harvesting_soon'})}
                        className={`flex-1 py-4 rounded-2xl font-black text-[10px] uppercase tracking-widest transition-all ${formData.status === 'harvesting_soon' ? 'bg-orange-500 text-white shadow-xl shadow-orange-900/40' : 'bg-white/5 text-white/30 border border-white/5'}`}
                      >
                          Soon
                      </button>
                   </div>
                </section>
             </div>
          </div>

          {/* Actions */}
          <div className="flex gap-6 max-w-lg pt-10 border-t border-gray-100">
             <button
               onClick={() => handleSubmit('draft')}
               disabled={loading}
               className="flex-1 bg-white border border-gray-200 text-gray-900 py-5 rounded-[24px] font-black uppercase tracking-[0.2em] text-[10px] hover:bg-gray-50 transition-all flex items-center justify-center gap-3 shadow-sm"
             >
                <Save size={20}/> Cache Draft
             </button>
             <button
               onClick={() => handleSubmit('active')}
               disabled={loading}
               className="flex-[2] bg-[#131921] text-white py-5 rounded-[24px] font-black uppercase tracking-[0.2em] text-[10px] shadow-2xl hover:bg-[#16a34a] transition-all flex items-center justify-center gap-3"
             >
                {loading ? <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div> : <><Send size={20}/> Publish Intelligence</>}
             </button>
          </div>
        </div>
      </div>
    </div>
  );
};

const FormField = ({ label, value, onChange, placeholder, type = 'text' }: any) => (
    <div className="space-y-3">
      <label className="block text-[10px] font-black text-gray-400 uppercase tracking-widest italic leading-none">{label}</label>
      <input
        type={type}
        className="w-full bg-gray-50 border border-gray-100 rounded-2xl py-4 px-6 outline-none focus:ring-4 focus:ring-[#16a34a]/10 focus:bg-white transition-all font-bold text-xs uppercase tracking-tight placeholder:text-gray-200"
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
);
