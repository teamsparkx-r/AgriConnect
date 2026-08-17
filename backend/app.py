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
                role="admin",
                account_status="active"
            )
            db.add(admin_user)
            db.flush()
            print(f"Created default admin user: {ADMIN_MOBILE}")

        # 2. Create Demo Farmer
        farmer_user_id = "5737b51e-8640-48c2-bdaa-63fbba1b70a7"
        existing_farmer = db.query(User).filter(User.id == farmer_user_id).first()
        if not existing_farmer:
            print("Seeding demo farmer user...")
            farmer_user = User(
                id=farmer_user_id,
                mobile_number="8888888888",
                password_hash=AuthService.hash_password("Farmer123!"),
                full_name="Demo Farmer",
                role="farmer",
                account_status="active",
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
        else:
            # Force status to ACTIVE for demo farmer if already exists
            existing_farmer.account_status = "active"
            farmer_profile = db.query(Farmer).filter(Farmer.user_id == farmer_user_id).first()
            if not farmer_profile:
                print("Farmer profile missing for existing demo user. Creating it...")
                farmer_profile = Farmer(
                    user_id=existing_farmer.id,
                    state="Maharashtra",
                    district="Pune",
                    village="Demo Village"
                )
                db.add(farmer_profile)
            db.flush()

        # Update/Reset demo products
        if farmer_profile:
            # Clear old products for this farmer to avoid duplicates
            count_deleted = db.query(Product).filter(Product.farmer_id == farmer_profile.id).delete()
            print(f"Deleted {count_deleted} old products for demo farmer")
            db.flush()

            crops_pool = [
                ("Organic Wheat", ProductCategory.GRAINS, 25.0, "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=800"),
                ("Red Tomatoes", ProductCategory.VEGETABLES, 18.5, "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=800"),
                ("Basmati Rice", ProductCategory.GRAINS, 65.0, "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=800"),
                ("Yellow Corn", ProductCategory.GRAINS, 22.0, "https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=800"),
                ("Fresh Onions", ProductCategory.VEGETABLES, 15.0, "https://images.unsplash.com/photo-1508747703725-719777637510?w=800"),
                ("Potatoes", ProductCategory.VEGETABLES, 12.0, "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=800"),
                ("Green Chillies", ProductCategory.VEGETABLES, 30.0, "https://images.unsplash.com/photo-1588252303782-cb80119abd6d?w=800"),
                ("Ginger", ProductCategory.SPICES, 120.0, "https://images.unsplash.com/photo-1599940824399-b87987ceb72a?w=800"),
                ("Turmeric", ProductCategory.SPICES, 150.0, "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=800"),
                ("Garlic", ProductCategory.VEGETABLES, 80.0, "https://images.unsplash.com/photo-1540148426945-6cf22a6b2383?w=800"),
                ("Red Chilli", ProductCategory.SPICES, 180.0, "https://images.unsplash.com/photo-1599488615731-7e5c2823ff28?w=800"),
                ("Soybeans", ProductCategory.OIL_SEEDS, 45.0, "https://images.unsplash.com/photo-1589923188900-85dae523342b?w=800"),
                ("Mustard Seeds", ProductCategory.OIL_SEEDS, 55.0, "https://images.unsplash.com/photo-1599409673963-8f304a55903b?w=800"),
                ("Cotton", ProductCategory.COMMERCIAL_CROPS, 75.0, "https://images.unsplash.com/photo-1594903310503-492723326f21?w=800"),
                ("Sugarcane", ProductCategory.COMMERCIAL_CROPS, 3.5, "https://images.unsplash.com/photo-1593113617719-79883584852b?w=800"),
                ("Alphonso Mango", ProductCategory.FRUITS, 250.0, "https://images.unsplash.com/photo-1553134839-497746777b73?w=800"),
                ("Green Apples", ProductCategory.FRUITS, 140.0, "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=800"),
                ("Banana (Robusta)", ProductCategory.FRUITS, 20.0, "https://images.unsplash.com/photo-1571771894821-ad990241fab4?w=800"),
                ("Watermelon", ProductCategory.FRUITS, 15.0, "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=800"),
                ("Grapes (Thompson)", ProductCategory.FRUITS, 90.0, "https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=800"),
                ("Cabbage", ProductCategory.VEGETABLES, 10.0, "https://images.unsplash.com/photo-1591461141441-38e9329007f3?w=800"),
                ("Cauliflower", ProductCategory.VEGETABLES, 25.0, "https://images.unsplash.com/photo-1568584711075-3d021a7c3ec3?w=800"),
                ("Carrots", ProductCategory.VEGETABLES, 40.0, "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=800"),
                ("Spinach", ProductCategory.VEGETABLES, 20.0, "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=800"),
                ("Moong Dal", ProductCategory.PULSES, 110.0, "https://images.unsplash.com/photo-1585994192703-274638314741?w=800"),
                ("Arhar Dal", ProductCategory.PULSES, 140.0, "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=800"),
                ("Black Gram", ProductCategory.PULSES, 125.0, "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=800"),
                ("Chickpeas", ProductCategory.PULSES, 95.0, "https://images.unsplash.com/photo-1585994192703-274638314741?w=800"),
                ("Peanuts", ProductCategory.OIL_SEEDS, 85.0, "https://images.unsplash.com/photo-1527661591475-527312dd65f5?w=800"),
                ("Sunflower Seeds", ProductCategory.OIL_SEEDS, 70.0, "https://images.unsplash.com/photo-1597426102235-93722956c802?w=800"),
                ("Cardamom", ProductCategory.SPICES, 2500.0, "https://images.unsplash.com/photo-1599409673963-8f304a55903b?w=800"),
                ("Black Pepper", ProductCategory.SPICES, 450.0, "https://images.unsplash.com/photo-1599409673963-8f304a55903b?w=800"),
                ("Cloves", ProductCategory.SPICES, 900.0, "https://images.unsplash.com/photo-1599409673963-8f304a55903b?w=800"),
            ]

            import random

            # Generate ~60 products by repeating and varying the pool
            for i in range(60):
                crop_template = crops_pool[i % len(crops_pool)]
                name, cat, base_price, img = crop_template

                # Add variations
                variation = random.uniform(0.9, 1.1) # +/- 10% price variation
                quantity = random.randint(100, 5000)
                price = round(base_price * variation, 2)

                new_p = Product(
                    farmer_id=farmer_profile.id,
                    name=f"{name} (Lot #{i+1})",
                    category=cat.value if hasattr(cat, "value") else cat,
                    description=f"Premium grade {name} directly from Pune farms. Harvested recently with high quality standards.",
                    quantity=quantity,
                    unit="kg" if cat != ProductCategory.FRUITS else "units",
                    expected_price=price,
                    status="active",
                    images=img,
                    state="Maharashtra",
                    district="Pune",
                    village="Demo Village"
                )
                db.add(new_p)

            print(f"Reset and Seeded 60 products for demo farmer")
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
                role="buyer",
                account_status="active",
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
        else:
            # Force status to active for demo buyer
            existing_buyer.account_status = "active"

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
    
    from sqlalchemy import func
    total_users = db.query(User).count()
    total_farmers = db.query(User).filter(func.lower(User.role) == "farmer").count()
    total_buyers = db.query(User).filter(func.lower(User.role) == "buyer").count()
    total_products = db.query(Product).count()
    active_products = db.query(Product).filter(func.lower(Product.status) == "active").count()
    total_bookings = db.query(Booking).count()
    completed_bookings = db.query(Booking).filter(func.lower(Booking.status) == "completed").count()
    
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
