from sqlalchemy import text, inspect
from database import engine

def migrate():
    """Manually add missing columns and fix Postgres Enum conflicts"""
    print("Checking for database migrations...")

    inspector = inspect(engine)

    def add_column_if_not_exists(table_name, column_name, column_type):
        try:
            columns = [c['name'] for c in inspector.get_columns(table_name)]
            if column_name not in columns:
                print(f"Adding column '{column_name}' to '{table_name}' table...")
                with engine.begin() as conn: # Use begin() for automatic commit
                    conn.execute(text(f"ALTER TABLE {table_name} ADD COLUMN {column_name} {column_type}"))
                    print(f"Successfully added '{column_name}'.")
            else:
                # Still check if it needs conversion from Enum to Varchar if it already exists
                pass
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

    # --- Postgres Specific: Convert ENUM columns to VARCHAR to avoid strictness ---
    if "postgresql" in str(engine.url):
        print("Postgres detected. Ensuring columns are standard VARCHAR strings...")
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

        # We perform these outside engine.begin() if they might fail individually
        with engine.connect() as conn:
            for table, col in cols_to_fix:
                try:
                    # Check the current data type
                    sql = text(f"SELECT data_type FROM information_schema.columns WHERE table_name='{table}' AND column_name='{col}'")
                    res = conn.execute(sql).fetchone()

                    if res:
                        data_type = res[0].upper()
                        # If it's not already a variant of character/text, force convert it
                        if data_type not in ['CHARACTER VARYING', 'VARCHAR', 'TEXT', 'CHARACTER']:
                            print(f"FORCING CONVERSION: {table}.{col} (Current Type: {data_type}) -> VARCHAR(50)")
                            # Drop any default constraints first to be safe
                            conn.execute(text(f"ALTER TABLE {table} ALTER COLUMN {col} DROP DEFAULT"))
                            # Perform the conversion
                            conn.execute(text(f"ALTER TABLE {table} ALTER COLUMN {col} TYPE VARCHAR(50) USING {col}::varchar"))
                            # Re-add a string-based default if it's account_status
                            if col == 'account_status':
                                conn.execute(text(f"ALTER TABLE {table} ALTER COLUMN {col} SET DEFAULT 'pending'"))

                            conn.commit()
                            print(f"SUCCESS: Converted {table}.{col} to VARCHAR")
                except Exception as e:
                    print(f"Conversion failed for {table}.{col}: {e}")
                    # Don't rollback the whole connection, just continue
                    pass

    print("Migration check complete.")

if __name__ == "__main__":
    migrate()
