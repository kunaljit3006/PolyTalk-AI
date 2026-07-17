from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import torch
from transformers import AutoTokenizer, AutoModelForSeq2SeqLM, VitsModel
from peft import PeftModel
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from gtts import gTTS
import io
import os
import scipy.io.wavfile

# Lazy loaded TTS models
mms_tts_models = {}
mms_tts_tokenizers = {}

def get_mms_tts_model(lang_code):
    if lang_code not in mms_tts_models:
        model_id = f"facebook/mms-tts-{lang_code}"
        mms_tts_models[lang_code] = VitsModel.from_pretrained(model_id).to(device)
        mms_tts_tokenizers[lang_code] = AutoTokenizer.from_pretrained(model_id)
    return mms_tts_models[lang_code], mms_tts_tokenizers[lang_code]

app = FastAPI(title="PolyTalk AI Translation API")

# Allow the Android App to make requests to this API (CORS)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global variables to hold the loaded model in memory
tokenizer = None
model = None

# Hugging Face Free Spaces use CPUs
device = "cpu" 

@app.on_event("startup")
async def load_model():
    global tokenizer, model
    print("Downloading and loading the model into memory. This may take a few minutes on first boot...")
    
    model_id = "facebook/nllb-200-1.3B"
    adapter_id = "heykunal123/polytalk-ai-lora-nllb-1.3b"
    
    try:
        # Load tokenizer
        tokenizer = AutoTokenizer.from_pretrained(model_id)
        
        # Load base model (16GB RAM is plenty for a 1.3B model on CPU)
        base_model = AutoModelForSeq2SeqLM.from_pretrained(model_id, device_map=device)
        
        # Attach your fine-tuned LoRA adapter
        model = PeftModel.from_pretrained(base_model, adapter_id)
        model.eval()
        print("✅ PolyTalk AI Model successfully loaded and ready for translations!")
    except Exception as e:
        print(f"Error loading model: {e}")

# Pydantic schema to strictly define what the Android App must send
class TranslationRequest(BaseModel):
    text: str
    src_lang: str
    tgt_lang: str

@app.get("/")
def health_check():
    return {
        "status": "online", 
        "message": "PolyTalk AI Backend is running!",
        "model": "heykunal123/polytalk-ai-lora-nllb-1.3b",
        "ready": model is not None
    }

# Provide the list of all 18 fine-tuned languages so the Android App can build its Dropdown Menus
@app.get("/languages")
def get_languages():
    return {
        "supported_languages": [
            {"name": "English", "code": "eng_Latn"},
            {"name": "Hindi", "code": "hin_Deva"},
            {"name": "Tamil", "code": "tam_Taml"},
            {"name": "Telugu", "code": "tel_Telu"},
            {"name": "Bengali", "code": "ben_Beng"},
            {"name": "Marathi", "code": "mar_Deva"},
            {"name": "Gujarati", "code": "guj_Gujr"},
            {"name": "Kannada", "code": "kan_Knda"},
            {"name": "Malayalam", "code": "mal_Mlym"},
            {"name": "Punjabi", "code": "pan_Guru"},
            {"name": "Odia", "code": "ory_Orya"},
            {"name": "Assamese", "code": "asm_Beng"},
            {"name": "French", "code": "fra_Latn"},
            {"name": "Spanish", "code": "spa_Latn"},
            {"name": "German", "code": "deu_Latn"},
            {"name": "Italian", "code": "ita_Latn"},
            {"name": "Russian", "code": "rus_Cyrl"},
            {"name": "Japanese", "code": "jpn_Jpan"}
        ]
    }

# Mapping of NLLB language tags to 2-letter ISO language codes for gTTS
nllb_to_gtts = {
    "eng_Latn": "en",
    "hin_Deva": "hi",
    "tam_Taml": "ta",
    "tel_Telu": "te",
    "ben_Beng": "bn",
    "mar_Deva": "mr",
    "guj_Gujr": "gu",
    "kan_Knda": "kn",
    "mal_Mlym": "ml",
    "pan_Guru": "pa",
    "ory_Orya": "ori", # Will be handled by MMS
    "asm_Beng": "asm", # Will be handled by MMS
    "fra_Latn": "fr",
    "spa_Latn": "es",
    "deu_Latn": "de",
    "ita_Latn": "it",
    "rus_Cyrl": "ru",
    "jpn_Jpan": "ja"
}

@app.get("/tts")
async def text_to_speech(text: str, lang: str):
    gtts_lang = nllb_to_gtts.get(lang, "en")
    
    try:
        if gtts_lang in ["asm", "ori"]:
            # Use Meta's native MMS TTS models for Assamese and Odia
            model_tts, tokenizer_tts = get_mms_tts_model(gtts_lang)
            inputs = tokenizer_tts(text, return_tensors="pt").to(device)
            with torch.no_grad():
                output = model_tts(**inputs).waveform
            
            fp = io.BytesIO()
            # Convert PyTorch tensor to numpy array for scipy
            audio_data = output.cpu().numpy().squeeze()
            scipy.io.wavfile.write(fp, rate=model_tts.config.sampling_rate, data=audio_data)
            fp.seek(0)
            return StreamingResponse(fp, media_type="audio/wav")
        else:
            # Generate speech in memory and stream the MP3 payload using gTTS
            tts = gTTS(text=text, lang=gtts_lang)
            fp = io.BytesIO()
            tts.write_to_fp(fp)
            fp.seek(0)
            return StreamingResponse(fp, media_type="audio/mpeg")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Text-to-Speech generation failed: {str(e)}")

@app.post("/translate")
async def translate(req: TranslationRequest):
    if not model or not tokenizer:
        raise HTTPException(status_code=503, detail="Model is still initializing. Please wait a moment.")
    
    try:
        # Set the source language
        tokenizer.src_lang = req.src_lang
        inputs = tokenizer(req.text, return_tensors="pt").to(device)
        
        # Get the ID for the target language
        target_lang_id = tokenizer.convert_tokens_to_ids(req.tgt_lang)
        
        # Generate the translation
        with torch.no_grad():
            outputs = model.generate(
                **inputs, 
                forced_bos_token_id=target_lang_id, 
                max_new_tokens=128
            )
        
        # Decode the output tokens back into text
        translated_text = tokenizer.decode(outputs[0], skip_special_tokens=True)
        
        return {
            "status": "success",
            "original_text": req.text,
            "src_lang": req.src_lang,
            "tgt_lang": req.tgt_lang,
            "translated_text": translated_text
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Translation failed: {str(e)}")
