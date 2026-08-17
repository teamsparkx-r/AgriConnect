from fastapi import FastAPI, Depends
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from database import init_db, get_db, SessionLocal
from farmer import router as farmer_router
from buyer import router as buyer_router
from admin import router as admin_router
from auth_routes import router as auth_router
from auth import AuthService
from models import User, Product, Booking, ProductStatus, BookingStatus, UserRole, Farmer, Buyer, ProductCategory, AccountStatus
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
        # 1. Create default admin
        admin_id = "2687eded-053d-4cbc-8a06-18be9ad5888b"
        existing_admin = db.query(User).filter(User.id == admin_id).first()
        if not existing_admin:
            print(f"Seeding admin. Password length: {len(str(ADMIN_PASSWORD))}")
            admin_user = User(
                id=admin_id,
                mobile_number=ADMIN_MOBILE,
                password_hash=AuthService.hash_password(str(ADMIN_PASSWORD)),
                full_name="AgriConnect Admin",
                role=UserRole.ADMIN,
                account_status=AccountStatus.ACTIVE
            )
            db.add(admin_user)
            db.flush()
            print(f"Created default admin user: {ADMIN_MOBILE}")

        # 2. Create Demo Farmer
        farmer_user_id = "5737b51e-8640-48c2-bdaa-63fbba1b70a7"
        existing_farmer = db.query(User).filter(User.id == farmer_user_id).first()
        if not existing_farmer:
            farmer_user = User(
                id=farmer_user_id,
                mobile_number="8888888888",
                password_hash=AuthService.hash_password("Farmer123!"),
                full_name="Demo Farmer",
                role=UserRole.FARMER,
                account_status=AccountStatus.ACTIVE,
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

            # Add demo products
            products_data = [
                {"name": "Organic Wheat", "category": ProductCategory.GRAINS, "qty": 1000, "price": 25.0, "img": "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=800"},
                {"name": "Red Tomatoes", "category": ProductCategory.VEGETABLES, "qty": 500, "price": 18.5, "img": "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=800"},
            ]

            for p in products_data:
                new_p = Product(
                    farmer_id=farmer_profile.id,
                    name=p["name"],
                    category=p["category"],
                    description=f"High quality {p['name']} directly from the farm.",
                    quantity=p["qty"],
                    unit="kg",
                    expected_price=p["price"],
                    status=ProductStatus.ACTIVE,
                    images=p["img"],
                    state="Maharashtra",
                    district="Pune",
                    village="Demo Village"
                )
                db.add(new_p)
            print(f"Created demo farmer and products")

        # 3. Create Demo Buyer
        buyer_user_id = "c5393115-bd07-4f3e-aee3-89d3d97745b9"
        existing_buyer = db.query(User).filter(User.id == buyer_user_id).first()
        if not existing_buyer:
            buyer_user = User(
                id=buyer_user_id,
                mobile_number="7777777777",
                password_hash=AuthService.hash_password("Buyer123!"),
                full_name="Demo Buyer",
                role=UserRole.BUYER,
                account_status=AccountStatus.ACTIVE,
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
            print(f"Created demo buyer")

        db.commit()
        print("Database seeded successfully!")
    except Exception as e:
        print(f"CRITICAL ERROR during seeding: {type(e).__name__}: {e}")
        import traceback
        traceback.print_exc()
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
