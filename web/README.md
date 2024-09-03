# React + TypeScript + Vite

Preview Prod in local dev

```
docker exec -it nuisancemaps_web_1 bash
npm run build
npm run preview
```

---

## Web Install Notes

* Bootstrap container from `Dockerfile` needs to have vite installed
* create basic node lts Dockerfile w/ typescript template

```
FROM node:20.11.0

COPY . /web
WORKDIR /web

RUN npm install

RUN chown -R node /web
USER node
CMD ["npm", "run", "dev"]

```

* Build container
  * `docker build -t <name/web>`
  * `docker run -v $(pwd):/web -it <name/web>`

* Install vite to generate project configuration and preserve on bind mount
  * `npm create vite@latest <web> --template react-ts`
  * `cd <web>`
  * `npm install`
  * adjust `vite.config.ts` server param to bind to '0.0.0.0'
* Added service `<web>` to `docker-compose.yml`.



---


This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react/README.md) uses [Babel](https://babeljs.io/) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type aware lint rules:

- Configure the top-level `parserOptions` property like this:

```js
export default {
  // other rules...
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module',
    project: ['./tsconfig.json', './tsconfig.node.json'],
    tsconfigRootDir: __dirname,
  },
}
```

- Replace `plugin:@typescript-eslint/recommended` to `plugin:@typescript-eslint/recommended-type-checked` or `plugin:@typescript-eslint/strict-type-checked`
- Optionally add `plugin:@typescript-eslint/stylistic-type-checked`
- Install [eslint-plugin-react](https://github.com/jsx-eslint/eslint-plugin-react) and add `plugin:react/recommended` & `plugin:react/jsx-runtime` to the `extends` list
