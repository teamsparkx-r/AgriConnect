from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import and_, desc
from models import (
    Farmer, User, Product, Booking, UserRole, ProductStatus, BookingStatus,
    ProductCategory, Language, AccountStatus, Notification
)
from database import get_db
from auth import AuthService
from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime
import uuid

router = APIRouter()

# ==================== PYDANTIC MODELS ====================
class FarmerRegisterRequest(BaseModel):
    mobile_number: str
    password: str
    full_name: str
    email: Optional[str] = None
    state: str
    district: str
    village: str
    farm_address: Optional[str] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None
    preferred_language: str = "english"

class FarmerLoginRequest(BaseModel):
    identity: str
    password: str

class OTPRequest(BaseModel):
    mobile_number: str
    otp_code: str

class ProductCreateRequest(BaseModel):
    name: str
    category: str
    description: Optional[str] = None
    quantity: float
    unit: str
    expected_price: Optional[float] = None
    harvest_date: Optional[str] = None
    images: Optional[str] = None
    state: Optional[str] = None
    district: Optional[str] = None
    village: Optional[str] = None
    farm_address: Optional[str] = None
    status: Optional[str] = "draft"

class ProductUpdateRequest(BaseModel):
    name: Optional[str] = None
    description: Optional[str] = None
    quantity: Optional[float] = None
    unit: Optional[str] = None
    expected_price: Optional[float] = None
    harvest_date: Optional[str] = None
    images: Optional[str] = None
    status: Optional[str] = None
    state: Optional[str] = None
    district: Optional[str] = None
    village: Optional[str] = None
    farm_address: Optional[str] = None

class CounterOfferRequest(BaseModel):
    quantity: float
    price: float
    message: Optional[str] = None

# ==================== ENDPOINTS ====================

@router.post("/register")
def register_farmer(request: FarmerRegisterRequest, db: Session = Depends(get_db)):
    """Register a new farmer (OTP disabled)"""
    try:
        # Register user account with verified=True to bypass OTP
        success, message, user = AuthService.register_user(
            db=db,
            mobile_number=request.mobile_number,
            password=request.password,
            full_name=request.full_name,
            role=UserRole.FARMER,
            email=request.email,
            verified=True
        )
        
        if not success:
            raise HTTPException(status_code=400, detail=message)
        
        # Create farmer profile
        farmer = Farmer(
            user_id=user.id,
            state=request.state,
            district=request.district,
            village=request.village,
            farm_address=request.farm_address,
            latitude=request.latitude,
            longitude=request.longitude,
            preferred_language=request.preferred_language
        )
        db.add(farmer)
        db.commit()
        
        # Generate tokens directly
        access_token, _ = AuthService.create_access_token(user.id, user.role.value)
        refresh_token, _ = AuthService.create_refresh_token(user.id)
        
        return {
            "success": True,
            "message": "Farmer registered successfully",
            "access_token": access_token,
            "refresh_token": refresh_token,
            "user": {
                "id": user.id,
                "role": user.role.value,
                "mobile": user.mobile_number,
                "full_name": user.full_name,
                "account_status": user.account_status.value
            }
        }
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/verify-otp")
def verify_otp(request: OTPRequest, db: Session = Depends(get_db)):
    """Verify OTP for farmer"""
    user = db.query(User).filter(User.mobile_number == request.mobile_number).first()
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    success, message = AuthService.verify_otp(db, user.id, request.otp_code, purpose="verification")
    
    if not success:
        raise HTTPException(status_code=400, detail=message)
    
    # Generate tokens
    access_token, access_expires = AuthService.create_access_token(user.id, user.role.value)
    refresh_token, refresh_expires = AuthService.create_refresh_token(user.id)
    
    return {
        "success": True,
        "message": message,
        "access_token": access_token,
        "refresh_token": refresh_token,
        "user_id": user.id,
        "role": user.role.value
    }

@router.post("/login")
def login_farmer(request: FarmerLoginRequest, db: Session = Depends(get_db)):
    """Login farmer with mobile/email and password"""
    success, message, user = AuthService.authenticate_user(
        db=db,
        identity=request.identity,
        password=request.password
    )
    
    if not success:
        raise HTTPException(status_code=401, detail=message)
    
    # Generate tokens
    access_token, access_expires = AuthService.create_access_token(user.id, user.role.value)
    refresh_token, refresh_expires = AuthService.create_refresh_token(user.id)
    
    farmer = db.query(Farmer).filter(Farmer.user_id == user.id).first()
    
    return {
        "success": True,
        "message": message,
        "access_token": access_token,
        "refresh_token": refresh_token,
        "user_id": user.id,
        "farmer_id": farmer.id if farmer else None,
        "role": user.role.value,
        "account_status": user.account_status.value
    }

@router.get("/dashboard")
def get_farmer_dashboard(user_id: str, db: Session = Depends(get_db)):
    """Get farmer dashboard with statistics"""
    farmer = db.query(Farmer).filter(Farmer.user_id == user_id).first()
    
    if not farmer:
        raise HTTPException(status_code=404, detail="Farmer not found")
    
    # Get statistics
    total_products = db.query(Product).filter(Product.farmer_id == farmer.id).count()
    active_products = db.query(Product).filter(
        and_(Product.farmer_id == farmer.id, Product.status == ProductStatus.ACTIVE)
    ).count()
    harvesting_soon = db.query(Product).filter(
        and_(Product.farmer_id == farmer.id, Product.status == ProductStatus.SOLD) # Assuming SOLD or similar is harvesting soon for now or just filter by date
    ).count()
    total_bookings = db.query(Booking).filter(Booking.farmer_id == farmer.id).count()

    # Unread messages
    unread_count = db.query(Notification).filter(
        and_(Notification.user_id == user_id, Notification.is_read == False)
    ).count()

    # Total Earnings (Mocking for now as bookings * 100)
    total_earnings = farmer.completed_bookings * 1000

    # Recent activity
    recent_products = db.query(Product).filter(Product.farmer_id == farmer.id).order_by(desc(Product.created_at)).limit(4).all()
    recent_bookings = db.query(Booking).filter(Booking.farmer_id == farmer.id).order_by(desc(Booking.created_at)).limit(4).all()
    recent_notifications = db.query(Notification).filter(Notification.user_id == user_id).order_by(desc(Notification.created_at)).limit(4).all()

    return {
        "success": True,
        "stats": {
            "total_products": total_products,
            "active_products": active_products,
            "harvesting_soon": harvesting_soon,
            "total_bookings": total_bookings,
            "total_earnings": total_earnings,
            "unread_messages": unread_count
        },
        "recent_products": [
            {
                "id": p.id,
                "name": p.name,
                "status": p.status.value,
                "quantity": p.quantity,
                "unit": p.unit,
                "expected_price": p.expected_price,
                "images": p.images,
                "state": p.state,
                "district": p.district,
                "created_at": p.created_at
            } for p in recent_products
        ],
        "recent_bookings": [
            {
                "id": b.booking_id,
                "product_name": b.product.name,
                "buyer_name": b.buyer.user.full_name,
                "status": b.status.value,
                "created_at": b.created_at
            } for b in recent_bookings
        ],
        "notifications": [
            {
                "title": n.title,
                "message": n.message,
                "type": n.notification_type,
                "related_id": n.related_id,
                "created_at": n.created_at
            } for n in recent_notifications
        ]
    }

@router.post("/products")
def create_product(request: ProductCreateRequest, user_id: str, db: Session = Depends(get_db)):
    """Create a new product listing"""
    user = db.query(User).filter(User.id == user_id).first()
    if not user or user.account_status != AccountStatus.ACTIVE:
        raise HTTPException(status_code=403, detail="Account not approved for listing crops.")

    farmer = db.query(Farmer).filter(Farmer.user_id == user_id).first()
    
    if not farmer:
        raise HTTPException(status_code=404, detail="Farmer not found")
    
    try:
        harvest_date = None
        if request.harvest_date and request.harvest_date.strip():
            try:
                harvest_date = datetime.fromisoformat(request.harvest_date)
            except:
                harvest_date = None
        
        product = Product(
            farmer_id=farmer.id,
            name=request.name,
            category=request.category,
            description=request.description,
            quantity=request.quantity,
            unit=request.unit,
            expected_price=request.expected_price,
            harvest_date=harvest_date,
            images=request.images,
            status=request.status,
            state=request.state or farmer.state,
            district=request.district or farmer.district,
            village=request.village or farmer.village,
            farm_address=request.farm_address or farmer.farm_address
        )
        
        db.add(product)
        db.commit()
        db.refresh(product)
        
        return {
            "success": True,
            "message": "Product created successfully",
            "product_id": product.id,
            "status": product.status.value
        }
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/products")
def get_farmer_products(user_id: str, db: Session = Depends(get_db)):
    """Get all products created by farmer"""
    farmer = db.query(Farmer).filter(Farmer.user_id == user_id).first()
    
    if not farmer:
        raise HTTPException(status_code=404, detail="Farmer not found")
    
    products = db.query(Product).filter(Product.farmer_id == farmer.id).order_by(desc(Product.created_at)).all()
    
    return {
        "success": True,
        "products": [
            {
                "id": p.id,
                "name": p.name,
                "category": p.category.value,
                "quantity": p.quantity,
                "unit": p.unit,
                "expected_price": p.expected_price,
                "status": p.status.value,
                "harvest_date": p.harvest_date.isoformat() if p.harvest_date else None,
                "images": p.images,
                "created_at": p.created_at.isoformat(),
                "updated_at": p.updated_at.isoformat()
            }
            for p in products
        ],
        "total": len(products)
    }

@router.get("/products/{product_id}")
def get_product_details(product_id: str, user_id: str, db: Session = Depends(get_db)):
    """Get product details"""
    product = db.query(Product).filter(Product.id == product_id).first()
    
    if not product:
        raise HTTPException(status_code=404, detail="Product not found")
    
    farmer = db.query(Farmer).filter(Farmer.user_id == user_id).first()
    if product.farmer_id != farmer.id:
        raise HTTPException(status_code=403, detail="Unauthorized")
    
    return {
        "success": True,
        "product": {
            "id": product.id,
            "name": product.name,
            "category": product.category.value,
            "description": product.description,
            "quantity": product.quantity,
            "unit": product.unit,
            "expected_price": product.expected_price,
            "harvest_date": product.harvest_date.isoformat() if product.harvest_date else None,
            "status": product.status.value,
            "images": product.images,
            "created_at": product.created_at.isoformat(),
            "updated_at": product.updated_at.isoformat()
        }
    }

@router.put("/products/{product_id}")
def update_product(product_id: str, request: ProductUpdateRequest, user_id: str, db: Session = Depends(get_db)):
    """Update product details"""
    product = db.query(Product).filter(Product.id == product_id).first()
    
    if not product:
        raise HTTPException(status_code=404, detail="Product not found")
    
    farmer = db.query(Farmer).filter(Farmer.user_id == user_id).first()
    if product.farmer_id != farmer.id:
        raise HTTPException(status_code=403, detail="Unauthorized")
    
    # Update fields
    if request.name:
        product.name = request.name
    if request.description:
        product.description = request.description
    if request.quantity:
        product.quantity = request.quantity
    if request.unit:
        product.unit = request.unit
    if request.expected_price:
        product.expected_price = request.expected_price
    if request.harvest_date:
        product.harvest_date = datetime.fromisoformat(request.harvest_date)
    if request.images:
        product.images = request.images
    if request.status:
        product.status = request.status
    if request.state:
        product.state = request.state
    if request.district:
        product.district = request.district
    if request.village:
        product.village = request.village
    if request.farm_address:
        product.farm_address = request.farm_address

    product.updated_at = datetime.utcnow()
    db.commit()
    
    return {
        "success": True,
        "message": "Product updated successfully",
        "product_id": product.id
    }

@router.post("/products/{product_id}/publish")
def publish_product(product_id: str, user_id: str, db: Session = Depends(get_db)):
    """Publish product to ACTIVE status"""
    product = db.query(Product).filter(Product.id == product_id).first()
    
    if not product:
        raise HTTPException(status_code=404, detail="Product not found")
    
    farmer = db.query(Farmer).filter(Farmer.user_id == user_id).first()
    if product.farmer_id != farmer.id:
        raise HTTPException(status_code=403, detail="Unauthorized")
    
    product.status = ProductStatus.ACTIVE
    db.commit()
    
    return {
        "success": True,
        "message": "Product published successfully",
        "status": product.status.value
    }

@router.delete("/products/{product_id}")
def delete_product(product_id: str, user_id: str, db: Session = Depends(get_db)):
    """Delete product"""
    product = db.query(Product).filter(Product.id == product_id).first()
    
    if not product:
        raise HTTPException(status_code=404, detail="Product not found")
    
    farmer = db.query(Farmer).filter(Farmer.user_id == user_id).first()
    if product.farmer_id != farmer.id:
        raise HTTPException(status_code=403, detail="Unauthorized")
    
    product.status = ProductStatus.REMOVED
    db.commit()
    
    return {
        "success": True,
        "message": "Product removed successfully"
    }

@router.get("/bookings")
def get_farmer_bookings(user_id: str, status: Optional[str] = None, db: Session = Depends(get_db)):
    """Get all anonymous bookings for farmer's products"""
    farmer = db.query(Farmer).filter(Farmer.user_id == user_id).first()
    
    if not farmer:
        raise HTTPException(status_code=404, detail="Farmer not found")
    
    query = db.query(Booking).filter(Booking.farmer_id == farmer.id)
    
    if status:
        query = query.filter(Booking.status == status)
    
    bookings = query.order_by(desc(Booking.created_at)).all()
    
    return {
        "success": True,
        "bookings": [
            {
                "booking_id": b.booking_id,
                "product_name": b.product.name,
                "buyer_id_alias": f"MERCH-{b.buyer_id[:6].upper()}",
                "buyer_district": b.buyer.district,
                "status": b.status.value,
                "created_at": b.created_at.isoformat()
            }
            for b in bookings
        ],
        "total": len(bookings)
    }

@router.get("/bookings/{booking_id}")
def get_booking_details(booking_id: str, user_id: str, db: Session = Depends(get_db)):
    """Get detailed booking information (Anonymous)"""
    booking = db.query(Booking).filter(Booking.booking_id == booking_id).first()
    
    if not booking:
        raise HTTPException(status_code=404, detail="Booking not found")
    
    farmer = db.query(Farmer).filter(Farmer.user_id == user_id).first()
    if booking.farmer_id != farmer.id:
        raise HTTPException(status_code=403, detail="Unauthorized")
    
    return {
        "success": True,
        "booking": {
            "booking_id": booking.booking_id,
            "product_id": booking.product_id,
            "product_name": booking.product.name,
            "product_quantity": booking.product.quantity,
            "product_unit": booking.product.unit,
            "expected_price": booking.product.expected_price,
            "requested_quantity": booking.requested_quantity,
            "negotiated_price": booking.negotiated_price,
            "buyer_id_alias": f"MERCH-{booking.buyer_id[:6].upper()}",
            "buyer_type": booking.buyer.buyer_type.value,
            "buyer_district": booking.buyer.district,
            "status": booking.status.value,
            "created_at": booking.created_at.isoformat()
        }
    }

@router.post("/bookings/{booking_id}/accept")
def farmer_accept_offer(booking_id: str, user_id: str, db: Session = Depends(get_db)):
    """Farmer accepts the current offer from Merchant"""
    booking = db.query(Booking).filter(Booking.booking_id == booking_id).first()
    if not booking:
        raise HTTPException(status_code=404, detail="Booking not found")

    farmer = db.query(Farmer).filter(Farmer.user_id == user_id).first()
    if booking.farmer_id != farmer.id:
        raise HTTPException(status_code=403, detail="Unauthorized")

    booking.status = BookingStatus.CONFIRMED
    booking.updated_at = datetime.utcnow()

    # Create history entry
    from models import NegotiationHistory
    negotiation = NegotiationHistory(
        booking_id=booking.id,
        sender_id=user_id,
        receiver_id=booking.buyer.user_id,
        quantity=booking.requested_quantity,
        price=booking.negotiated_price,
        status=BookingStatus.ACCEPTED
    )
    db.add(negotiation)

    # Notify merchant
    new_notif = Notification(
        user_id=booking.buyer.user_id,
        title="Deal Confirmed!",
        message=f"Farmer has accepted your offer for {booking.product.name}. Order is now CONFIRMED.",
        notification_type="order_confirmed",
        related_id=booking.booking_id
    )
    db.add(new_notif)
    db.commit()

    return {"success": True, "message": "Offer accepted and order confirmed."}

@router.post("/bookings/{booking_id}/reject")
def farmer_reject_offer(booking_id: str, user_id: str, db: Session = Depends(get_db)):
    """Farmer rejects the current offer from Merchant"""
    booking = db.query(Booking).filter(Booking.booking_id == booking_id).first()
    if not booking:
        raise HTTPException(status_code=404, detail="Booking not found")

    farmer = db.query(Farmer).filter(Farmer.user_id == user_id).first()
    if booking.farmer_id != farmer.id:
        raise HTTPException(status_code=403, detail="Unauthorized")

    booking.status = BookingStatus.REJECTED
    booking.updated_at = datetime.utcnow()

    # Record history
    from models import NegotiationHistory
    negotiation = NegotiationHistory(
        booking_id=booking.id,
        sender_id=user_id,
        receiver_id=booking.buyer.user_id,
        quantity=booking.requested_quantity,
        price=booking.negotiated_price,
        status=BookingStatus.REJECTED
    )
    db.add(negotiation)

    # Notify merchant
    new_notif = Notification(
        user_id=booking.buyer.user_id,
        title="Inquiry Rejected",
        message=f"Farmer has rejected the enquiry for {booking.product.name}.",
        notification_type="negotiation_rejected",
        related_id=booking.booking_id
    )
    db.add(new_notif)
    db.commit()

    return {"success": True, "message": "Inquiry rejected."}

@router.post("/bookings/{booking_id}/counter")
def farmer_counter_offer(booking_id: str, user_id: str, request: CounterOfferRequest, db: Session = Depends(get_db)):
    """Farmer sends a counter offer to Merchant"""
    booking = db.query(Booking).filter(Booking.booking_id == booking_id).first()
    if not booking:
        raise HTTPException(status_code=404, detail="Booking not found")

    farmer = db.query(Farmer).filter(Farmer.user_id == user_id).first()
    if booking.farmer_id != farmer.id:
        raise HTTPException(status_code=403, detail="Unauthorized")

    booking.status = BookingStatus.COUNTER_OFFER
    booking.requested_quantity = request.quantity
    booking.negotiated_price = request.price
    booking.updated_at = datetime.utcnow()

    # Record history
    from models import NegotiationHistory
    negotiation = NegotiationHistory(
        booking_id=booking.id,
        sender_id=user_id,
        receiver_id=booking.buyer.user_id,
        quantity=request.quantity,
        price=request.price,
        message=request.message,
        status=BookingStatus.COUNTER_OFFER
    )
    db.add(negotiation)

    # Notify merchant
    new_notif = Notification(
        user_id=booking.buyer.user_id,
        title="New Counter Offer",
        message=f"Farmer has responded to your {booking.product.name} enquiry with a counter offer: {request.quantity} {booking.product.unit} @ ₹{request.price}/{booking.product.unit}",
        notification_type="counter_offer",
        related_id=booking.booking_id
    )
    db.add(new_notif)
    db.commit()

    return {"success": True, "message": "Counter offer sent successfully."}

@router.get("/profile/{user_id}")
def get_farmer_profile(user_id: str, db: Session = Depends(get_db)):
    """Get farmer profile"""
    user = db.query(User).filter(User.id == user_id).first()
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    farmer = db.query(Farmer).filter(Farmer.user_id == user_id).first()
    
    return {
        "success": True,
        "profile": {
            "user_id": user.id,
            "farmer_id": farmer.id if farmer else None,
            "full_name": user.full_name,
            "mobile_number": user.mobile_number,
            "email": user.email,
            "state": farmer.state if farmer else None,
            "district": farmer.district if farmer else None,
            "village": farmer.village if farmer else None,
            "farm_address": farmer.farm_address if farmer else None,
            "latitude": farmer.latitude if farmer else None,
            "longitude": farmer.longitude if farmer else None,
            "share_farm_address": farmer.share_farm_address if farmer else None,
            "share_coordinates": farmer.share_coordinates if farmer else None,
            "completed_bookings": farmer.completed_bookings if farmer else 0,
            "rating": farmer.rating if farmer else 0,
            "account_status": user.account_status.value,
            "created_at": user.created_at.isoformat()
        }
    }

@router.put("/profile/{user_id}")
def update_farmer_profile(user_id: str, updates: dict, db: Session = Depends(get_db)):
    """Update farmer profile"""
    user = db.query(User).filter(User.id == user_id).first()
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    farmer = db.query(Farmer).filter(Farmer.user_id == user_id).first()
    
    # Update user fields
    if "full_name" in updates:
        user.full_name = updates["full_name"]
    if "email" in updates:
        user.email = updates["email"]
    
    # Update farmer fields
    if farmer:
        if "state" in updates:
            farmer.state = updates["state"]
        if "district" in updates:
            farmer.district = updates["district"]
        if "village" in updates:
            farmer.village = updates["village"]
        if "farm_address" in updates:
            farmer.farm_address = updates["farm_address"]
        if "latitude" in updates:
            farmer.latitude = updates["latitude"]
        if "longitude" in updates:
            farmer.longitude = updates["longitude"]
        if "share_farm_address" in updates:
            farmer.share_farm_address = updates["share_farm_address"]
        if "share_coordinates" in updates:
            farmer.share_coordinates = updates["share_coordinates"]
    
    db.commit()
    
    return {
        "success": True,
        "message": "Profile updated successfully"
    }

@router.get("/notifications/{user_id}")
def get_notifications(user_id: str, db: Session = Depends(get_db)):
    """Get farmer notifications"""
    notifications = db.query(Notification).filter(
        Notification.user_id == user_id
    ).order_by(desc(Notification.created_at)).all()
    
    return {
        "success": True,
        "notifications": [
            {
                "id": n.id,
                "title": n.title,
                "message": n.message,
                "notification_type": n.notification_type,
                "related_id": n.related_id,
                "is_read": n.is_read,
                "created_at": n.created_at.isoformat()
            }
            for n in notifications
        ],
        "total": len(notifications),
        "unread_count": len([n for n in notifications if not n.is_read])
    }
