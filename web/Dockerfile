FROM node:20.11.0

COPY . /web
WORKDIR /web

RUN npm install

RUN chown -R node /web
USER node
CMD ["npm", "run", "dev"]
