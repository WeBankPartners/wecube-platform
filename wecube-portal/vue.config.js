const CompressionPlugin = require('compression-webpack-plugin')

const baseURL = 'http://127.0.0.1:8080'

module.exports = {
  devServer: {
    open: true,
    port: 3000,
    proxy: {
      '/': {
        target: baseURL
      }
    }
  },
  runtimeCompiler: true,
  publicPath: '/',
  chainWebpack: config => {
    // remove the old loader
    const img = config.module.rule('images')
    img.uses.clear()
    // add the new one
    img
      .use('file-loader')
      .loader('file-loader')
      .options({
        outputPath: 'img'
      })
  },
  configureWebpack: config => {
    if (process.env.NODE_ENV === 'production') {
      return {
        plugins: [
          new CompressionPlugin({
            algorithm: 'gzip',
            test: /\.js$|\.html$|.\css/, // 匹配文件名
            threshold: 10240, // 对超过10k的数据压缩
            deleteOriginalAssets: false // 不删除源文件
          })
        ]
      }
    }
  }
}
