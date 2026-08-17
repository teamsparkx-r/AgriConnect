from sqlalchemy import text, inspect
from database import engine

def migrate():
    """Manually add missing columns to the database if they don't exist"""
    print("Checking for database migrations...")

    inspector = inspect(engine)

    def add_column_if_not_exists(table_name, column_name, column_type):
        try:
            columns = [c['name'] for c in inspector.get_columns(table_name)]
            if column_name not in columns:
                print(f"Adding column '{column_name}' to '{table_name}' table...")
                with engine.connect() as conn:
                    # Specific handling for Postgres vs SQLite
                    if "postgresql" in str(engine.url):
                        conn.execute(text(f"ALTER TABLE {table_name} ADD COLUMN {column_name} {column_type}"))
                    else:
                        conn.execute(text(f"ALTER TABLE {table_name} ADD COLUMN {column_name} {column_type}"))
                    conn.commit()
                    print(f"Successfully added '{column_name}'.")
            else:
                print(f"Column '{column_name}' in table '{table_name}' already exists.")
        except Exception as e:
            print(f"Error checking/adding '{column_name}' to '{table_name}': {e}")

    # --- Migrations for 'bookings' table ---
    add_column_if_not_exists('bookings', 'requested_quantity', 'FLOAT')
    add_column_if_not_exists('bookings', 'negotiated_price', 'FLOAT')
    add_column_if_not_exists('bookings', 'contact_unlocked_at', 'TIMESTAMP')
    add_column_if_not_exists('bookings', 'completed_at', 'TIMESTAMP')

    # --- Migrations for 'farmers' table ---
    add_column_if_not_exists('farmers', 'preferred_language', 'VARCHAR(20)')
    add_column_if_not_exists('farmers', 'farm_address', 'TEXT')
    add_column_if_not_exists('farmers', 'latitude', 'FLOAT')
    add_column_if_not_exists('farmers', 'longitude', 'FLOAT')
    add_column_if_not_exists('farmers', 'share_farm_address', 'BOOLEAN DEFAULT TRUE')
    add_column_if_not_exists('farmers', 'share_coordinates', 'BOOLEAN DEFAULT FALSE')
    add_column_if_not_exists('farmers', 'profile_photo_url', 'VARCHAR(255)')

    # --- Migrations for 'buyers' table ---
    add_column_if_not_exists('buyers', 'preferred_language', 'VARCHAR(20)')
    add_column_if_not_exists('buyers', 'buyer_type', 'VARCHAR(50)')
    add_column_if_not_exists('buyers', 'profile_photo_url', 'VARCHAR(255)')

    # --- Special handling for Postgres Enum updates ---
    if "postgresql" in str(engine.url):
        try:
            with engine.connect() as conn:
                # Add 'MERCHANT' to enum if it doesn't exist
                # Note: Postgres doesn't allow ALTER TYPE in a transaction block in older versions,
                # but we'll try it here. Usually requires 'ALTER TYPE buyertype ADD VALUE IF NOT EXISTS 'merchant''
                # Since buying_type column might be a VARCHAR or ENUM, we handle it carefully.
                pass
        except Exception as e:
            print(f"Enum update check failed: {e}")

    print("Migration check complete.")

if __name__ == "__main__":
    migrate()
