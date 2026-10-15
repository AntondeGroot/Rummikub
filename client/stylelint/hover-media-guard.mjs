import stylelint from 'stylelint';

const ruleName = 'local/hover-media-guard';
const messages = stylelint.utils.ruleMessages(ruleName, {
  unguarded: (selector) =>
    `"${selector}" uses :hover outside @media (hover: hover) and (pointer: fine); on touch screens it sticks after a tap`,
});
const HOVER_MEDIA = /\bhover\s*:\s*hover\b/;

function insideHoverMedia(node) {
  for (let parent = node.parent; parent; parent = parent.parent) {
    if (parent.type === 'atrule' && parent.name === 'media' && HOVER_MEDIA.test(parent.params)) {
      return true;
    }
  }
  return false;
}

/** Every :hover style must sit inside a hover-capable media query (see the checklist's "CSS and touch input"). */
const rule = (enabled) => (root, result) => {
  if (!enabled) return;
  root.walkRules((node) => {
    if (node.selector.includes(':hover') && !insideHoverMedia(node)) {
      stylelint.utils.report({
        ruleName,
        result,
        node,
        message: messages.unguarded(node.selector),
      });
    }
  });
};
rule.ruleName = ruleName;
rule.messages = messages;

export default stylelint.createPlugin(ruleName, rule);
