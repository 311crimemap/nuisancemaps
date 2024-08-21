FROM continuumio/miniconda3

RUN apt-get update && apt-get install -y jq

RUN pip install pandas jq python-dotenv gnureadline openai bs4

COPY . /classifier
WORKDIR /classifier

CMD ["/bin/bash"]
