module.exports = {
  root: true,
  env: {
    browser: true,
    node: true,
    es2021: true
  },
  parser: 'vue-eslint-parser',
  parserOptions: {
    parser: '@babel/eslint-parser',
    ecmaVersion: 2020,
    sourceType: 'module',
    requireConfigFile: false // Babel 配置文件可选
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
    'vue/no-arrow-functions-in-watch': 'off'
  }
}
