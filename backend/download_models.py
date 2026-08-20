import whisper
import torch
from transformers import AutoTokenizer, AutoModelForCausalLM

def download():
    print("--- Starting Model Downloads ---")

    # 1. Download Whisper
    print("Downloading Whisper 'base' model...")
    whisper.load_model("base")
    print("Whisper downloaded.")

    # 2. Download Gemma
    # Note: Requires HF login if gated
    model_id = "google/gemma-2b-it"
    print(f"Downloading Gemma '{model_id}' (approx 5GB)...")
    try:
        AutoTokenizer.from_pretrained(model_id)
        AutoModelForCausalLM.from_pretrained(model_id)
        print("Gemma downloaded.")
    except Exception as e:
        print(f"Gemma download failed: {e}")
        print("Please ensure you have run 'huggingface-cli login' and accepted the model terms at: https://huggingface.co/google/gemma-2b-it")

if __name__ == "__main__":
    download()
