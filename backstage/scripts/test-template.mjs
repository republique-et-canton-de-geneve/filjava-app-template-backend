import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { cp, mkdtemp, readFile, readdir, rm, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { renameJavaProject } from '../scaffolder-backend-module-filjava/src/rename-project.mjs';

const scriptDirectory = path.dirname(fileURLToPath(import.meta.url));
const repositoryRoot = path.resolve(scriptDirectory, '..', '..');
const outputRoot = await mkdtemp(path.join(tmpdir(), 'filjava-generated-'));
const values = {
  componentId: 'gestion-dossiers',
  componentName: 'Gestion des dossiers',
  description: 'Service de gestion des dossiers',
  javaPackage: 'ch.ge.gestiondossiers',
  owner: 'group:default/equipe-dossiers',
  system: 'system:default/dossiers',
};

try {
  await cp(repositoryRoot, outputRoot, {
    recursive: true,
    filter(source) {
      const relative = path.relative(repositoryRoot, source);
      return !relative.split(path.sep).some(segment =>
        segment === '.git' || segment === 'target' || segment === 'node_modules');
    },
  });
  await renderBackstageValues(outputRoot, values);
  await renameJavaProject({
    workspacePath: outputRoot,
    componentId: values.componentId,
    componentName: values.componentName,
    description: values.description,
    sourcePackage: 'ch.ge.filjava.apptemplatebackend',
    targetPackage: values.javaPackage,
    sourceMainClass: 'FiljavaApplication',
    logger: { info: message => process.stdout.write(`${message}\n`) },
  });
  await verifyGeneratedProject(outputRoot, values);

  const maven = spawnSync('mvn', ['-version'], { cwd: outputRoot, encoding: 'utf8', shell: process.platform === 'win32' });
  if (maven.status === 0) {
    const verify = spawnSync('mvn', ['clean', 'verify'], {
      cwd: outputRoot,
      encoding: 'utf8',
      shell: process.platform === 'win32',
      stdio: 'inherit',
    });
    assert.equal(verify.status, 0, 'mvn clean verify a échoué dans le projet généré');
  } else {
    process.stdout.write('Maven indisponible : vérification Maven du projet généré ignorée.\n');
  }
  process.stdout.write(`Projet généré et contrôlé dans ${outputRoot}\n`);
} finally {
  if (process.env.KEEP_GENERATED_PROJECT !== 'true') {
    await rm(outputRoot, { recursive: true, force: true });
  }
}

async function renderBackstageValues(root, parameters) {
  for (const file of await listFiles(root)) {
    if (!['.yaml', '.yml', '.md', '.xml', '.java', '.html'].includes(path.extname(file))) continue;
    const content = await readFile(file, 'utf8');
    const rendered = content.replace(/\$\{\{\s*values\.([A-Za-z][A-Za-z0-9]*)\s*\}\}/g,
      (_, name) => {
        assert.ok(Object.hasOwn(parameters, name), `Valeur Backstage inconnue: ${name}`);
        return parameters[name];
      });
    if (rendered !== content) await writeFile(file, rendered, 'utf8');
  }
}

async function verifyGeneratedProject(root, parameters) {
  for (const moduleName of ['core', 'infra', 'ui']) {
    assert.ok(await exists(path.join(root, moduleName, 'pom.xml')), `Module ${moduleName} absent`);
  }
  const mainClass = path.join(root, 'ui', 'src', 'main', 'java', 'ch', 'ge',
    'gestiondossiers', 'ui', 'main', 'GestionDossiersApplication.java');
  assert.ok(await exists(mainClass), 'Classe principale renommée absente');

  const pom = await readFile(path.join(root, 'pom.xml'), 'utf8');
  assert.match(pom, /<artifactId>gestion-dossiers<\/artifactId>/);
  assert.match(pom, new RegExp(`<groupId>${parameters.javaPackage}<\\/groupId>`));
  const application = await readFile(path.join(root, 'ui', 'src', 'main', 'resources', 'application.yml'), 'utf8');
  assert.match(application, /name: gestion-dossiers/);
  const readme = await readFile(path.join(root, 'README.md'), 'utf8');
  assert.match(readme, /^# Gestion des dossiers/);
  assert.doesNotMatch(readme, /Software Template Backstage|backstage\/README\.md/);
  const catalog = await readFile(path.join(root, 'catalog-info.yaml'), 'utf8');
  for (const value of [parameters.componentId, parameters.componentName, parameters.owner, parameters.system]) {
    assert.ok(catalog.includes(value), `catalog-info.yaml ne contient pas ${value}`);
  }

  const forbiddenFiles = ['template.yaml', 'ticket.md', 'AGENTS.md', 'backstage', '.github'];
  for (const file of forbiddenFiles) assert.equal(await exists(path.join(root, file)), false, `${file} ne doit pas être généré`);

  const files = await listFiles(root);
  const forbiddenStrings = [
    '${{ values.', 'ch.ge.filjava.apptemplatebackend', 'ch/ge/filjava/apptemplatebackend',
    'filjava-app-template-backend', 'FiljavaApplication',
  ];
  for (const file of files) {
    if (!['.java', '.xml', '.yml', '.yaml', '.md', '.html', '.conf', '.json', ''].includes(path.extname(file))) continue;
    const content = await readFile(file, 'utf8');
    for (const forbidden of forbiddenStrings) {
      assert.equal(content.includes(forbidden), false, `${forbidden} subsiste dans ${path.relative(root, file)}`);
    }
  }
}

async function listFiles(directory) {
  const files = [];
  for (const entry of await readdir(directory, { withFileTypes: true })) {
    const child = path.join(directory, entry.name);
    if (entry.isDirectory()) files.push(...await listFiles(child));
    else if (entry.isFile()) files.push(child);
  }
  return files;
}

async function exists(target) {
  try {
    await readFile(target);
    return true;
  } catch (error) {
    if (error?.code === 'EISDIR' || error?.code === 'EPERM') return true;
    if (error?.code === 'ENOENT') return false;
    throw error;
  }
}
