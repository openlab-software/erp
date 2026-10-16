#!/usr/bin/env node
// Cria um microserviço Quarkus novo a partir da base comum de customer-service (pom, Dockerfile, application.yaml,
// tratamento de erros, i18n, outbox + RabbitMQ). Só a infraestrutura é copiada; o domínio é escrito à mão.
//
//   node scripts/new-service.mjs <nome>-service --package <pkg> --port <porta> --title "Texto" [--schema <schema>]
//
// Depois: registrar o serviço no module.json do módulo dono (modules/<módulo>/module.json) e rodar
// `node scripts/modules.mjs validate`.
import { cpSync, existsSync, mkdirSync, readFileSync, readdirSync, statSync, writeFileSync } from 'node:fs';
import { dirname, join, relative, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const TEMPLATE = join(ROOT, 'customer-service');

const args = process.argv.slice(2);
const name = args[0];
const opt = (flag) => {
  const i = args.indexOf(flag);
  return i >= 0 ? args[i + 1] : undefined;
};
const pkg = opt('--package');
const port = opt('--port');
const title = opt('--title');
const schema = opt('--schema') ?? pkg;

if (!name || !name.endsWith('-service') || !pkg || !port || !title) {
  console.error('uso: new-service.mjs <nome>-service --package <pkg> --port <porta> --title "Descrição" [--schema <schema>]');
  process.exit(2);
}
if (existsSync(join(ROOT, name))) {
  console.error(`erro: ${name} já existe`);
  process.exit(1);
}

const JAVA = 'src/main/java/software/openlab/customer';
const copy = [
  'pom.xml',
  '.gitignore',
  'src/main/docker/Dockerfile.jvm',
  'src/main/resources/application.yaml',
  `${JAVA}/domain/shared`,
  `${JAVA}/infra/i18n`,
  `${JAVA}/infra/messaging`,
  `${JAVA}/infra/persistence/OutboxEntry.java`,
  `${JAVA}/infra/rest/JacksonConfig.java`,
  `${JAVA}/infra/rest/exception`,
];

const cap = (s) => s[0].toUpperCase() + s.slice(1);

function transform(text) {
  return text
    .replaceAll('8082', port)
    .replaceAll('Customer Service API', `${cap(pkg)} Service API`)
    .replace(/description: .*cadastro de clientes.*/, `description: ${title}`)
    .replaceAll('customer', pkg)
    .replaceAll(`software.openlab.${pkg}.${pkg}`, `software.openlab.${pkg}`);
}

function walk(path) {
  if (statSync(path).isDirectory()) return readdirSync(path).flatMap((f) => walk(join(path, f)));
  return [path];
}

function emit(rel, content) {
  const target = join(ROOT, name, rel.split('\\').join('/').replace('software/openlab/customer', `software/openlab/${pkg}`));
  mkdirSync(dirname(target), { recursive: true });
  writeFileSync(target, content);
}

for (const entry of copy) {
  for (const file of walk(join(TEMPLATE, entry))) {
    emit(relative(TEMPLATE, file), transform(readFileSync(file, 'utf8')));
  }
}

// application.yaml: o schema pode diferir do nome do pacote
if (schema !== pkg) {
  const yaml = join(ROOT, name, 'src/main/resources/application.yaml');
  writeFileSync(yaml, readFileSync(yaml, 'utf8').replaceAll(`default-schema: ${pkg}`, `default-schema: ${schema}`)
    .replaceAll(`schemas: ${pkg}`, `schemas: ${schema}`));
}

// Mensagens: só as chaves genéricas e de validação (as de domínio são do serviço).
for (const locale of ['en', 'pt_BR']) {
  const src = readFileSync(join(TEMPLATE, `src/main/resources/i18n/messages_${locale}.properties`), 'utf8');
  const lines = src.split('\n');
  const start = lines.findIndex((l) => /^# --- (Customer|Cliente)/.test(l));
  const end = lines.findIndex((l) => /^# --- (Payload validation|Validacao)/.test(l));
  const kept = [...lines.slice(0, start), `# --- ${cap(pkg)} ---`, '', ...lines.slice(end)];
  emit(`src/main/resources/i18n/messages_${locale}.properties`, kept.join('\n'));
}

// Migration inicial: schema + outbox.
emit(`src/main/resources/db/migration/V1__init_${pkg}_schema.sql`, `CREATE SCHEMA IF NOT EXISTS ${schema};

CREATE TABLE IF NOT EXISTS ${schema}.outbox_entries (
    outbox_entry_id  BIGSERIAL    PRIMARY KEY,
    routing_key      TEXT         NOT NULL,
    payload          TEXT         NOT NULL,
    status           TEXT         NOT NULL DEFAULT 'pending',
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at     TIMESTAMPTZ,
    error            TEXT
);

CREATE INDEX IF NOT EXISTS idx_${pkg}_outbox_status_created ON ${schema}.outbox_entries (status, created_at);
`);

console.log(`${name} criado (pacote software.openlab.${pkg}, porta ${port}, schema ${schema}).`);
console.log('Próximos passos: domínio em domain/ application/ infra/, migrations V2+, entrada em modules/<módulo>/module.json.');
