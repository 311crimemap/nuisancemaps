#!/usr/bin/env python3
"""Render harness text-category labels in the legacy classifier formats."""

import csv
import json
import sys
from pathlib import Path


LABELS = {
    "311": set(map(str, range(15))),
    "crime": set(map(str, range(7))),
}


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit("usage: render_labels.py DATA_DIRECTORY label,label,...")

    directory = Path(sys.argv[1])
    labels = [label.strip() for label in sys.argv[2].split(",")]
    category = json.loads((directory / "meta.json").read_text())["category"]
    texts = [line.rstrip("\n\r") for line in (directory / "text_categories.txt").read_text().splitlines() if line]

    if category not in LABELS:
        raise SystemExit(f"unsupported category: {category}")
    if len(labels) != len(texts):
        raise SystemExit(f"expected {len(texts)} labels, received {len(labels)}")
    if not set(labels) <= LABELS[category]:
        raise SystemExit("labels include an index outside the category taxonomy")

    records = [
        {"dataType": category, "text": text, "label": label}
        for text, label in zip(texts, labels)
    ]
    json_output = directory / "text_categories.txt.out.json"
    csv_output = directory / "text_categories.txt.out.csv"
    final_output = directory / "text_categories.txt.out.csv.final.json"
    json_output.write_text(json.dumps(records))
    with csv_output.open("w", newline="") as output:
        writer = csv.DictWriter(output, fieldnames=["dataType", "text", "label"])
        writer.writeheader()
        writer.writerows(records)
    final_output.write_text(json.dumps(records, indent=4) + "\n")


if __name__ == "__main__":
    main()
