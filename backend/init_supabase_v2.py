import socket
import psycopg2
from database import init_db
import os
from dotenv import load_dotenv

def get_ip_from_dns(hostname):
    # Try to resolve hostname using system first
    try:
        return socket.gethostbyname(hostname)
    except:
        print(f"System DNS failed for {hostname}. Trying hardcoded IP check...")
        # These are common Supabase IPv4s for certain regions, but not guaranteed
        return None

def run_init():
    load_dotenv()
    url = os.getenv("DATABASE_URL")
    print(f"Original URL: {url.split('@')[-1]}")

    # Force the hostname to resolve if possible
    try:
        init_db()
        print("SUCCESS!")
    except Exception as e:
        print(f"FAILED: {e}")

        if "could not translate host name" in str(e):
            print("\nDNS FAILURE DETECTED.")
            print("Your internet connection cannot find 'db.bgcgmmrmakgvuiozqvjy.supabase.co'.")
            print("1. Try switching to a Mobile Hotspot.")
            print("2. Try changing your PC DNS to 8.8.8.8 (Google).")
            print("3. Check if your Supabase project is Paused.")

if __name__ == "__main__":
    run_init()
