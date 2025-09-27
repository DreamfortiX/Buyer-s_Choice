# Buyer's Choice Backend (Flask)

A minimal Flask backend that matches the Android app contracts.

## Endpoints

- POST `/analyze`
  - Request JSON: `{ "review_text": "string (>=10 chars)" }`
  - Response JSON: `{ sentiment: string, confidence: float, classification?: string, message?: string, distribution?: {positive:int,neutral:int,negative:int} }`

- POST `/compare`
  - Request JSON: `{ "product_ids": ["id1", "id2", ...] }`
  - Response JSON: `{ products: [ { product_id, product_name, total_reviews, positive_percentage, neutral_percentage, negative_percentage, overall_sentiment } ], top_product: string, comparison_date: string }`

- POST `/summarize`
  - Request JSON: `{ "product_id": "id" }`
  - Response JSON: `{ product_id, product_name, total_reviews_analyzed, summary, key_insights: string[], word_cloud: {word,frequency}[], sentiment_breakdown: {positive,neutral,negative}, summary_generated_at }`

## Quickstart

```bash
# 1) Create virtual environment (recommended)
python -m venv .venv
# Windows PowerShell
. .venv\\Scripts\\Activate.ps1

# 2) Install dependencies
pip install -r requirements.txt

# 3) Run server
python app.py
# Server runs on http://localhost:5000
```

## Android Emulator Base URL

In `app/src/main/java/com/example/reviews/data/network/RetrofitClient.kt` set:
```kotlin
private const val BASE_URL = "http://10.0.2.2:5000/"
```
This allows the Android emulator to access the host machine.

## Notes

- This backend uses mock logic and deterministic pseudo-randomness for stable demos.
- Replace mocks with your real database and ML pipelines when ready.
