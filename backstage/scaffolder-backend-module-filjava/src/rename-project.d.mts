export interface RenameProjectOptions {
  workspacePath: string;
  componentId: string;
  componentName: string;
  description: string;
  sourcePackage: string;
  targetPackage: string;
  sourceMainClass: string;
  dryRun?: boolean;
  logger?: { info(message: string): void };
}
export function deriveMainClass(componentId: string): string;
export function validateComponentId(componentId: string): void;
export function validateJavaPackage(javaPackage: string): void;
export function renameJavaProject(options: RenameProjectOptions): Promise<{
  targetMainClass: string;
  changes: string[];
}>;
