from __future__ import annotations

from datetime import datetime
from typing import List, Dict, Any
import math
import random

from flask import Flask, request, jsonify
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

# -------------------------------
# Helpers / Mock Data
# -------------------------------

PRODUCTS = {
    "B08N5WRWNW": "Wireless Earbuds Pro",
    "B08N5N3R8K": "Bluetooth Speaker",
    "B0ABCD1234": "Gaming Headset",
    "p1": "Echo Dot (5th Gen)",
    "p2": "Kindle Paperwhite",
    "p3": "Fire TV Stick 4K",
}

POSITIVE_WORDS = {"good", "great", "awesome", "amazing", "love", "excellent", "perfect", "best", "nice"}
NEGATIVE_WORDS = {"bad", "terrible", "awful", "hate", "worst", "poor", "disappointing", "broken"}


def simple_sentiment(text: str) -> tuple[str, float]:
    """A tiny mock sentiment function to emulate an ML model.
    Returns sentiment and confidence [0-1].
    """
    text_l = text.lower()
    p = sum(1 for w in POSITIVE_WORDS if w in text_l)
    n = sum(1 for w in NEGATIVE_WORDS if w in text_l)
    if p > n:
        return "POSITIVE", min(0.6 + 0.1 * p, 0.99)
    if n > p:
        return "NEGATIVE", min(0.6 + 0.1 * n, 0.99)
    return "NEUTRAL", 0.7


def stable_rng(seed: str) -> random.Random:
    r = random.Random()
    r.seed(seed)
    return r


def mock_reviews(product_id: str, count: int) -> List[str]:
    r = stable_rng(product_id)
    samples = [
        "Excellent sound quality and great battery life.",
        "Works fine but the connection drops sometimes.",
        "Amazing value for the price!",
        "Terrible packaging and poor build quality.",
        "Comfortable to use and setup was easy.",
        "Not worth the money, very disappointing.",
        "Battery lasts long, quality is awesome!",
        "Mediocre performance, nothing special.",
    ]
    return [r.choice(samples) for _ in range(count)]


# -------------------------------
# Routes
# -------------------------------

@app.route("/health", methods=["GET"])  # simple probe
def health() -> Any:
    return {"status": "ok", "time": datetime.now().isoformat()}


@app.route("/analyze", methods=["POST"])
def analyze() -> Any:
    try:
        data = request.get_json(silent=True) or {}
        review_text = data.get("review_text", "")
        if not isinstance(review_text, str) or len(review_text.strip()) < 1:
            return jsonify({"error": "Missing review_text"}), 400
        if len(review_text.strip()) < 10:
            return jsonify({"error": "Review text too short"}), 400

        sentiment, confidence = simple_sentiment(review_text)
        return jsonify({
            "sentiment": sentiment,
            "confidence": float(confidence),
            "classification": f"{sentiment.title()} review",
            "message": f"Detected {sentiment.lower()} sentiment",
            # Optional distribution if you want to display bars in ResultActivity
            # "distribution": {"positive": 70, "neutral": 20, "negative": 10},
        })
    except Exception as e:
        return jsonify({"error": f"Analysis failed: {str(e)}"}), 500


@app.route("/compare", methods=["POST"])
def compare_products() -> Any:
    try:
        data = request.get_json(silent=True) or {}
        product_ids: List[str] = data.get("product_ids", [])
        if not isinstance(product_ids, list) or not product_ids:
            return jsonify({"error": "product_ids must be a non-empty list"}), 400

        products: List[Dict[str, Any]] = []
        for pid in product_ids:
            name = PRODUCTS.get(pid, pid)
            # create a stable pseudo-random distribution per product id
            r = stable_rng(f"compare:{pid}")
            total_reviews = r.randint(200, 2000)
            pos = r.uniform(0.5, 0.9)  # bias positive
            neg = r.uniform(0.02, 0.2)
            neu = max(0.0, 1.0 - pos - neg)
            # convert to integer percentages that sum to 100
            pos_pct = int(round(pos * 100))
            neg_pct = int(round(neg * 100))
            neu_pct = max(0, 100 - pos_pct - neg_pct)
            overall = "positive" if pos_pct >= max(neg_pct, neu_pct) else ("negative" if neg_pct >= neu_pct else "neutral")
            products.append({
                "product_id": pid,
                "product_name": name,
                "total_reviews": total_reviews,
                "positive_percentage": pos_pct,
                "negative_percentage": neg_pct,
                "neutral_percentage": neu_pct,
                "overall_sentiment": overall,
            })

        # Choose top product by highest positive_percentage
        top = max(products, key=lambda x: x["positive_percentage"]) if products else None
        return jsonify({
            "products": products,
            "top_product": top["product_id"] if top else None,
            "comparison_date": datetime.utcnow().isoformat() + "Z",
        })
    except Exception as e:
        return jsonify({"error": f"Comparison failed: {str(e)}"}), 500


@app.route("/summarize", methods=["POST"])
def summarize_reviews() -> Any:
    try:
        data = request.get_json(silent=True) or {}
        product_id: str = data.get("product_id", "")
        if not product_id:
            return jsonify({"error": "product_id is required"}), 400

        name = PRODUCTS.get(product_id, product_id)
        # mock reviews and compute a tiny summary
        reviews = mock_reviews(product_id, count=stable_rng(product_id).randint(200, 1500))
        total = len(reviews)

        # naive key insights and summary
        positives = sum(1 for t in reviews if simple_sentiment(t)[0] == "POSITIVE")
        negatives = sum(1 for t in reviews if simple_sentiment(t)[0] == "NEGATIVE")
        neutrals = total - positives - negatives

        pos_pct = round(positives / total * 100, 1) if total else 0.0
        neg_pct = round(negatives / total * 100, 1) if total else 0.0
        neu_pct = round(neutrals / total * 100, 1) if total else 0.0

        summary = (
            f"Customers generally report positive experiences with {name}. "
            f"Common praise includes sound/quality and battery/comfort. "
            f"Some users mention connectivity or build concerns."
        )

        key_insights = [
            f"{pos_pct}% positive overall",
            "Battery life commonly praised",
            "Comfort and ease of use mentioned frequently",
            "A minority report connectivity/issues",
        ]

        # mock word frequency
        freq_words = [
            {"word": "sound", "frequency": 240},
            {"word": "battery", "frequency": 180},
            {"word": "quality", "frequency": 160},
            {"word": "comfortable", "frequency": 150},
        ]

        sentiment_breakdown = {
            "positive": float(pos_pct),
            "negative": float(neg_pct),
            "neutral": float(neu_pct),
        }

        return jsonify({
            "product_id": product_id,
            "product_name": name,
            "total_reviews_analyzed": total,
            "summary": summary,
            "key_insights": key_insights,
            "word_cloud": freq_words,
            "sentiment_breakdown": sentiment_breakdown,
            "summary_generated_at": datetime.utcnow().isoformat() + "Z",
        })
    except Exception as e:
        return jsonify({"error": f"Summarization failed: {str(e)}"}), 500


if __name__ == "__main__":
    # Bind to all interfaces for easy device testing; change as needed
    app.run(host="0.0.0.0", port=5000, debug=True)
