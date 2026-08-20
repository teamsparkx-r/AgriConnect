import os
import sys

# Add app to path
sys.path.append(os.path.abspath("."))

from app.ai.whisper import WhisperSTT

def test_whisper():
    print("--- Whisper STT Test ---")
    stt = WhisperSTT()

    # Use the voice_reference.wav we just renamed
    audio_path = "voice_reference.wav"
    if not os.path.exists(audio_path):
        print(f"ERROR: Audio file not found at {audio_path}")
        return

    text = stt.transcribe(audio_path)
    print(f"Transcribed Text: {text}")

if __name__ == "__main__":
    test_whisper()
