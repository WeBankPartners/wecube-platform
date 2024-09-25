// eslint-disable-next-line
module.exports = {
  extends: ['eslint:recommended', 'plugin:vue/essential'],
  plugins: ['vue'],
  ignorePatterns: ['node_modules/'],
  parser: 'vue-eslint-parser',
  parserOptions: {
    requireConfigFile: false,
    babelOptions: {
      presets: ['@babel/preset-env']
    },
    ecmaVersion: 2020,
    sourceType: 'module'
  },
  rules: {
    'vue/no-parsing-error': [2, { 'x-invalid-end-tag': false }],
    'comma-dangle': 0,
    eqeqeq: [1, 'always'],
    indent: [
      1,
      2,
      {
        VariableDeclarator: 1,
        SwitchCase: 1
      }
    ],
    'no-useless-escape': 0,
    quotes: [1, 'single'],
    semi: ['error', 'never'],
    'no-extra-semi': 0,
    'spaced-comment': [
      1,
      'always',
      {
        markers: ['/']
      }
    ],
    'space-infix-ops': 0,
    'no-async-promise-executor': 0,
    'space-before-function-paren': [
      1,
      {
        anonymous: 'always',
        named: 'never',
        asyncArrow: 'always'
      }
    ],
    'key-spacing': [
      1,
      {
        beforeColon: false,
        afterColon: true
      }
    ],
    'keyword-spacing': [
      1,
      {
        before: true,
        after: true
      }
    ],
    'comma-spacing': 0,
    'space-in-parens': [1, 'never'],
    'arrow-spacing': [
      1,
      {
        before: true,
        after: true
      }
    ],
    'arrow-parens': [1, 'as-needed'],
    'max-depth': [1, 8],
    'handle-callback-err': [1, '^(err|error)$'],
    'no-extra-boolean-cast': 1,
    'no-trailing-spaces': 1,
    'no-spaced-func': 1,
    'semi-spacing': [
      1,
      {
        before: false,
        after: true
      }
    ],
    'space-before-blocks': 0,
    'no-param-reassign': [
      1,
      {
        props: false
      }
    ],
    'no-multiple-empty-lines': [
      1,
      {
        max: 1,
        maxBOF: 0,
        maxEOF: 0
      }
    ],
    'no-var': 1,
    'no-multi-spaces': 1,
    'operator-linebreak': [
      'error',
      'before',
      {
        overrides: {
          '=': 'none'
        }
      }
    ],
    'max-params': [1, 10],
    'max-lines': [
      1,
      {
        max: 4000,
        skipBlankLines: true,
        skipComments: true
      }
    ],
    'no-constant-condition': 1,
    'object-curly-spacing': 0,
    'array-bracket-spacing': [1, 'never'],
    curly: [1, 'all'],
    'eol-last': [1, 'always'],
    'prefer-const': 1,
    'no-else-return': 1,
    'brace-style': [1, '1tbs', { allowSingleLine: true }],
    'newline-per-chained-call': [
      'error',
      {
        ignoreChainWithDepth: 2
      }
    ],
    'object-shorthand': [
      1,
      'always',
      {
        ignoreConstructors: false,
        avoidQuotes: true
      }
    ],
    'object-curly-newline': [
      1,
      {
        ObjectExpression: {
          minProperties: 4,
          multiline: true,
          consistent: true
        },
        ObjectPattern: {
          minProperties: 4,
          multiline: true,
          consistent: true
        },
        ImportDeclaration: {
          minProperties: 4,
          multiline: true,
          consistent: true
        },
        ExportDeclaration: {
          minProperties: 4,
          multiline: true,
          consistent: true
        }
      }
    ],
    'function-call-argument-newline': [1, 'consistent'],
    'function-paren-newline': [1, 'multiline-arguments'],
    'object-property-newline': [
      1,
      {
        allowAllPropertiesOnSameLine: false
      }
    ],
    'quote-props': [
      1,
      'as-needed',
      {
        keywords: false,
        unnecessary: true,
        numbers: false
      }
    ],
    'array-bracket-newline': ['off', 'consistent'],
    'array-element-newline': [
      'off',
      {
        multiline: true,
        minItems: 3
      }
    ],
    'no-console': [
      'error',
      {
        allow: ['error', 'warn', 'info']
      }
    ],
    'no-debugger': 'off',
    'arrow-body-style': [
      'error',
      'as-needed',
      {
        requireReturnForObjectLiteral: false
      }
    ],
    'vue/html-indent': [1, 2],
    'vue/html-closing-bracket-newline': [
      1,
      {
        singleline: 'never',
        multiline: 'always'
      }
    ],
    'vue/html-self-closing': [
      1,
      {
        html: {
          void: 'always',
          normal: 'never',
          component: 'any'
        }
      }
    ],
    'vue/multi-word-component-names': 0,
    'vue/object-curly-spacing': 1,
    'vue/space-infix-ops': 1,
    'vue/key-spacing': [
      1,
      {
        beforeColon: false,
        afterColon: true
      }
    ],
    'vue/eqeqeq': 1,
    'vue/comma-dangle': 1,
    'vue/array-bracket-spacing': [1, 'never']
  }
}
