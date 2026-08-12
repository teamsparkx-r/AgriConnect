from sqlalchemy import Column, String, Integer, Float, DateTime, Boolean, Enum, ForeignKey, Text
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship
from datetime import datetime
import enum
import uuid

Base = declarative_base()

# ==================== ENUMS ====================
class UserRole(str, enum.Enum):
    BUYER = "buyer"
    FARMER = "farmer"
    ADMIN = "admin"

class AccountStatus(str, enum.Enum):
    ACTIVE = "active"
    SUSPENDED = "suspended"
    DELETED = "deleted"

class BuyerType(str, enum.Enum):
    WHOLESALER = "wholesaler"
    RETAILER = "retailer"
    TRADER = "trader"
    RESTAURANT_HOTEL = "restaurant_hotel"
    PROCESSOR = "processor"
    EXPORTER = "exporter"
    INSTITUTIONAL = "institutional"
    INDIVIDUAL = "individual"
    OTHER = "other"

class Language(str, enum.Enum):
    ENGLISH = "english"
    HINDI = "hindi"
    TAMIL = "tamil"
    TELUGU = "telugu"
    KANNADA = "kannada"
    MARATHI = "marathi"

class ProductCategory(str, enum.Enum):
    VEGETABLES = "vegetables"
    FRUITS = "fruits"
    GRAINS = "grains"
    PULSES = "pulses"
    SPICES = "spices"
    OIL_SEEDS = "oilseeds"
    COMMERCIAL_CROPS = "commercial_crops"
    OTHER = "other"

class ProductStatus(str, enum.Enum):
    DRAFT = "draft"
    ACTIVE = "active"
    SOLD = "sold"
    EXPIRED = "expired"
    REMOVED = "removed"

class BookingStatus(str, enum.Enum):
    INITIATED = "initiated"
    CONFIRMED = "confirmed"
    COMPLETED = "completed"
    CANCELLED = "cancelled"

class ReportReason(str, enum.Enum):
    FALSE_PRODUCT_INFO = "false_product_info"
    MISLEADING_IMAGES = "misleading_images"
    FARMER_UNREACHABLE = "farmer_unreachable"
    SUSPICIOUS_BEHAVIOR = "suspicious_behavior"
    FRAUD_ATTEMPT = "fraud_attempt"
    ABUSIVE_BEHAVIOR = "abusive_behavior"
    NOT_AVAILABLE = "not_available"
    OTHER = "other"

class ReportStatus(str, enum.Enum):
    SUBMITTED = "submitted"
    UNDER_REVIEW = "under_review"
    RESOLVED = "resolved"
    DISMISSED = "dismissed"

# ==================== USERS & AUTHENTICATION ====================
class User(Base):
    __tablename__ = "users"
    
    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    mobile_number = Column(String(15), unique=True, nullable=False, index=True)
    email = Column(String(100), unique=True, nullable=True)
    password_hash = Column(String(255), nullable=False)
    full_name = Column(String(100), nullable=False)
    role = Column(Enum(UserRole), nullable=False)
    account_status = Column(Enum(AccountStatus), default=AccountStatus.ACTIVE, nullable=False)
    mobile_verified = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    buyer = relationship("Buyer", uselist=False, back_populates="user", cascade="all, delete-orphan")
    farmer = relationship("Farmer", uselist=False, back_populates="user", cascade="all, delete-orphan")
    otp_records = relationship("OTPRecord", back_populates="user", cascade="all, delete-orphan")

class OTPRecord(Base):
    __tablename__ = "otp_records"
    
    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String(36), ForeignKey("users.id"), nullable=False)
    otp_code = Column(String(6), nullable=False)
    purpose = Column(String(50), default="verification")  # verification, password_reset, etc.
    is_used = Column(Boolean, default=False)
    failed_attempts = Column(Integer, default=0)
    created_at = Column(DateTime, default=datetime.utcnow)
    expires_at = Column(DateTime, nullable=False)
    
    user = relationship("User", back_populates="otp_records")

# ==================== BUYER ====================
class Buyer(Base):
    __tablename__ = "buyers"
    
    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String(36), ForeignKey("users.id"), nullable=False, unique=True)
    buyer_type = Column(Enum(BuyerType), default=BuyerType.OTHER)
    state = Column(String(50), nullable=False, index=True)
    district = Column(String(50), nullable=False, index=True)
    preferred_language = Column(Enum(Language), default=Language.ENGLISH)
    profile_photo_url = Column(String(255), nullable=True)
    bookings_count = Column(Integer, default=0)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    user = relationship("User", back_populates="buyer")
    bookings = relationship("Booking", foreign_keys="Booking.buyer_id", back_populates="buyer")
    reports = relationship("Report", foreign_keys="Report.reporter_id", back_populates="reporter")

# ==================== FARMER ====================
class Farmer(Base):
    __tablename__ = "farmers"
    
    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String(36), ForeignKey("users.id"), nullable=False, unique=True)
    state = Column(String(50), nullable=False, index=True)
    district = Column(String(50), nullable=False, index=True)
    village = Column(String(100), nullable=False)
    farm_address = Column(Text, nullable=True)
    latitude = Column(Float, nullable=True)
    longitude = Column(Float, nullable=True)
    share_farm_address = Column(Boolean, default=True)
    share_coordinates = Column(Boolean, default=False)
    preferred_language = Column(Enum(Language), default=Language.ENGLISH)
    profile_photo_url = Column(String(255), nullable=True)
    completed_bookings = Column(Integer, default=0)
    rating = Column(Float, default=0.0)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    user = relationship("User", back_populates="farmer")
    products = relationship("Product", back_populates="farmer", cascade="all, delete-orphan")
    bookings = relationship("Booking", foreign_keys="Booking.farmer_id", back_populates="farmer")
    reports = relationship("Report", foreign_keys="Report.reported_user_id", back_populates="reported_user")

# ==================== PRODUCTS ====================
class Product(Base):
    __tablename__ = "products"
    
    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    farmer_id = Column(String(36), ForeignKey("farmers.id"), nullable=False)
    name = Column(String(100), nullable=False, index=True)
    category = Column(Enum(ProductCategory), nullable=False, index=True)
    description = Column(Text, nullable=True)
    quantity = Column(Float, nullable=False)
    unit = Column(String(20), nullable=False)  # kg, quintals, units, etc.
    expected_price = Column(Float, nullable=True)
    harvest_date = Column(DateTime, nullable=True)
    status = Column(Enum(ProductStatus), default=ProductStatus.DRAFT, index=True)
    images = Column(String(1000), nullable=True)  # JSON string of image URLs

    # Location fields (Defaults to farmer's location but can be specific)
    state = Column(String(50), nullable=True)
    district = Column(String(50), nullable=True)
    village = Column(String(100), nullable=True)
    farm_address = Column(Text, nullable=True)

    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    farmer = relationship("Farmer", back_populates="products")
    bookings = relationship("Booking", back_populates="product", cascade="all, delete-orphan")

# ==================== BOOKINGS ====================
class Booking(Base):
    __tablename__ = "bookings"
    
    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    booking_id = Column(String(20), unique=True, nullable=False, index=True)  # Human-readable ID
    buyer_id = Column(String(36), ForeignKey("buyers.id"), nullable=False)
    farmer_id = Column(String(36), ForeignKey("farmers.id"), nullable=False)
    product_id = Column(String(36), ForeignKey("products.id"), nullable=False)
    status = Column(Enum(BookingStatus), default=BookingStatus.INITIATED, index=True)
    contact_unlocked_at = Column(DateTime, nullable=True)
    terms_accepted = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow, index=True)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    completed_at = Column(DateTime, nullable=True)
    
    # Relationships
    buyer = relationship("Buyer", foreign_keys=[buyer_id], back_populates="bookings")
    farmer = relationship("Farmer", foreign_keys=[farmer_id], back_populates="bookings")
    product = relationship("Product", back_populates="bookings")

# ==================== REPORTS ====================
class Report(Base):
    __tablename__ = "reports"
    
    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    reporter_id = Column(String(36), ForeignKey("buyers.id"), nullable=False)
    reported_user_id = Column(String(36), ForeignKey("farmers.id"), nullable=True)
    product_id = Column(String(36), ForeignKey("products.id"), nullable=True)
    booking_id = Column(String(36), ForeignKey("bookings.id"), nullable=True)
    reason = Column(Enum(ReportReason), nullable=False)
    description = Column(Text, nullable=True)
    status = Column(Enum(ReportStatus), default=ReportStatus.SUBMITTED, index=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    reporter = relationship("Buyer", foreign_keys=[reporter_id], back_populates="reports")
    reported_user = relationship("Farmer", foreign_keys=[reported_user_id], back_populates="reports")

# ==================== SAVED PRODUCTS ====================
class SavedProduct(Base):
    __tablename__ = "saved_products"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    buyer_id = Column(String(36), ForeignKey("buyers.id"), nullable=False)
    product_id = Column(String(36), ForeignKey("products.id"), nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)

    # Relationships
    buyer = relationship("Buyer", backref="saved_products")
    product = relationship("Product", backref="saved_by")

# ==================== NOTIFICATIONS ====================
class Notification(Base):
    __tablename__ = "notifications"
    
    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(String(36), ForeignKey("users.id"), nullable=False, index=True)
    title = Column(String(100), nullable=False)
    message = Column(Text, nullable=False)
    notification_type = Column(String(50), nullable=False)  # booking_confirmed, product_status_changed, etc.
    related_id = Column(String(36), nullable=True)  # booking_id, product_id, etc.
    is_read = Column(Boolean, default=False, index=True)
    created_at = Column(DateTime, default=datetime.utcnow, index=True)

# ==================== AUDIT LOGS ====================
class AuditLog(Base):
    __tablename__ = "audit_logs"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    admin_id = Column(String(36), ForeignKey("users.id"), nullable=False)
    action = Column(String(100), nullable=False)  # suspend_user, remove_product, etc.
    target_id = Column(String(36), nullable=True)
    reason = Column(String(255), nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)

    admin = relationship("User")

# ==================== PLATFORM SETTINGS ====================
class PlatformSettings(Base):
    __tablename__ = "platform_settings"

    key = Column(String(50), primary_key=True)
    value = Column(String(255), nullable=False)
    description = Column(String(255), nullable=True)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
