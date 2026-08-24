import { createBackendModule } from '@backstage/backend-plugin-api';
import { scaffolderActionsExtensionPoint } from '@backstage/plugin-scaffolder-node/alpha';
import { createRenameJavaPackageAction } from './action.js';

export const scaffolderModuleFiljava = createBackendModule({
  pluginId: 'scaffolder',
  moduleId: 'filjava',
  register(registration) {
    registration.registerInit({
      deps: { scaffolder: scaffolderActionsExtensionPoint },
      async init({ scaffolder }) {
        scaffolder.addActions(createRenameJavaPackageAction());
      },
    });
  },
});
