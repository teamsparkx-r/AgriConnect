import torch
from transformers import AutoTokenizer, AutoModelForCausalLM
import json

class GemmaBrain:
    def __init__(self, model_id="google/gemma-2b-it"):
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        print(f"Loading Gemma model: {model_id} on {self.device}...")

        try:
            self.tokenizer = AutoTokenizer.from_pretrained(model_id)
            self.model = AutoModelForCausalLM.from_pretrained(
                model_id,
                torch_dtype=torch.float32 if self.device == "cpu" else torch.float16,
                device_map="auto" if self.device == "cuda" else None
            )
            print("Gemma loaded successfully.")
        except Exception as e:
            print(f"Error loading Gemma: {e}")
            self.model = None

    def process(self, text, context_data):
        """
        Process user text with screen-aware context.
        """
        if self.model is None:
            return {
                "type": "SPOKEN_RESPONSE",
                "action": "EXPLAIN",
                "response": "క్షమించండి మిత్రమా, నా మెదడు ఇంకా పూర్తిగా సిద్ధం కాలేదు."
            }

        # Construct a rich prompt based on context
        role = context_data.get("role", "FARMER")
        screen = context_data.get("screen", "HOME")
        fields = context_data.get("fields", {})
        available_actions = context_data.get("available_actions", [])

        prompt = f"""
        <start_of_turn>user
        You are 'AgriConnect Mitra'. You MUST respond with ONLY a JSON object.
        NO extra text before or after the JSON.

        Context:
        - Role: {role}
        - Screen: {screen}
        - Current Data: {json.dumps(fields)}
        - Actions: {json.dumps(available_actions)}

        User request: "{text}"

        Your JSON response must follow this EXACT format:
        {{
            "type": "SPOKEN_RESPONSE",
            "action": "ACTION_NAME",
            "response": "Natural Telugu response script here",
            "arguments": {{}}
        }}

        JSON Output:
        <end_of_turn>
        <start_of_turn>model
        """

        inputs = self.tokenizer(prompt, return_tensors="pt").to(self.device)
        outputs = self.model.generate(
            **inputs,
            max_new_tokens=300,
            do_sample=True,
            temperature=0.1,
            top_p=0.95
        )
        response_full = self.tokenizer.decode(outputs[0], skip_special_tokens=True)
        print(f"Raw Gemma response: {response_full}")

        try:
            # Look for everything between the first '{' and the last '}' in the model's turn
            # But specifically look AFTER the prompt.
            model_marker = "model"
            model_idx = response_full.find(model_marker)
            if model_idx != -1:
                search_space = response_full[model_idx + len(model_marker):]
            else:
                search_space = response_full

            json_start = search_space.find("{")
            json_end = search_space.rfind("}") + 1

            if json_start != -1 and json_end > json_start:
                json_str = search_space[json_start:json_end]
                # Clean up any potential markdown code blocks
                json_str = json_str.replace("```json", "").replace("```", "").strip()
                return json.loads(json_str)
            else:
                raise ValueError("No JSON block found in response")
        except Exception as e:
            print(f"Failed to parse Gemma output: {e}")
            if 'json_str' in locals():
                 print(f"Cleaned JSON attempted: {json_str}")
            return {
                "type": "SPOKEN_RESPONSE",
                "action": "EXPLAIN",
                "response": "మిత్రమా, మీరు చెప్పింది నాకు అర్థమైంది కానీ నా సమాధానాన్ని సరిగ్గా రూపకల్పన చేయలేకపోయాను."
            }
