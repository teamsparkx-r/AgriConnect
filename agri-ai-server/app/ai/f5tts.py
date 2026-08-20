import os
import torch
import torchaudio
import numpy as np
from f5_tts.model import DiT
from f5_tts.infer.utils_infer import load_model, load_vocoder, preprocess_ref_audio_text, infer_process
from f5_tts.model.utils import get_tokenizer

class F5TTSAssistant:
    def __init__(self, model_type="F5-TTS", ckpt_path=None, vocab_file="", device=None):
        self.device = device or ("cuda" if torch.cuda.is_available() else "cpu")
        self.model_type = model_type

        # Default paths or provided ones
        self.ckpt_path = ckpt_path
        self.vocab_file = vocab_file

        print(f"Initializing {model_type} on {self.device}...")

        # Load model and vocoder
        # Note: The f5-tts library might handle automatic downloading if paths are None
        self.ema_model = load_model(
            model_cls=DiT,
            model_cfg=dict(dim=1024, depth=22, heads=16, ff_mult=2, text_dim=512, conv_layers=4),
            ckpt_path=self.ckpt_path,
            vocab_file=self.vocab_file,
            device=self.device
        )
        self.vocoder = load_vocoder(is_local=False, device=self.device)
        print(f"{model_type} initialized successfully.")

    def generate_speech(self, ref_audio_path, ref_text, gen_text, output_path):
        """
        Generate speech using a reference audio and text.
        """
        print(f"Generating speech for: {gen_text}")

        # Prepare reference audio
        ref_audio, ref_text = preprocess_ref_audio_text(ref_audio_path, ref_text)

        # Inference
        print("Starting F5-TTS Inference (this may take a few minutes on CPU)...")
        audio, sr, spectr = infer_process(
            ref_audio,
            ref_text,
            gen_text,
            self.ema_model,
            self.vocoder,
            cross_fade_duration=0.15,
            speed=1.0,
            device=self.device
        )

        # Save output
        torchaudio.save(output_path, torch.from_numpy(audio).unsqueeze(0), sr)
        print(f"Audio saved to: {output_path}")
        return output_path

if __name__ == "__main__":
    # Test script for Phase 1
    # User needs to provide 'voice_reference.wav'
    ref_audio = "../../voice_reference.wav"
    if not os.path.exists(ref_audio):
        print(f"ERROR: Reference audio not found at {ref_audio}")
        print("Please place a clear recording of your voice named 'voice_reference.wav' in the root folder.")
    else:
        tts = F5TTSAssistant()
        tts.generate_speech(
            ref_audio_path=ref_audio,
            ref_text="This is my reference voice recording.", # User should ideally provide the text of their reference
            gen_text="Okay, let's add your crop. What crop are you selling?",
            output_path="../../test_output.wav"
        )
