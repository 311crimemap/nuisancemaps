FROM python:3.11.7

RUN apt-get update && apt-get install -y jq curl

RUN pip install pandas jq python-dotenv gnureadline openai bs4

COPY . /classifier
WORKDIR /classifier

RUN echo "alias ll='ls -l'" >> /.bashrc

CMD ["/bin/bash"]
