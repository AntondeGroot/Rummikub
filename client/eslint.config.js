// @ts-check
const eslint = require('@eslint/js');
const { defineConfig } = require('eslint/config');
const tseslint = require('typescript-eslint');
const angular = require('angular-eslint');
const sonarjs = require('eslint-plugin-sonarjs');

module.exports = defineConfig([
  // Generated from openapi/ by `npm run generate`; not hand-written, not committed.
  { ignores: ['src/generated/**'] },
  {
    files: ['**/*.ts'],
    extends: [
      eslint.configs.recommended,
      tseslint.configs.recommended,
      tseslint.configs.stylistic,
      angular.configs.tsRecommended,
      sonarjs.configs.recommended,
    ],
    processor: angular.processInlineTemplates,
    languageOptions: {
      parserOptions: {
        projectService: true,
        tsconfigRootDir: __dirname,
      },
    },
    rules: {
      '@angular-eslint/directive-selector': [
        'error',
        {
          type: 'attribute',
          prefix: 'app',
          style: 'camelCase',
        },
      ],
      '@angular-eslint/component-selector': [
        'error',
        {
          type: 'element',
          prefix: 'app',
          style: 'kebab-case',
        },
      ],
      '@typescript-eslint/no-deprecated': 'error',
      '@typescript-eslint/prefer-readonly': 'error',
      '@typescript-eslint/no-floating-promises': 'error',
      'no-console': ['error', { allow: ['warn', 'error'] }],
      'eqeqeq': ['error', 'always', { null: 'ignore' }],
      // Each spec's generated code has one public entry point, its index. Deep imports would tie
      // the app to the generator's file layout, which changes between generator versions.
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            {
              group: ['**/generated/*/**'],
              message:
                "Import from the spec's index instead: '…/generated/game', '…/generated/session' or '…/generated/gameroom'.",
            },
          ],
        },
      ],
      // God-object / size caps — stop a class growing one harmless method at a time. Specs are
      // exempt below (fixtures/provider setup make them legitimately long).
      'max-lines': ['error', { max: 200, skipBlankLines: true, skipComments: true }],
      'max-lines-per-function': ['error', { max: 25, skipBlankLines: true, skipComments: true }],
      'max-classes-per-file': ['error', 1],
      // Same bar as PMD's CognitiveComplexity on the server: 15 per function at most.
      'sonarjs/cognitive-complexity': ['error', 15],
      // Off: game randomness (shuffling tiles) is not security-sensitive, so this rule is noise here.
      'sonarjs/pseudo-random': 'off',
      // Off: TODO markers are deliberate, discoverable debt markers for ratcheted gates.
      'sonarjs/todo-tag': 'off',
    },
  },
  {
    files: ['**/*.spec.ts'],
    rules: {
      '@typescript-eslint/no-explicit-any': 'off',
      '@typescript-eslint/no-empty-function': 'off',
      // Specs are legitimately long (fixtures, provider setup) and use long describe/it callbacks.
      'max-lines': 'off',
      'max-lines-per-function': 'off',
    },
  },
  {
    files: ['**/*.html'],
    extends: [angular.configs.templateRecommended, angular.configs.templateAccessibility],
    rules: {},
  },
]);
