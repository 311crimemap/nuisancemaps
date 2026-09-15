FROM node:22.14.0

WORKDIR /web
RUN chown node:node /web

COPY --chown=node:node package.json package-lock.json ./
USER node
RUN npm ci

COPY --chown=node:node . /web

CMD ["npm", "run", "dev"]
