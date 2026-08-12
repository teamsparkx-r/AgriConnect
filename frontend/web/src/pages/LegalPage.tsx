import React from 'react';
import { useLocation, Link } from 'react-router-dom';
import { ChevronLeft, Shield, FileText, Info } from 'lucide-react';

export const LegalPage: React.FC = () => {
  const location = useLocation();
  const type = location.pathname.split('/').pop() || 'terms';

  const content: any = {
    terms: {
      title: 'Conditions of Use',
      icon: <FileText size={40} />,
      text: 'AgriConnect is a 0% mediator platform. We do not participate in negotiations, logistics, or final payments. By using this platform, you agree that any transactions conducted are solely between the buyer and the farmer.'
    },
    privacy: {
      title: 'Privacy Notice',
      icon: <Shield size={40} />,
      text: 'We protect farmer and buyer data. Farmer contact details are only unlocked after a successful booking request. We do not sell your personal data to third parties.'
    },
    about: {
        title: 'About AgriConnect',
        icon: <Info size={40} />,
        text: 'AgriConnect was built to empower farmers by removing expensive middlemen. Our mission is to provide a free, transparent marketplace where food producers and consumers can connect directly.'
    }
  };

  const active = content[type] || content.terms;

  return (
    <div className="min-h-screen bg-white">
      <div className="container mx-auto px-4 py-16 max-w-3xl">
        <Link to="/" className="inline-flex items-center gap-2 text-sm font-bold text-gray-400 hover:text-green-600 mb-12 transition-colors">
          <ChevronLeft size={16} /> Back to Home
        </Link>

        <div className="text-green-600 mb-8">{active.icon}</div>
        <h1 className="text-4xl font-black text-gray-900 mb-8">{active.title}</h1>

        <div className="prose prose-green lg:prose-xl text-gray-600 leading-relaxed italic">
          {active.text}
          <p className="mt-8 not-italic font-medium text-gray-900">
            For more specific inquiries, please contact our support team at support@agriconnect.com
          </p>
        </div>

        <div className="mt-20 pt-12 border-t border-gray-100 flex flex-wrap gap-8">
            <Link to="/legal/about" className="text-sm font-bold text-gray-900 hover:text-green-600">About</Link>
            <Link to="/legal/terms" className="text-sm font-bold text-gray-900 hover:text-green-600">Terms</Link>
            <Link to="/legal/privacy" className="text-sm font-bold text-gray-900 hover:text-green-600">Privacy</Link>
        </div>
      </div>
    </div>
  );
};
