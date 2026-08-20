import os
from app.ai.whisper import WhisperSTT
from app.ai.gemma import GemmaBrain
from app.ai.f5tts import F5TTSAssistant
import torch

class AgriAssistant:
    def __init__(self, config):
        self.config = config
        self.stt = WhisperSTT(model_name=config.get("WHISPER_MODEL", "base"))
        self.brain = GemmaBrain(model_id=config.get("GEMMA_MODEL", "google/gemma-2b-it"))

        ckpt_path = config.get("TTS_CKPT_PATH")
        self.tts = F5TTSAssistant(ckpt_path=ckpt_path)

        self.ref_audio = config.get("VOICE_REFERENCE_AUDIO", "voice_reference.wav")
        self.ref_text = config.get("VOICE_REFERENCE_TEXT", "This is my reference voice.")

    async def handle_voice_input(self, audio_path, context):
        """
        Complete Pipeline: Audio -> Transcript -> Action -> Response -> Speech
        """
        # 1. Hearing (STT)
        transcript = self.stt.transcribe(audio_path)

        # 2. Thinking (Brain)
        ai_result = self.brain.process(transcript, context)

        # 3. Speaking (TTS) - only if there's a spoken response
        audio_url = None
        if ai_result.get("response") and ai_result.get("type") in ["SPOKEN_RESPONSE", "NAVIGATION", "FORM_UPDATE"]:
            output_filename = f"response_{os.urandom(4).hex()}.wav"
            output_path = os.path.join("app/static/audio", output_filename)
            os.makedirs(os.path.dirname(output_path), exist_ok=True)

            self.tts.generate_speech(
                ref_audio_path=self.ref_audio,
                ref_text=self.ref_text,
                gen_text=ai_result["response"],
                output_path=output_path
            )
            audio_url = f"/static/audio/{output_filename}"

        return {
            "transcript": transcript,
            "action": ai_result.get("action"),
            "type": ai_result.get("type"),
            "response": ai_result.get("response"),
            "arguments": ai_result.get("arguments", {}),
            "audio_url": audio_url
        }
