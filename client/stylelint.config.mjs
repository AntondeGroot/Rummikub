export default {
  extends: ['stylelint-config-standard'],
  plugins: ['./stylelint/hover-media-guard.mjs'],
  rules: {
    'local/hover-media-guard': true,
  },
};
