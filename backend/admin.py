from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import desc
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

# ==================== DASHBOARD & STATS ====================

@router.get("/dashboard")
def get_admin_dashboard(db: Session = Depends(get_db)):
    """Get comprehensive admin dashboard statistics"""
    total_farmers = db.query(User).filter(User.role == UserRole.FARMER).count()
    total_merchants = db.query(User).filter(User.role == UserRole.BUYER).count()
    active_products = db.query(Product).filter(Product.status == ProductStatus.ACTIVE).count()
    total_bookings = db.query(Booking).count()
    pending_reports = db.query(Report).filter(Report.status == ReportStatus.SUBMITTED).count()

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
        activity.append({
            "type": "booking",
            "message": f"Merchant booked '{b.product.name}'",
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
        query = query.filter(User.role == role)
    if status:
        query = query.filter(User.account_status == status)

    users = query.order_by(desc(User.created_at)).offset(skip).limit(limit).all()
    return {
        "success": True,
        "users": [
            {
                "id": u.id,
                "mobile": u.mobile_number,
                "full_name": u.full_name,
                "role": u.role.value,
                "status": u.account_status.value,
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
    return {"success": True, "message": f"User status updated to {request.status.value}"}

# ==================== PRODUCT MANAGEMENT ====================

@router.get("/products")
def list_products(status: Optional[ProductStatus] = None, db: Session = Depends(get_db)):
    """List all products"""
    query = db.query(Product)
    if status:
        query = query.filter(Product.status == status)

    products = query.order_by(desc(Product.created_at)).all()
    return {
        "success": True,
        "products": [
            {
                "id": p.id,
                "name": p.name,
                "farmer": p.farmer.user.full_name,
                "category": p.category.value,
                "status": p.status.value,
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
        query = query.filter(Report.status == status)

    reports = query.order_by(desc(Report.created_at)).all()
    return {
        "success": True,
        "reports": [
            {
                "id": r.id,
                "reporter": r.reporter.user.full_name,
                "reason": r.reason.value,
                "status": r.status.value,
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
        "role": user.role.value,
        "status": user.account_status.value,
        "verified": user.mobile_verified,
        "created_at": user.created_at,
        "email": user.email
    }

    if user.role == UserRole.FARMER and user.farmer:
        detail["profile"] = {
            "state": user.farmer.state,
            "district": user.farmer.district,
            "village": user.farmer.village,
            "address": user.farmer.farm_address,
            "rating": user.farmer.rating,
            "completed_bookings": user.farmer.completed_bookings,
            "listings_count": len(user.farmer.products)
        }
    elif user.role == UserRole.BUYER and user.buyer:
        detail["profile"] = {
            "state": user.buyer.state,
            "district": user.buyer.district,
            "type": user.buyer.buyer_type.value,
            "bookings_count": user.buyer.bookings_count
        }

    return {"success": True, "user": detail}

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
            "category": product.category.value,
            "description": product.description,
            "quantity": product.quantity,
            "unit": product.unit,
            "price": product.expected_price,
            "status": product.status.value,
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

    product.status = ProductStatus.REMOVED
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
                "status": b.status.value,
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
