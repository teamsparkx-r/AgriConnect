from sqlalchemy import text
from database import engine

def migrate():
    """Manually add missing columns to the database if they don't exist"""
    print("Checking for database migrations...")

    with engine.connect() as conn:
        # 1. Check for requested_quantity in bookings
        try:
            conn.execute(text("SELECT requested_quantity FROM bookings LIMIT 1"))
            print("Column 'requested_quantity' already exists.")
        except Exception:
            print("Adding column 'requested_quantity' to 'bookings' table...")
            conn.execute(text("ALTER TABLE bookings ADD COLUMN requested_quantity FLOAT"))
            conn.commit()

        # 2. Check for negotiated_price in bookings
        try:
            conn.execute(text("SELECT negotiated_price FROM bookings LIMIT 1"))
            print("Column 'negotiated_price' already exists.")
        except Exception:
            print("Adding column 'negotiated_price' to 'bookings' table...")
            conn.execute(text("ALTER TABLE bookings ADD COLUMN negotiated_price FLOAT"))
            conn.commit()

    print("Migration check complete.")

if __name__ == "__main__":
    migrate()
