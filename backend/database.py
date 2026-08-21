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
DATABASE_URL = os.getenv("DATABASE_URL")
DB_PASSWORD = os.getenv("DB_PASSWORD")

if DATABASE_URL and DB_PASSWORD and "@" in DATABASE_URL:
    # Check if there is already a password after the first colon but before the @
    # Format: postgresql://user[:password]@host
    protocol_part, remainder = DATABASE_URL.split("://", 1)
    userinfo, hostinfo = remainder.split("@", 1)

    if ":" not in userinfo:
        # No password in URL, inject it
        DATABASE_URL = f"{protocol_part}://{userinfo}:{quote_plus(DB_PASSWORD)}@{hostinfo}"
    else:
        # Password exists, but we want to use the CHANGED password from DB_PASSWORD if it's there
        user_part, _ = userinfo.split(":", 1)
        DATABASE_URL = f"{protocol_part}://{user_part}:{quote_plus(DB_PASSWORD)}@{hostinfo}"

if not DATABASE_URL:
    db_path = os.path.abspath(os.path.join(os.path.dirname(__file__), "agriconnect.db"))
    DATABASE_URL = f"sqlite:///{db_path}"

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