from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, Session
from sqlalchemy.pool import StaticPool
from models import Base
import os
from dotenv import load_dotenv
from urllib.parse import quote_plus

# Load environment variables from .env file
load_dotenv()

# Database URL
DATABASE_URL = os.getenv("DATABASE_URL", "").strip().strip("'").strip('"')
DB_PASSWORD = os.getenv("DB_PASSWORD", "").strip().strip("'").strip('"')

if DATABASE_URL and "@" in DATABASE_URL:
    try:
        # Robust parsing
        protocol_part, remainder = DATABASE_URL.split("://", 1)
        user_pass, host_info = remainder.rsplit("@", 1)

        if ":" in user_pass:
            current_user, current_pass = user_pass.split(":", 1)
        else:
            current_user = user_pass
            current_pass = ""

        # Override password if DB_PASSWORD is provided
        if DB_PASSWORD:
            current_pass = DB_PASSWORD

        # FIX: Ensure Supabase pooler username has the project ref
        # Project Ref: bgcgmmrmakgvuiozqvjy
        if "pooler.supabase.com" in host_info and "." not in current_user:
            current_user = f"{current_user}.bgcgmmrmakgvuiozqvjy"
            print(f"DATABASE CONFIG: Appending project ref to Supabase user -> {current_user}")

        # Rebuild URL with proper encoding
        from urllib.parse import quote
        DATABASE_URL = f"{protocol_part}://{quote(current_user)}:{quote(current_pass)}@{host_info}"

        print(f"DATABASE CONFIG: Connecting to {host_info.split(':')[0]} as user '{current_user}'")
    except Exception as e:
        print(f"DATABASE CONFIG: Error parsing URL: {e}")

if not DATABASE_URL or "sqlite" in DATABASE_URL:
    # Final fallback for Render if environment variables fail
    # Using the direct connection host which is often more reliable
    db_pass = quote("AgriConnect.123")
    DATABASE_URL = f"postgresql://postgres:{db_pass}@db.bgcgmmrmakgvuiozqvjy.supabase.co:5432/postgres?sslmode=require"
    print("DATABASE CONFIG: Using direct connection fallback")

if "sqlite" not in DATABASE_URL and DATABASE_URL.startswith("postgres://"):
    DATABASE_URL = DATABASE_URL.replace("postgres://", "postgresql://", 1)

# Create engine with appropriate options
engine_args = {}
if "sqlite" in DATABASE_URL:
    engine_args["connect_args"] = {"check_same_thread": False}
    engine_args["poolclass"] = StaticPool
else:
    # Postgres/Supabase settings
    engine_args["connect_args"] = {
        "sslmode": "require",
        "connect_timeout": 10
    }
    # For serverless/Render, it's often better to let SQLAlchemy handle pooling
    # unless using an external pooler like Supavisor (Port 6543)

engine = create_engine(DATABASE_URL, **engine_args)

# Create session factory
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Create all tables on startup
def init_db():
    """Initialize database - create all tables"""
    print(f"Connecting to database: {DATABASE_URL.split('@')[-1]}") # Log host safely
    Base.metadata.create_all(bind=engine)

    # Run custom migrations
    from migrate import migrate
    migrate()

    print("Database tables initialized successfully!")

# Dependency to get DB session
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()