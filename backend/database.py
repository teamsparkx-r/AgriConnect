from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, Session
from sqlalchemy.pool import StaticPool
from models import Base
import os
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

# Database URL
DATABASE_URL = os.getenv("DATABASE_URL")
if not DATABASE_URL:
    db_path = os.path.abspath(os.path.join(os.path.dirname(__file__), "agriconnect.db"))
    DATABASE_URL = f"sqlite:///{db_path}"

# Supabase fix: SQLAlchemy requires postgresql:// instead of postgres://
if DATABASE_URL.startswith("postgres://"):
    DATABASE_URL = DATABASE_URL.replace("postgres://", "postgresql://", 1)

# Create engine with appropriate options
engine = create_engine(
    DATABASE_URL,
    # SSL mode is often required for Supabase
    connect_args={"check_same_thread": False} if "sqlite" in DATABASE_URL else {"sslmode": "require"},
    poolclass=StaticPool if "sqlite" in DATABASE_URL else None,
)

# Create session factory
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Create all tables on startup
def init_db():
    """Initialize database - create all tables"""
    print(f"Connecting to database: {DATABASE_URL.split('@')[-1]}") # Log host safely
    Base.metadata.create_all(bind=engine)
    print("Database tables initialized successfully!")

# Dependency to get DB session
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()