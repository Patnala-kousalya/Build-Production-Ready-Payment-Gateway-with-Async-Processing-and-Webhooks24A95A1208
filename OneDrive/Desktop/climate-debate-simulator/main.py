from fastapi import FastAPI, HTTPException
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel
import json
from agents.debater import generate_agent_response
from core.rag_service import load_policies

app = FastAPI()

# Load RAG data at startup
load_policies()

# Serve frontend
app.mount("/", StaticFiles(directory="static", html=True), name="static")


# -------------------------
# Health Endpoint
# -------------------------
@app.get("/health")
def health():
    return {"status": "ok"}


# -------------------------
# Policy Retrieval Endpoint
# -------------------------
@app.get("/policies/{country_code}")
def get_policy(country_code: str):
    try:
        file_path = f"data/policies/{country_code}_policy.json"

        with open(file_path, "r") as f:
            data = json.load(f)

        return data

    except FileNotFoundError:
        raise HTTPException(status_code=404, detail="Policy not found")


# -------------------------
# Debate Request Model
# -------------------------
class DebateRequest(BaseModel):
    topic: str
    rounds: int


# -------------------------
# Debate Endpoint
# -------------------------
@app.post("/debate/start")
def start_debate(request: DebateRequest):

    if request.rounds < 1 or request.rounds > 5:
        raise HTTPException(status_code=400, detail="Rounds must be between 1 and 5")

    agents = ["USA", "EU", "China"]
    history = ""
    messages = []

    for r in range(1, request.rounds + 1):
        for agent in agents:

            result = generate_agent_response(agent, request.topic, history)

            message_object = {
                "round": r,
                "agent": agent,
                "message": result["message"],
                "stance": result["stance"],
                "timestamp": result["timestamp"]
            }

            history += f"{agent}: {result['message']}\n"
            messages.append(message_object)

    return {"messages": messages}