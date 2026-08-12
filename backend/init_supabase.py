from database import init_db
import sys

if __name__ == "__main__":
    print("Starting Supabase Database Initialization...")
    try:
        init_db()
        print("\nSUCCESS: All tables created in Supabase!")
        print("You can now see them in the Supabase Table Editor.")
    except Exception as e:
        print("\nERROR: Could not connect to Supabase.")
        print(f"Details: {e}")
        print("\nPlease check:")
        print("1. Is your DATABASE_URL correct in .env?")
        print("2. Did you run 'pip install psycopg2-binary'?")
        print("3. Does your Supabase password contain special characters? (If so, you might need to URL-encode them)")
        sys.exit(1)
