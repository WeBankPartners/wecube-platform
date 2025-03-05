const CompressionPlugin = require('compression-webpack-plugin')
const dotenv = require('dotenv')
dotenv.config()

module.exports = {
  devServer: {
    port: 3000,
    proxy: {
      '/': {
        target: process.env.BASE_URL,
        changeOrigin: true,
        ws: false,
      }
    }
  },
  runtimeCompiler: true,
  publicPath: '/',
  productionSourceMap: false,
  css: {
    loaderOptions: {
      less: {
        javascriptEnabled: true
      }
    }
  },
  configureWebpack: () => {
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
