from sqlalchemy import text, inspect
from database import engine

def migrate():
    """Manually add missing columns to the database if they don't exist"""
    print("Checking for database migrations...")

    # Use inspector to check columns without triggering SQL transaction errors
    inspector = inspect(engine)
    try:
        columns = [c['name'] for c in inspector.get_columns('bookings')]
    except Exception as e:
        print(f"Could not inspect 'bookings' table: {e}")
        return

    with engine.connect() as conn:
        # 1. Check for requested_quantity
        if 'requested_quantity' not in columns:
            print("Adding column 'requested_quantity' to 'bookings' table...")
            try:
                conn.execute(text("ALTER TABLE bookings ADD COLUMN requested_quantity FLOAT"))
                conn.commit()
                print("Successfully added 'requested_quantity'.")
            except Exception as e:
                print(f"Error adding 'requested_quantity': {e}")
                conn.rollback()
        else:
            print("Column 'requested_quantity' already exists.")

        # 2. Check for negotiated_price
        if 'negotiated_price' not in columns:
            print("Adding column 'negotiated_price' to 'bookings' table...")
            try:
                conn.execute(text("ALTER TABLE bookings ADD COLUMN negotiated_price FLOAT"))
                conn.commit()
                print("Successfully added 'negotiated_price'.")
            except Exception as e:
                print(f"Error adding 'negotiated_price': {e}")
                conn.rollback()
        else:
            print("Column 'negotiated_price' already exists.")

    print("Migration check complete.")

if __name__ == "__main__":
    migrate()
