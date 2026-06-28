# How to release

- Create tag "vX.Y.Z"
- Push tag
- GitHub Actions will build and publish release

## API docs

API docs are generated with Dokka and served from `docs/api/` on the `demo` branch
(GitHub Pages), alongside the wasm demo at the site root:

- Reference: https://darkrock-studios.github.io/epub4kmp/api/

### Automatic (on release)

Pushing a `vX.Y.Z` tag also triggers the `deploy-docs.yml` workflow, which builds the
Dokka docs and commits them to `docs/api/` on the `demo` branch — the wasm demo files
there are left untouched. It can also be run on demand from the Actions tab
(**Deploy Docs → Run workflow**).

### Manual

```bash
./gradlew updateDocs   # generates docs/api/
```

Commit `docs/api/` on the `demo` branch (not `main` — it's generated output). The wasm
demo is refreshed separately on the `demo` branch via
`./gradlew :samples:reader-web:updateDemo` (see `docs/DEMO.md`).