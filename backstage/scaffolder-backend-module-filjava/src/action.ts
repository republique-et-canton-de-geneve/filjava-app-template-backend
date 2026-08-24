import { createTemplateAction } from '@backstage/plugin-scaffolder-node';
import { z } from 'zod/v3';
import { renameJavaProject } from './rename-project.mjs';

export function createRenameJavaPackageAction() {
  return createTemplateAction({
    id: 'filjava:javaPackage:rename',
    description: 'Renomme les packages, la classe principale et les métadonnées du template Java FILJAVA.',
    supportsDryRun: true,
    examples: [
      {
        description: 'Génère le projet gestion-dossiers.',
        example: `steps:
  - id: renameJava
    action: filjava:javaPackage:rename
    input:
      componentId: gestion-dossiers
      componentName: Gestion des dossiers
      description: Service de gestion des dossiers
      sourcePackage: ch.ge.filjava.apptemplatebackend
      targetPackage: ch.ge.gestiondossiers
      sourceMainClass: FiljavaApplication`,
      },
    ],
    schema: {
      input: z.object({
        componentId: z.string(),
        componentName: z.string(),
        description: z.string(),
        sourcePackage: z.string(),
        targetPackage: z.string(),
        sourceMainClass: z.string(),
      }),
      output: z.object({ targetMainClass: z.string(), changeCount: z.number() }),
    },
    async handler(ctx) {
      const result = await renameJavaProject({
        workspacePath: ctx.workspacePath,
        ...ctx.input,
        dryRun: ctx.isDryRun,
        logger: ctx.logger,
      });
      ctx.output('targetMainClass', result.targetMainClass);
      ctx.output('changeCount', result.changes.length);
    },
  });
}
