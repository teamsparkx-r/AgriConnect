import os
import sys
import json

# Add app to path
sys.path.append(os.path.abspath("."))

from app.ai.gemma import GemmaBrain

def test_phase_2():
    print("--- Phase 2: Gemma Reasoning Test ---")
    brain = GemmaBrain()

    context = {
        "role": "FARMER",
        "screen": "HOME",
        "fields": {},
        "available_actions": ["OPEN_ADD_CROP", "VIEW_ORDERS"]
    }

    text = "I want to add a crop"
    result = brain.process(text, context)

    print(f"User: {text}")
    print(f"AI Result: {json.dumps(result, indent=2, ensure_ascii=False)}")

if __name__ == "__main__":
    test_phase_2()
