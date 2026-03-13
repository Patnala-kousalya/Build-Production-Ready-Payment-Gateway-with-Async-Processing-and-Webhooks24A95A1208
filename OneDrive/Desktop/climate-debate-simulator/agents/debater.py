import os
import requests
from datetime import datetime
from core.rag_service import retrieve_relevant_points

OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL")
LLM_MODEL_NAME = os.getenv("LLM_MODEL_NAME")


def generate_agent_response(country: str, topic: str, history: str):
    """
    Generate a debate response for a specific country.
    """

    # Retrieve relevant policy points using RAG
    policy_points = retrieve_relevant_points(country, topic)

    policy_text = "\n".join(policy_points)

    prompt = f"""
You are the official climate policy debate representative for {country}.

Debate Topic:
{topic}

Debate History:
{history}

You must base your response ONLY on the following official policy points:
{policy_text}

Instructions:
- Respond in ONE clear paragraph.
- Stay strictly on topic.
- At the very end, explicitly conclude with one word:
supportive OR opposed OR neutral
"""

    try:
        response = requests.post(
            f"{OLLAMA_BASE_URL}/api/generate",
            json={
                "model": LLM_MODEL_NAME,
                "prompt": prompt,
                "stream": False
            },
            timeout=120
        )

        result_text = response.json().get("response", "").strip()

    except Exception as e:
        result_text = f"Error generating response: {str(e)}"

    # Determine stance safely
    stance = "neutral"
    lower_text = result_text.lower()

    if "supportive" in lower_text:
        stance = "supportive"
    elif "opposed" in lower_text:
        stance = "opposed"

    return {
        "message": result_text,
        "stance": stance,
        "timestamp": datetime.utcnow().isoformat()
    }