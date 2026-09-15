FROM python:3.11.7

RUN apt-get update \
    && apt-get install --no-install-recommends -y jq curl \
    && rm -rf /var/lib/apt/lists/*

RUN pip install --no-cache-dir pandas jq python-dotenv gnureadline openai bs4 openpyxl csvkit chardet

WORKDIR /classifier
RUN groupadd --gid 10001 app \
    && useradd --uid 10001 --gid app --create-home --shell /usr/sbin/nologin app

COPY --chown=app:app . /classifier

USER app

CMD ["/bin/bash"]
