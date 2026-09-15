---
name: text-category-classification
description: Classify pending 311 or crime text categories into Nuisance Maps labels and create the review and submission artifacts. Use for pending text-category batches; do not use for source-field configuration.
---

# Text-category classification

Use the harness to replace the deprecated `01-text-cat.sh` / OpenAI API
workflow. The input directory is normally under `classifier/source-config/data/`
and contains `meta.json` and `text_categories.txt`.

1. Read `meta.json` and select the matching `prompt/prompt_<category>.txt` and
   `prompt/categories_<category>.txt`. Treat the category list and its ordinal
   indexes as authoritative.
2. Classify every non-empty line in `text_categories.txt`. Preserve the text
   verbatim (apart from its line ending), retain order, and use a string label.
   Do not deduplicate or omit uncertain entries; use `Other` only when none of
   the listed categories applies.
3. Write the legacy-compatible artifacts in the input directory:
   - `text_categories.txt.out.json`: an array of `{dataType, text, label}`
     records.
   - `text_categories.txt.out.csv`: the same records, with the exact header
     `dataType,text,label`.
   - `text_categories.txt.out.csv.final.json`: the submission JSON, with the
     same records and ordering.
4. Validate before handoff: all three artifacts have exactly one record per
   non-empty input line; all `dataType` values equal `meta.json.category`; all
   labels are allowed indexes for that category; and the JSON and CSV records
   agree exactly.

Use `scripts/render_labels.py <data-directory> <comma-separated-labels>` to
serialize harness labels and perform those checks. It does not classify text or
call an external API.

Do not invoke `01-text-cat.sh` or `4-text-category-classifier.py`: those are
the previous external API workflow. The CSV remains a human-review surface; a
reviewer may correct `label` values before submitting the final JSON.
