import os
import psycopg2
from dotenv import load_dotenv
from urllib.parse import quote_plus

def try_connections():
    project_id = "bgcgmmrmakgvuiozqvjy"
    password = "AgriConnect.123"

    # List of possible connection strings
    options = [
        # Direct IPv6 (if supported)
        f"postgresql://postgres:{quote_plus(password)}@[2406:da14:1d4f:7400:85c0:e60a:dba6:c110]:5432/postgres",
        # Standard Direct
        f"postgresql://postgres:{quote_plus(password)}@db.{project_id}.supabase.co:5432/postgres",
        # Pooler Session
        f"postgresql://postgres.{project_id}:{quote_plus(password)}@aws-0-ap-south-1.pooler.supabase.com:5432/postgres",
        # Pooler Transaction
        f"postgresql://postgres.{project_id}:{quote_plus(password)}@aws-0-ap-south-1.pooler.supabase.com:6543/postgres"
    ]

    for url in options:
        print(f"Trying: {url.split('@')[-1]}...")
        try:
            conn = psycopg2.connect(url, connect_timeout=5)
            print("SUCCESS!! Connection established.")
            conn.close()
            return url
        except Exception as e:
            print(f"Failed: {str(e)[:100]}")

    return None

if __name__ == "__main__":
    result = try_connections()
    if result:
        print("\n" + "="*50)
        print("COPY THIS TO YOUR .ENV FILE:")
        print(f"DATABASE_URL={result}")
        print("="*50)
    else:
        print("\nALL ATTEMPTS FAILED.")
        print("Please check if your Supabase project is active and if your IP is allowed in settings.")
