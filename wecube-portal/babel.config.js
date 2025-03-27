// eslint-disable-next-line
module.exports = {
  presets: [
    '@vue/app',
    [  // 修改 @babel/preset-env 的配置
      '@babel/preset-env',
      {
        useBuiltIns: 'entry',
        corejs: 3
      }
    ]
  ],
  ignore: ['src/bpmn/*'],
  sourceType: 'unambiguous'
}
