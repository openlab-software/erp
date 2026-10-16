#!/usr/bin/env node
// Ferramenta dos módulos do ERP (sem dependências). Os manifestos ficam em modules/<nome>/module.json.
//
//   node scripts/modules.mjs list                      módulos, dependências e serviços
//   node scripts/modules.mjs validate                  confere os manifestos (CI roda isto)
//   node scripts/modules.mjs resolve crm sales         fecho de dependências, em ordem de instalação
//   node scripts/modules.mjs services [mod ...]        serviços (JSON) de todos os módulos ou dos informados + dependências
//   node scripts/modules.mjs stage                     copia os manifestos para .devops/helm/files/modules (o chart os lê)
import { cpSync, existsSync, mkdirSync, readdirSync, readFileSync, rmSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const MODULES_DIR = join(ROOT, 'modules');
const CHART_MODULES_DIR = join(ROOT, '.devops', 'helm', 'files', 'modules');

export function loadModules() {
  const modules = new Map();
  for (const entry of readdirSync(MODULES_DIR, { withFileTypes: true })) {
    if (!entry.isDirectory()) continue;
    const file = join(MODULES_DIR, entry.name, 'module.json');
    if (!existsSync(file)) continue;
    const manifest = JSON.parse(readFileSync(file, 'utf8'));
    manifest.__dir = entry.name;
    modules.set(manifest.name, manifest);
  }
  return modules;
}

/** Módulos pedidos + dependências obrigatórias (`depends`), dependências primeiro. */
export function closure(modules, requested) {
  const ordered = [];
  const visiting = new Set();
  const visit = (name, path) => {
    const mod = modules.get(name);
    if (!mod) throw new Error(`módulo desconhecido: ${name} (${path.join(' -> ')})`);
    if (ordered.includes(name)) return;
    if (visiting.has(name)) throw new Error(`dependência circular: ${[...path, name].join(' -> ')}`);
    visiting.add(name);
    for (const dep of mod.depends ?? []) visit(dep, [...path, name]);
    visiting.delete(name);
    ordered.push(name);
  };
  for (const name of requested) visit(name, []);
  return ordered;
}

export function validate(modules) {
  const errors = [];
  const ports = new Map();
  const serviceOwner = new Map();
  const known = new Set(modules.keys());

  for (const [name, mod] of modules) {
    const where = `modules/${mod.__dir}/module.json`;
    if (name !== mod.__dir) errors.push(`${where}: "name" (${name}) deve ser igual ao diretório (${mod.__dir})`);
    for (const key of ['display_name', 'summary', 'category', 'version']) {
      if (!mod[key]) errors.push(`${where}: campo obrigatório ausente: ${key}`);
    }
    for (const field of ['depends', 'integrates_with']) {
      for (const other of mod[field] ?? []) {
        if (!known.has(other)) errors.push(`${where}: ${field} referencia módulo inexistente: ${other}`);
        if (other === name) errors.push(`${where}: ${field} referencia o próprio módulo`);
      }
    }
    for (const other of mod.integrates_with ?? []) {
      if ((mod.depends ?? []).includes(other)) {
        errors.push(`${where}: "${other}" está em depends e em integrates_with (escolha um)`);
      }
    }
    for (const svc of mod.services ?? []) {
      if (!existsSync(join(ROOT, svc.name, 'pom.xml'))) errors.push(`${where}: serviço sem diretório/pom.xml: ${svc.name}`);
      if (serviceOwner.has(svc.name)) {
        errors.push(`${where}: serviço ${svc.name} já pertence ao módulo ${serviceOwner.get(svc.name)}`);
      }
      serviceOwner.set(svc.name, name);
      if (ports.has(svc.port)) errors.push(`${where}: porta ${svc.port} já usada por ${ports.get(svc.port)}`);
      ports.set(svc.port, svc.name);
    }
    for (const item of mod.menu ?? []) {
      for (const key of ['group', 'label', 'path', 'icon']) {
        if (!item[key]) errors.push(`${where}: item de menu sem "${key}": ${JSON.stringify(item)}`);
      }
    }
  }

  try {
    closure(modules, [...modules.keys()]);
  } catch (e) {
    errors.push(e.message);
  }

  // Todo módulo precisa ter a chave de liga/desliga no values.yaml do chart (senão nunca seria implantado).
  const valuesFile = join(ROOT, '.devops', 'helm', 'values.yaml');
  if (existsSync(valuesFile)) {
    const values = readFileSync(valuesFile, 'utf8').replace(/\r\n/g, '\n');
    const block = values.match(/^modules:\n((?: {2,}.*\n|\n)*)/m)?.[1] ?? '';
    const inChart = new Set([...block.matchAll(/^ {2}([\w-]+):/gm)].map((m) => m[1]));
    for (const name of modules.keys()) {
      if (!inChart.has(name)) errors.push(`.devops/helm/values.yaml: falta \`modules.${name}\``);
    }
    for (const name of inChart) {
      if (!modules.has(name)) errors.push(`.devops/helm/values.yaml: \`modules.${name}\` não existe em modules/`);
    }
  }

  // bff_prefix é a base da variável <PREFIXO>_SERVICE_URL do frontend: não pode faltar nem se repetir.
  const prefixes = new Map();
  for (const mod of modules.values()) {
    for (const svc of mod.services ?? []) {
      if (!svc.bff_prefix) {
        errors.push(`modules/${mod.__dir}/module.json: serviço ${svc.name} sem bff_prefix`);
      } else if (prefixes.has(svc.bff_prefix)) {
        errors.push(`bff_prefix ${svc.bff_prefix} repetido (${prefixes.get(svc.bff_prefix)} e ${svc.name})`);
      } else {
        prefixes.set(svc.bff_prefix, svc.name);
      }
    }
  }

  // Todo diretório de serviço com pom.xml precisa pertencer a algum módulo.
  for (const entry of readdirSync(ROOT, { withFileTypes: true })) {
    if (entry.isDirectory() && entry.name.endsWith('-service') && existsSync(join(ROOT, entry.name, 'pom.xml'))) {
      if (!serviceOwner.has(entry.name)) errors.push(`${entry.name} não pertence a nenhum módulo`);
    }
  }
  return errors;
}

function servicesOf(modules, names) {
  return names.flatMap((n) => modules.get(n).services.map((s) => ({ ...s, module: n })));
}

function main() {
  const [cmd, ...args] = process.argv.slice(2);
  const modules = loadModules();

  switch (cmd) {
    case 'list': {
      for (const [name, m] of modules) {
        const svc = m.services.map((s) => s.name).join(', ') || '-';
        console.log(`${name.padEnd(12)} depends=[${(m.depends ?? []).join(', ')}] integrates_with=[${(m.integrates_with ?? []).join(', ')}] services=[${svc}]`);
      }
      break;
    }
    case 'validate': {
      const errors = validate(modules);
      if (errors.length) {
        for (const e of errors) console.error(`erro: ${e}`);
        process.exit(1);
      }
      console.log(`ok: ${modules.size} módulos`);
      break;
    }
    case 'resolve': {
      console.log(closure(modules, args).join('\n'));
      break;
    }
    case 'services': {
      const names = args.length ? closure(modules, args) : [...modules.keys()];
      console.log(JSON.stringify(servicesOf(modules, names).map((s) => s.name)));
      break;
    }
    case 'stage': {
      rmSync(CHART_MODULES_DIR, { recursive: true, force: true });
      mkdirSync(CHART_MODULES_DIR, { recursive: true });
      for (const [name, m] of modules) {
        const file = join(MODULES_DIR, m.__dir, 'module.json');
        cpSync(file, join(CHART_MODULES_DIR, `${name}.json`));
      }
      console.log(`${modules.size} manifestos copiados para ${CHART_MODULES_DIR}`);
      break;
    }
    default:
      console.error('uso: modules.mjs list | validate | resolve <mod...> | services [mod...] | stage');
      process.exit(2);
  }
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  try {
    main();
  } catch (e) {
    console.error(`erro: ${e.message}`);
    process.exit(1);
  }
}
