# Instalación manual de los archivos de contexto

Ejecutar desde la raíz del repositorio `Pikudo_Chicken2`:

```bash
git checkout main
git pull
git checkout -b lavender/context-implementation

# Copiar el contenido de este paquete sobre la raíz del repo.
# Deben quedar estos paths:
# - AGENTS.md
# - .codex/context.md
# - docs/context/*.md

git add AGENTS.md .codex docs/context
git commit -m "docs: add Codex context for Pikudo backend implementation"
git push -u origin lavender/context-implementation
```

Si la rama ya existe:

```bash
git fetch origin
git checkout lavender/context-implementation
cp -r <carpeta_extraida>/* .
git add AGENTS.md .codex docs/context
git commit -m "docs: update Codex context for Pikudo backend implementation"
git push
```
