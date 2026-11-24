import { readFileSync, writeFileSync } from 'node:fs'

const [input, output] = process.argv.slice(2)
const spec = JSON.parse(readFileSync(input, 'utf8'))

const paths = {}
for (const [path, operations] of Object.entries(spec.paths)) {
  if (path !== '/api/products' && !path.startsWith('/api/products/')) continue
  if (!operations.get || path.endsWith('/review/eligibility')) continue
  paths[path] = { get: operations.get }
}

const schemas = spec.components.schemas
const used = new Set()
const visit = (node) => {
  if (Array.isArray(node)) return node.forEach(visit)
  if (node === null || typeof node !== 'object') return
  for (const [key, value] of Object.entries(node)) {
    if (key === '$ref') {
      const name = value.split('/').pop()
      if (!used.has(name)) {
        used.add(name)
        visit(schemas[name])
      }
    } else {
      visit(value)
    }
  }
}
visit(paths)

const publicSpec = {
  openapi: spec.openapi,
  info: { title: 'Store catalog API', version: spec.info.version },
  paths,
  components: { schemas: Object.fromEntries([...used].sort().map((name) => [name, schemas[name]])) }
}

writeFileSync(output, JSON.stringify(publicSpec, null, 2) + '\n')
console.log(`${Object.keys(paths).length} paths, ${used.size} schemas`)
