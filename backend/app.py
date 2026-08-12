from fastapi import FastAPI, Depends
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from database import init_db, get_db, SessionLocal
from farmer import router as farmer_router
from buyer import router as buyer_router
from admin import router as admin_router
from auth_routes import router as auth_router
from auth import AuthService
from models import User, Product, Booking, ProductStatus, BookingStatus, UserRole, Farmer, Buyer
import os
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# Initialize FastAPI app
app = FastAPI(
    title="AgriConnect API",
    description="A 0% mediator marketplace connecting farmers and buyers",
    version="1.0.0"
)

# Initialize database
init_db()

# Create default admin user if none exists
ADMIN_MOBILE = os.getenv("ADMIN_MOBILE", "9999999999")
ADMIN_PASSWORD = os.getenv("ADMIN_PASSWORD", "Admin123!")

def seed_db():
    db = SessionLocal()
    try:
        # Create default admin
        existing_admin = db.query(User).filter(User.role == UserRole.ADMIN).first()
        if not existing_admin:
            admin_user = User(
                mobile_number=ADMIN_MOBILE,
                email=None,
                password_hash=AuthService.hash_password(ADMIN_PASSWORD),
                full_name="AgriConnect Admin",
                role=UserRole.ADMIN,
                account_status="active"
            )
            db.add(admin_user)
            print(f"Created default admin user: {ADMIN_MOBILE}")

        # Create Demo Farmer
        demo_farmer_mobile = "8888888888"
        existing_farmer = db.query(User).filter(User.mobile_number == demo_farmer_mobile).first()
        if not existing_farmer:
            farmer_user = User(
                mobile_number=demo_farmer_mobile,
                password_hash=AuthService.hash_password("Farmer123!"),
                full_name="Demo Farmer",
                role=UserRole.FARMER,
                mobile_verified=True
            )
            db.add(farmer_user)
            db.flush()

            farmer_profile = Farmer(
                user_id=farmer_user.id,
                state="Maharashtra",
                district="Pune",
                village="Demo Village"
            )
            db.add(farmer_profile)
            db.flush()

            # Add diverse demo products
            products_data = [
                {"name": "Organic Wheat", "category": "grains", "qty": 1000, "price": 25.0, "img": "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=800"},
                {"name": "Red Tomatoes", "category": "vegetables", "qty": 500, "price": 18.5, "img": "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=800"},
                {"name": "Basmati Rice", "category": "grains", "qty": 2000, "price": 65.0, "img": "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=800"},
                {"name": "Fresh Alphonso Mangoes", "category": "fruits", "qty": 100, "price": 450.0, "img": "https://images.unsplash.com/photo-1553279768-865429fa0078?w=800"},
                {"name": "Kashmiri Chillies", "category": "spices", "qty": 50, "price": 120.0, "img": "https://images.unsplash.com/photo-1597131628347-c96939c3f42c?w=800"},
                {"name": "Yellow Dal", "category": "pulses", "qty": 800, "price": 95.0, "img": "https://images.unsplash.com/photo-1585994192701-d1939103bc39?w=800"},
            ]

            for p in products_data:
                new_p = Product(
                    farmer_id=farmer_profile.id,
                    name=p["name"],
                    category=p["category"],
                    description=f"High quality {p['name']} directly from the farm.",
                    quantity=p["qty"],
                    unit="kg" if p["category"] != "fruits" else "box",
                    expected_price=p["price"],
                    status=ProductStatus.ACTIVE,
                    images=p["img"]
                )
                db.add(new_p)

            print(f"Created demo farmer and products: {demo_farmer_mobile}")

        # Create Demo Buyer
        demo_buyer_mobile = "7777777777"
        existing_buyer = db.query(User).filter(User.mobile_number == demo_buyer_mobile).first()
        if not existing_buyer:
            buyer_user = User(
                mobile_number=demo_buyer_mobile,
                password_hash=AuthService.hash_password("Buyer123!"),
                full_name="Demo Buyer",
                role=UserRole.BUYER,
                mobile_verified=True
            )
            db.add(buyer_user)
            db.flush()

            buyer_profile = Buyer(
                user_id=buyer_user.id,
                state="Maharashtra",
                district="Mumbai"
            )
            db.add(buyer_profile)
            print(f"Created demo buyer: {demo_buyer_mobile}")

        db.commit()
    except Exception as e:
        print(f"Error seeding database: {e}")
        db.rollback()
    finally:
        db.close()

seed_db()

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify allowed origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(auth_router, prefix="/api/auth", tags=["Authentication"])
app.include_router(farmer_router, prefix="/api/farmer", tags=["Farmer"])
app.include_router(buyer_router, prefix="/api/buyer", tags=["Buyer"])
app.include_router(admin_router, prefix="/api/admin", tags=["Admin"])

# ==================== HOME ENDPOINTS ====================

@app.get("/")
def home():
    """Welcome endpoint"""
    return {
        "message": "Welcome to AgriConnect - 0% Mediator Marketplace",
        "version": "1.0.0",
        "description": "Connect farmers and buyers directly. Completely FREE platform.",
        "endpoints": {
            "farmer": "/api/farmer",
            "buyer": "/api/buyer",
            "docs": "/docs",
            "redoc": "/redoc"
        }
    }

@app.get("/health")
def health_check():
    """Health check endpoint"""
    return {
        "status": "healthy",
        "service": "AgriConnect API",
        "timestamp": __import__('datetime').datetime.utcnow().isoformat()
    }

@app.get("/api/stats")
def get_platform_stats(db: Session = Depends(get_db)):
    """Get platform statistics"""
    from models import User, Product, Booking, ProductStatus, BookingStatus
    
    total_users = db.query(User).count()
    total_farmers = db.query(User).filter(User.role == UserRole.FARMER).count()
    total_buyers = db.query(User).filter(User.role == UserRole.BUYER).count()
    total_products = db.query(Product).count()
    active_products = db.query(Product).filter(Product.status == ProductStatus.ACTIVE).count()
    total_bookings = db.query(Booking).count()
    completed_bookings = db.query(Booking).filter(Booking.status == BookingStatus.COMPLETED).count()
    
    return {
        "success": True,
        "statistics": {
            "total_users": total_users,
            "total_farmers": total_farmers,
            "total_buyers": total_buyers,
            "total_products": total_products,
            "active_products": active_products,
            "total_bookings": total_bookings,
            "completed_bookings": completed_bookings,
            "completion_rate": f"{(completed_bookings / total_bookings * 100):.1f}%" if total_bookings > 0 else "0%"
        }
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )
