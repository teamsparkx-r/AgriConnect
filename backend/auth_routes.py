from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.orm import Session
from database import get_db
from auth import AuthService

router = APIRouter()

class LoginRequest(BaseModel):
    identity: str
    password: str

class OTPVerifyRequest(BaseModel):
    mobile_number: str
    otp_code: str

@router.post("/login")
def login(request: LoginRequest, db: Session = Depends(get_db)):
    """Authenticate user and return tokens directly (OTP disabled for now)."""
    success, message, user = AuthService.authenticate_user(
        db=db,
        identity=request.identity,
        password=request.password
    )
    if not success:
        raise HTTPException(status_code=401, detail=message)

    # Generate tokens directly
    access_token, _ = AuthService.create_access_token(user.id, user.role)
    refresh_token, _ = AuthService.create_refresh_token(user.id)

    return {
        "success": True,
        "message": "Login successful",
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

@router.post("/verify-otp")
def verify_otp(request: OTPVerifyRequest, db: Session = Depends(get_db)):
    """Verify OTP and authenticate user."""
    success, message, user = AuthService.authenticate_with_otp(
        db=db,
        mobile_number=request.mobile_number,
        otp_code=request.otp_code
    )

    if not success:
        raise HTTPException(status_code=401, detail=message)

    access_token, _ = AuthService.create_access_token(user.id, user.role)
    refresh_token, _ = AuthService.create_refresh_token(user.id)

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
        }
    }
