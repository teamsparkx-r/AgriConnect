import os
import torch
import whisper
from fastapi import FastAPI, UploadFile, File, Body, APIRouter
from pydantic import BaseModel
from transformers import pipeline, AutoTokenizer, AutoModelForCausalLM
import json
from dotenv import load_dotenv
import shutil

load_dotenv()

app = FastAPI()
router = APIRouter()

# --- MODEL INITIALIZATION ---
print("Initializing AI Models... This may take a few minutes on first run.")

# 1. Load Whisper (Speech-to-Text)
# Options: tiny, base, small, medium, large
STT_MODEL_NAME = "base"
print(f"Loading Whisper model: {STT_MODEL_NAME}...")
stt_model = whisper.load_model(STT_MODEL_NAME)

# 2. Load Gemma (Intent Understanding)
# Note: google/gemma-2b-it is recommended for local development
# You may need to run `huggingface-cli login` once in your terminal
LLM_MODEL_ID = "google/gemma-2b-it"
print(f"Loading Gemma model: {LLM_MODEL_ID}...")

# Use CPU if no CUDA, otherwise use GPU
device = "cuda" if torch.cuda.is_available() else "cpu"

try:
    tokenizer = AutoTokenizer.from_pretrained(LLM_MODEL_ID)
    model = AutoModelForCausalLM.from_pretrained(
        LLM_MODEL_ID,
        torch_dtype=torch.float32 if device == "cpu" else torch.float16,
        device_map="auto" if device == "cuda" else None
    )
    print("Gemma loaded successfully.")
except Exception as e:
    print(f"Error loading Gemma: {e}")
    print("TIP: If this fails, ensure you have accepted the license on HuggingFace and run 'huggingface-cli login'.")
    model = None

# --- API MODELS ---

class AIContext(BaseModel):
    role: str
    current_screen: str
    user_id: str = None
    current_product_id: str = None

class AIRequest(BaseModel):
    text: str
    context: AIContext

# --- ENDPOINTS ---

@router.post("/ai/whisper")
async def transcribe(audio: UploadFile = File(...)):
    """Endpoint for Whisper transcription"""
    print(f"Received audio for transcription: {audio.filename}")
    # Save temp file
    temp_file = "temp_voice.m4a"
    with open(temp_file, "wb") as buffer:
        shutil.copyfileobj(audio.file, buffer)

    # Transcribe
    print("Transcribing with Whisper...")
    result = stt_model.transcribe(temp_file)
    os.remove(temp_file)
    print(f"Transcription complete: {result['text']}")

    return {"text": result["text"].strip()}

@router.post("/ai/process")
async def process_intent(request: AIRequest):
    """Endpoint for Gemma reasoning"""
    print(f"Processing text intent: {request.text}")

    # Mapping app intelligence
    routes_info = """
    Available Routes:
    - farmer/dashboard (Home for farmers)
    - farmer/products (View listed crops)
    - farmer/add-product (Start selling/listing)
    - farmer/bookings (View orders from merchants)
    - merchant/portal (Home for buyers)
    - merchant/explore (Search for crops/market)
    - merchant/bookings (My purchase orders)
    - merchant/saved (Wishlist)
    """

    prompt = f"""
    <start_of_turn>user
    You are 'AgriConnect Mitra', a very friendly and helpful friend for farmers.
    You MUST speak in TELUGU language (using Telugu script).
    Tone: Rural, warm, empathetic, and expert. Use phrases like 'రైతు సోదరా' (farmer brother) or 'మిత్రమా'.

    Farming Knowledge Base:
    - Paddy (వరి): Needs 120-150 days. Suggest checking for stem borer (మొవ్వు తొలిచే పురుగు).
    - Cotton (పత్తి): Needs black soil. Suggest pest control for pink bollworm (గులాబీ రంగు పురుగు).
    - Chilli (మిర్చి): Advise on high-quality seeds and proper irrigation.
    - Weather: Always suggest avoiding chemical sprays if rain is expected in 24 hours.

    {routes_info}

    Context:
    - User is a: {request.context.role}
    - Current Screen: {request.context.current_screen}

    User Request: "{request.text}"

    Task:
    1. If user asks for advice (e.g., "how to grow paddy"), provide expertise in Telugu and use action: EXPLAIN.
    2. For navigation/market tasks, use NAVIGATE, SEARCH, CHECK_PRICE.
    3. Always keep the response in Telugu.

    JSON Format Example:
    {{
      "action": "EXPLAIN",
      "target": null,
      "params": {{}},
      "response_text": "రైతు సోదరా, వరి సాగులో నీటి యాజమాన్యం చాలా ముఖ్యం. పొలంలో ఎప్పుడూ ఒక అంగుళం నీరు ఉండేలా చూసుకోండి.",
      "requires_confirmation": false
    }}

    JSON Output:
    <end_of_turn>
    <start_of_turn>model
    """

    if model is None:
        print("Gemma model is NULL, returning fallback response")
        return {
            "action": "EXPLAIN",
            "response_text": f"క్షమించండి మిత్రమా, నా ఆలోచనా శక్తి ఇంకా పూర్తిగా సిద్ధం కాలేదు. కానీ నీవు అన్నది నాకు వినిపించింది: '{request.text}'",
            "requires_confirmation": false
        }

    print("Generating AI response with Gemma...")
    inputs = tokenizer(prompt, return_tensors="pt").to(device)
    outputs = model.generate(**inputs, max_new_tokens=150)
    response_full = tokenizer.decode(outputs[0], skip_special_tokens=True)

    # Extract JSON part (simple extraction for local dev)
    try:
        json_start = response_full.find("{")
        json_end = response_full.rfind("}") + 1
        json_str = response_full[json_start:json_end]
        response_data = json.loads(json_str)
        print(f"AI Response success: {response_data['action']}")
        return response_data
    except Exception as e:
        print(f"JSON Parsing failed: {e}")
        print(f"Raw model response: {response_full}")
        return {
            "action": "EXPLAIN",
            "response_text": "మిత్రమా, నీవు అన్నది నాకు అర్థమైంది కానీ నా సమాధానాన్ని సరిగ్గా రూపకల్పన చేయలేకపోయాను. దయచేసి మళ్ళీ అడుగు.",
            "requires_confirmation": false
        }

app.include_router(router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)
