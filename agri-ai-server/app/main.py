import os
from fastapi import FastAPI, UploadFile, File, Form, WebSocket
from fastapi.staticfiles import StaticFiles
from app.assistant.agent import AgriAssistant
import json
import shutil

app = FastAPI(title="AgriConnect AI Server")

# Configuration
CONFIG = {
    "WHISPER_MODEL": "base",
    "GEMMA_MODEL": "google/gemma-2b-it",
    "TTS_CKPT_PATH": "C:/Users/ravi kiran/.cache/huggingface/hub/models--SWivid--F5-TTS/snapshots/84e5a410d9cead4de2f847e7c9369a6440bdfaca/F5TTS_Base/model_1200000.safetensors",
    "VOICE_REFERENCE_AUDIO": "voice_reference.wav",
    "VOICE_REFERENCE_TEXT": "This is my reference voice recording for custom speech output."
}

# Ensure directories exist
os.makedirs("app/static/audio", exist_ok=True)
os.makedirs("temp", exist_ok=True)

# Static files for audio access
app.mount("/static", StaticFiles(directory="app/static"), name="static")

# Initialize Assistant
# Lazy initialization or direct? Let's do direct for dev.
assistant = None

@app.on_event("startup")
async def startup_event():
    global assistant
    assistant = AgriAssistant(CONFIG)

@app.post("/assistant/voice")
async def voice_assistant(
    audio: UploadFile = File(...),
    context: str = Form(...)
):
    """
    HTTP Endpoint for voice interaction
    """
    context_data = json.loads(context)

    # Save uploaded audio
    temp_path = f"temp/{audio.filename}"
    with open(temp_path, "wb") as buffer:
        shutil.copyfileobj(audio.file, buffer)

    result = await assistant.handle_voice_input(temp_path, context_data)

    # Cleanup temp audio
    if os.path.exists(temp_path):
        os.remove(temp_path)

    return result

@app.websocket("/ws/assistant")
async def websocket_assistant(websocket: WebSocket):
    """
    WebSocket for real-time interaction
    """
    await websocket.accept()
    try:
        while True:
            # Receive context + audio blob?
            # For now, let's stick to HTTP for audio uploads and WS for state sync if needed.
            # Or handle binary data here.
            data = await websocket.receive_text()
            # ... implementation ...
            pass
    except Exception as e:
        print(f"WS error: {e}")
    finally:
        await websocket.close()

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5001)
