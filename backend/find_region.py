import socket
import psycopg2
from urllib.parse import quote_plus

def check_pooler():
    project_id = "bgcgmmrmakgvuiozqvjy"
    password = "AgriConnect.123"
    # Common Supabase regions
    regions = [
        "ap-south-1",        # Mumbai
        "ap-southeast-1",    # Singapore
        "us-east-1",         # N. Virginia
        "eu-central-1",      # Frankfurt
        "ap-northeast-1"     # Tokyo
    ]

    print(f"Searching for IPv4 Pooler for project: {project_id}...")

    for region in regions:
        host = f"aws-0-{region}.pooler.supabase.com"
        print(f"Checking {region} ({host})...", end=" ", flush=True)
        try:
            # First check if the host exists
            socket.gethostbyname(host)

            # Try connecting (Transaction mode - port 6543 is safer)
            user = f"postgres.{project_id}"
            url = f"postgresql://{user}:{quote_plus(password)}@{host}:6543/postgres"

            conn = psycopg2.connect(url, connect_timeout=3)
            print("SUCCESS!")
            return url
        except socket.gaierror:
            print("Host not found.")
        except Exception as e:
            if "FATAL" in str(e) and "not found" in str(e):
                print("Region mismatch.")
            else:
                print(f"Found host, but error: {str(e)[:50]}")

    return None

if __name__ == "__main__":
    found_url = check_pooler()
    if found_url:
        print("\n" + "="*60)
        print("FOUND CORRECT CONNECTION! Update your .env with this:")
        print(f"DATABASE_URL={found_url}")
        print("="*60)
    else:
        print("\nCould not find a working IPv4 pooler.")
        print("Please log into Supabase -> Settings -> Database")
        print("Look for the 'Connection Pooler' section and copy the 'URI'.")
