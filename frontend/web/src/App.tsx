import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { BuyerSignup } from './pages/BuyerSignup';
import { FarmerSignup } from './pages/FarmerSignup';
import { ProductDetail } from './pages/ProductDetail';
import { FarmerDashboard } from './pages/FarmerDashboard';
import { MyProducts } from './pages/MyProducts';
import { AddProduct } from './pages/AddProduct';
import { FarmerBookings } from './pages/FarmerBookings';
import { AdminDashboard } from './pages/AdminDashboard';
import { AdminManagement } from './pages/AdminManagement';
import { AdminLogin } from './pages/AdminLogin';
import { MyBookings } from './pages/MyBookings';
import { Profile } from './pages/Profile';
import { LegalPage } from './pages/LegalPage';
import { Explore } from './pages/Explore';
import { Saved } from './pages/Saved';
import { Notifications } from './pages/Notifications';
import { BottomNav } from './components/BottomNav';
import { AdminLayout } from './components/AdminLayout';
import { MerchantLayout } from './components/MerchantLayout';
import { MerchantPortal } from './pages/MerchantPortal';
import { FarmerLayout } from './components/FarmerLayout';
import { FarmersRegistry } from './pages/FarmersRegistry';
import { FarmerPublicProfile } from './pages/FarmerPublicProfile';
import { Layers } from 'lucide-react';
import { useAuthStore } from './store/authStore';
import { App as CapApp } from '@capacitor/app';
import { StatusBar, Style } from '@capacitor/status-bar';
import { SplashScreen } from '@capacitor/splash-screen';

function PlaceholderView({ title }: { title: string }) {
  return (
    <div className="text-center py-40 bg-white rounded-xl border border-gray-200 shadow-sm animate-in fade-in duration-500">
      <div className="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-6 text-gray-200">
        <Layers size={40} />
      </div>
      <h3 className="text-2xl font-black text-gray-900 uppercase tracking-tighter mb-2">{title}</h3>
      <p className="text-gray-400 italic font-medium">This module is currently being optimized for the unified portal experience.</p>
    </div>
  );
}

function App() {
  const { user, token } = useAuthStore();

  React.useEffect(() => {
    // Initialize Native UI
    const initNative = async () => {
      try {
        await StatusBar.setStyle({ style: Style.Dark });
        await StatusBar.setBackgroundColor({ color: '#002f1a' });
        await SplashScreen.hide();
      } catch (e) {
        console.warn('Native UI initialization skipped (Web mode)');
      }
    };

    initNative();

    // Handle Android hardware back button
    const backListener = CapApp.addListener('backButton', ({ canGoBack }) => {
      if (!canGoBack || window.location.pathname === '/' || window.location.pathname === '/farmer/dashboard' || window.location.pathname === '/admin/dashboard') {
        CapApp.exitApp();
      } else {
        window.history.back();
      }
    });

    return () => {
      backListener.then(l => l.remove());
    };
  }, []);

  return (
    <Router>
      <Routes>
        {/* Auth routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/farmer/login" element={<LoginPage />} />
        <Route path="/buyer/signup" element={<BuyerSignup />} />
        <Route path="/farmer/signup" element={<FarmerSignup />} />
        <Route path="/admin/login" element={<AdminLogin />} />

        {/* Admin Routes */}
        <Route path="/admin" element={token && user?.role === 'admin' ? <AdminLayout /> : <Navigate to="/login" />}>
          <Route path="dashboard" element={<AdminDashboard />} />
          <Route path="manage" element={<AdminManagement />} />
          <Route path="profile" element={<Profile />} />
          <Route index element={<Navigate to="/admin/dashboard" replace />} />
        </Route>

        {/* Farmer Portal Routes */}
        <Route path="/farmer" element={token && user?.role === 'farmer' ? <FarmerLayout /> : <Navigate to="/login" />}>
          <Route path="dashboard" element={<FarmerDashboard />} />
          <Route path="products" element={<MyProducts />} />
          <Route path="add-product" element={<AddProduct />} />
          <Route path="edit-product/:id" element={<AddProduct />} />
          <Route path="bookings" element={<FarmerBookings />} />
          <Route path="profile" element={<Profile />} />
          <Route path="notifications" element={<Notifications />} />
          <Route path="messages" element={<Notifications />} />
          <Route path="earnings" element={<PlaceholderView title="Earnings Dashboard" />} />
          <Route index element={<Navigate to="/farmer/dashboard" replace />} />
        </Route>

        {/* Guest and Merchant root */}
        <Route path="/" element={<MerchantLayout />}>
          <Route index element={<MerchantPortal />} />
          <Route path="merchant/portal" element={<Navigate to="/" replace />} />
          <Route path="merchant/explore" element={<Explore />} />
          <Route path="merchant/bookings" element={<MyBookings />} />
          <Route path="merchant/saved" element={<Saved />} />
          <Route path="merchant/profile" element={<Profile />} />
          <Route path="merchant/notifications" element={<Notifications />} />
          <Route path="merchant/messages" element={<Notifications />} />
          <Route path="merchant/product/:id" element={<ProductDetail />} />
          <Route path="merchant/farmer/:id" element={<FarmerPublicProfile />} />
          <Route path="merchant/farmers" element={<FarmersRegistry />} />
          <Route path="merchant/payments" element={<PlaceholderView title="Payments & Invoices" />} />
          <Route path="merchant/invoices" element={<PlaceholderView title="Invoices" />} />
          <Route path="merchant/reports" element={<PlaceholderView title="Incident Reports" />} />

          <Route path="explore" element={<Explore />} />
          <Route path="product/:id" element={<ProductDetail />} />
          <Route path="farmer/:id" element={<FarmerPublicProfile />} />
        </Route>

        <Route path="/legal/*" element={<LegalPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
