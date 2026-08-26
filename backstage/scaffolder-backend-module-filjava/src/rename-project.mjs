import { lstat, mkdir, readdir, readFile, rename, rm, writeFile } from 'node:fs/promises';
import path from 'node:path';

const JAVA_KEYWORDS = new Set([
  'abstract', 'assert', 'boolean', 'break', 'byte', 'case', 'catch', 'char',
  'class', 'const', 'continue', 'default', 'do', 'double', 'else', 'enum',
  'extends', 'final', 'finally', 'float', 'for', 'goto', 'if', 'implements',
  'import', 'instanceof', 'int', 'interface', 'long', 'native', 'new',
  'package', 'private', 'protected', 'public', 'return', 'short', 'static',
  'strictfp', 'super', 'switch', 'synchronized', 'this', 'throw', 'throws',
  'transient', 'try', 'void', 'volatile', 'while', '_', 'true', 'false', 'null',
]);

const TEXT_EXTENSIONS = new Set([
  '', '.java', '.xml', '.yml', '.yaml', '.md', '.html', '.properties', '.conf',
  '.txt', '.json',
]);

const TEMPLATE_ONLY_PATHS = [
  'template.yaml', 'ticket.md', 'AGENTS.md', 'backstage', '.github',
];

export function deriveMainClass(componentId) {
  validateComponentId(componentId);
  const name = componentId.split('-')
    .map(part => part.charAt(0).toUpperCase() + part.slice(1)).join('');
  const result = `${name}Application`;
  if (!/^[A-Za-z_$][A-Za-z0-9_$]*$/.test(result)) {
    throw new Error(`Le nom de classe dérivé est invalide: ${result}`);
  }
  return result;
}

export function validateComponentId(componentId) {
  if (typeof componentId !== 'string' || !/^[a-z][a-z0-9-]{0,62}$/.test(componentId)) {
    throw new Error('componentId doit respecter ^[a-z][a-z0-9-]*$ et faire au plus 63 caractères');
  }
  if (componentId.includes('--') || componentId.endsWith('-')) {
    throw new Error('componentId ne peut pas contenir de tirets consécutifs ni finir par un tiret');
  }
}

export function validateJavaPackage(javaPackage) {
  if (typeof javaPackage !== 'string' || javaPackage.length > 240) {
    throw new Error('Le package Java doit être une chaîne de 240 caractères au maximum');
  }
  const segments = javaPackage.split('.');
  if (segments.length < 2 || segments.some(segment => !/^[a-z_][a-z0-9_]*$/.test(segment))) {
    throw new Error('Le package Java doit contenir au moins deux segments Java minuscules valides');
  }
  const keyword = segments.find(segment => JAVA_KEYWORDS.has(segment));
  if (keyword) throw new Error(`Le segment de package Java « ${keyword} » est réservé`);
}

export async function renameJavaProject(options) {
  const {
    workspacePath, componentId, componentName, description, sourcePackage,
    targetPackage, sourceMainClass, dryRun = false,
    logger = { info() {} },
  } = options;

  validateComponentId(componentId);
  validateJavaPackage(sourcePackage);
  validateJavaPackage(targetPackage);
  if (!componentName?.trim() || !description?.trim()) {
    throw new Error('componentName et description sont obligatoires');
  }
  if (!/^[A-Za-z_$][A-Za-z0-9_$]*$/.test(sourceMainClass)) {
    throw new Error('sourceMainClass doit être un identifiant Java valide');
  }

  const root = path.resolve(workspacePath);
  const targetMainClass = deriveMainClass(componentId);
  const sourcePath = sourcePackage.replaceAll('.', path.sep);
  const targetPath = targetPackage.replaceAll('.', path.sep);
  const changes = [];

  if (dryRun) logger.info('Dry run Backstage: adaptation du workspace temporaire sans effet externe');

  const sourceRoots = await findJavaSourceRoots(root, sourcePath);
  if (sourceRoots.length === 0) {
    throw new Error(`Aucun package source ${sourcePackage} trouvé dans le workspace`);
  }
  const moves = [];
  const oldMainFiles = [];
  for (const sourceDirectory of sourceRoots) {
    const sourceStat = await lstat(sourceDirectory);
    if (!sourceStat.isDirectory() || sourceStat.isSymbolicLink()) {
      throw new Error(`Le package source doit être un répertoire réel: ${sourceDirectory}`);
    }
    const javaRoot = sourceDirectory.slice(0, -sourcePath.length - 1);
    const targetDirectory = safeChild(root, path.join(javaRoot, targetPath));
    if (await exists(targetDirectory)) throw new Error(`La destination existe déjà: ${targetDirectory}`);
    moves.push({ sourceDirectory, targetDirectory });
    oldMainFiles.push(...(await listFiles(sourceDirectory))
      .filter(file => path.basename(file) === `${sourceMainClass}.java`));
  }
  if (oldMainFiles.length !== 1) {
    throw new Error(`Une unique classe principale ${sourceMainClass}.java est attendue, trouvé: ${oldMainFiles.length}`);
  }
  const targetMainFileBeforeMove = path.join(path.dirname(oldMainFiles[0]), `${targetMainClass}.java`);
  if (targetMainClass !== sourceMainClass && await exists(targetMainFileBeforeMove)) {
    throw new Error(`Le fichier de classe principale cible existe déjà: ${targetMainClass}.java`);
  }

  for (const { sourceDirectory, targetDirectory } of moves) {
    await mkdir(path.dirname(targetDirectory), { recursive: true });
    await rename(sourceDirectory, targetDirectory);
    changes.push(`move:${path.relative(root, sourceDirectory)}->${path.relative(root, targetDirectory)}`);
  }

  const replacements = new Map([
    [sourcePackage, targetPackage],
    ['filjava-app-template-backend', componentId],
    ['filjava_app_template_ui', `${componentId}-ui`],
    [sourceMainClass, targetMainClass],
    ['FILJAVA Backend Template', componentName.trim()],
    ['Template backend Java destiné au développement de services applicatifs', description.trim()],
    ['Image de test ci-validation-container-full-ci-jib', componentName.trim()],
    ['Image de l’application ci-validation-container-full-ci-jib', description.trim()],
  ]);

  for (const file of await listFiles(root)) {
    if (!TEXT_EXTENSIONS.has(path.extname(file).toLowerCase())) continue;
    const content = await readFile(file, 'utf8');
    let updated = content;
    for (const [from, to] of replacements) updated = updated.replaceAll(from, to);
    if (path.basename(file) === 'README.md' && updated.startsWith('# FILJAVA - Template backend')) {
      updated = updated.replace('# FILJAVA - Template backend', `# ${componentName.trim()}`);
      updated = updated.replace(
        /## Créer une nouvelle application à partir du template[\s\S]*?(?=## Vérification)/,
        `## Application\n\n${description.trim()}\n\nCe projet a été généré à partir du template backend FILJAVA. Son package Java\n` +
        `racine est \`${targetPackage}\`.\n\n`,
      );
    }
    if (updated !== content) {
      await writeFile(file, updated, 'utf8');
      changes.push(`edit:${path.relative(root, file)}`);
    }
  }

  const movedMainFiles = (await listFiles(root))
    .filter(file => path.basename(file) === `${sourceMainClass}.java`);
  for (const oldMainFile of movedMainFiles) {
    if (targetMainClass === sourceMainClass) continue;
    const newMainFile = safeChild(root, path.join(path.dirname(oldMainFile), `${targetMainClass}.java`));
    if (await exists(newMainFile)) throw new Error(`Le fichier cible existe déjà: ${newMainFile}`);
    await rename(oldMainFile, newMainFile);
    changes.push(`move:${path.relative(root, oldMainFile)}->${path.relative(root, newMainFile)}`);
  }

  for (const relativePath of TEMPLATE_ONLY_PATHS) {
    const target = safeChild(root, relativePath);
    if (await exists(target)) {
      await rm(target, { recursive: true, force: false });
      changes.push(`remove:${relativePath}`);
    }
  }
  logger.info(`Projet adapté: ${changes.length} opération(s)`);
  return { targetMainClass, changes };
}

function safeChild(root, child) {
  const resolved = path.resolve(root, child);
  const relative = path.relative(root, resolved);
  if (relative.startsWith('..') || path.isAbsolute(relative)) {
    throw new Error(`Chemin hors du workspace refusé: ${child}`);
  }
  return resolved;
}

async function findJavaSourceRoots(root, sourcePath) {
  const result = [];
  for (const moduleName of ['core', 'infra', 'ui']) {
    for (const sourceSet of ['main', 'test']) {
      const candidate = safeChild(root, path.join(moduleName, 'src', sourceSet, 'java', sourcePath));
      if (await exists(candidate)) result.push(candidate);
    }
  }
  return result;
}

async function listFiles(directory) {
  const result = [];
  for (const entry of await readdir(directory, { withFileTypes: true })) {
    if (entry.name === '.git' || entry.name === 'target' || entry.name === 'node_modules') continue;
    const child = path.join(directory, entry.name);
    if (entry.isSymbolicLink()) continue;
    if (entry.isDirectory()) result.push(...await listFiles(child));
    else if (entry.isFile()) result.push(child);
  }
  return result;
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
