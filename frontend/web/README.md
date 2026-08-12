# AgriConnect Web Frontend

A modern, responsive React web application for the AgriConnect marketplace platform.

## 🌾 About AgriConnect

AgriConnect is a **100% FREE** marketplace connecting farmers directly with buyers. No middlemen, no fees, no hidden charges.

## 📋 Features

### For Buyers
- ✅ User registration and authentication (Mobile + OTP)
- ✅ Product discovery and search with filters
- ✅ Instant FREE product booking
- ✅ Direct farmer contact information (unlocked on booking)
- ✅ Booking management
- ✅ Report system for issues

### For Farmers
- ✅ Farmer account registration
- ✅ Product management (create, update, delete, publish)
- ✅ Sales dashboard with statistics
- ✅ Booking management and buyer inquiries
- ✅ Real-time notifications

### Platform Features
- 🔐 JWT + OTP authentication
- 📱 Fully responsive design
- 🎨 Modern Tailwind CSS styling
- ⚡ Fast with Vite bundler
- 🔗 Direct API integration with FastAPI backend
- 📊 Real-time statistics and dashboards

## 🚀 Quick Start

### Prerequisites
- Node.js 16+ and npm/yarn
- Backend API running on http://localhost:8000

### Installation

1. **Navigate to the frontend directory**
```bash
cd "c:\Users\ravi kiran\OneDrive\Documents\agri connect\frontend\web"
```

2. **Install dependencies**
```bash
npm install
```

3. **Start development server**
```bash
npm run dev
```

The application will be available at `http://localhost:3000`

## 📖 Usage

### Home Page
- Landing page with information about AgriConnect
- Quick access links to buyer and farmer signup

### Buyer Flow
1. **Sign Up** - Register with mobile number
2. **Verify OTP** - Verify OTP (check terminal for test OTP)
3. **Home** - Discover featured products
4. **Search** - Search for specific products
5. **Product Detail** - View product and farmer info
6. **Booking** - Book products instantly (FREE)
7. **Contact** - Get farmer contact details after booking

### Farmer Flow
1. **Sign Up** - Create farmer account
2. **Verify OTP** - Complete registration
3. **Dashboard** - View statistics and manage products
4. **Add Product** - Create new product listings
5. **Manage Bookings** - View buyer inquiries

### Login
- Choose user type (Buyer/Farmer)
- Enter mobile number and password
- Verify OTP to complete login

## 🏗️ Project Structure

```
frontend/web/
├── src/
│   ├── components/
│   │   └── Navbar.tsx          # Navigation bar component
│   ├── pages/
│   │   ├── HomePage.tsx        # Landing page
│   │   ├── LoginPage.tsx       # Login page
│   │   ├── BuyerSignup.tsx     # Buyer registration
│   │   ├── FarmerSignup.tsx    # Farmer registration
│   │   ├── BuyerHome.tsx       # Buyer dashboard
│   │   ├── FarmerDashboard.tsx # Farmer dashboard
│   │   └── ProductDetail.tsx   # Product details page
│   ├── services/
│   │   ├── api.ts             # Axios API client
│   │   ├── authService.ts     # Authentication API calls
│   │   └── productService.ts  # Product API calls
│   ├── store/
│   │   └── authStore.ts       # Zustand auth state management
│   ├── App.tsx                # Main app with routing
│   ├── index.css              # Global styles
│   └── main.tsx               # React entry point
├── index.html                 # HTML entry point
├── vite.config.ts            # Vite configuration
├── tsconfig.json             # TypeScript configuration
├── tailwind.config.js        # Tailwind CSS configuration
├── postcss.config.js         # PostCSS configuration
├── package.json              # Dependencies
├── .env                      # Environment variables
├── .env.example              # Example environment variables
├── .gitignore               # Git ignore file
└── README.md                # This file
```

## 🔧 Configuration

### Environment Variables

Create a `.env` file from `.env.example`:

```env
VITE_REACT_APP_API_URL=http://localhost:8000/api
VITE_APP_NAME=AgriConnect
VITE_APP_VERSION=1.0.0
```

### API Proxy Configuration

Vite is configured to proxy API requests from `/api` to the backend server:

```
/api/... → http://localhost:8000/api/...
```

## 🎨 Styling

- **Tailwind CSS** - Utility-first CSS framework
- **Custom components** - Custom button and card styles
- **Responsive design** - Works on desktop, tablet, and mobile
- **Color scheme** - Green for buyers, blue for farmers

### Custom CSS Classes

- `.btn-primary` - Primary action button
- `.btn-secondary` - Secondary action button
- `.card` - Card component
- `.input-field` - Form input field
- `.badge` - Badge component

## 📱 API Integration

The frontend communicates with the FastAPI backend via REST API.

### Key Endpoints

**Authentication:**
- `POST /api/buyer/register` - Buyer registration
- `POST /api/buyer/verify-otp` - Buyer OTP verification
- `POST /api/buyer/login` - Buyer login
- `POST /api/farmer/register` - Farmer registration
- `POST /api/farmer/verify-otp` - Farmer OTP verification
- `POST /api/farmer/login` - Farmer login

**Products:**
- `GET /api/buyer/home` - Featured products
- `GET /api/buyer/search` - Search products
- `GET /api/buyer/products/{id}` - Product details
- `POST /api/farmer/products` - Create product
- `GET /api/farmer/products` - Get farmer's products
- `PUT /api/farmer/products/{id}` - Update product
- `DELETE /api/farmer/products/{id}` - Delete product

**Bookings:**
- `POST /api/buyer/booking` - Create booking
- `GET /api/buyer/bookings` - Get buyer's bookings
- `GET /api/farmer/bookings` - Get farmer's bookings
- `POST /api/buyer/bookings/{id}/complete` - Complete booking

See the [Backend IMPLEMENTATION_GUIDE.md](../../../backend/IMPLEMENTATION_GUIDE.md) for complete API documentation.

## 🔐 Authentication

The frontend uses JWT tokens for authentication:

1. **Access Token** - Short-lived token (30 minutes) for API requests
2. **Refresh Token** - Long-lived token (7 days) for refreshing access
3. **OTP** - 6-digit one-time password for verification

Tokens are stored in `localStorage` and automatically added to request headers.

## 🛠️ Available Scripts

```bash
# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Check TypeScript errors
npm run type-check

# Run linter
npm run lint
```

## 📦 Dependencies

- **React 18** - UI library
- **React Router v6** - Routing
- **Axios** - HTTP client
- **Zustand** - State management
- **Tailwind CSS** - Styling
- **Lucide React** - Icons
- **Vite** - Build tool
- **TypeScript** - Type safety

## 🚀 Production Build

```bash
npm run build
```

This creates an optimized production build in the `dist/` directory.

## 🔗 Backend Integration

Make sure the FastAPI backend is running on `http://localhost:8000` with these details:

- **Database**: SQLite (`agriconnect.db`)
- **API**: Running on http://0.0.0.0:8000
- **Documentation**: http://localhost:8000/docs (Swagger UI)

## 📝 Development Tips

1. **Hot Reload** - Changes are automatically reflected in the browser
2. **React DevTools** - Use React DevTools browser extension for debugging
3. **Network Tab** - Check browser DevTools Network tab to debug API calls
4. **Console Logs** - Check browser console for error messages
5. **OTP in Test Mode** - OTPs are printed in backend terminal during testing

## 🐛 Troubleshooting

### "API connection refused"
- Ensure backend is running on http://localhost:8000
- Check if CORS is enabled in backend (it is by default)

### "Port 3000 already in use"
```bash
# Kill process on port 3000 or use different port
npm run dev -- --port 3001
```

### "Module not found"
```bash
# Clear node_modules and reinstall
rm -r node_modules
npm install
```

### "OTP not working"
- Check terminal where backend is running for OTP value
- OTP is displayed in console during test mode

## 📄 License

AgriConnect - 100% FREE marketplace for farmers and buyers

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:
1. Create a new branch for your feature
2. Make your changes
3. Test thoroughly
4. Submit a pull request

## 📞 Support

For issues or questions:
1. Check the troubleshooting section
2. Review backend IMPLEMENTATION_GUIDE.md
3. Check browser console for error messages
4. Review backend logs for API issues

---

**Built with ❤️ for farmers and buyers**
