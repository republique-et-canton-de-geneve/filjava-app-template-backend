import assert from 'node:assert/strict';
import { mkdtemp, readFile, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import path from 'node:path';
import test from 'node:test';
import { generateProject, parseArguments } from '../../scripts/generate-project.mjs';

test('parse les paramètres de génération', () => {
  assert.deepEqual(parseArguments([
    '--output', '../gestion-dossiers',
    '--component-id', 'gestion-dossiers',
    '--component-name', 'Gestion des dossiers',
    '--description', 'Service de gestion des dossiers',
    '--java-package', 'ch.ge.gestiondossiers',
    '--owner', 'group:default/equipe-dossiers',
    '--system', 'system:default/dossiers',
    '--skip-verify',
  ]), {
    output: '../gestion-dossiers',
    componentId: 'gestion-dossiers',
    componentName: 'Gestion des dossiers',
    description: 'Service de gestion des dossiers',
    javaPackage: 'ch.ge.gestiondossiers',
    owner: 'group:default/equipe-dossiers',
    system: 'system:default/dossiers',
    skipVerify: true,
  });
});

test('refuse une option inconnue', () => {
  assert.throws(() => parseArguments(['--inconnue', 'valeur']), /Option inconnue/);
});

test('génère une application complète hors du template', async () => {
  const temporaryRoot = await mkdtemp(path.join(tmpdir(), 'filjava-generator-test-'));
  const output = path.join(temporaryRoot, 'gestion-dossiers');
  try {
    const result = await generateProject({
      output,
      componentId: 'gestion-dossiers',
      componentName: 'Gestion des dossiers',
      description: 'Service de gestion des dossiers',
      javaPackage: 'ch.ge.gestiondossiers',
      owner: 'group:default/equipe-dossiers',
      system: 'system:default/dossiers',
      skipVerify: true,
    });

    assert.equal(result.targetMainClass, 'GestionDossiersApplication');
    const mainClass = path.join(output, 'ui', 'src', 'main', 'java', 'ch', 'ge',
      'gestiondossiers', 'ui', 'main', 'GestionDossiersApplication.java');
    assert.match(await readFile(mainClass, 'utf8'), /class GestionDossiersApplication/);
    const catalog = await readFile(path.join(output, 'catalog-info.yaml'), 'utf8');
    assert.match(catalog, /name: gestion-dossiers/);
    await assert.rejects(readFile(path.join(output, 'template.yaml')), /ENOENT/);
    await assert.rejects(readFile(path.join(output, 'backstage', 'README.md')), /ENOENT/);
  } finally {
    await rm(temporaryRoot, { recursive: true, force: true });
  }
});
