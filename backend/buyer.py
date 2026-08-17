from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import and_, or_, desc
from models import (
    Buyer, User, Product, Booking, UserRole, ProductStatus, BookingStatus,
    Report, ReportReason, Notification, SavedProduct, Farmer, AccountStatus
)
from database import get_db
from auth import AuthService
from pydantic import BaseModel, EmailStr
from typing import Optional, List
from datetime import datetime
import uuid
import string

router = APIRouter()

# ==================== PYDANTIC MODELS ====================
class BuyerRegisterRequest(BaseModel):
    mobile_number: str
    password: str
    full_name: str
    email: Optional[str] = None
    buyer_type: Optional[str] = "individual"
    location: Optional[str] = None
    state: Optional[str] = None
    district: Optional[str] = None
    preferred_language: str = "english"

class BuyerLoginRequest(BaseModel):
    identity: str
    password: str

class OTPRequest(BaseModel):
    mobile_number: str
    otp_code: str

class ProductResponse(BaseModel):
    id: str
    name: str
    category: str
    quantity: float
    unit: str
    expected_price: Optional[float]
    harvest_date: Optional[str]
    description: Optional[str]
    images: Optional[str]
    district: str
    state: str

class BookingRequest(BaseModel):
    product_id: str
    quantity: float
    price: float
    message: Optional[str] = None
    terms_accepted: bool

class BookingResponse(BaseModel):
    booking_id: str
    product_id: str
    farmer_id: str
    status: str
    farmer_name: Optional[str]
    farmer_mobile: Optional[str]
    farmer_village: Optional[str]
    farm_address: Optional[str]
    contact_unlocked_at: Optional[str]
    created_at: str

class ReportRequest(BaseModel):
    booking_id: str
    reason: str
    description: Optional[str]

class CounterOfferRequest(BaseModel):
    quantity: float
    price: float
    message: Optional[str] = None

# ==================== ENDPOINTS ====================

@router.post("/register")
def register_buyer(request: BuyerRegisterRequest, db: Session = Depends(get_db)):
    """Register a new buyer (OTP disabled)"""
    try:
        # Register user account with verified=True to bypass OTP
        success, message, user = AuthService.register_user(
            db=db,
            mobile_number=request.mobile_number,
            password=request.password,
            full_name=request.full_name,
            role=UserRole.BUYER,
            email=request.email,
            verified=True,
            commit=False # Don't commit yet
        )
        
        if not success:
            db.rollback()
            raise HTTPException(status_code=400, detail=message)
        
        # Fill location fields for buyer profile
        state = request.state or request.location or "Unknown"
        district = request.district or request.location or "Unknown"

        # Ensure buyer_type is a valid enum value
        try:
            from models import BuyerType
            # Normalize and validate buyer_type
            b_type = request.buyer_type.lower() if request.buyer_type else "individual"
            # Map "merchant" (common from frontend) to "trader" if not explicitly in enum
            if b_type == "merchant" and "merchant" not in [e.value for e in BuyerType]:
                b_type = "trader"

            # Final fallback to individual if still invalid
            if b_type not in [e.value for e in BuyerType]:
                b_type = "individual"
        except Exception:
            b_type = "individual"

        # Ensure preferred_language is a valid enum value
        try:
            from models import Language
            lang = request.preferred_language.lower() if request.preferred_language else "english"
            if lang not in [e.value for e in Language]:
                lang = "english"
        except Exception:
            lang = "english"

        # Create buyer profile
        buyer = Buyer(
            user_id=user.id,
            buyer_type=b_type,
            state=state,
            district=district,
            preferred_language=lang
        )
        db.add(buyer)
        db.commit() # Now commit User and Buyer together
        db.refresh(user)
        
        # Generate tokens directly
        access_token, _ = AuthService.create_access_token(user.id, user.role)
        refresh_token, _ = AuthService.create_refresh_token(user.id)
        
        return {
            "success": True,
            "message": "Buyer registered successfully",
            "access_token": access_token,
            "refresh_token": refresh_token,
            "user": {
                "id": user.id,
                "role": user.role,
                "mobile": user.mobile_number,
                "full_name": user.full_name,
                "account_status": user.account_status
            }
        }
    except HTTPException as e:
        db.rollback()
        raise e
    except Exception as e:
        db.rollback()
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"Registration failed: {str(e)}")

@router.post("/verify-otp")
def verify_otp(request: OTPRequest, db: Session = Depends(get_db)):
    """Verify OTP for buyer"""
    user = db.query(User).filter(User.mobile_number == request.mobile_number).first()
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    success, message = AuthService.verify_otp(db, user.id, request.otp_code, purpose="verification")
    
    if not success:
        raise HTTPException(status_code=400, detail=message)
    
    # Generate tokens
    access_token, access_expires = AuthService.create_access_token(user.id, user.role)
    refresh_token, refresh_expires = AuthService.create_refresh_token(user.id)
    
    return {
        "success": True,
        "message": message,
        "access_token": access_token,
        "refresh_token": refresh_token,
        "user": {
            "id": user.id,
            "role": user.role,
            "mobile": user.mobile_number,
            "full_name": user.full_name,
            "account_status": user.account_status
        },
        "user_id": user.id,
        "role": user.role
    }

@router.post("/login")
def login_buyer(request: BuyerLoginRequest, db: Session = Depends(get_db)):
    """Login buyer with mobile/email and password"""
    success, message, user = AuthService.authenticate_user(
        db=db,
        identity=request.identity,
        password=request.password
    )
    
    if not success:
        raise HTTPException(status_code=401, detail=message)
    
    # Generate tokens
    access_token, access_expires = AuthService.create_access_token(user.id, user.role)
    refresh_token, refresh_expires = AuthService.create_refresh_token(user.id)
    
    buyer = db.query(Buyer).filter(Buyer.user_id == user.id).first()
    
    return {
        "success": True,
        "message": message,
        "access_token": access_token,
        "refresh_token": refresh_token,
        "user": {
            "id": user.id,
            "role": user.role,
            "mobile": user.mobile_number,
            "full_name": user.full_name,
            "account_status": user.account_status
        },
        "user_id": user.id,
        "buyer_id": buyer.id if buyer else None,
        "role": user.role
    }

@router.get("/dashboard/{user_id}")
def get_merchant_dashboard(user_id: str, db: Session = Depends(get_db)):
    """Get merchant dashboard summary and activities"""
    user = db.query(User).filter(User.id == user_id).first()
    buyer = db.query(Buyer).filter(Buyer.user_id == user_id).first()
    if not buyer or not user:
        raise HTTPException(status_code=404, detail="Buyer not found")

    total_bookings = db.query(Booking).filter(Booking.buyer_id == buyer.id).count()
    active_bookings = db.query(Booking).filter(
        and_(Booking.buyer_id == buyer.id, Booking.status != BookingStatus.COMPLETED, Booking.status != BookingStatus.CANCELLED)
    ).count()
    completed_bookings = db.query(Booking).filter(
        and_(Booking.buyer_id == buyer.id, Booking.status == BookingStatus.COMPLETED)
    ).count()

    # Amount spent is total bookings * 100 (connection fee)
    amount_spent = total_bookings * 100

    # Recent bookings
    recent_bookings = db.query(Booking).filter(Booking.buyer_id == buyer.id).order_by(desc(Booking.created_at)).limit(4).all()

    # Recent messages (simulated for now)
    messages = [
        {"initial": "R", "name": "Ramesh Kumar", "msg": "Thank you for showing interest. The onions are freshly...", "time": "10m ago", "unread": 1},
        {"initial": "S", "name": "Suresh Yadav", "msg": "The tomatoes will be available for delivery tomorrow.", "time": "35m ago", "unread": 1},
        {"initial": "M", "name": "Mahesh Patil", "msg": "Quality potatoes available. We can discuss logistics.", "time": "1h ago", "unread": 0}
    ]

    return {
        "success": True,
        "account_status": user.account_status,
        "summary": {
            "total_bookings": total_bookings,
            "active_bookings": active_bookings,
            "completed_bookings": completed_bookings,
            "amount_spent": amount_spent
        },
        "recent_bookings": [
            {
                "id": f"AGR-{b.booking_id}",
                "product": b.product.name if b.product else "Unknown Product",
                "farmer": b.farmer.user.full_name if b.farmer and b.farmer.user else "Verified Farmer",
                "status": b.status if isinstance(b.status, str) else str(b.status),
                "color": "bg-green-100 text-green-700" if b.status == BookingStatus.CONFIRMED else "bg-blue-100 text-blue-700"
            } for b in recent_bookings
        ],
        "messages": messages
    }

@router.get("/home")
def get_buyer_home(
    skip: int = 0,
    limit: int = 10,
    db: Session = Depends(get_db)
):
    """Get buyer home - featured and recent products"""
    try:
        # Get featured ACTIVE products (recently added)
        # Robust filtering to handle potential case variations in DB
        products = db.query(Product).filter(
            or_(Product.status == ProductStatus.ACTIVE, Product.status == "ACTIVE", Product.status == "active")
        ).order_by(desc(Product.created_at)).offset(skip).limit(limit).all()
        
        return {
            "success": True,
            "products": [
                {
                    "id": p.id,
                    "name": p.name,
                    "category": p.category,
                    "quantity": p.quantity,
                    "unit": p.unit,
                    "expected_price": p.expected_price,
                    "harvest_date": p.harvest_date.isoformat() if p.harvest_date else None,
                    "district": p.district or (p.farmer.district if p.farmer else "Unknown"),
                    "state": p.state or (p.farmer.state if p.farmer else "Unknown"),
                    "images": p.images,
                    "status": p.status,
                    "farmer_id": p.farmer_id,
                    "farmer_id_alias": f"FARMER-{p.farmer_id[:8].upper()}",
                    "created_at": p.created_at.isoformat()
                }
                for p in products
            ],
            "total": len(products)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/search")
def search_products(
    query: Optional[str] = Query(None),
    category: Optional[str] = None,
    state: Optional[str] = None,
    district: Optional[str] = None,
    min_price: Optional[float] = None,
    max_price: Optional[float] = None,
    skip: int = 0,
    limit: int = 20,
    db: Session = Depends(get_db)
):
    """Search and filter products"""
    try:
        filters = [or_(Product.status == ProductStatus.ACTIVE, Product.status == "ACTIVE", Product.status == "active")]
        
        # Search in product name if query provided
        if query:
            filters.append(Product.name.ilike(f"%{query}%"))
        
        if category:
            filters.append(or_(Product.category == category, Product.category == category.upper(), Product.category == category.lower()))
        
        if state:
            filters.append(Product.farmer.has(state=state))
        
        if district:
            filters.append(Product.farmer.has(district=district))
        
        if min_price is not None:
            filters.append(Product.expected_price >= min_price)
        
        if max_price is not None:
            filters.append(Product.expected_price <= max_price)
        
        products = db.query(Product).filter(and_(*filters)).offset(skip).limit(limit).all()
        
        return {
            "success": True,
            "products": [
                {
                    "id": p.id,
                    "name": p.name,
                    "category": p.category,
                    "quantity": p.quantity,
                    "unit": p.unit,
                    "expected_price": p.expected_price,
                    "harvest_date": p.harvest_date.isoformat() if p.harvest_date else None,
                    "district": p.district or (p.farmer.district if p.farmer else "Unknown"),
                    "state": p.state or (p.farmer.state if p.farmer else "Unknown"),
                    "images": p.images,
                    "status": p.status,
                    "farmer_id": p.farmer_id,
                    "farmer_id_alias": f"FARMER-{p.farmer_id[:8].upper()}"
                }
                for p in products
            ],
            "total": len(products)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@router.get("/products/{product_id}")
def get_product_details(product_id: str, db: Session = Depends(get_db)):
    """Get anonymous product details"""
    product = db.query(Product).filter(Product.id == product_id).first()
    
    if not product:
        raise HTTPException(status_code=404, detail="Product listing no longer available.")
    
    if product.status != ProductStatus.ACTIVE and product.status != ProductStatus.SOLD:
        raise HTTPException(status_code=400, detail="Product is not available")
    
    return {
        "success": True,
        "product": {
            "id": product.id,
            "name": product.name,
            "category": product.category,
            "description": product.description,
            "quantity": product.quantity,
            "unit": product.unit,
            "expected_price": product.expected_price,
            "harvest_date": product.harvest_date.isoformat() if product.harvest_date else None,
            "district": product.district or (product.farmer.district if product.farmer else "Unknown"),
            "state": product.state or (product.farmer.state if product.farmer else "Unknown"),
            "images": product.images,
            "status": product.status,
            "farmer_id_alias": f"FARMER-{product.farmer_id[:8].upper()}",
            "is_anonymous": True
        }
    }

@router.post("/booking")
def create_booking(request: BookingRequest, user_id: str, db: Session = Depends(get_db)):
    """Stage 4: Book crop (Anonymously mediated by AgriConnect)"""
    try:
        user = db.query(User).filter(User.id == user_id).first()
        if not user or user.account_status != "active":
            raise HTTPException(status_code=403, detail="Account not approved for purchase enquiries.")

        product = db.query(Product).filter(Product.id == request.product_id).first()

        if not product:
            raise HTTPException(status_code=404, detail="Product listing no longer available.")

        if product.status != ProductStatus.ACTIVE:
            raise HTTPException(status_code=400, detail="This crop has already been reserved or is unavailable.")
        
        # Get buyer
        buyer = db.query(Buyer).filter(Buyer.user_id == user_id).first()
        if not buyer:
            raise HTTPException(status_code=404, detail="Buyer profile not found")

        # Check if already booked by this merchant
        existing = db.query(Booking).filter(
            and_(Booking.buyer_id == buyer.id, Booking.product_id == product.id)
        ).first()
        if existing:
            return {"success": True, "message": "You have already booked this crop.", "booking_id": existing.booking_id}
        
        # Create booking record
        booking_id = str(uuid.uuid4())[:8].upper()
        booking = Booking(
            booking_id=booking_id,
            buyer_id=buyer.id,
            farmer_id=product.farmer_id,
            product_id=product.id,
            status=BookingStatus.ENQUIRY_SENT,
            requested_quantity=request.quantity,
            negotiated_price=request.price,
            terms_accepted=True
        )

        db.add(booking)
        db.flush() # Get booking.id

        # Create initial negotiation entry
        from models import NegotiationHistory
        negotiation = NegotiationHistory(
            booking_id=booking.id,
            sender_id=user_id,
            receiver_id=product.farmer.user_id,
            quantity=request.quantity,
            price=request.price,
            message=request.message,
            status=BookingStatus.ENQUIRY_SENT
        )
        db.add(negotiation)

        # Create notification for farmer (Anonymous)
        notification = Notification(
            user_id=product.farmer.user_id,
            title="New Purchase Enquiry",
            message=f"A merchant is interested in your {product.name}. Requested: {request.quantity} {product.unit} @ ₹{request.price}/{product.unit}",
            notification_type="new_enquiry",
            related_id=booking.booking_id
        )
        db.add(notification)
        db.commit()
        db.refresh(booking)

        print(f"ENQUIRY CREATED: Booking {booking.booking_id} for Farmer {product.farmer.user_id}")

        return {
            "success": True,
            "message": "Crop successfully booked! AgriConnect will now mediate the exchange.",
            "booking": {
                "booking_id": booking.booking_id,
                "product_id": product.id,
                "status": booking.status,
                "created_at": booking.created_at.isoformat()
            }
        }
    except HTTPException as e:
        db.rollback()
        raise e
    except Exception as e:
        db.rollback()
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"Registration failed: {str(e)}")
    except HTTPException as e:
        db.rollback()
        raise e
    except Exception as e:
        db.rollback()
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"Registration failed: {str(e)}")

@router.get("/bookings")
def get_buyer_bookings(user_id: str, db: Session = Depends(get_db)):
    """Stage 5-7: Get buyer's booking history"""
    buyer = db.query(Buyer).filter(Buyer.user_id == user_id).first()
    
    if not buyer:
        raise HTTPException(status_code=404, detail="Buyer not found")
    
    bookings = db.query(Booking).filter(Booking.buyer_id == buyer.id).order_by(desc(Booking.created_at)).all()
    
    return {
        "success": True,
        "bookings": [
            {
                "booking_id": b.booking_id,
                "product_id": b.product_id,
                "product_name": b.product.name,
                "farmer_name": b.farmer.user.full_name,
                "farmer_mobile": b.farmer.user.mobile_number,
                "status": b.status,
                "created_at": b.created_at.isoformat(),
                "contact_unlocked_at": b.contact_unlocked_at.isoformat() if b.contact_unlocked_at else None,
                "completed_at": b.completed_at.isoformat() if b.completed_at else None
            }
            for b in bookings
        ],
        "total": len(bookings)
    }

@router.get("/bookings/{booking_id}")
def get_booking_details(booking_id: str, user_id: str, db: Session = Depends(get_db)):
    """Get detailed booking info"""
    # Try searching by human-readable ID first, then fallback to UUID
    booking = db.query(Booking).filter(Booking.booking_id == booking_id).first()
    if not booking:
        booking = db.query(Booking).filter(Booking.id == booking_id).first()

    if not booking:
        raise HTTPException(status_code=404, detail="Booking not found")
    
    buyer = db.query(Buyer).filter(Buyer.user_id == user_id).first()
    if not booking or booking.buyer_id != buyer.id:
        raise HTTPException(status_code=403, detail="Unauthorized")
    
    return {
        "success": True,
        "booking": {
            "booking_id": booking.booking_id,
            "product_id": booking.product_id,
            "product_name": booking.product.name,
            "product_image": booking.product.images,
            "product_description": booking.product.description,
            "quantity": booking.product.quantity,
            "unit": booking.product.unit,
            "expected_price": booking.product.expected_price,
            "requested_quantity": booking.requested_quantity,
            "negotiated_price": booking.negotiated_price,
            "farmer_id": booking.farmer.id,
            "farmer_name": booking.farmer.user.full_name,
            "farmer_mobile": booking.farmer.user.mobile_number,
            "farmer_village": booking.farmer.village,
            "farm_address": booking.farmer.farm_address,
            "latitude": booking.farmer.latitude,
            "longitude": booking.farmer.longitude,
            "status": booking.status,
            "created_at": booking.created_at.isoformat(),
            "contact_unlocked_at": booking.contact_unlocked_at.isoformat() if booking.contact_unlocked_at else None,
            "completed_at": booking.completed_at.isoformat() if booking.completed_at else None
        }
    }

@router.post("/bookings/{booking_id}/accept")
def merchant_accept_offer(booking_id: str, user_id: str, db: Session = Depends(get_db)):
    """Merchant accepts the current offer from Farmer"""
    booking = db.query(Booking).filter(Booking.booking_id == booking_id).first()
    if not booking:
        raise HTTPException(status_code=404, detail="Booking not found")

    buyer = db.query(Buyer).filter(Buyer.user_id == user_id).first()
    if booking.buyer_id != buyer.id:
        raise HTTPException(status_code=403, detail="Unauthorized")

    booking.status = BookingStatus.CONFIRMED
    booking.updated_at = datetime.utcnow()

    # Create history entry
    from models import NegotiationHistory
    negotiation = NegotiationHistory(
        booking_id=booking.id,
        sender_id=user_id,
        receiver_id=booking.farmer.user_id,
        quantity=booking.requested_quantity,
        price=booking.negotiated_price,
        status=BookingStatus.ACCEPTED
    )
    db.add(negotiation)

    # Notify farmer
    new_notif = Notification(
        user_id=booking.farmer.user_id,
        title="Deal Confirmed!",
        message=f"Merchant has accepted your counter offer for {booking.product.name}. Order is now CONFIRMED.",
        notification_type="order_confirmed",
        related_id=booking.booking_id
    )
    db.add(new_notif)
    db.commit()

    return {"success": True, "message": "Offer accepted and deal confirmed."}

@router.post("/bookings/{booking_id}/reject")
def merchant_reject_offer(booking_id: str, user_id: str, db: Session = Depends(get_db)):
    """Merchant rejects the current offer from Farmer"""
    booking = db.query(Booking).filter(Booking.booking_id == booking_id).first()
    if not booking:
        raise HTTPException(status_code=404, detail="Booking not found")

    buyer = db.query(Buyer).filter(Buyer.user_id == user_id).first()
    if booking.buyer_id != buyer.id:
        raise HTTPException(status_code=403, detail="Unauthorized")

    booking.status = BookingStatus.REJECTED
    booking.updated_at = datetime.utcnow()

    # Record history
    from models import NegotiationHistory
    negotiation = NegotiationHistory(
        booking_id=booking.id,
        sender_id=user_id,
        receiver_id=booking.farmer.user_id,
        quantity=booking.requested_quantity,
        price=booking.negotiated_price,
        status=BookingStatus.REJECTED
    )
    db.add(negotiation)

    # Notify farmer
    new_notif = Notification(
        user_id=booking.farmer.user_id,
        title="Negotiation Ended",
        message=f"Merchant has rejected the terms for {booking.product.name}.",
        notification_type="negotiation_rejected",
        related_id=booking.booking_id
    )
    db.add(new_notif)
    db.commit()

    return {"success": True, "message": "Negotiation rejected."}

@router.post("/bookings/{booking_id}/counter")
def merchant_counter_offer(booking_id: str, user_id: str, request: CounterOfferRequest, db: Session = Depends(get_db)):
    """Merchant sends a counter offer back to Farmer"""
    booking = db.query(Booking).filter(Booking.booking_id == booking_id).first()
    if not booking:
        raise HTTPException(status_code=404, detail="Booking not found")

    buyer = db.query(Buyer).filter(Buyer.user_id == user_id).first()
    if booking.buyer_id != buyer.id:
        raise HTTPException(status_code=403, detail="Unauthorized")

    booking.status = BookingStatus.MERCHANT_RESPONDED
    booking.requested_quantity = request.quantity
    booking.negotiated_price = request.price
    booking.updated_at = datetime.utcnow()

    # Record history
    from models import NegotiationHistory
    negotiation = NegotiationHistory(
        booking_id=booking.id,
        sender_id=user_id,
        receiver_id=booking.farmer.user_id,
        quantity=request.quantity,
        price=request.price,
        message=request.message,
        status=BookingStatus.MERCHANT_RESPONDED
    )
    db.add(negotiation)

    # Notify farmer
    new_notif = Notification(
        user_id=booking.farmer.user_id,
        title="Merchant Responded",
        message=f"Merchant has sent a counter offer for {booking.product.name}: {request.quantity} {booking.product.unit} @ ₹{request.price}/{booking.product.unit}",
        notification_type="merchant_responded",
        related_id=booking.booking_id
    )
    db.add(new_notif)
    db.commit()

    return {"success": True, "message": "Counter offer sent successfully."}

@router.post("/bookings/{booking_id}/complete")
def mark_booking_completed(booking_id: str, user_id: str, db: Session = Depends(get_db)):
    """Stage 7: Mark booking as completed"""
    booking = db.query(Booking).filter(Booking.booking_id == booking_id).first()
    
    if not booking:
        raise HTTPException(status_code=404, detail="Booking not found")
    
    buyer = db.query(Buyer).filter(Buyer.user_id == user_id).first()
    if booking.buyer_id != buyer.id:
        raise HTTPException(status_code=403, detail="Unauthorized")
    
    if booking.status == BookingStatus.COMPLETED:
        raise HTTPException(status_code=400, detail="Booking already completed")
    
    booking.status = BookingStatus.COMPLETED
    booking.completed_at = datetime.utcnow()
    
    # Update farmer completed bookings count
    booking.farmer.completed_bookings += 1
    
    db.commit()
    
    return {
        "success": True,
        "message": "Booking marked as completed",
        "booking_id": booking.booking_id,
        "status": booking.status
    }

@router.post("/report")
def report_farmer(request: ReportRequest, user_id: str, db: Session = Depends(get_db)):
    """Stage 7: Submit report about farmer or listing"""
    buyer = db.query(Buyer).filter(Buyer.user_id == user_id).first()
    
    if not buyer:
        raise HTTPException(status_code=404, detail="Buyer not found")
    
    booking = db.query(Booking).filter(Booking.booking_id == request.booking_id).first()
    if not booking or booking.buyer_id != buyer.id:
        raise HTTPException(status_code=404, detail="Booking not found")
    
    # Rate limit: max 10 reports per day per buyer
    today_start = datetime.utcnow().replace(hour=0, minute=0, second=0, microsecond=0)
    today_reports = db.query(Report).filter(
        and_(
            Report.reporter_id == buyer.id,
            Report.created_at >= today_start
        )
    ).count()
    
    if today_reports >= 10:
        raise HTTPException(status_code=429, detail="Report limit reached. Try again tomorrow.")
    
    report = Report(
        reporter_id=buyer.id,
        reported_user_id=booking.farmer_id,
        product_id=booking.product_id,
        booking_id=booking.id,
        reason=request.reason,
        description=request.description
    )
    
    db.add(report)
    db.commit()
    
    return {
        "success": True,
        "message": "Report submitted successfully. Our team will review it shortly.",
        "report_id": report.id
    }

@router.get("/profile/{user_id}")
def get_buyer_profile(user_id: str, db: Session = Depends(get_db)):
    """Get buyer profile"""
    user = db.query(User).filter(User.id == user_id).first()
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    buyer = db.query(Buyer).filter(Buyer.user_id == user_id).first()
    
    return {
        "success": True,
        "profile": {
            "user_id": user.id,
            "buyer_id": buyer.id if buyer else None,
            "full_name": user.full_name,
            "mobile_number": user.mobile_number,
            "email": user.email,
            "buyer_type": buyer.buyer_type if buyer else None,
            "state": buyer.state if buyer else None,
            "district": buyer.district if buyer else None,
            "preferred_language": buyer.preferred_language if buyer else None,
            "bookings_count": buyer.bookings_count if buyer else 0,
            "account_status": user.account_status,
            "created_at": user.created_at.isoformat()
        }
    }

@router.put("/profile/{user_id}")
def update_buyer_profile(user_id: str, updates: dict, db: Session = Depends(get_db)):
    """Update buyer profile"""
    user = db.query(User).filter(User.id == user_id).first()
    
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    buyer = db.query(Buyer).filter(Buyer.user_id == user_id).first()
    
    # Update user fields
    if "full_name" in updates:
        user.full_name = updates["full_name"]
    if "email" in updates:
        user.email = updates["email"]
    
    # Update buyer fields
    if buyer:
        if "buyer_type" in updates:
            buyer.buyer_type = updates["buyer_type"]
        if "state" in updates:
            buyer.state = updates["state"]
        if "district" in updates:
            buyer.district = updates["district"]
        if "preferred_language" in updates:
            buyer.preferred_language = updates["preferred_language"]
        if "business_name" in updates:
            # Note: models.py doesn't have business_name in Buyer,
            # maybe it should be added or it was intended for something else.
            # I'll skip it for now or add it if I can.
            pass

    db.commit()
    
    return {
        "success": True,
        "message": "Profile updated successfully"
    }

@router.get("/notifications/{user_id}")
def get_notifications(user_id: str, db: Session = Depends(get_db)):
    """Get buyer notifications"""
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

@router.post("/notifications/read-all")
def mark_all_notifications_read(user_id: str, db: Session = Depends(get_db)):
    """Mark all notifications as read for buyer"""
    db.query(Notification).filter(
        Notification.user_id == user_id,
        Notification.is_read == False
    ).update({"is_read": True})
    db.commit()
    return {"success": True, "message": "All notifications marked as read"}

@router.get("/farmers/{farmer_id}")
def get_farmer_public_profile(farmer_id: str, db: Session = Depends(get_db)):
    """Get a farmer's public profile for buyers"""
    farmer = db.query(Farmer).filter(Farmer.id == farmer_id).first()
    if not farmer:
        raise HTTPException(status_code=404, detail="Farmer not found")

    # Get active products for this farmer
    products = db.query(Product).filter(
        and_(Product.farmer_id == farmer.id, Product.status == ProductStatus.ACTIVE)
    ).all()

    return {
        "success": True,
        "farmer": {
            "id": farmer.id,
            "full_name": farmer.user.full_name,
            "state": farmer.state,
            "district": farmer.district,
            "village": farmer.village,
            "profile_photo": farmer.profile_photo_url,
            "completed_bookings": farmer.completed_bookings,
            "rating": farmer.rating,
            "joined_at": farmer.user.created_at.isoformat(),
            "active_crops": [
                {
                    "id": p.id,
                    "name": p.name,
                    "category": p.category,
                    "quantity": p.quantity,
                    "unit": p.unit,
                    "expected_price": p.expected_price,
                    "harvest_date": p.harvest_date.isoformat() if p.harvest_date else None,
                    "images": p.images
                } for p in products
            ]
        }
    }

# ==================== SAVED PRODUCTS ====================

@router.post("/saved/{product_id}")
def save_product(product_id: str, user_id: str, db: Session = Depends(get_db)):
    """Save a product for later (Wishlist)"""
    buyer = db.query(Buyer).filter(Buyer.user_id == user_id).first()
    if not buyer:
        raise HTTPException(status_code=404, detail="Buyer profile not found")

    # Check if already saved
    existing = db.query(SavedProduct).filter(
        SavedProduct.buyer_id == buyer.id,
        SavedProduct.product_id == product_id
    ).first()

    if existing:
        return {"success": True, "message": "Product already saved"}

    saved = SavedProduct(buyer_id=buyer.id, product_id=product_id)
    db.add(saved)
    db.commit()
    return {"success": True, "message": "Product saved"}

@router.delete("/saved/{product_id}")
def remove_saved_product(product_id: str, user_id: str, db: Session = Depends(get_db)):
    """Remove a product from saved list"""
    buyer = db.query(Buyer).filter(Buyer.user_id == user_id).first()
    if not buyer:
        raise HTTPException(status_code=404, detail="Buyer profile not found")

    saved = db.query(SavedProduct).filter(
        SavedProduct.buyer_id == buyer.id,
        SavedProduct.product_id == product_id
    ).first()

    if not saved:
        return {"success": True, "message": "Product not found in saved list"}

    db.delete(saved)
    db.commit()
    return {"success": True, "message": "Product removed from saved"}

@router.get("/saved")
def get_saved_products(user_id: str, db: Session = Depends(get_db)):
    """Get list of saved products"""
    buyer = db.query(Buyer).filter(Buyer.user_id == user_id).first()
    if not buyer:
        raise HTTPException(status_code=404, detail="Buyer profile not found")

    saved_items = db.query(SavedProduct).filter(SavedProduct.buyer_id == buyer.id).all()

    return {
        "success": True,
        "products": [
            {
                "id": s.product.id,
                "name": s.product.name,
                "category": s.product.category,
                "expected_price": s.product.expected_price,
                "quantity": s.product.quantity,
                "unit": s.product.unit,
                "images": s.product.images,
                "state": s.product.farmer.state,
                "district": s.product.farmer.district,
                "created_at": s.product.created_at
            } for s in saved_items
        ]
    }
