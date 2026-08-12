from datetime import datetime, timedelta
from passlib.context import CryptContext
from jose import JWTError, jwt
from sqlalchemy.orm import Session
from models import User, OTPRecord, UserRole, AccountStatus
import random
import os
from typing import Optional, Tuple

# Password hashing
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# JWT configuration
SECRET_KEY = os.getenv("SECRET_KEY", "your-super-secret-key-change-in-production")
ALGORITHM = os.getenv("ALGORITHM", "HS256")
ACCESS_TOKEN_EXPIRE_MINUTES = 30
REFRESH_TOKEN_EXPIRE_DAYS = 7

# OTP configuration
OTP_EXPIRY_MINUTES = 5
OTP_MAX_RESENDS = 3
OTP_MAX_FAILED_ATTEMPTS = 5
OTP_LOCKOUT_MINUTES = 15


class AuthService:
    """Authentication service for user registration, login, OTP verification, etc."""
    
    @staticmethod
    def hash_password(password: str) -> str:
        """Hash a password"""
        return pwd_context.hash(password)
    
    @staticmethod
    def verify_password(plain_password: str, hashed_password: str) -> bool:
        """Verify a password against hash"""
        return pwd_context.verify(plain_password, hashed_password)
    
    @staticmethod
    def create_access_token(user_id: str, role: str) -> Tuple[str, datetime]:
        """Create JWT access token"""
        expires = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
        payload = {
            "sub": user_id,
            "role": role,
            "exp": expires,
            "iat": datetime.utcnow()
        }
        token = jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)
        return token, expires
    
    @staticmethod
    def create_refresh_token(user_id: str) -> Tuple[str, datetime]:
        """Create JWT refresh token"""
        expires = datetime.utcnow() + timedelta(days=REFRESH_TOKEN_EXPIRE_DAYS)
        payload = {
            "sub": user_id,
            "type": "refresh",
            "exp": expires,
            "iat": datetime.utcnow()
        }
        token = jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)
        return token, expires
    
    @staticmethod
    def verify_token(token: str) -> Optional[dict]:
        """Verify and decode JWT token"""
        try:
            payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
            return payload
        except JWTError:
            return None
    
    @staticmethod
    def generate_otp() -> str:
        """Generate a 6-digit OTP"""
        return "".join([str(random.randint(0, 9)) for _ in range(6)])
    
    @staticmethod
    def create_otp_record(db: Session, user_id: str, purpose: str = "verification") -> str:
        """Create OTP record for user"""
        otp_code = AuthService.generate_otp()
        expires_at = datetime.utcnow() + timedelta(minutes=OTP_EXPIRY_MINUTES)
        
        otp_record = OTPRecord(
            user_id=user_id,
            otp_code=otp_code,
            purpose=purpose,
            expires_at=expires_at
        )
        db.add(otp_record)
        db.commit()
        
        return otp_code
    
    @staticmethod
    def verify_otp(db: Session, user_id: str, otp_code: str, purpose: str = "verification") -> Tuple[bool, str]:
        """Verify OTP for user"""
        # FOR DEVELOPMENT: Allow bypass with 000000
        if otp_code == "000000":
            return True, "OTP verified successfully (Dev Bypass)"

        otp_record = db.query(OTPRecord).filter(
            OTPRecord.user_id == user_id,
            OTPRecord.purpose == purpose,
            OTPRecord.is_used == False
        ).order_by(OTPRecord.created_at.desc()).first()
        
        if not otp_record:
            return False, "No OTP found"
        
        # Check if OTP is expired
        if datetime.utcnow() > otp_record.expires_at:
            return False, "OTP expired"
        
        # Check failed attempts
        if otp_record.failed_attempts >= OTP_MAX_FAILED_ATTEMPTS:
            # Check if lockout period has passed
            lockout_until = otp_record.created_at + timedelta(minutes=OTP_LOCKOUT_MINUTES)
            if datetime.utcnow() < lockout_until:
                return False, f"Too many failed attempts. Try again after {(lockout_until - datetime.utcnow()).seconds // 60} minutes"
            else:
                otp_record.failed_attempts = 0  # Reset failed attempts
        
        # Verify OTP
        if otp_record.otp_code != otp_code:
            otp_record.failed_attempts += 1
            db.commit()
            return False, "Invalid OTP"
        
        # Mark OTP as used
        otp_record.is_used = True
        db.commit()
        
        # Mark user as verified
        user = db.query(User).filter(User.id == user_id).first()
        if user:
            user.mobile_verified = True
            db.commit()
        
        return True, "OTP verified successfully"
    
    @staticmethod
    def resend_otp(db: Session, user_id: str, purpose: str = "verification") -> Tuple[bool, str]:
        """Resend OTP to user"""
        # Count resend attempts in last OTP_EXPIRY_MINUTES
        cutoff_time = datetime.utcnow() - timedelta(minutes=OTP_EXPIRY_MINUTES)
        resend_count = db.query(OTPRecord).filter(
            OTPRecord.user_id == user_id,
            OTPRecord.purpose == purpose,
            OTPRecord.created_at > cutoff_time
        ).count()
        
        if resend_count >= OTP_MAX_RESENDS:
            return False, f"Maximum resends reached. Try again in {OTP_EXPIRY_MINUTES} minutes"
        
        # Generate new OTP
        otp_code = AuthService.create_otp_record(db, user_id, purpose)
        return True, otp_code
    
    @staticmethod
    def register_user(
        db: Session,
        mobile_number: str,
        password: str,
        full_name: str,
        role: UserRole,
        email: Optional[str] = None,
        verified: bool = False
    ) -> Tuple[bool, str, Optional[User]]:
        """Register a new user"""
        # Check if user already exists
        existing_user = db.query(User).filter(User.mobile_number == mobile_number).first()
        if existing_user:
            return False, "Mobile number already registered", None
        
        if email:
            existing_email = db.query(User).filter(User.email == email).first()
            if existing_email:
                return False, "Email already registered", None
        
        # Create new user
        user = User(
            mobile_number=mobile_number,
            email=email,
            password_hash=AuthService.hash_password(password),
            full_name=full_name,
            role=role,
            account_status=AccountStatus.ACTIVE,
            mobile_verified=verified
        )
        
        db.add(user)
        db.commit()
        db.refresh(user)
        
        return True, "User registered successfully", user
    
    @staticmethod
    def authenticate_user(
        db: Session,
        identity: str,
        password: str
    ) -> Tuple[bool, str, Optional[User]]:
        """Authenticate user with mobile or email and password"""
        # Check if identity is email or mobile
        if "@" in identity:
            user = db.query(User).filter(User.email == identity).first()
        else:
            user = db.query(User).filter(User.mobile_number == identity).first()
        
        if not user:
            return False, "Identity not found in registry", None
        
        if not AuthService.verify_password(password, user.password_hash):
            return False, "Invalid password", None
        
        if user.account_status == AccountStatus.SUSPENDED:
            return False, "Account suspended", None
        
        if user.account_status == AccountStatus.DELETED:
            return False, "Account deleted", None
        
        return True, "Authentication successful", user
    
    @staticmethod
    def authenticate_with_otp(
        db: Session,
        mobile_number: str,
        otp_code: str
    ) -> Tuple[bool, str, Optional[User]]:
        """Authenticate user with mobile and OTP"""
        user = db.query(User).filter(User.mobile_number == mobile_number).first()
        
        if not user:
            return False, "User not found", None
        
        # Verify OTP
        success, message = AuthService.verify_otp(db, user.id, otp_code, purpose="verification")
        if not success:
            return False, message, None
        
        if user.account_status == AccountStatus.SUSPENDED:
            return False, "Account suspended", None
        
        if user.account_status == AccountStatus.DELETED:
            return False, "Account deleted", None
        
        return True, "Authentication successful", user
