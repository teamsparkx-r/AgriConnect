import psycopg2
import os
from dotenv import load_dotenv

load_dotenv()

def test_raw_conn():
    # Example format: postgresql://postgres:[PASSWORD]@[HOST]:5432/postgres
    full_url = os.getenv("DATABASE_URL")
    print(f"Testing URL: {full_url.split('@')[-1] if full_url else 'None'}")

    try:
        # Try connecting with the full URL
        conn = psycopg2.connect(full_url)
        print("SUCCESS: Connected to Supabase!")
        conn.close()
    except Exception as e:
        print(f"FAILED: {e}")
        print("\nTIP: If your password has special characters like @, #, or !,")
        print("the URL gets confused. Let's try connecting with individual parts instead.")

        # YOU CAN EDIT THESE TO MATCH YOUR SUPABASE CREDENTIALS
        db_user = "postgres"
        db_password = "AgriConnect123."
        db_host = "db.bgcgmmrmakgvuiozqvjy.supabase.co"
        db_port = "5432"
        db_name = "postgres"

        if db_password != "REPLACE_WITH_YOUR_ACTUAL_PASSWORD":
            print(f"\nTrying direct connection to {db_host}...")
            try:
                conn = psycopg2.connect(
                    dbname=db_name,
                    user=db_user,
                    password=db_password,
                    host=db_host,
                    port=db_port
                )
                print("SUCCESS: Direct connection worked!")
                print("\nUSE THIS URL IN YOUR .ENV (Encoded):")
                from urllib.parse import quote_plus
                encoded_pass = quote_plus(db_password)
                print(f"DATABASE_URL=postgresql://{db_user}:{encoded_pass}@{db_host}:{db_port}/{db_name}")
                conn.close()
            except Exception as e2:
                print(f"STILL FAILED: {e2}")

if __name__ == "__main__":
    test_raw_conn()
