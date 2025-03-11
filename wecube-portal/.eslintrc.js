module.exports = {
  root: true,
  env: {
    browser: true,
    node: true,
  },
  parser: 'vue-eslint-parser',
  parserOptions: {
    parser: '@babel/eslint-parser',
    ecmaVersion: 2020,
    sourceType: 'module',
    requireConfigFile: false, // Babel 配置文件可选
    ecmaFeatures: { jsx: true }
  },
  extends: ['eslint:recommended', 'plugin:vue/recommended', 'plugin:import/recommended', 'plugin:promise/recommended'],
  rules: {
    // 根据项目需求调整规则
    'vue/max-attributes-per-line': 'off',
    'import/no-unresolved': 'off',
    'promise/always-return': 'warn',
    'vue/multi-word-component-names': 'off', // 禁用多单词组件名规则
    'vue/no-reserved-component-names': 'off',
    'vue/no-mutating-props': 'off',
    'vue/no-use-v-if-with-v-for': 'off',
    'promise/catch-or-return': 'off',
    'vue/no-arrow-functions-in-watch': 'off',
    'no-useless-escape': 'off',
    'vue/singleline-html-element-content-newline': 'off',
    'vue/attributes-order': 'off',
    'vue/require-prop-types': 'off',
    'vue/multiline-html-element-content-newline': 'off',
    'vue/html-indent': 'off',
    'vue/html-self-closing': 'off',
    'vue/attribute-hyphenation': 'off',
    'vue/order-in-components': 'off',
    'vue/this-in-template': 'off',
    'vue/no-v-html': 'off',
    'vue/require-default-prop': 'off',
    'vue/no-template-shadow': 'off',
    'promise/no-promise-in-callback': 'off',
    'import/no-duplicates': 'off',
    'vue/component-definition-name-casing': 'off',
    'import/no-named-as-default-member': 'off',
    'promise/no-nesting': 'off',
    'vue/no-lone-template': 'off'
  }
}
