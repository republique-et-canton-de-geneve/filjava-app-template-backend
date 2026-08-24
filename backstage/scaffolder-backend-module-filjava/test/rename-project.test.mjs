import assert from 'node:assert/strict';
import { mkdtemp, mkdir, readFile, rm, writeFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import path from 'node:path';
import test from 'node:test';
import {
  deriveMainClass,
  renameJavaProject,
  validateComponentId,
  validateJavaPackage,
} from '../src/rename-project.mjs';

const sourcePackage = 'ch.ge.filjava.apptemplatebackend';
const sourcePath = path.join('ch', 'ge', 'filjava', 'apptemplatebackend');

test('dérive un nom de classe principal déterministe', () => {
  assert.equal(deriveMainClass('gestion-dossiers'), 'GestionDossiersApplication');
});

test('refuse les identifiants de composant ambigus', () => {
  assert.throws(() => validateComponentId('Gestion-Dossiers'));
  assert.throws(() => validateComponentId('gestion--dossiers'));
  assert.throws(() => validateComponentId('gestion-'));
});

test('refuse un package incorrect ou contenant un mot réservé Java', () => {
  assert.throws(() => validateJavaPackage('package.unique'));
  assert.throws(() => validateJavaPackage('ch.ge.Gestion'));
  assert.throws(() => validateJavaPackage('../ch.ge.test'));
});

test('renomme packages, imports, classe principale et métadonnées', async t => {
  const root = await createFixture();
  t.after(() => rm(root, { recursive: true, force: true }));

  const result = await renameJavaProject(defaultOptions(root));
  assert.equal(result.targetMainClass, 'GestionDossiersApplication');

  const targetRoot = path.join(root, 'ui', 'src', 'main', 'java', 'ch', 'ge', 'gestiondossiers');
  const mainFile = path.join(targetRoot, 'ui', 'main', 'GestionDossiersApplication.java');
  const mainContent = await readFile(mainFile, 'utf8');
  assert.match(mainContent, /package ch\.ge\.gestiondossiers\.ui\.main;/);
  assert.match(mainContent, /class GestionDossiersApplication/);
  assert.doesNotMatch(mainContent, /apptemplatebackend|FiljavaApplication/);

  const testContent = await readFile(path.join(
    root, 'core', 'src', 'test', 'java', 'ch', 'ge', 'gestiondossiers', 'domain', 'ExampleTest.java'), 'utf8');
  assert.match(testContent, /import ch\.ge\.gestiondossiers\.domain\.Example;/);

  assert.match(await readFile(path.join(root, 'pom.xml'), 'utf8'), /gestion-dossiers-core/);
  assert.match(await readFile(path.join(root, 'README.md'), 'utf8'), /^# Gestion des dossiers/);
  await assert.rejects(readFile(path.join(root, 'template.yaml'), 'utf8'), { code: 'ENOENT' });
  await assert.rejects(readFile(path.join(root, 'backstage', 'README.md'), 'utf8'), { code: 'ENOENT' });
  assert.equal(await readFile(path.join(root, 'keep.txt'), 'utf8'), 'FILJAVA est le nom de la filière.');
});

test('échoue lorsque le package cible existe déjà', async t => {
  const root = await createFixture();
  t.after(() => rm(root, { recursive: true, force: true }));
  await mkdir(path.join(root, 'core', 'src', 'main', 'java', 'ch', 'ge', 'gestiondossiers'), { recursive: true });
  await assert.rejects(renameJavaProject(defaultOptions(root)), /destination existe déjà/i);
});

test('échoue lorsque le package source est absent', async t => {
  const root = await mkdtemp(path.join(tmpdir(), 'filjava-action-'));
  t.after(() => rm(root, { recursive: true, force: true }));
  await assert.rejects(renameJavaProject(defaultOptions(root)), /Aucun package source/);
});

test('échoue avant déplacement lorsque la classe principale est absente', async t => {
  const root = await createFixture();
  t.after(() => rm(root, { recursive: true, force: true }));
  await rm(path.join(root, 'ui', 'src', 'main', 'java', sourcePath,
    'ui', 'main', 'FiljavaApplication.java'));
  await assert.rejects(renameJavaProject(defaultOptions(root)), /unique classe principale/i);
  assert.match(await readFile(path.join(root, 'core', 'src', 'main', 'java', sourcePath,
    'domain', 'Example.java'), 'utf8'), /apptemplatebackend/);
});

test('échoue avant déplacement lorsque la classe principale cible existe', async t => {
  const root = await createFixture();
  t.after(() => rm(root, { recursive: true, force: true }));
  await write(path.join(root, 'ui', 'src', 'main', 'java', sourcePath,
    'ui', 'main', 'GestionDossiersApplication.java'), 'public class GestionDossiersApplication {}\n');
  await assert.rejects(renameJavaProject(defaultOptions(root)), /classe principale cible existe/i);
  assert.match(await readFile(path.join(root, 'core', 'src', 'main', 'java', sourcePath,
    'domain', 'Example.java'), 'utf8'), /apptemplatebackend/);
});

test('le dry run adapte le workspace temporaire pour prévisualiser le résultat', async t => {
  const root = await createFixture();
  t.after(() => rm(root, { recursive: true, force: true }));
  const result = await renameJavaProject({ ...defaultOptions(root), dryRun: true });
  assert.equal(result.targetMainClass, 'GestionDossiersApplication');
  assert.ok(result.changes.length > 0);
  assert.match(await readFile(path.join(
    root, 'ui', 'src', 'main', 'java', 'ch', 'ge', 'gestiondossiers',
    'ui', 'main', 'GestionDossiersApplication.java'), 'utf8'), /GestionDossiersApplication/);
  await assert.rejects(readFile(path.join(root, 'template.yaml'), 'utf8'), { code: 'ENOENT' });
});

test('refuse indirectement toute traversée via un package', async t => {
  const root = await createFixture();
  t.after(() => rm(root, { recursive: true, force: true }));
  await assert.rejects(
    renameJavaProject({ ...defaultOptions(root), targetPackage: 'ch.ge...outside' }),
    /package Java/i,
  );
});

function defaultOptions(workspacePath) {
  return {
    workspacePath,
    componentId: 'gestion-dossiers',
    componentName: 'Gestion des dossiers',
    description: 'Service de gestion des dossiers',
    sourcePackage,
    targetPackage: 'ch.ge.gestiondossiers',
    sourceMainClass: 'FiljavaApplication',
  };
}

async function createFixture() {
  const root = await mkdtemp(path.join(tmpdir(), 'filjava-action-'));
  for (const moduleName of ['core', 'infra', 'ui']) {
    for (const sourceSet of ['main', 'test']) {
      const packageRoot = path.join(root, moduleName, 'src', sourceSet, 'java', sourcePath);
      await mkdir(packageRoot, { recursive: true });
      if (moduleName === 'core' && sourceSet === 'main') {
        await write(path.join(packageRoot, 'domain', 'Example.java'),
          `package ${sourcePackage}.domain;\npublic class Example {}\n`);
      } else if (moduleName === 'core' && sourceSet === 'test') {
        await write(path.join(packageRoot, 'domain', 'ExampleTest.java'),
          `package ${sourcePackage}.domain;\nimport ${sourcePackage}.domain.Example;\n`);
      } else if (moduleName === 'ui' && sourceSet === 'main') {
        await write(path.join(packageRoot, 'ui', 'main', 'FiljavaApplication.java'),
          `package ${sourcePackage}.ui.main;\npublic class FiljavaApplication {}\n`);
      } else {
        await write(path.join(packageRoot, 'Marker.java'), `package ${sourcePackage};\n`);
      }
    }
  }
  await write(path.join(root, 'pom.xml'), '<artifactId>filjava-app-template-backend-core</artifactId>\n');
  await write(path.join(root, 'README.md'), '# FILJAVA - Template backend\n');
  await write(path.join(root, 'template.yaml'), 'kind: Template\n');
  await write(path.join(root, 'ticket.md'), 'ticket\n');
  await write(path.join(root, 'AGENTS.md'), 'instructions\n');
  await write(path.join(root, 'backstage', 'README.md'), 'admin\n');
  await write(path.join(root, 'keep.txt'), 'FILJAVA est le nom de la filière.');
  return root;
}

async function write(file, content) {
  await mkdir(path.dirname(file), { recursive: true });
  await writeFile(file, content, 'utf8');
}
