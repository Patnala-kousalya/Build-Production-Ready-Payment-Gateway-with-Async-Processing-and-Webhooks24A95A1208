import os
import json
import chromadb
from sentence_transformers import SentenceTransformer

# Initialize embedding model
embedding_model = SentenceTransformer("all-MiniLM-L6-v2")

# Create in-memory Chroma client
chroma_client = chromadb.Client()
collection = chroma_client.get_or_create_collection("policy_collection")


def load_policies():
    """
    Load all country policy documents into ChromaDB at startup.
    """
    base_path = "data/policies"

    for file in os.listdir(base_path):
        if file.endswith(".json"):
            full_path = os.path.join(base_path, file)

            with open(full_path, "r") as f:
                data = json.load(f)

            country = data["country"]

            documents = data["key_positions"] + data["red_lines"]

            embeddings = embedding_model.encode(documents).tolist()

            ids = [f"{country}_{i}" for i in range(len(documents))]
            metadatas = [{"country": country} for _ in documents]

            collection.add(
                documents=documents,
                embeddings=embeddings,
                ids=ids,
                metadatas=metadatas
            )


def retrieve_relevant_points(country: str, query: str):
    """
    Retrieve most relevant policy points for a country.
    """
    query_embedding = embedding_model.encode([query]).tolist()

    results = collection.query(
        query_embeddings=query_embedding,
        n_results=3,
        where={"country": country}
    )

    if results["documents"]:
        return results["documents"][0]

    return []