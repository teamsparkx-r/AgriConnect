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
DATABASE_URL = os.getenv("DATABASE_URL", "").strip()
DB_PASSWORD = os.getenv("DB_PASSWORD", "").strip()

if DATABASE_URL and "@" in DATABASE_URL:
    # If a separate DB_PASSWORD is provided, override the one in the URL
    protocol_part, remainder = DATABASE_URL.split("://", 1)
    userinfo, hostinfo = remainder.split("@", 1)

    # Use DB_PASSWORD if it exists, otherwise keep existing password in userinfo
    current_user = userinfo.split(":", 1)[0]

    if DB_PASSWORD:
        # URL encode password to handle special characters like '.'
        DATABASE_URL = f"{protocol_part}://{current_user}:{quote_plus(DB_PASSWORD)}@{hostinfo}"

    print(f"DATABASE CONFIG: Using user '{current_user}' on host '{hostinfo.split(':')[0]}'")
elif not DATABASE_URL:
    # Final fallback for Render if environment variables fail
    DATABASE_URL = f"postgresql://postgres.bgcgmmrmakgvuiozqvjy:{quote_plus('AgriConnect.123')}@aws-0-ap-northeast-1.pooler.supabase.com:6543/postgres?sslmode=require"
    print("DATABASE CONFIG: Using hardcoded fallback credentials")

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