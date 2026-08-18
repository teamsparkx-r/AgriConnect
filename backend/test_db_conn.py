import sqlalchemy
from sqlalchemy import create_engine, text
import os

DATABASE_URL = "postgresql://postgres.bgcgmmrmakgvuiozqvjy:AgriConnect.123@aws-0-ap-northeast-1.pooler.supabase.com:6543/postgres"

try:
    engine = create_engine(DATABASE_URL)
    with engine.connect() as conn:
        res = conn.execute(text("SELECT 1"))
        print(f"Connection successful: {res.fetchone()}")
except Exception as e:
    print(f"Connection failed: {e}")
