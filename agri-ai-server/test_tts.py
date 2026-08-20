import os
import sys

# Add app to path
sys.path.append(os.path.abspath("."))

from app.ai.f5tts import F5TTSAssistant

def test_phase_1():
    ref_audio = "voice_reference.wav"
    if not os.path.exists(ref_audio):
        print(f"ERROR: Reference audio not found at {ref_audio}")
        print("Please place a clear recording of your voice named 'voice_reference.wav' in the root folder.")
        return

    print("--- Phase 1: Standalone F5-TTS Test ---")
    try:
        model_path = "C:/Users/ravi kiran/.cache/huggingface/hub/models--SWivid--F5-TTS/snapshots/84e5a410d9cead4de2f847e7c9369a6440bdfaca/F5TTS_Base/model_1200000.safetensors"
        tts = F5TTSAssistant(ckpt_path=model_path)
        tts.generate_speech(
            ref_audio_path=ref_audio,
            ref_text="This is my reference voice recording.",
            gen_text="Hello.",
            output_path="test_output.wav"
        )
        print("DONE! Check 'test_output.wav' to hear your cloned voice.")
    except Exception as e:
        print(f"CRITICAL ERROR: {e}")
        if "ffmpeg" in str(e).lower() or "ffprobe" in str(e).lower():
            print("\nTIP: It looks like FFmpeg is missing. Please install FFmpeg and add it to your PATH.")

if __name__ == "__main__":
    test_phase_1()
