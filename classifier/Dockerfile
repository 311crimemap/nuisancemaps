FROM continuumio/miniconda3

RUN apt-get update && apt-get install -y jq

RUN pip install jq python-dotenv gnureadline openai

COPY . /classifier
WORKDIR /classifier

CMD ["/bin/bash"]
