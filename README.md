# AgriConnect - 0% Mediator Marketplace

## Overview

AgriConnect is a completely FREE marketplace platform that connects farmers directly with buyers. The platform acts as a contact intermediary only—all negotiations, inspections, logistics, and payments happen directly between farmers and buyers off-platform.

### Key Features

✅ **Completely FREE** - No booking fees, no platform charges  
✅ **Instant Contact Unlock** - Farmer details available immediately upon booking  
✅ **7-Stage Buyer Journey** - Clear progression from registration to completion  
✅ **Multi-Role Support** - Farmer, Buyer, and Admin roles  
✅ **Real-Time Notifications** - Booking updates and status changes  
✅ **Product Management** - Farmers can list, manage, and publish products  
✅ **Search & Filter** - Buyers can find products by category, location, price  
✅ **Safety & Reporting** - Report suspicious activity or false listings  

---

## Technology Stack

### Backend
- **Framework**: FastAPI (Python)
- **Database**: SQLite (SQLAlchemy ORM)
- **Authentication**: JWT + OTP (6-digit, SMS-based)
- **Password Hashing**: Bcrypt
- **Server**: Uvicorn

### Frontend
- **Framework**: Flutter (Native Android/iOS)
- **State Management**: Provider/Riverpod
- **Networking**: HTTP/REST API

---

## Installation & Setup

### Prerequisites
- Python 3.9+
- pip or conda
- Virtual environment (recommended)

### Backend Setup

1. **Clone/Navigate to the project**
   ```bash
   cd "c:\Users\ravi kiran\OneDrive\Documents\agri connect\backend"
   ```

2. **Create virtual environment**
   ```bash
   python -m venv venv
   venv\Scripts\activate  # Windows
   # source venv/bin/activate  # Mac/Linux
   ```

3. **Install dependencies**
   ```bash
   pip install -r requirements.txt
   ```

4. **Run the server**
   ```bash
   python -m uvicorn app:app --reload --host 0.0.0.0 --port 8000
   ```

   The API will be available at: `http://localhost:8000`

5. **Access documentation**
   - Swagger UI: `http://localhost:8000/docs`
   - ReDoc: `http://localhost:8000/redoc`

---

## API Documentation

### Authentication Endpoints

#### Farmer Registration
```
POST /api/farmer/register
{
  "mobile_number": "9876543210",
  "password": "Password123",
  "full_name": "Rajesh Kumar",
  "state": "Maharashtra",
  "district": "Pune",
  "village": "Dhule Patil Village"
}
```

#### Buyer Registration
```
POST /api/buyer/register
{
  "mobile_number": "9876543210",
  "password": "Password123",
  "full_name": "Priya Sharma",
  "state": "Maharashtra",
  "district": "Pune",
  "buyer_type": "merchant"
}
```

#### Verify OTP
```
POST /api/farmer/verify-otp
{
  "mobile_number": "9876543210",
  "otp_code": "123456"
}
```

#### Login
```
POST /api/farmer/login
{
  "mobile_number": "9876543210",
  "password": "Password123"
}
```

### Farmer Endpoints

#### Create Product
```
POST /api/farmer/products
Headers: Authorization: Bearer {token}
{
  "name": "Fresh Tomatoes",
  "category": "vegetables",
  "quantity": 100,
  "unit": "kg",
  "expected_price": 30,
  "description": "Organic, pesticide-free tomatoes"
}
```

#### Get All Products
```
GET /api/farmer/products
Headers: Authorization: Bearer {token}
```

#### Publish Product
```
POST /api/farmer/products/{product_id}/publish
Headers: Authorization: Bearer {token}
```

#### Get Bookings
```
GET /api/farmer/bookings
Headers: Authorization: Bearer {token}
```

#### Get Dashboard
```
GET /api/farmer/dashboard
Headers: Authorization: Bearer {token}
```

### Buyer Endpoints

#### Browse Home
```
GET /api/buyer/home?skip=0&limit=10
```

#### Search Products
```
GET /api/buyer/search?query=tomato&state=Maharashtra&district=Pune&min_price=20&max_price=50
```

#### Get Product Details
```
GET /api/buyer/products/{product_id}
```

#### Create Booking (Stage 4: Instant Contact Unlock)
```
POST /api/buyer/booking
Headers: Authorization: Bearer {token}
{
  "product_id": "uuid",
  "terms_accepted": true
}
```

#### Get My Bookings
```
GET /api/buyer/bookings
Headers: Authorization: Bearer {token}
```

#### Mark Booking Completed
```
POST /api/buyer/bookings/{booking_id}/complete
Headers: Authorization: Bearer {token}
```

#### Submit Report
```
POST /api/buyer/report
Headers: Authorization: Bearer {token}
{
  "booking_id": "ABC123D4",
  "reason": "farmer_unreachable",
  "description": "Farmer not responding to calls"
}
```

### Platform Endpoints

#### Get Statistics
```
GET /api/stats
```

#### Health Check
```
GET /health
```

---

## Data Models

### User
- `id`: UUID (unique identifier)
- `mobile_number`: String (10-15 digits, unique)
- `email`: String (optional, unique)
- `password_hash`: Encrypted password
- `full_name`: String
- `role`: Enum (BUYER, FARMER, ADMIN)
- `account_status`: Enum (ACTIVE, SUSPENDED, DELETED)
- `mobile_verified`: Boolean
- `created_at`, `updated_at`: Timestamps

### Buyer
- `id`: UUID
- `user_id`: Foreign Key to User
- `buyer_type`: Enum (MERCHANT, WHOLESALER, RETAILER, INDIVIDUAL, OTHER)
- `state`, `district`: Location
- `preferred_language`: Enum
- `bookings_count`: Integer
- `created_at`, `updated_at`: Timestamps

### Farmer
- `id`: UUID
- `user_id`: Foreign Key to User
- `state`, `district`, `village`: Location
- `farm_address`: Text (optional)
- `latitude`, `longitude`: GPS coordinates (optional)
- `share_farm_address`, `share_coordinates`: Boolean (privacy controls)
- `completed_bookings`: Integer
- `rating`: Float
- `created_at`, `updated_at`: Timestamps

### Product
- `id`: UUID
- `farmer_id`: Foreign Key to Farmer
- `name`: String
- `category`: Enum (VEGETABLES, FRUITS, GRAINS, etc.)
- `quantity`: Float
- `unit`: String (kg, quintal, units, etc.)
- `expected_price`: Float (optional)
- `harvest_date`: DateTime (optional)
- `status`: Enum (DRAFT, ACTIVE, SOLD, EXPIRED, REMOVED)
- `images`: String (JSON URLs)
- `created_at`, `updated_at`: Timestamps

### Booking
- `id`: UUID
- `booking_id`: Human-readable ID (e.g., ABC123D4)
- `buyer_id`: Foreign Key to Buyer
- `farmer_id`: Foreign Key to Farmer
- `product_id`: Foreign Key to Product
- `status`: Enum (INITIATED, CONFIRMED, COMPLETED, CANCELLED)
- `contact_unlocked_at`: DateTime (when farmer details become visible)
- `terms_accepted`: Boolean
- `created_at`, `updated_at`, `completed_at`: Timestamps

### Notification
- `id`: UUID
- `user_id`: Foreign Key to User
- `title`, `message`: Strings
- `notification_type`: String (booking_confirmed, product_status_changed, etc.)
- `is_read`: Boolean
- `created_at`: Timestamp

### Report
- `id`: UUID
- `reporter_id`: Foreign Key to Buyer
- `reported_user_id`: Foreign Key to Farmer (optional)
- `product_id`: Foreign Key to Product (optional)
- `booking_id`: Foreign Key to Booking (optional)
- `reason`: Enum (FALSE_PRODUCT_INFO, MISLEADING_IMAGES, etc.)
- `description`: Text (optional)
- `status`: Enum (SUBMITTED, UNDER_REVIEW, RESOLVED, DISMISSED)

---

## 7-Stage Buyer Journey

### Stage 1: Account Setup
- Register as Buyer
- Verify mobile via OTP
- Create buyer profile with location

### Stage 2: Discovery
- Browse products by category
- Search by product name (fuzzy matching)
- Filter by location, price, quantity, harvest date

### Stage 3: Product Evaluation
- View detailed product information
- Farmer contact details are hidden
- See district/state level location

### Stage 4: Booking & Unlock (FREE & INSTANT)
- Click "Book Product"
- Review and accept terms
- Contact details unlock instantly (no payment)
- Booking is CONFIRMED immediately

### Stage 5: Direct Engagement
- Farmer is notified in real-time
- Buyer receives farmer's full contact details
- Call, message, or arrange visit directly
- No platform involvement in this stage

### Stage 6: Off-Platform Transaction
- Visit farm and inspect product
- Negotiate price and terms directly
- Agree on logistics and payment method
- Complete transaction off-platform

### Stage 7: Completion
- Mark booking as COMPLETED
- Optional: Submit report if issues occurred
- Transaction history is recorded

---

## Business Rules

### Free Platform
✅ No booking fee  
✅ No transaction charge  
✅ No platform fees  
✅ 100% farmer-buyer direct connection  

### Rate Limiting (Abuse Prevention)
- Max 20 bookings per buyer per day (prevents contact harvesting)
- Max 10 reports per buyer per day (prevents harassment)
- Max 3 OTP resends per 5 minutes

### Privacy Controls
- Farmer contact hidden pre-booking
- Field-level consent for location sharing
- Buyer location not shown to farmer
- Contact info, once shared, is permanent

### Product Management
- Only ACTIVE products visible to buyers
- Draft/Sold/Expired/Removed products hidden
- Farmers can publish products anytime
- Once booking confirmed, product reference persists

### Payment (Not Applicable)
- Platform is 100% FREE
- No payment gateway integration
- No transaction records
- Direct farmer-buyer settlement off-platform

---

## File Structure

```
backend/
├── app.py                 # Main FastAPI app, routers initialization
├── models.py              # SQLAlchemy ORM models (all entities)
├── database.py            # Database setup, session management
├── auth.py                # Authentication logic (OTP, JWT, hashing)
├── farmer.py              # Farmer role endpoints
├── buyer.py               # Buyer role endpoints
├── merchant.py            # Merchant role endpoints (optional)
├── payment.py             # Payment logic (not used, FREE platform)
├── requriments.txt        # Python dependencies
└── agriconnect.db         # SQLite database (created on first run)

frontend/
├── android/               # Android app (Flutter)
├── ios/                   # iOS app (Flutter)
└── lib/                   # Flutter source code
```

---

## Development Workflow

1. **Backend Development**
   ```bash
   cd backend
   python -m venv venv
   venv\Scripts\activate
   pip install -r requirements.txt
   python -m uvicorn app:app --reload
   ```

2. **Test API**
   - Visit `http://localhost:8000/docs` (Swagger UI)
   - Try out endpoints with sample data

3. **Frontend Development**
   ```bash
   cd frontend
   flutter pub get
   flutter run
   ```

---

## Security Considerations

### Production Checklist
- [ ] Change SECRET_KEY in auth.py
- [ ] Enable HTTPS everywhere
- [ ] Use environment variables for configuration
- [ ] Implement rate limiting on all endpoints
- [ ] Add request logging and monitoring
- [ ] Use a production database (PostgreSQL, MySQL)
- [ ] Implement proper CORS configuration
- [ ] Add request validation and sanitization
- [ ] Implement user authentication middleware
- [ ] Add admin panel for user/content moderation
- [ ] Set up automated backups
- [ ] Implement error tracking (Sentry)
- [ ] Add SMS gateway for OTP delivery
- [ ] Implement refresh token rotation
- [ ] Add password reset functionality

---

## Future Enhancements

1. **Payment Integration** - If monetization needed
2. **Farmer Rating System** - Based on completed bookings
3. **In-App Messaging** - Integrated chat
4. **Order History** - Analytics for farmers
5. **Multi-Language Support** - Already in models
6. **Admin Dashboard** - Content moderation, reports review
7. **Mobile Push Notifications** - Real-time alerts
8. **Advanced Analytics** - Platform insights
9. **Batch Ordering** - Multiple products in one booking
10. **Quality Rating** - Product feedback system

---

## License

MIT License - Free to use and modify

---

## Support

For issues, questions, or contributions:
- Create an issue in the repository
- Contact development team
- Check documentation at `/docs`

---

## Changelog

### v1.0.0 (Initial Release)
- ✅ Complete Farmer role with product management
- ✅ Complete Buyer role with discovery and booking
- ✅ Free, instant booking system
- ✅ 7-stage buyer journey
- ✅ OTP-based authentication
- ✅ Real-time notifications
- ✅ Reporting system
- ✅ Rate limiting and abuse prevention
- ✅ Multi-location support (state/district/village)
- ✅ Product categorization and search

---

**Built with ❤️ for Indian Agriculture**
