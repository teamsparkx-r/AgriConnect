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

    # --- Postgres Specific: Convert ENUM columns to VARCHAR to avoid sync issues ---
    if "postgresql" in str(engine.url):
        print("Postgres detected. Ensuring columns are VARCHAR...")
        cols_to_fix = [
            ('users', 'role'),
            ('users', 'account_status'),
            ('farmers', 'preferred_language'),
            ('buyers', 'buyer_type'),
            ('buyers', 'preferred_language'),
            ('products', 'category'),
            ('products', 'status'),
            ('bookings', 'status'),
            ('negotiation_history', 'status'),
            ('reports', 'reason'),
            ('reports', 'status')
        ]
        with engine.connect() as conn:
            for table, col in cols_to_fix:
                try:
                    # Check if column is an ENUM (user-defined type)
                    # Using a more robust check for USER-DEFINED types in information_schema
                    # Also checking if it is 'ARRAY' or something else
                    res = conn.execute(text(f"SELECT data_type, udt_name FROM information_schema.columns WHERE table_name='{table}' AND column_name='{col}'")).fetchone()
                    if res:
                        print(f"Column {table}.{col} is type: {res[0]} (UDT: {res[1]})")
                        if res[0].upper() == 'USER-DEFINED' or 'enum' in res[1].lower():
                            print(f"Converting {table}.{col} from ENUM ({res[1]}) to VARCHAR...")
                            # Forces conversion and drops existing constraints linked to the Enum type
                            conn.execute(text(f"ALTER TABLE {table} ALTER COLUMN {col} TYPE VARCHAR(50) USING {col}::varchar"))
                            conn.commit()
                            print(f"Successfully converted {table}.{col}")
                except Exception as e:
                    print(f"Skipping conversion for {table}.{col}: {e}")
                    # conn.rollback() is handled by context or can be explicit

    print("Migration check complete.")

if __name__ == "__main__":
    migrate()
