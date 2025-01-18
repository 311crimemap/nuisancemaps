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
* Build container
  * `docker build -t <name/web>`
  * `docker run -v $(pwd):/web -it <name/web>`

* Install vite to generate project configuration and preserve on bind mount
  * `npm create vite@latest <web> --template react-ts`
  * `cd <web>`
  * `npm install`
  * adjust `vite.config.ts` server param to bind to '0.0.0.0'
* Added service `<web>` to `docker-compose.yml`.
