import whisper
import os

class WhisperSTT:
    def __init__(self, model_name="base"):
        print(f"Loading Whisper model: {model_name}...")
        self.model = whisper.load_model(model_name)
        print("Whisper loaded.")

    def transcribe(self, audio_path):
        if not os.path.exists(audio_path):
            raise FileNotFoundError(f"Audio file not found: {audio_path}")

        print(f"Transcribing: {audio_path}")
        result = self.model.transcribe(audio_path)
        return result["text"].strip()
