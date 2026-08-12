#!/usr/bin/env python3
"""
Quick setup and API verification script for AgriConnect
"""

import subprocess
import sys
import os
from pathlib import Path

def run_command(cmd, description):
    """Run a command and report status"""
    print(f"\n{'='*60}")
    print(f"📌 {description}")
    print(f"{'='*60}")
    print(f"$ {cmd}\n")
    
    try:
        result = subprocess.run(cmd, shell=True, check=True)
        print(f"✅ {description} - SUCCESS\n")
        return True
    except subprocess.CalledProcessError as e:
        print(f"❌ {description} - FAILED\n")
        return False

def main():
    print("""
    ╔════════════════════════════════════════════════════════════╗
    ║         AgriConnect - Platform Setup & Verification         ║
    ║              Free Farmer-Buyer Marketplace                  ║
    ╚════════════════════════════════════════════════════════════╝
    """)
    
    # Get backend directory
    backend_dir = Path(__file__).parent
    os.chdir(backend_dir)
    
    # Step 1: Check Python version
    print("\n1️⃣  Checking Python version...")
    if sys.version_info < (3, 9):
        print(f"❌ Python 3.9+ required. Current: {sys.version}")
        return False
    print(f"✅ Python version: {sys.version}")
    
    # Step 2: Create virtual environment if not exists
    venv_path = backend_dir / "venv"
    if not venv_path.exists():
        if not run_command("python -m venv venv", "Creating virtual environment"):
            return False
    else:
        print(f"\n✅ Virtual environment already exists at {venv_path}")
    
    # Step 3: Activate venv and install dependencies
    activate_cmd = "venv\\Scripts\\activate" if sys.platform == "win32" else "source venv/bin/activate"
    
    if not run_command(f"{activate_cmd} && pip install --upgrade pip", "Upgrading pip"):
        return False
    
    if not run_command(f"{activate_cmd} && pip install -r requriments.txt", "Installing dependencies"):
        return False
    
    # Step 4: Verify imports
    print(f"\n2️⃣  Verifying Python imports...")
    try:
        import fastapi
        import sqlalchemy
        import pydantic
        import passlib
        import jose
        print("✅ All required packages imported successfully")
    except ImportError as e:
        print(f"❌ Import error: {e}")
        return False
    
    # Step 5: Database initialization
    print(f"\n3️⃣  Initializing database...")
    try:
        from models import Base
        from database import engine
        Base.metadata.create_all(bind=engine)
        print("✅ Database tables created successfully")
    except Exception as e:
        print(f"❌ Database initialization failed: {e}")
        return False
    
    # Step 6: Final instructions
    print(f"""
    
    ╔════════════════════════════════════════════════════════════╗
    ║                  Setup Complete! 🎉                         ║
    ║                                                              ║
    ║  To start the server, run:                                 ║
    ║                                                              ║
    ║  Windows:                                                   ║
    ║    venv\\Scripts\\activate                                   ║
    ║    python -m uvicorn app:app --reload                      ║
    ║                                                              ║
    ║  Mac/Linux:                                                 ║
    ║    source venv/bin/activate                                ║
    ║    python -m uvicorn app:app --reload                      ║
    ║                                                              ║
    ║  Then open your browser:                                   ║
    ║                                                              ║
    ║  📚 API Docs:  http://localhost:8000/docs                  ║
    ║  📘 ReDoc:     http://localhost:8000/redoc                 ║
    ║  🏠 Home:      http://localhost:8000/                      ║
    ║  📊 Stats:     http://localhost:8000/api/stats             ║
    ║                                                              ║
    ║  Quick Test:                                                ║
    ║    python test_api.py  (if available)                      ║
    ║                                                              ║
    ╚════════════════════════════════════════════════════════════╝
    """)
    
    return True

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
