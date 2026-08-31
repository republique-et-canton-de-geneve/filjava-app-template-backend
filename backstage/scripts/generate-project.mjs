import { spawnSync } from 'node:child_process';
import { cp, lstat, mkdir, readFile, readdir, rm, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import {
  renameJavaProject,
  validateComponentId,
  validateJavaPackage,
} from '../scaffolder-backend-module-filjava/src/rename-project.mjs';

const scriptDirectory = path.dirname(fileURLToPath(import.meta.url));
const repositoryRoot = path.resolve(scriptDirectory, '..', '..');

export async function generateProject(options) {
  validateOptions(options);
  const output = path.resolve(options.output);
  assertSafeOutput(output);

  if (await exists(output)) {
    throw new Error(`Le dossier de destination existe déjà: ${output}`);
  }

  await mkdir(path.dirname(output), { recursive: true });
  try {
    await cp(repositoryRoot, output, {
      recursive: true,
      filter(source) {
        const relative = path.relative(repositoryRoot, source);
        return !relative.split(path.sep).some(segment =>
          segment === '.git' || segment === 'target' || segment === 'node_modules');
      },
    });

    await renderBackstageValues(output, options);
    const result = await renameJavaProject({
      workspacePath: output,
      componentId: options.componentId,
      componentName: options.componentName,
      description: options.description,
      sourcePackage: 'ch.ge.filjava.apptemplatebackend',
      targetPackage: options.javaPackage,
      sourceMainClass: 'FiljavaApplication',
      logger: { info: message => process.stdout.write(`${message}\n`) },
    });

    if (!options.skipVerify) runMavenVerify(output);
    process.stdout.write(`Projet généré dans ${output}\n`);
    process.stdout.write(`Classe principale: ${result.targetMainClass}\n`);
    return { output, ...result };
  } catch (error) {
    await rm(output, { recursive: true, force: true });
    throw error;
  }
}

export function parseArguments(argumentsList) {
  const options = {};
  for (let index = 0; index < argumentsList.length; index += 1) {
    const argument = argumentsList[index];
    if (argument === '--skip-verify') {
      options.skipVerify = true;
      continue;
    }
    if (argument === '--help' || argument === '-h') return { help: true };
    if (!argument.startsWith('--')) throw new Error(`Argument inconnu: ${argument}`);
    const key = argument.slice(2);
    if (!['output', 'component-id', 'component-name', 'description', 'java-package', 'owner', 'system'].includes(key)) {
      throw new Error(`Option inconnue: ${argument}`);
    }
    const value = argumentsList[index + 1];
    if (!value || value.startsWith('--')) throw new Error(`Valeur manquante pour ${argument}`);
    options[toCamelCase(key)] = value;
    index += 1;
  }
  return options;
}

function validateOptions(options) {
  const required = ['output', 'componentId', 'componentName', 'description', 'javaPackage', 'owner', 'system'];
  const missing = required.filter(name => typeof options[name] !== 'string' || !options[name].trim());
  if (missing.length > 0) throw new Error(`Options obligatoires manquantes: ${missing.join(', ')}`);
  validateComponentId(options.componentId);
  validateJavaPackage(options.javaPackage);
}

function assertSafeOutput(output) {
  const relativeToRepository = path.relative(repositoryRoot, output);
  if (relativeToRepository === '' || (!relativeToRepository.startsWith('..') && !path.isAbsolute(relativeToRepository))) {
    throw new Error('Le dossier de destination doit se trouver hors du dépôt du template');
  }
}

async function renderBackstageValues(root, parameters) {
  for (const file of await listFiles(root)) {
    if (!['.yaml', '.yml', '.md', '.xml', '.java', '.html'].includes(path.extname(file))) continue;
    const content = await readFile(file, 'utf8');
    const rendered = content.replace(/\$\{\{\s*values\.([A-Za-z][A-Za-z0-9]*)\s*\}\}/g, (_, name) => {
      if (!Object.hasOwn(parameters, name)) throw new Error(`Valeur Backstage inconnue: ${name}`);
      return parameters[name];
    });
    if (rendered !== content) await writeFile(file, rendered, 'utf8');
  }
}

function runMavenVerify(output) {
  const result = spawnSync('mvn', ['clean', 'verify'], {
    cwd: output,
    encoding: 'utf8',
    shell: process.platform === 'win32',
    stdio: 'inherit',
  });
  if (result.error?.code === 'ENOENT') throw new Error('Maven est introuvable. Installez Maven ou utilisez --skip-verify.');
  if (result.status !== 0) throw new Error('mvn clean verify a échoué');
}

async function listFiles(directory) {
  const files = [];
  for (const entry of await readdir(directory, { withFileTypes: true })) {
    if (entry.name === '.git' || entry.name === 'target' || entry.name === 'node_modules') continue;
    const child = path.join(directory, entry.name);
    if (entry.isDirectory()) files.push(...await listFiles(child));
    else if (entry.isFile()) files.push(child);
  }
  return files;
}

async function exists(target) {
  try {
    await lstat(target);
    return true;
  } catch (error) {
    if (error?.code === 'ENOENT') return false;
    throw error;
  }
}

function toCamelCase(value) {
  return value.replace(/-([a-z])/g, (_, letter) => letter.toUpperCase());
}

function printHelp() {
  process.stdout.write(`Usage:\n  node backstage/scripts/generate-project.mjs [options]\n\n` +
    `Options obligatoires:\n` +
    `  --output <dossier>\n  --component-id <identifiant>\n  --component-name <nom>\n` +
    `  --description <description>\n  --java-package <package>\n` +
    `  --owner <référence Backstage>\n  --system <référence Backstage>\n\n` +
    `Option facultative:\n  --skip-verify   Ne pas exécuter mvn clean verify\n`);
}

if (process.argv[1] && import.meta.url === pathToFileURL(path.resolve(process.argv[1])).href) {
  try {
    const options = parseArguments(process.argv.slice(2));
    if (options.help) printHelp();
    else await generateProject(options);
  } catch (error) {
    process.stderr.write(`Erreur: ${error.message}\n`);
    process.exitCode = 1;
  }
}
