from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import desc, func
from models import (
    User, Farmer, Buyer, Product, Booking, Report,
    UserRole, AccountStatus, ProductStatus, BookingStatus, ReportStatus,
    AuditLog, PlatformSettings, Notification as Announcement
)
from database import get_db
from auth import AuthService
from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime

router = APIRouter()

# ==================== PYDANTIC MODELS ====================
class UserStatusUpdate(BaseModel):
    status: AccountStatus

class ReportResolveRequest(BaseModel):
    status: ReportStatus
    admin_notes: Optional[str] = None

@router.post("/seed-massive")
def seed_massive_crops(db: Session = Depends(get_db)):
    """Special endpoint to force-seed 60 crops for the demo farmer"""
    try:
        from models import Farmer, Product, ProductCategory, User, Booking, SavedProduct
        import random

        farmer_user_id = "5737b51e-8640-48c2-bdaa-63fbba1b70a7"
        farmer = db.query(Farmer).filter(Farmer.user_id == farmer_user_id).first()

        if not farmer:
            return {"success": False, "message": "Demo farmer profile not found. Run standard seed first."}

        # 1. Clear existing products to prevent duplicates (with dependency handling)
        demo_product_ids = [p.id for p in db.query(Product).filter(Product.farmer_id == farmer.id).all()]
        if demo_product_ids:
            # Delete bookings associated with these products
            db.query(Booking).filter(Booking.product_id.in_(demo_product_ids)).delete(synchronize_session=False)
            # Delete wishlist items
            db.query(SavedProduct).filter(SavedProduct.product_id.in_(demo_product_ids)).delete(synchronize_session=False)
            # Finally delete the products
            db.query(Product).filter(Product.id.in_(demo_product_ids)).delete(synchronize_session=False)
            db.flush()

        # 2. Define templates
        crops_pool = [
            ("Organic Wheat", "grains", 25.0, "https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=800"),
            ("Red Tomatoes", "vegetables", 18.5, "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=800"),
            ("Basmati Rice", "grains", 65.0, "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=800"),
            ("Yellow Corn", "grains", 22.0, "https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=800"),
            ("Fresh Onions", "vegetables", 15.0, "https://images.unsplash.com/photo-1508747703725-719777637510?w=800"),
            ("Potatoes", "vegetables", 12.0, "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=800"),
            ("Green Chillies", "vegetables", 30.0, "https://images.unsplash.com/photo-1588252303782-cb80119abd6d?w=800"),
            ("Ginger", "spices", 120.0, "https://images.unsplash.com/photo-1599940824399-b87987ceb72a?w=800"),
            ("Turmeric", "spices", 150.0, "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=800"),
            ("Garlic", "vegetables", 80.0, "https://images.unsplash.com/photo-1540148426945-6cf22a6b2383?w=800"),
            ("Red Chilli", "spices", 180.0, "https://images.unsplash.com/photo-1599488615731-7e5c2823ff28?w=800"),
            ("Soybeans", "oilseeds", 45.0, "https://images.unsplash.com/photo-1589923188900-85dae523342b?w=800"),
            ("Mustard Seeds", "oilseeds", 55.0, "https://images.unsplash.com/photo-1599409673963-8f304a55903b?w=800"),
            ("Cotton", "commercial_crops", 75.0, "https://images.unsplash.com/photo-1594903310503-492723326f21?w=800"),
            ("Sugarcane", "commercial_crops", 3.5, "https://images.unsplash.com/photo-1593113617719-79883584852b?w=800"),
            ("Alphonso Mango", "fruits", 250.0, "https://images.unsplash.com/photo-1553134839-497746777b73?w=800"),
            ("Green Apples", "fruits", 140.0, "https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?w=800"),
            ("Banana", "fruits", 20.0, "https://images.unsplash.com/photo-1571771894821-ad990241fab4?w=800"),
            ("Watermelon", "fruits", 15.0, "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=800"),
            ("Grapes", "fruits", 90.0, "https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=800"),
            ("Cabbage", "vegetables", 10.0, "https://images.unsplash.com/photo-1591461141441-38e9329007f3?w=800"),
            ("Cauliflower", "vegetables", 25.0, "https://images.unsplash.com/photo-1568584711075-3d021a7c3ec3?w=800"),
            ("Carrots", "vegetables", 40.0, "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=800"),
            ("Spinach", "vegetables", 20.0, "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=800"),
            ("Moong Dal", "pulses", 110.0, "https://images.unsplash.com/photo-1585994192703-274638314741?w=800"),
            ("Arhar Dal", "pulses", 140.0, "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=800"),
            ("Black Gram", "pulses", 125.0, "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=800"),
            ("Chickpeas", "pulses", 95.0, "https://images.unsplash.com/photo-1585994192703-274638314741?w=800"),
            ("Peanuts", "oilseeds", 85.0, "https://images.unsplash.com/photo-1527661591475-527312dd65f5?w=800"),
            ("Sunflower Seeds", "oilseeds", 70.0, "https://images.unsplash.com/photo-1597426102235-93722956c802?w=800"),
            ("Cardamom", "spices", 2500.0, "https://images.unsplash.com/photo-1599409673963-8f304a55903b?w=800"),
            ("Black Pepper", "spices", 450.0, "https://images.unsplash.com/photo-1599409673963-8f304a55903b?w=800"),
            ("Cloves", "spices", 900.0, "https://images.unsplash.com/photo-1599409673963-8f304a55903b?w=800"),
        ]

        # 3. Generate 60
        for i in range(60):
            template = crops_pool[i % len(crops_pool)]
            name, cat, base_price, img = template

            variation = random.uniform(0.85, 1.15)
            quantity = random.randint(200, 8000)
            price = round(base_price * variation, 2)

            new_p = Product(
                farmer_id=farmer.id,
                name=f"{name} (Lot #{i+1})",
                category=cat,
                description=f"Fresh supply of {name} from demo farms. Graded for premium commercial use. Secure direct sourcing node.",
                quantity=quantity,
                unit="kg" if cat != "fruits" else "units",
                expected_price=price,
                status="active",
                images=img,
                state=farmer.state,
                district=farmer.district,
                village=farmer.village
            )
            db.add(new_p)

        db.commit()
        return {"success": True, "message": "Successfully seeded 60 massive crop nodes."}
    except Exception as e:
        db.rollback()
        return {"success": False, "message": str(e)}

# ==================== DASHBOARD & STATS ====================

@router.get("/dashboard")
def get_admin_dashboard(db: Session = Depends(get_db)):
    """Get comprehensive admin dashboard statistics"""
    total_farmers = db.query(User).filter(func.lower(User.role) == "farmer").count()
    total_merchants = db.query(User).filter(func.lower(User.role) == "buyer").count()
    active_products = db.query(Product).filter(func.lower(Product.status) == "active").count()
    total_bookings = db.query(Booking).count()
    pending_reports = db.query(Report).filter(func.lower(Report.status) == "submitted").count()

    # Revenue is total bookings * 100 (connection fee)
    total_revenue = total_bookings * 100

    # Recent activity log (Mocking some variety for the UI)
    recent_users = db.query(User).order_by(desc(User.created_at)).limit(3).all()
    recent_bookings = db.query(Booking).order_by(desc(Booking.created_at)).limit(3).all()

    activity = []
    for u in recent_users:
        role_label = "farmer" if u.role == UserRole.FARMER else "merchant"
        activity.append({
            "type": "user",
            "message": f"New {role_label} '{u.full_name}' registered",
            "time": u.created_at
        })
    for b in recent_bookings:
        product_name = b.product.name if b.product else "Unknown Product"
        activity.append({
            "type": "booking",
            "message": f"Merchant booked '{product_name}'",
            "time": b.created_at
        })

    # Sort activity by time
    activity.sort(key=lambda x: x["time"], reverse=True)

    return {
        "success": True,
        "stats": {
            "total_farmers": total_farmers,
            "total_merchants": total_merchants,
            "active_products": active_products,
            "total_bookings": total_bookings,
            "total_revenue": total_revenue,
            "pending_reports": pending_reports
        },
        "recent_activity": activity[:5]
    }

# ==================== USER MANAGEMENT ====================

@router.get("/users")
def list_users(
    role: Optional[UserRole] = None,
    status: Optional[AccountStatus] = None,
    skip: int = 0,
    limit: int = 50,
    db: Session = Depends(get_db)
):
    """List and filter users"""
    query = db.query(User)
    if role:
        query = query.filter(func.lower(User.role) == role.lower())
    if status:
        query = query.filter(func.lower(User.account_status) == status.lower())

    users = query.order_by(desc(User.created_at)).offset(skip).limit(limit).all()
    return {
        "success": True,
        "users": [
            {
                "id": u.id,
                "mobile": u.mobile_number,
                "full_name": u.full_name,
                "role": u.role,
                "status": u.account_status,
                "verified": u.mobile_verified,
                "created_at": u.created_at
            } for u in users
        ]
    }

@router.patch("/users/{user_id}/status")
def update_user_status(user_id: str, request: UserStatusUpdate, db: Session = Depends(get_db)):
    """Update user account status (suspend/activate)"""
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    user.account_status = request.status
    db.commit()
    return {"success": True, "message": f"User status updated to {request.status}"}

# ==================== PRODUCT MANAGEMENT ====================

@router.get("/products")
def list_products(status: Optional[ProductStatus] = None, db: Session = Depends(get_db)):
    """List all products"""
    query = db.query(Product)
    if status:
        query = query.filter(func.lower(Product.status) == status.lower())

    products = query.order_by(desc(Product.created_at)).all()
    return {
        "success": True,
        "products": [
            {
                "id": p.id,
                "name": p.name,
                "farmer": p.farmer.user.full_name,
                "category": p.category,
                "status": p.status,
                "created_at": p.created_at
            } for p in products
        ]
    }

# ==================== REPORT MANAGEMENT ====================

@router.get("/reports")
def list_reports(status: Optional[ReportStatus] = None, db: Session = Depends(get_db)):
    """List all reports"""
    query = db.query(Report)
    if status:
        query = query.filter(func.lower(Report.status) == status.lower())

    reports = query.order_by(desc(Report.created_at)).all()
    return {
        "success": True,
        "reports": [
            {
                "id": r.id,
                "reporter": r.reporter.user.full_name,
                "reason": r.reason,
                "status": r.status,
                "created_at": r.created_at
            } for r in reports
        ]
    }

@router.post("/reports/{report_id}/resolve")
def resolve_report(report_id: str, request: ReportResolveRequest, db: Session = Depends(get_db)):
    """Resolve or dismiss a report"""
    report = db.query(Report).filter(Report.id == report_id).first()
    if not report:
        raise HTTPException(status_code=404, detail="Report not found")

    report.status = request.status
    # Here we could add logic to actually penalize the user if needed
    db.commit()
    return {"success": True, "message": "Report status updated"}

@router.get("/users/{user_id}")
def get_user_detail(user_id: str, db: Session = Depends(get_db)):
    """Get detailed profile for a specific user"""
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    detail = {
        "id": user.id,
        "mobile": user.mobile_number,
        "full_name": user.full_name,
        "role": user.role,
        "status": user.account_status,
        "verified": user.mobile_verified,
        "created_at": user.created_at,
        "email": user.email
    }

    if user.role.lower() == "farmer" and user.farmer:
        detail["profile"] = {
            "state": user.farmer.state,
            "district": user.farmer.district,
            "village": user.farmer.village,
            "address": user.farmer.farm_address,
            "rating": user.farmer.rating,
            "completed_bookings": user.farmer.completed_bookings,
            "listings_count": len(user.farmer.products)
        }
    elif user.role.lower() == "buyer" and user.buyer:
        detail["profile"] = {
            "state": user.buyer.state,
            "district": user.buyer.district,
            "type": user.buyer.buyer_type,
            "bookings_count": user.buyer.bookings_count
        }

    return {"success": True, "user": detail}

@router.post("/users/{user_id}/approve")
def approve_user(user_id: str, db: Session = Depends(get_db)):
    """Approve a pending user registration"""
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    user.account_status = "active"

    # Create notification for user
    from models import Notification
    message = "Your AgriConnect account has been approved. You can now list and sell your crops." if user.role.lower() == "farmer" else "Your AgriConnect account has been approved. You can now send enquiries and place orders."
    new_notif = Notification(
        user_id=user.id,
        title="Account Approved",
        message=message,
        notification_type="account_approved"
    )
    db.add(new_notif)

    # Log action
    log = AuditLog(admin_id="2687eded-053d-4cbc-8a06-18be9ad5888b", action="approve_user", target_id=user.id)
    db.add(log)

    db.commit()
    return {"success": True, "message": "User approved successfully"}

@router.post("/users/{user_id}/reject")
def reject_user(user_id: str, reason: Optional[str] = None, db: Session = Depends(get_db)):
    """Reject a pending user registration"""
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")

    user.account_status = "rejected"

    # Create notification for user
    from models import Notification
    message = f"Your AgriConnect account registration was not approved. Reason: {reason}" if reason else "Your AgriConnect account registration was not approved. Please contact support."
    new_notif = Notification(
        user_id=user.id,
        title="Account Rejected",
        message=message,
        notification_type="account_rejected"
    )
    db.add(new_notif)

    # Log action
    log = AuditLog(admin_id="2687eded-053d-4cbc-8a06-18be9ad5888b", action="reject_user", target_id=user.id, reason=reason)
    db.add(log)

    db.commit()
    return {"success": True, "message": "User rejected"}

@router.get("/products/{product_id}")
def get_admin_product_detail(product_id: str, db: Session = Depends(get_db)):
    """Get detailed info for a product listing"""
    product = db.query(Product).filter(Product.id == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="Product not found")

    return {
        "success": True,
        "product": {
            "id": product.id,
            "name": product.name,
            "category": product.category,
            "description": product.description,
            "quantity": product.quantity,
            "unit": product.unit,
            "price": product.expected_price,
            "status": product.status,
            "farmer": product.farmer.user.full_name,
            "farmer_mobile": product.farmer.user.mobile_number,
            "location": f"{product.district}, {product.state}",
            "created_at": product.created_at
        }
    }

@router.delete("/products/{product_id}")
def admin_delete_product(product_id: str, db: Session = Depends(get_db)):
    """Force delete/remove a product listing"""
    product = db.query(Product).filter(Product.id == product_id).first()
    if not product:
        raise HTTPException(status_code=404, detail="Product not found")

    product.status = "removed"
    db.commit()
    return {"success": True, "message": "Product removed by admin"}

# ==================== BOOKING & PAYMENT MANAGEMENT ====================

@router.get("/bookings")
def list_all_bookings(db: Session = Depends(get_db)):
    """List all marketplace bookings"""
    bookings = db.query(Booking).order_by(desc(Booking.created_at)).all()
    return {
        "success": True,
        "bookings": [
            {
                "booking_id": b.booking_id,
                "product_name": b.product.name,
                "buyer_name": b.buyer.user.full_name,
                "farmer_name": b.farmer.user.full_name,
                "status": b.status,
                "created_at": b.created_at
            } for b in bookings
        ]
    }

@router.get("/payments")
def list_all_payments(db: Session = Depends(get_db)):
    """List all booking fee payments (Simulated from bookings)"""
    bookings = db.query(Booking).order_by(desc(Booking.created_at)).all()
    return {
        "success": True,
        "payments": [
            {
                "id": b.id,
                "booking_id": b.booking_id,
                "merchant": b.buyer.user.full_name,
                "amount": 100,
                "status": "successful",
                "created_at": b.created_at
            } for b in bookings
        ]
    }

# ==================== CATEGORY MANAGEMENT ====================

@router.get("/categories")
def list_categories(db: Session = Depends(get_db)):
    """List product categories and their listing counts"""
    from models import ProductCategory
    categories = []
    for cat in ProductCategory:
        count = db.query(Product).filter(Product.category == cat).count()
        categories.append({
            "id": cat.value,
            "name": cat.value,
            "listings": count,
            "status": "active"
        })
    return {"success": True, "categories": categories}

# ==================== AUDIT LOGS ====================

@router.get("/logs")
def get_audit_logs(db: Session = Depends(get_db)):
    """Get system administrative logs"""
    logs = db.query(AuditLog).order_by(desc(AuditLog.created_at)).all()
    return {
        "success": True,
        "logs": [
            {
                "admin": l.admin.full_name if l.admin else "System",
                "action": l.action,
                "target": l.target_id,
                "reason": l.reason,
                "time": l.created_at
            } for l in logs
        ]
    }

@router.get("/notifications")
def get_admin_notifications(db: Session = Depends(get_db)):
    """Get notifications for the admin"""
    from models import Notification
    admin_id = "2687eded-053d-4cbc-8a06-18be9ad5888b"
    notifications = db.query(Notification).filter(
        Notification.user_id == admin_id
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
        ]
    }

@router.post("/notifications/read-all")
def mark_all_notifications_read(db: Session = Depends(get_db)):
    """Mark all notifications as read for admin"""
    from models import Notification
    admin_id = "2687eded-053d-4cbc-8a06-18be9ad5888b"
    db.query(Notification).filter(
        Notification.user_id == admin_id,
        Notification.is_read == False
    ).update({"is_read": True})
    db.commit()
    return {"success": True, "message": "All notifications marked as read"}

# ==================== ANNOUNCEMENTS ====================

class AnnouncementRequest(BaseModel):
    title: str
    message: str
    target: str  # all, farmers, buyers

@router.post("/broadcast")
def broadcast_announcement(request: AnnouncementRequest, db: Session = Depends(get_db)):
    """Broadcast an announcement to users"""
    query = db.query(User)
    if request.target == "farmers":
        query = query.filter(User.role == UserRole.FARMER)
    elif request.target == "buyers":
        query = query.filter(User.role == UserRole.BUYER)

    users = query.all()
    for user in users:
        announcement = Announcement(
            user_id=user.id,
            title=request.title,
            message=request.message,
            notification_type="announcement"
        )
        db.add(announcement)

    db.commit()
    return {"success": True, "message": f"Broadcast sent to {len(users)} users"}

# ==================== PLATFORM SETTINGS ====================

class SettingUpdate(BaseModel):
    value: str

@router.patch("/settings/{key}")
def update_setting(key: str, request: SettingUpdate, db: Session = Depends(get_db)):
    """Update a platform setting"""
    setting = db.query(PlatformSettings).filter(PlatformSettings.key == key).first()
    if not setting:
        raise HTTPException(status_code=404, detail="Setting not found")

    setting.value = request.value
    db.commit()
    return {"success": True, "message": f"Setting {key} updated"}

@router.get("/settings")
def get_settings(db: Session = Depends(get_db)):
    """Get platform configuration settings"""
    settings = db.query(PlatformSettings).all()
    if not settings:
        defaults = [
            PlatformSettings(key="booking_fee", value="100", description="Connection fee in INR"),
            PlatformSettings(key="listing_expiry_days", value="30", description="Days before listing expires"),
            PlatformSettings(key="maintenance_mode", value="false", description="Disable public access")
        ]
        db.add_all(defaults)
        db.commit()
        settings = defaults

    return {
        "success": True,
        "settings": {s.key: {"value": s.value, "desc": s.description} for s in settings}
    }
