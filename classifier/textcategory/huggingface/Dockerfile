# for local machine runtime
# do need GPU for training

FROM continuumio/miniconda3:24.1.2-0

COPY ./ /classifier
COPY ../api/src/main/resources/data/classifier_categories.json /classifier_categories.json
WORKDIR /classifier

RUN conda install --yes pytorch::pytorch=2.2.1 cpuonly
RUN conda install --yes conda-forge::transformers==4.38.2
RUN conda install --yes psycopg2==2.9.9 scikit-learn evaluate accelerate datasets setfit
